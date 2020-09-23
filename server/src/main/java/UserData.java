public class UserData {
    private String login;
    private String passHash;

    public UserData(String login, String pass) {
        this.login = login;
        this.passHash = pass;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassHash() {
        return passHash;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }
}
