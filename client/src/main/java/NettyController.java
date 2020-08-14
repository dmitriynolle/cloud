import io.netty.channel.ChannelHandlerContext;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
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

    private final String clientPath = "./client/src/main/resources/";
    private final File dirClient = new File(clientPath);
    public static ChannelHandlerContext ctx;

    public static void setCtx(ChannelHandlerContext ctm) {
        ctx = ctm;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        NettyNetwork network = NettyNetwork.getInstance();
        try {
            listViewClient.setOnMouseClicked(a -> {
                String[] fileName = listViewClient.getSelectionModel().getSelectedItem().split(" : ");
                if (a.getClickCount() == 1) {
                    command.setText(Library.FILEUPLOAD);
                    text.setText(fileName[0]);
                }
            });
            listViewServer.setOnMouseClicked(a -> {
                String[] fileName = listViewServer.getSelectionModel().getSelectedItem().split(" : ");
                if (a.getClickCount() == 1) {
                    command.setText(Library.FILEDOWNLOAD);
                    text.setText(fileName[0]);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //        Запрос копирование файла
    public void copyCommand() {
        if (command.getText().equals(Library.FILEUPLOAD))
            Library.ReadFile(ctx, text.getText(), clientPath);
        else if (command.getText().equals(Library.FILEDOWNLOAD))
            ctx.writeAndFlush(new SendClass(Library.FILEDOWNLOAD, text.getText()));
        sleep();
        text.clear();
        message.setText(returnSendClass.getFileName());
        clientFileList(dirClient);
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
            if (!returnSendClass.getFileName().equals(Library.ERROR)) {
                passwordPane.setVisible(false);
                mainPane.setVisible(true);
                Stage stage = (Stage) mainPane.getScene().getWindow();
                stage.setTitle("My Cloud | User: " + returnSendClass.getFileName());
                clientFileList(dirClient);
                serverFileList();
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
            if (!returnSendClass.getFileName().equals(Library.ERROR)) {
                passwordPane.setVisible(false);
                mainPane.setVisible(true);
                Stage stage = (Stage) mainPane.getScene().getWindow();
                stage.setTitle("My Cloud | User: " + returnSendClass.getFileName());
                clientFileList(dirClient);
                serverFileList();
            } else {
                errorText.setText("User already exists");
                loginText.clear();
                passwordText.setText(null);
            }
        }
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
            Library.FileDelete(ctx, text.getText(), clientPath);
        else if (command.getText().equals(Library.FILEDOWNLOAD))
            ctx.writeAndFlush(new SendClass(Library.FILEDELETE, text.getText()));
        sleep();
        text.clear();
        message.setText(returnSendClass.getFileName());
        clientFileList(dirClient);
        serverFileList();
    }

    //        Запрос списка файлов с сервера
    public void serverFileList() {
        ctx.writeAndFlush(new SendClass(Library.SERVERFILELIST));
        sleep();
        listViewServer.getItems().clear();
        String[] serverFileList = (new String(returnSendClass.getBuffer())).split("\\|");
        for (String s : serverFileList) {
            listViewServer.getItems().add(s);
        }
    }

    //        Клиентский файл лист
    private void clientFileList(File dir) {
        listViewClient.getItems().clear();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            listViewClient.getItems().add(file.getName() + " : " + file.length());
        }
    }

    //        Используется для перехвата объекта от сервера
    public static void returnSendObject(Object msg) {
        returnSendClass = (SendClass) msg;
    }

    public void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
