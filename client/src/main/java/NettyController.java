import io.netty.channel.ChannelHandlerContext;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public class NettyController implements Initializable {

    private static SendClass returnSendClass;
    public Button delete;
    public ListView<String> listViewClient;
    public TextField text;
    public ListView<String> listViewServer;
    public Label message;
    public Button signIn;
    public Button cancel;
    public Button signUp;
    public AnchorPane passwordPane;
    public AnchorPane mainPane;
    public TextField loginText;
    public PasswordField passwordText;
    public Label errorText;
    public Label command;
    public Button copy;
    public Button mkdir;
    public ListView<String> clientList;
    public Button share;
    public Button update;
    public AnchorPane closeApplication;

    private String clientPath = "./client/src/main/resources/";
    private String serverPath = "./server/src/main/resources/";
    private String path;
    private String user;
    public static ChannelHandlerContext ctx;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NettyNetwork network = NettyNetwork.getInstance();
        try {
            listViewClient.setOnMouseClicked(a -> {
                String[] fileName = listViewClient.getSelectionModel().getSelectedItem().split(" : ");
                if (a.getClickCount() == 1) {
                    if (fileName.length == 1 && !fileName[0].equals("..")) {
                        clientPath += fileName[0] + "/";
                        clientFileList(clientPath);
                    } else if (fileName[0].equals("..")) {
                        clientPath = new File(clientPath).getParent().toString() + "/";
                        clientFileList(clientPath);
                    } else {
                        command.setText(Library.FILEUPLOAD);
                        text.setText(fileName[0]);
                    }
                }
            });
            listViewServer.setOnMouseClicked(a -> {
                String[] fileName = listViewServer.getSelectionModel().getSelectedItem().split(" : ");
                if (a.getClickCount() == 1) {
                    if (fileName.length == 1 && !fileName[0].equals("..")) {
                        serverPath += fileName[0] + "/";
                        serverFileList();
                    } else if (fileName[0].equals("..")) {
                        serverPath = new File(serverPath).getParent().toString() + "/";
                        serverFileList();
                    } else {
                        command.setText(Library.FILEDOWNLOAD);
                        text.setText(fileName[0]);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //        Запрос копирование файла
    public void copyCommand() {
        if (command.getText().equals(Library.FILEUPLOAD))
            Library.ReadFile(ctx, user, text.getText(), clientPath + Library.DELIMITER + serverPath);
        else if (command.getText().equals(Library.FILEDOWNLOAD))
            ctx.writeAndFlush(new SendClass(Library.FILEDOWNLOAD, user, text.getText(), "", serverPath + Library.DELIMITER + clientPath));
        text.clear();
        sleep();
        message.setText(returnSendClass.getUser());
        clientFileList(clientPath);
        serverFileList();
    }

    //        Вход зарегистрированного пользователя
    public void signIn() {
        String login = loginText.getText();
        String pas = passwordText.getText();
        if (login.equals("") || pas.equals("")) errorText.setText("Login and password must not be empty");
        else {
            ctx.writeAndFlush(new SendClass(Library.SIGN_IN, login, pas));
            sleep();
            if (!returnSendClass.getUser().equals(Library.ERROR)) {
                regOk();
            } else {
                errorText.setText("Incorrect login or password");
                loginText.clear();
                passwordText.setText(null);
            }
        }
    }

    //        Регистрация нового пользователя
    public void signUp() {
        String login = loginText.getText();
        String pas = passwordText.getText();
        if (login.equals("") || pas.equals("")) errorText.setText("Login and password must not be empty");
        else {
            ctx.writeAndFlush(new SendClass(Library.SIGN_UP, login, pas));
            sleep();
            if (!returnSendClass.getUser().equals(Library.ERROR)) {
                regOk();
            } else {
                errorText.setText("User already exists");
                loginText.clear();
                passwordText.setText(null);
            }
        }
    }

    //        Регистрация или вход пройден
    public void regOk() {
        message.setText("Welcome to MyCloud");
        passwordPane.setVisible(false);
        mainPane.setVisible(true);
        user = returnSendClass.getUser();
        Stage stage = (Stage) mainPane.getScene().getWindow();
        stage.setTitle("My Cloud | User: " + user);
        serverPath += user + "/";
        path = serverPath;
        returnSendClass = null;
        clientFileList(clientPath);
        serverFileList();
        ctx.writeAndFlush(new SendClass(Library.USERLIST));
        sleep();
        String[] userList = returnSendClass.getUser().split("\\|");
        for (String s : userList) {
            if (!s.equals(user))
                clientList.getItems().add(s);
        }
        returnSendClass = null;
    }

    //        Отмена и закрытие приложения
    public void cancel() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        NettyNetwork.StopTread();
        stage.close();
    }

    //        Запрос удаление файла
    public void delCommand() {
        if (command.getText().equals(Library.FILEUPLOAD))
            Library.FileDelete(ctx, user, text.getText(), clientPath);
        else if (command.getText().equals(Library.FILEDOWNLOAD))
            ctx.writeAndFlush(new SendClass(Library.FILEDELETE, user, text.getText(), "", serverPath));
        sleep();
        text.clear();
        message.setText(returnSendClass.getUser());
        returnSendClass = null;
        clientFileList(clientPath);
        serverFileList();
    }

    //        Создание каталога
    public void mkDir() {
        ctx.writeAndFlush(new SendClass(Library.MKDIR, user, text.getText(), "", serverPath));
        sleep();
        text.clear();
        returnSendClass = null;
        clientFileList(clientPath);
        serverFileList();
    }

    //        Запрос списка файлов с сервера
    public void serverFileList() {
        ctx.writeAndFlush(new SendClass(Library.SERVERFILELIST, user, serverPath));
        sleep();
        listViewServer.getItems().clear();
        File dir = new File(serverPath);
        if (!serverPath.equals(path))
            listViewServer.getItems().add("..");
        String[] serverFileList = (new String(returnSendClass.getBuffer())).split("\\|");
        for (String s : serverFileList) {
            listViewServer.getItems().add(s);
        }
        returnSendClass = null;
    }

    //        Клиентский файл лист
    private void clientFileList(String clientPath) {
        listViewClient.getItems().clear();
        File dir = new File(clientPath);
        if (!clientPath.equals("./client/src/main/resources/"))
            listViewClient.getItems().add("..");
        File[] fileList = Objects.requireNonNull(dir.listFiles());
        Arrays.sort(fileList);
        for (File file : fileList) {
            if (file.isDirectory())
                listViewClient.getItems().add(file.getName());
        }
        for (File file : fileList) {
            if (!file.isDirectory())
                listViewClient.getItems().add(file.getName() + " : " + file.length());
        }
    }

    //        Используется для перехвата объекта от сервера
    public static void returnSendObject(Object msg) {
        returnSendClass = (SendClass) msg;
    }

    public void sleep() {
        try {
            do {
                if (!ctx.channel().isActive()){
                    closeApplication.setVisible(true);
                    mainPane.setVisible(false);
                    passwordPane.setVisible(false);
                    break;
                }
                Thread.sleep(100);
            } while (returnSendClass == null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //      Создание шары
    public void shareCommand(ActionEvent actionEvent) {
        ctx.writeAndFlush(new SendClass(Library.SHARE, user, text.getText(), clientList.getSelectionModel().getSelectedItem(), serverPath));
        sleep();
        clientList.getSelectionModel().clearSelection();
        message.setText(returnSendClass.getUser());
        returnSendClass = null;
    }

    public void updateFileListCommand(ActionEvent actionEvent) {
        clientFileList(clientPath);
        serverFileList();
    }

}
