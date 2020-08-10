import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class ConnectionHandler implements Runnable {

    private DataInputStream is;
    private DataOutputStream os;
    private File dir;
    private String clientPath = "./server/src/main/resources/";
    private String clientFileList;


    public ConnectionHandler(Socket socket) throws IOException, InterruptedException {
        System.out.println("Client Connected");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        dir = new File(clientPath);
        for(File file: Objects.requireNonNull(dir.listFiles())) {
            os.writeUTF(file.getName() + " : " + file.length());
        }
        os.writeUTF("|");
//        Thread.sleep(2000);
    }



    @Override
    public void run() {
        boolean foundFileName = false;
        while (true) {
            try {
                String command = is.readUTF();
                if (command.equals("./download")){
                    String fileName = is.readUTF();
                    for (File file : Objects.requireNonNull(dir.listFiles())) {
                        if (file.getName().equals(fileName)){
                            os.writeBoolean(true);
                            os.writeUTF(fileName);
                            os.writeLong(file.length());
                            Library.fileRead(os, clientPath + fileName);
                            System.out.println(is.readUTF());
                        }
                    }
                    if (!foundFileName) os.writeBoolean(foundFileName);
                }
                if (command.equals("./upload")) {
                    String fileName = is.readUTF();
                    System.out.println("fileName: " + fileName);
                    long fileLength = is.readLong();
                    System.out.println("fileLength: " + fileLength);
                    Library.fileSave(os, is, clientPath + fileName,fileLength);
                }
            } catch (Exception e) {
                System.out.println("Connection close");
                Thread.currentThread().stop();
                e.printStackTrace();
            }
        }
    }
}
