import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public HBox authPanel;
    @FXML
    public HBox sendPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passField;
    @FXML
    public TextField messageField;
    @FXML
    public TextArea history;
    @FXML
    public ListView<String> clients;

    private boolean authenticated;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) messageField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        sendPanel.setVisible(authenticated);
        sendPanel.setManaged(authenticated);
        clients.setVisible(authenticated);
        clients.setManaged(authenticated);

        if (authenticated) {
            LogsController.start(String.format("logs/%s.txt", loginField.getText()));
            history.appendText(LogsController.getLastRows(100));
            setTitle("Simple chat for " + loginField.getText().trim().toLowerCase());
        } else {
            LogsController.stop();
            setTitle("Simple chat");
            history.clear();
        }
    }

    private void setTitle(String title) {
        Platform.runLater(() -> {
            stage.setTitle(title);
            clients.getItems().clear();
        });
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connectTo("localhost", 8189);
        }

        try {
            String login = loginField.getText().trim().toLowerCase();
            String pass = passField.getText().trim();
            if (StringUtils.isNotEmpty(login) && StringUtils.isNotEmpty(pass)) {
                try {
                    out.writeUTF(String.format("/auth %s %s", login, getPassHash(pass)));
                    passField.clear();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            } else {
                printMessage("Соединение не установлено. Логин или пароль пусты или состоят из пробелов");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPassHash(String pass) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        return Arrays.toString(messageDigest.digest(pass.getBytes(StandardCharsets.UTF_8)));
    }

    private void connectTo(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String message = in.readUTF();
                        if (message.equals("/end")) {
                            throw new RuntimeException("Сервер закрыл соединение без авторизации");

                        }
                        if (message.equals("/authOk")) {
                            setAuthenticated(true);
                            break;
                        }
                        printMessage(message);
                    }
                    while (true) {
                        String message = in.readUTF();
                        if (message.startsWith("/")) {
                            if (message.equals("/end")) {
                                printMessage("Чат завершен");
                                break;
                            }
                            if (message.startsWith("/newLogin ")) {
                                String[] command = message.split("\\s+", 2);
                                setTitle("Simple chat for " + command[1]);
                            }
                            if (message.startsWith("/clients ")) {
                                String[] clients = message.substring(9).split("\\s+");
                                updateClients(clients);
                            }
                        } else {
                            printMessage(message);
                        }
                    }
                } catch (RuntimeException | IOException e) {
                    //e.printStackTrace();
                } finally {
                    System.out.println("Мы отключились от сервера");
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateClients(String[] clients) {
        Platform.runLater(() -> {
            this.clients.getItems().clear();
            this.clients.getItems().addAll(Arrays.asList(clients));
        });
    }

    public void sendMessage() {
        try {
            String message = messageField.getText();
            if (message.length() > 0) {
                if (message.startsWith("/note ")) {
                    if (message.length() > 6) {
                        printMessage(String.format("Личная заметка: %s", messageField.getText().substring(6)));
                        messageField.clear();
                        messageField.requestFocus();
                    }
                } else if (message.startsWith("/changePassword ")) {
                    String[] command = message.split("\\s+", 2);
                    if (command.length < 2 || StringUtils.isEmpty(command[1].trim())) {
                        printMessage("Неправильный формат команды смены пароля! Ожидается '/changePassword newPassword'");
                        messageField.requestFocus();
                    } else {
                        out.writeUTF(String.format("/changePassword %s", getPassHash(command[1])));
                        messageField.clear();
                        messageField.requestFocus();
                    }
                } else {
                    out.writeUTF(message);
                    messageField.clear();
                    messageField.requestFocus();
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void printMessage(String message) {
        if (message.length() > 0) {
            history.appendText(String.format("%s\n\n", message));
            LogsController.write(String.format("%s\n\n", message));
        }
    }

    public void clickOnClients(MouseEvent mouseEvent) {
        String receiver = clients.getSelectionModel().getSelectedItem();
        if (!receiver.equals("null")) {
            messageField.setText(String.format("/for %s ", receiver));
            messageField.requestFocus();
            messageField.selectEnd();
        } else {
            messageField.clear();
        }
    }
}
