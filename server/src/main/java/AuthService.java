import java.sql.SQLException;

public interface AuthService {
    boolean userExist(String login, String passHash) throws SQLException;

    boolean loginBusy(String login) throws SQLException;

    boolean addUser(String login, String passHash) throws SQLException;

    boolean removeUser(String login) throws SQLException;

    boolean changeLogin(String login, String s) throws SQLException;

    void changePassword(String login, String trim) throws SQLException;
}
