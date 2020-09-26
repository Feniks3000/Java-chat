import java.sql.*;

public class DB {
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;

    public static Connection getConnection() {
        return connection;
    }

    public static void setConnection(Connection connection) {
        DB.connection = connection;
    }

    public static Statement getStatement() {
        return statement;
    }

    public static void setStatement(Statement statement) {
        DB.statement = statement;
    }

    public static PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public static void setPreparedStatement(PreparedStatement preparedStatement) {
        DB.preparedStatement = preparedStatement;
    }

    public static void connect(String name) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + name);
        statement = connection.createStatement();
    }

    public static void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Integer userExist(String login, String passHash) throws SQLException {
        String sql = String.format("SELECT COUNT(*) FROM USERS WHERE LOGIN = '%s' AND PASSWORD = '%s'", login, passHash);
        return statement.executeQuery(sql).getInt(1);
    }

    public static Integer userExist(String login) throws SQLException {
        String sql = String.format("SELECT COUNT(*) FROM USERS WHERE LOGIN = '%s'", login);
        return statement.executeQuery(sql).getInt(1);
    }

    public static void insertUser(String login, String passHash) throws SQLException {
        String sql = String.format("INSERT INTO USERS (LOGIN, PASSWORD) VALUES ('%s', '%s')", login, passHash);
        statement.executeUpdate(sql);
    }

    public static void deleteUser(String login) throws SQLException {
        String sql = String.format("DELETE FROM USERS WHERE LOGIN = '%s'", login);
        statement.executeUpdate(sql);
    }

    public static void changeLogin(String login, String newLogin) throws SQLException {
        String sql = String.format("UPDATE USERS SET LOGIN = '%s' WHERE LOGIN = '%s'", newLogin, login);
        statement.executeUpdate(sql);
    }
}
