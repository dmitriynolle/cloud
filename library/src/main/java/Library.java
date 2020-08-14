import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public interface Library {

    public static final String DELIMITER = "|";
    public static final String FILEUPLOAD = "./upload";
    public static final String FILEDOWNLOAD = "./download";
    public static final String FILEDELETE = "./delete";
    public static final String SERVERFILELIST = "./filelist";
    public static final String SIGN_IN = "./signin";
    public static final String SIGN_UP = "./signup";
    public static final String ERROR = "./error";
    public static final String MESSAGE = "./message";
    String serverPath = "./server/src/main/resources/";
    String clientPath = "./client/src/main/resources/";
    String[] nickName = new String[5];

//        Разгребает входящий объект
    static void SelectFunction(Object msg, ChannelHandlerContext ctx, int flag) {
        SendClass sendClass = (SendClass) msg;
        switch (sendClass.getCommand()){
            case(SIGN_IN):
                nickName[1] = SqlClient.getNickname(sendClass.getFileName(), sendClass.getDelFile());
                ctx.writeAndFlush(new SendClass(Library.MESSAGE, nickName[1]));
                nickName[0] = nickName[1] + "/";
                break;

            case(SIGN_UP):
                nickName[1] = SqlClient.regNickname(sendClass.getFileName(), sendClass.getDelFile());
                if (!nickName[1].equals(Library.ERROR)){
                    nickName[0] = nickName[1] + "/";
                    File theDir = new File(serverPath + nickName[0]);
                    theDir.mkdir();
                }
                ctx.writeAndFlush(new SendClass(Library.MESSAGE, nickName[1]));
                break;

            case(SERVERFILELIST):
                ctx.writeAndFlush(ServerFileList(sendClass, serverPath + nickName[0]));
                break;

            case(FILEUPLOAD):
                if (flag == 1)
                    FileSave(sendClass, clientPath);

                else
                    FileSave(sendClass, serverPath + nickName[0]);
                ctx.writeAndFlush(new SendClass(Library.MESSAGE, "File '" + sendClass.getFileName() + "' saved"));
                break;

            case (FILEDOWNLOAD):
                ReadFile(ctx, sendClass.getFileName(), serverPath + nickName[0]);
                break;

            case(FILEDELETE):
                FileDelete(ctx, sendClass.getFileName(), serverPath + nickName[0]);
                break;
        }
    }

//        Удаление файла
    static void FileDelete(ChannelHandlerContext ctx, String fileName, String path) {
        File file = new File(path + fileName);
        if(file.delete()){
            ctx.writeAndFlush(new SendClass(Library.MESSAGE,"File '" + fileName + "' deleted"));
        }else ctx.writeAndFlush(new SendClass(Library.MESSAGE,"File '" + fileName + "' not found"));
    }

//        Запись файла
    static void FileSave(SendClass sendClass, String path) {
        try{
        File file = new File(path + sendClass.getFileName());
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

//        Создание серверного списка файлов
    static Object ServerFileList(SendClass sendClass, String path) {
        File dir = new File(path);
        String buffer = "";
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            buffer += file.getName() + " : " + file.length() + Library.DELIMITER;
        }
        sendClass.setBuffer(buffer.getBytes());
        return sendClass;
    }

//        Чтение файла
    static void ReadFile(ChannelHandlerContext ctx, String fileName, String path) {
        try {
            File dir = new File(path);
            SendClass sendClass;
            byte[] buffer;
            File file = findFileByName(fileName, dir);
            FileInputStream fis = new FileInputStream(path + fileName);
            long fileSize = file.length();
            sendClass = new SendClass(Library.FILEUPLOAD, fileName, Library.FILEDELETE);
            while (fis.available() > 0) {
                if (fileSize/1024 < 1)
                    buffer = new byte[(int) fileSize];
                else buffer = new byte[1024];
                fis.read(buffer);
                sendClass.clearBuffer();
                sendClass.setBuffer(buffer);
                ctx.write(sendClass);
                sendClass = new SendClass(Library.FILEUPLOAD, fileName, "");
                fileSize -= 1024;
            }
            ctx.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//        Проверка наличие файла на диске
    static File findFileByName(String fileName, File dir) {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().equals(fileName)){
                return file;
            }
        }
        return null;
    }

}
