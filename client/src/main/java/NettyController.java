
import io.netty.channel.ChannelHandlerContext;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;

public class NettyController implements Initializable {

    private static SendClass returnSendClass;
    public Button delete;
    public ListView<String> listViewClient;
    public TextField text;
    public ListView<String> listViewServer;
    public Label message;
    public Button singIn;
    public Button cancel;
    public Button checkIn;
    public AnchorPane passwordPane;
    public AnchorPane mainPane;
    public TextField loginText;
    public PasswordField passwordText;
    public Label errorText;
    public Label command;
    public Button copy;

    private List<File> clientFileList = new ArrayList<>();
    private String clientPath = "./client/src/main/resources/";
    private File dirClient = new File(clientPath);
    private NettyNetwork network;
    public static ChannelHandlerContext ctx;

    public static void setCtx(ChannelHandlerContext ctm) {
        ctx = ctm;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        network = NettyNetwork.getInstance();
        try
        {
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

    public void copyCommand(ActionEvent actionEvent) {
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

    public void singIn(ActionEvent actionEvent) {
        String login = loginText.getText();
        String pas = passwordText.getText();
        if (login.equals("") || pas.equals("")) errorText.setText("Login and password must not be empty");
        else {
            ctx.writeAndFlush(new SendClass(Library.SING_IN, login, pas));
            sleep();
            if (!returnSendClass.getFileName().equals(Library.ERROR)){
                passwordPane.setVisible(false);
                mainPane.setVisible(true);
                Stage stage = (Stage) mainPane.getScene().getWindow();
                stage.setTitle("My Cloud | User: " + returnSendClass.getFileName());
                clientFileList(dirClient);
                serverFileList();
            }
            else {
                errorText.setText("Incorrect login or password");
                loginText.clear();
                passwordText.setText(null);
            }
        }
    }

    public void cancel(ActionEvent actionEvent) {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        NettyNetwork.StopTread();
        stage.close();
    }

    public void checkIn(ActionEvent actionEvent) {
        String login = loginText.getText();
        String pas = passwordText.getText();
        if (login.equals("") || pas.equals("")) errorText.setText("Login and password must not be empty");
        else {
            ctx.writeAndFlush(new SendClass(Library.CHECK_IN, login, pas));
            sleep();
            System.out.println("");
            if (!returnSendClass.getFileName().equals(Library.ERROR)){
                passwordPane.setVisible(false);
                mainPane.setVisible(true);
                Stage stage = (Stage) mainPane.getScene().getWindow();
                stage.setTitle("My Cloud | User: " + returnSendClass.getFileName());
                clientFileList(dirClient);
                serverFileList();
            }
            else {
                errorText.setText("User already exists");
                loginText.clear();
                passwordText.setText(null);
            }
        }
    }

    public void delCommand(ActionEvent actionEvent) {
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

    public void serverFileList() {
        ctx.writeAndFlush(new SendClass(Library.SERVERFILELIST));
        sleep();
        listViewServer.getItems().clear();
        String[] serverFileList = (new String(returnSendClass.getBuffer())).split("\\|");
        for (String s : serverFileList) {
            listViewServer.getItems().add(s);
        }
    }

    private void clientFileList(File dir) {
        listViewClient.getItems().clear();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            clientFileList.add(file);
            listViewClient.getItems().add(file.getName() + " : " + file.length());
        }
    }

    public static void returnSendObject(Object msg){
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
