import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;

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

    synchronized static void addFile(String user, String filePath, String fileName, long fileSize, String share) {
        String query1 = String.format("select user from filelist where user='%s' and filepath = '%s' and filename = '%s'", user, filePath, fileName);
        try {
            ResultSet set1 = statement.executeQuery(query1);
            if (!set1.next()) {
                String query2 = String.format("INSERT INTO filelist (user, filepath, filename, filesize, share) VALUES('%s', '%s', '%s', '%s', '%s')", user, filePath, fileName, fileSize, share);
                int set2 = statement.executeUpdate(query2);
            }
            else{
                String query3= String.format("UPDATE filelist SET filesize = '%s' where user='%s' and filepath = '%s' and filename = '%s'", fileSize, user, filePath, fileName);
                int set3 = statement.executeUpdate(query3);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized static void delFile(String filePath, String fileName) {
        String query = String.format("DELETE FROM filelist WHERE filepath = '%s' and filename = '%s'", filePath, fileName);
        try {
            int set = statement.executeUpdate(query);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized static String getPath(String fileName, String  user) {
        String query = String.format("SELECT filepath FROM filelist where filename = '%s' and share = '%s'", fileName, user);
        try {
            ResultSet set = statement.executeQuery(query);
            return set.getString(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "error";
    }

    synchronized static Serializable getUsers(){
        String query = String.format("SELECT name FROM users ORDER BY name");
        try {
            ResultSet set = statement.executeQuery(query);
            ArrayList<String> result = new ArrayList<String>();
            for (int i = 0; set.next(); i++) {
                result.add(set.getString(1));
            }
            return (String[]) result.toArray(new String[result.size()]);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return "error";
    }

    synchronized static Serializable fileList(String user, String filePath) {
        String query1 = String.format("select filename, filesize, share from filelist where user='%s' and (filepath='%s' or (share!='%s' and share!='%s')) order by share, filename", user, filePath, 1, 2);
        try {
            ResultSet set1 = statement.executeQuery(query1);
            ArrayList<String[]> result = new ArrayList<String[]>();
            for (int i = 0; set1.next(); i++) {
                String[] strings = new String[3];
                strings[0] = set1.getString(1);
                strings[1] = set1.getString(2);
                strings[2] = set1.getString(3);
                result.add(strings);
            }
            return (String[][]) result.toArray(new String[result.size()][3]);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "error";
    }

    synchronized static void disconnect() {
        try {
            connection.close();
            System.out.println("SQL disconected");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
