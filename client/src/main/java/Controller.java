import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {

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

    private List<File> clientFileList = new ArrayList<>();
    public static Socket socket;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private String clientPath = "./client/src/main/resources/";
    private File dir = new File(clientPath);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try
        {
            socket = new Socket("localhost", 8189);
            is = new ObjectDecoderInputStream(socket.getInputStream());
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            clientFileList(dir);
            serverFileList();
            listViewClient.setOnMouseClicked(a -> {
                if (a.getClickCount() == 2) {
                    String[] fileName = listViewClient.getSelectionModel().getSelectedItem().split(" : ");
                    Library.ReadFileClient(os, fileName[0]);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void singIn(ActionEvent actionEvent) {
    }

    public void cancel(ActionEvent actionEvent) {
    }

    public void checkIn(ActionEvent actionEvent) {
    }

    public void delCommand(ActionEvent actionEvent) {
        try {
            byte[] buffer = ("test buffer".getBytes());
            SendClass message = new SendClass("upload", buffer);
            os.writeObject(message);
            Object msg = is.readObject();
            SendClass sendClass = (SendClass) msg;
            System.out.println(sendClass.getCommand());
            for (int i = 0; i < sendClass.getBuffer().length; i++) {
                System.out.print((char) sendClass.getBuffer()[i]);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void serverFileList() throws IOException, ClassNotFoundException {
        listViewServer.getItems().clear();
        os.writeObject(new SendClass(Library.SERVERFILELIST));
        Object msg = is.readObject();
        SendClass sendClass = (SendClass) msg;
        String[] serverFileList = (new String(sendClass.getBuffer())).split("\\|");
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

    private File findFileByName(String fileName) {
        for (File file : clientFileList) {
            if (file.getName().equals(fileName)){
                return file;
            }
        }
        return null;
    }
}
