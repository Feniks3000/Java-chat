public interface AuthService {
    boolean userExist(String login, String passHash);

    boolean loginBusy(String login);

    boolean addUser(String login, String passHash);

    boolean removeUser(String login);

    UserData getUserByLogin(String login);
}
