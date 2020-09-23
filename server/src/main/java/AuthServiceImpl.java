import java.util.ArrayList;
import java.util.List;

public class AuthServiceImpl implements AuthService {
    private List<UserData> users;

    public AuthServiceImpl() {
        this.users = new ArrayList<>();
    }

    @Override
    public boolean userExist(String login, String passHash) {
        for (UserData user : users) {
            if (user.getLogin().equals(login) && user.getPassHash().equals(passHash)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean loginBusy(String login) {
        for (UserData user : users) {
            if (user.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addUser(String login, String passHash) {
        if (!loginBusy(login) && !userExist(login, passHash)) {
            users.add(new UserData(login, passHash));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeUser(String login) {
        if (loginBusy(login)) {
            users.remove(getUserByLogin(login));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public UserData getUserByLogin(String login) {
        for (UserData user : users) {
            if (user.getLogin().equals(login)) {
                return user;
            }
        }
        return null;
    }
}
