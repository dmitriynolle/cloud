import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public Button send;
    public ListView<String> listViewClient;
    public TextField text;
    public ListView<String> listViewServer;
    private List<File> clientFileList;
    public static Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private String clientPath = "./client/src/main/resources/";
    private File dir = new File(clientPath);



    public void sendCommand(ActionEvent actionEvent) {
        String[] commandLine = text.getText().split(" ");
        try {
            os.writeUTF(commandLine[0]);
            os.writeUTF(commandLine[1]);
            if (is.readBoolean()){
                String fileName = is.readUTF();
                System.out.println("fileName: " + fileName);
                long fileLength = is.readLong();
                System.out.println("fileLength: " + fileLength);
                Library.fileSave(os, is, clientPath + fileName, fileLength);
                loadClientFileList(dir);
            } else System.out.println("No file !!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try{
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread.sleep(1000);
            while (true){
                String msg = is.readUTF();
                if (msg.equals("|")) break;
                listViewServer.getItems().add(msg);
            }
            clientFileList = new ArrayList<>();

            if (!dir.exists()) {
                throw new RuntimeException("directory resource not exists on client");
            }
            loadClientFileList(dir);
            listViewClient.setOnMouseClicked(a -> {
                if (a.getClickCount() == 2) {
                    String[] fileName = listViewClient.getSelectionModel().getSelectedItem().split(" : ");
                    File currentFile = findFileByName(fileName[0]);
                    if (currentFile != null) {
                        try {
                            os.writeUTF("./upload");
                            os.writeUTF(fileName[0]);
                            os.writeLong(currentFile.length());
                            Library.fileRead(os, clientPath + fileName[0]);
                            String response = is.readUTF();
                            System.out.println(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadClientFileList(File dir) {
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
