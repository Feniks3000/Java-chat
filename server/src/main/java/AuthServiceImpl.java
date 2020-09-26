import java.sql.SQLException;

public class AuthServiceImpl implements AuthService {
    @Override
    public boolean userExist(String login, String passHash) throws SQLException {
        return DB.userExist(login, passHash) != 0;
    }

    @Override
    public boolean loginBusy(String login) throws SQLException {
        return DB.userExist(login) != 0;
    }

    @Override
    public boolean addUser(String login, String passHash) throws SQLException {
        if (!loginBusy(login) && !userExist(login, passHash)) {
            DB.insertUser(login, passHash);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeUser(String login) throws SQLException {
        if (loginBusy(login)) {
            DB.deleteUser(login);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean changeLogin(String login, String newLogin) throws SQLException {
        if (!loginBusy(newLogin)) {
            DB.changeLogin(login, newLogin);
            return true;
        } else {
            return false;
        }
    }
}
