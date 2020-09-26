import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService = new AuthServiceImpl();

    ServerSocket server;
    Socket socket;

    public Server(int port) {
        clients = new Vector<>();

        if (DB.connect("main.db")) {
            try {
                server = new ServerSocket(port);
                System.out.printf("Сервер запущен на порту %d\n", port);

                while (true) {
                    socket = server.accept();
                    System.out.println("=> Подключился клиент");
                    new ClientHandler(this, socket);
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
            System.out.println("Ошибка подключения к БД");
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void broadcastMessage(String message) {
        System.out.println(message);
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
