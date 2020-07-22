import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class ConnectionHandler implements Runnable {

    private DataInputStream is;
    private DataOutputStream os;
    private File dir;

    public ConnectionHandler(Socket socket) throws IOException, InterruptedException {
        System.out.println("Connection accepted");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        dir = new File(Server.serverPath);
//        Thread.sleep(2000);
    }



    @Override
    public void run() {
        byte [] buffer = new byte[1024];
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
                            FileInputStream fis = new FileInputStream(file);
                            while (fis.available() > 0) {
                                int bytesRead = fis.read(buffer);
                                os.write(buffer, 0, bytesRead);
                            }
                            os.flush();
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
                    File file = new File(Server.serverPath + "/" + fileName);
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
                }
            } catch (Exception e) {
                System.out.println("Connection close");
                Thread.currentThread().stop();
                e.printStackTrace();
            }
        }
    }
}
