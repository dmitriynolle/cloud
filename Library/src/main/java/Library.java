import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.*;
import java.util.Objects;

public interface Library {

    public static final String DELIMITER = "|";
    public static final String FILEUPLOAD = "./upload";
    public static final String FILEDOWNLOAD = "./download";
    public static final String FILEDELETE = "./delete";
    public static final String SERVERFILELIST = "./filelist";
    public static final String SING_IN = "./singin";
    public static final String CHECK_IN = "./checkin";
    public static final String ERROR = "./error";
    String serverPath = "./server/src/main/resources/";
    File dirServer = new File(serverPath);

    static void SelectFunction(Object msg, ChannelHandlerContext ctx) {
        SendClass sendClass = (SendClass) msg;

        if ((sendClass).getCommand().equals(SERVERFILELIST)){
            ctx.writeAndFlush(ServerFileList(sendClass));
        }

        if ((sendClass).getCommand().equals(FILEUPLOAD)){
            FileSave(sendClass);
        }
    }

    static void FileSave(SendClass sendClass) {
        try{
        File file = new File(serverPath + sendClass.getFileName());
        if (file.exists() && sendClass.getDelFile().equals(Library.FILEDELETE))
            file.delete();
        if (!file.exists())
            file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file, true);
        fos.write(sendClass.getBuffer());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    static Object ServerFileList(SendClass sendClass) {
        String buffer = "";
        for (File file : Objects.requireNonNull(dirServer.listFiles())) {
            buffer += file.getName() + " : " + file.length() + Library.DELIMITER;
        }
        sendClass.setBuffer(buffer.getBytes());
        return sendClass;
    }

    static void ReadFile(ObjectEncoderOutputStream os, String fileName, String path) {
        try {
            File dir = new File(path);
            SendClass sendClass = new SendClass(Library.FILEUPLOAD, fileName);
            byte[] buffer;
            File file = findFileByName(fileName, dir);
            FileInputStream fis = new FileInputStream(path + fileName);
            long fileSize = file.length();
            sendClass.setDelFile(Library.FILEDELETE);
            while (fis.available() > 0) {
                if (fileSize/1024 < 1)
                    buffer = new byte[(int) fileSize];
                else buffer = new byte[1024];
                int bytesRead = fis.read(buffer);
                sendClass.clearBuffer();
                sendClass.setBuffer(buffer);
                os.writeObject(sendClass);
                sendClass.setDelFile("");
                fileSize -= 1024;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static File findFileByName(String fileName, File dir) {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().equals(fileName)){
                return file;
            }
        }
        return null;
    }

}
