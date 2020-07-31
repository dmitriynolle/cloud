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
    public ListView<String> listView;
    public TextField text;
    private List<File> clientFileList;
    public static Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private String clientPath = "./client/src/main/resources/";
    private byte [] buffer = new byte[1024];
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
                File file = new File(clientPath + "/" + fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                try(FileOutputStream fos = new FileOutputStream(file)) {
                    for (long i = 0; i < (fileLength / 1024 == 0 ? 1 : fileLength / 1024); i++) {
                        int bytesRead = is.read(buffer);
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                os.writeUTF("OK");
                os.flush();
                loadFileList(dir);
            } else System.out.println("No file !!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: 7/21/2020 init connect to server
        try{
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread.sleep(1000);
            clientFileList = new ArrayList<>();

            if (!dir.exists()) {
                throw new RuntimeException("directory resource not exists on client");
            }
            loadFileList(dir);
            listView.setOnMouseClicked(a -> {
                if (a.getClickCount() == 2) {
                    String[] fileName = listView.getSelectionModel().getSelectedItem().split(" : ");
                    File currentFile = findFileByName(fileName[0]);
                    if (currentFile != null) {
                        try {
//                            Для IO
//                            os.writeUTF("./upload");
//                            os.writeUTF(fileName[0]);
//                            os.writeLong(currentFile.length());

//                            Для NIO
                            os.write("./upload".getBytes());
                            os.write((fileName[0] + "|").getBytes());
                            FileInputStream fis = new FileInputStream(currentFile);
                            while (fis.available() > 0) {
                                int bytesRead = fis.read(buffer);
                                os.write(buffer, 0, bytesRead);
                            }
                            os.flush();
//                            Для IO
//                            byte response = is.readByte();
//                            System.out.println((char) response);

//                            Для NIO
                            int readBuffer = 0;
                            while (readBuffer != 124){
                                System.out.print((char) readBuffer);
                                readBuffer = is.read();
                            }
                            System.out.println();
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

    private void loadFileList(File dir) {
        listView.getItems().clear();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            clientFileList.add(file);
            listView.getItems().add(file.getName() + " : " + file.length());
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
