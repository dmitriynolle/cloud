import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public interface Library {

    String DELIMITER = "|";
    String FILEUPLOAD = "./upload";
    String FILEDOWNLOAD = "./download";
    String FILEDELETE = "./delete";
    String SERVERFILELIST = "./filelist";
    String SIGN_IN = "./signin";
    String SIGN_UP = "./signup";
    String ERROR = "./error";
    String MESSAGE = "./message";
    String MKDIR = "./mkdir";
    String USERLIST = "./userlist";
    String  SHARE = "./share";
    String serverPath = "./server/src/main/resources/";
//    String[] variables = new String[5];

//        Разгребает входящий объект
    static void SelectFunction(Object msg, ChannelHandlerContext ctx, int flag) {
        SendClass sendClass = (SendClass) msg;
        switch (sendClass.getCommand()){
            case(SHARE):
                SqlClient.addFile(sendClass.getDelFile(), sendClass.getFilePath(), sendClass.getFileName(), (new File(sendClass.getFilePath() + sendClass.getFileName()).length()), sendClass.getUser());
                ctx.writeAndFlush(new SendClass(Library.MESSAGE, "File '" + sendClass.getFileName() + "' shared for " + sendClass.getDelFile()));
                break;

            case(SIGN_IN):
                String sql1 = SqlClient.getNickname(sendClass.getUser(), sendClass.getFileName());
                ctx.writeAndFlush(new SendClass(Library.MESSAGE, sql1));
                break;

            case(SIGN_UP):
                String sql2 = SqlClient.regNickname(sendClass.getUser(), sendClass.getFileName());
                if (!sql2.equals(Library.ERROR)){
                    File theDir = new File(serverPath + sql2 + "/");
                    theDir.mkdir();
                }
                ctx.writeAndFlush(new SendClass(Library.MESSAGE, sql2));
                break;

            case(SERVERFILELIST):
                ctx.writeAndFlush(ServerFileList(sendClass));
                break;

            case(FILEUPLOAD):
                if (flag == 1)
                    FileSave(sendClass, sendClass.getFilePath());

                else{
                    FileSave(sendClass, sendClass.getFilePath());
                    if (sendClass.getBuffer().length < 1024)
                        SqlClient.addFile(sendClass.getUser(), sendClass.getFilePath(), sendClass.getFileName(), (new File(sendClass.getFilePath() + sendClass.getFileName()).length()), "2");
                }
                if (sendClass.getBuffer().length < 1024)
                    ctx.writeAndFlush(new SendClass(Library.MESSAGE, "File '" + sendClass.getFileName() + "' saved"));
                break;

            case (FILEDOWNLOAD):
                String[] share = sendClass.getFileName().split("` ");
                String fileName;
                String filePath;
                if (share.length == 2) {

                    fileName = share[1];
                    filePath = SqlClient.getPath(fileName, share[0]) + Library.DELIMITER + sendClass.getFilePath().split("\\|")[1];
                }
                else{
                    fileName = share[0];
                    filePath = sendClass.getFilePath();
                }
                ReadFile(ctx, sendClass.getUser(), fileName, filePath);
                break;

            case(FILEDELETE):
                FileDelete(ctx, sendClass.getUser(), sendClass.getFileName(), sendClass.getFilePath());
                SqlClient.delFile(sendClass.getFilePath(), sendClass.getFileName());
                break;

            case(MKDIR):
                File dir = new File(sendClass.getFilePath() + sendClass.getFileName());
                dir.mkdir();
                SqlClient.addFile(sendClass.getUser(), sendClass.getFilePath(), sendClass.getFileName(), 0, "1");

            case(USERLIST):
                String[] userList = (String[]) SqlClient.getUsers();
                String buffer = "";
                for (int i = 0; i < userList.length; i++) {
                    buffer += userList[i] + Library.DELIMITER;
                }
                ctx.writeAndFlush(new SendClass(Library.MESSAGE, buffer));
        }
    }

//        Удаление файла
    static void FileDelete(ChannelHandlerContext ctx, String User, String fileName, String path) {
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
    static Object ServerFileList(SendClass sendClass) {
        String [][] fileListSql = (String[][]) SqlClient.fileList(sendClass.getUser(), sendClass.getFileName());
        String buffer = "";
        if (fileListSql.length != 0)
        {
            for (String[] string : fileListSql) {
                if (string[2].equals("1"))
                    buffer += string[0] + Library.DELIMITER;
                if (string[2].equals("2"))
                    buffer += string[0] + " : " + string[1] + Library.DELIMITER;
                if (!string[2].equals("1") && !string[2].equals("2"))
                    buffer += string[2] + "` " + string[0] + " : " + string[1] + Library.DELIMITER;
            }
        }
        sendClass.setBuffer(buffer.getBytes());
        return sendClass;
    }

//        Чтение файла
    static void ReadFile(ChannelHandlerContext ctx, String user, String fileName, String path) {
        try {
            String[] filePath = path.split("\\|");
            File dir = new File(filePath[0]);
            SendClass sendClass;
            byte[] buffer;
            File file = findFileByName(fileName, dir);
            FileInputStream fis = new FileInputStream(filePath[0] + fileName);
            long fileSize = file.length();
            sendClass = new SendClass(Library.FILEUPLOAD, user, fileName, Library.FILEDELETE, filePath[1]);
            while (fis.available() > 0) {
                if (fileSize/1024 < 1)
                    buffer = new byte[(int) fileSize];
                else buffer = new byte[1024];
                fis.read(buffer);
                sendClass.clearBuffer();
                sendClass.setBuffer(buffer);
                ctx.write(sendClass);
                sendClass = new SendClass(Library.FILEUPLOAD, user, fileName, "", filePath[1]);
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
