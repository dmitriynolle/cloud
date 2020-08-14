import java.sql.*;

public class SqlClient {

    private static Connection connection;
    private static Statement statement;

    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:cloud-db.sqlite");
            statement = connection.createStatement();
            System.out.println("SQL connected");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static String getNickname(String login, String password) {
        String query = String.format("select name from users where name='%s' and password='%s'", login, password);
        try (ResultSet set = statement.executeQuery(query)) {
            if (set.next())
                return set.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Library.ERROR;
    }

    synchronized static String regNickname(String login, String password) {
        String query1 = String.format("select name from users where name='%s'", login);
        String query2 = String.format("INSERT INTO users (name, password) VALUES('%s', '%s')", login, password);
        try {
            ResultSet set1 = statement.executeQuery(query1);
            if (set1.next())
                return Library.ERROR;
            int set2 = statement.executeUpdate(query2);
            if (set2 == 1)
                return login;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return Library.ERROR;
    }

    synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
