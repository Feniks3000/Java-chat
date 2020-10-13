import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.*;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService = new AuthServiceImpl();
    Logger logger = Logger.getLogger(getClass().getName());
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    ServerSocket server;
    Socket socket;

    public Server(int port) {
        getLoggerParameters(logger);
        clients = new Vector<>();

        if (DB.connect("main.db")) {
            try {
                server = new ServerSocket(port);
                logger.info(String.format("Сервер запущен на порту %d", port));

                while (true) {
                    socket = server.accept();
                    logger.info("=> Подключился клиент");
                    new ClientHandler(this, socket, logger);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DB.disconnect();
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.severe("Ошибка подключения к БД");
        }
    }

    public void getLoggerParameters(Logger logger) {
        try {
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            Handler fileHandler = new FileHandler("server_%g.log", 1024 * 100, 10, true);
            fileHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("%s %s (%s) => %s\n", record.getLoggerName(), record.getLevel(), simpleDateFormat.format(new Date(record.getMillis())), record.getMessage());
                }
            });
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("%s, %s, %s => %s\n", record.getLoggerName(), simpleDateFormat.format(new Date(record.getMillis())), record.getLevel(), record.getMessage());
                }
            });
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void broadcastMessage(String message) {
        logger.fine(message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void broadcastClientList() {
        StringBuilder clientList = new StringBuilder("/clients ");
        for (ClientHandler client : clients) {
            clientList.append(client.getLogin()).append(" ");
        }
        broadcastMessage(clientList.toString());
    }

    public void privateMessage(ClientHandler client, String recipient, String message) {
        if (clientExits(recipient)) {
            ClientHandler receiver = getClientByLogin(recipient);
            getClientByLogin(recipient).sendMessage(message);
            if (!receiver.equals(client)) {
                client.sendMessage(message);
            }
        } else {
            client.sendMessage(String.format("Пользователь %s не найден. Сообщение не отправлено", recipient));
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastMessage(String.format("=> В чат вошел %s", client.getLogin()));
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        if (clients.contains(client)) {
            clients.remove(client);
            broadcastMessage(String.format("=> Чат покинул %s", client.getLogin()));
            broadcastClientList();
        }
    }

    public boolean clientExits(String login) {
        for (ClientHandler client : clients) {
            if (client.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public ClientHandler getClientByLogin(String login) {
        for (ClientHandler client : clients) {
            if (client.getLogin().equals(login)) {
                return client;
            }
        }
        return null;
    }
}
