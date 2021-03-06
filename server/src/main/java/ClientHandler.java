import org.apache.commons.lang3.StringUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private String login;
    private DataInputStream in;
    private DataOutputStream out;
    Logger logger;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    public ClientHandler(Server server, Socket socket, Logger logger) {
        this.server = server;
        this.socket = socket;
        this.logger = logger;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                if (authorization(server)) {
                    socket.setSoTimeout(0);
                    while (true) {
                        String message = in.readUTF();
                        if (message.startsWith("/")) {
                            if (message.equals("/end")) {
                                break;
                            }
                            if (message.startsWith("/for ")) {
                                String[] command = message.split("\\s+", 3);
                                if (command.length < 3) {
                                    sendMessage("Недопустимый формат команды. Ожидалось сообщение вида '/for login message'");
                                    continue;
                                }
                                server.privateMessage(this, command[1], String.format("%s лично для %s (%s):\t %s", login, command[1], simpleDateFormat.format(new Date()), command[2]));
                                continue;
                            }
                            if (message.startsWith("/changeLogin ")) {
                                String[] command = message.split("\\s+", 2);
                                if (command.length < 2 || StringUtils.isEmpty(command[1].trim())) {
                                    sendMessage("Недопустимый формат команды. Ожидалось сообщение вида '/changeLogin newLogin'");
                                    continue;
                                }
                                String newLogin = command[1].trim().toLowerCase();
                                if (!server.getAuthService().changeLogin(login, newLogin)) {
                                    sendMessage("Пользователь с таким логином существует");
                                    continue;
                                }
                                server.broadcastMessage(String.format("=> Пользователь %s изменил имя на %s", login, newLogin));
                                login = newLogin;
                                sendMessage(String.format("/newLogin %s", login));
                                server.broadcastClientList();
                                continue;
                            }
                            if (message.startsWith("/changePassword ")) {
                                String[] command = message.split("\\s+", 2);
                                if (command.length < 2 || StringUtils.isEmpty(command[1].trim())) {
                                    sendMessage("Недопустимый формат команды. Ожидалось сообщение вида '/changePassword newPassword'");
                                    continue;
                                }
                                server.getAuthService().changePassword(login, command[1].trim());
                                sendMessage("=> Пароль изменен");
                                continue;
                            }
                            sendMessage("Служебное сообщение данного вида не найдено");
                        } else {
                            server.broadcastMessage(String.format("%s (%s):\t %s", login, simpleDateFormat.format(new Date()), message));
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                sendMessage("/end");
                logger.warning("Сокет закрыт по таймауту");
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    server.unsubscribe(this);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean authorization(Server server) throws IOException, SQLException {
        socket.setSoTimeout(60000);
        while (true) {
            String message = in.readUTF();
            if (message.startsWith("/auth ")) {
                String[] token = message.split("\\s", 3);
                if (!server.getAuthService().loginBusy(token[1])) {
                    server.getAuthService().addUser(token[1], token[2]);
                    login = token[1];
                    sendMessage("/authOk");
                    server.subscribe(this);
                    return true;
                } else if (server.getAuthService().userExist(token[1], token[2])) {
                    if (!server.clientExits(token[1])) {
                        login = token[1];
                        sendMessage("/authOk");
                        server.subscribe(this);
                        return true;
                    } else {
                        logger.info(String.format("%s пытается открыть чат в другом окне", token[1]));
                        sendMessage(String.format("%s вы уже аторизованы в другом окне\n", token[1]));
                    }
                } else {
                    logger.warning(String.format("%s ввел неверный пароль", token[1]));
                    sendMessage("Неверный логин / пароль");
                }
            }
            if (message.equals("/end")) {
                return false;
            }
        }
    }

    public String getLogin() {
        return login;
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
