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
    String clientPath = "./client/src/main/resources/";

    File dir = new File(serverPath);

    String[] data = new String[10];
    public StringBuilder message = new StringBuilder();


    //    Чтение файла с диска
//    static void LoadFile(ByteBuffer msg, SocketChannel channel, String source, String fileName) {
//        try {
//            FileInputStream fis = new FileInputStream("./" + source + PATH + "/" + fileName);
////            ByteBuffer msg = ByteBuffer.allocate(1024);
//            while (fis.available() > 0) {
//                int bytesWritten = fis.read(msg.array());
//                msg.flip();
//                channel.write(msg);
//                msg.clear();
//                }
//
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
////    Запись файла на диск
//    public static void SaveFile(SocketChannel channel, int cnt, String source, String fileName){
//        try {
//            RandomAccessFile file = new RandomAccessFile("./" + source + PATH + "/" + fileName, "rw");
//            FileChannel inChannel = file.getChannel();
//            ByteBuffer msg = ByteBuffer.allocate(1024);
//            cnt = channel.read(msg);
//            while(cnt > 0){
//                msg.flip();
//                while(msg.hasRemaining()){
//                    int bytesWritten = inChannel.write(msg);;
//                }
//                msg.clear();
//                cnt = channel.read(msg);
//            }
//            file.close();
//            data[0] = null;
//            data[1] = null;
//            msg = ByteBuffer.wrap("File saved successfully".getBytes());
//            channel.write(msg);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
    static void SelectFunction(Object msg, ChannelHandlerContext ctx) {
        SendClass sendClass = (SendClass) msg;

        if ((sendClass).getCommand().equals(SERVERFILELIST)){
            ctx.writeAndFlush(ServerFileList(sendClass));
        }

        if ((sendClass).getCommand().equals(FILEUPLOAD)){
            try{
            File file = new File(serverPath + sendClass.getFileName());
            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(sendClass.getBuffer());
            }catch (IOException e){
                e.printStackTrace();
            }
//            System.out.println(new String(sendClass.getBuffer()));
        }
//        if (data[0].equals(Library.FILEUPLOAD) || data[0].equals(Library.FILEDOWNLOAD)){
//            while (buffer.hasRemaining()) {
//                byte buf = buffer.get();
//                if (buf == 124) {
//                    data[1] = message.toString();
//                    message.setLength(0);
//                    buffer.compact();
//                    break;
//                }
//                message.append((char) buf);
//            }
//        }
//        switch (data[0]) {
//            case Library.FILEUPLOAD:
////                SaveFile(channel, cnt, "server", data[1]);
//                break;
//            case Library.FILEDOWNLOAD:
////                LoadFile(buffer, channel, "server", data[1]);
//                break;
//        }
    }

    static Object ServerFileList(SendClass sendClass) {
        String buffer = "";
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            buffer += file.getName() + " : " + file.length() + Library.DELIMITER;
        }
        sendClass.setBuffer(buffer.getBytes());
        return sendClass;
    }

    static void ReadFileClient(ObjectEncoderOutputStream os, String fileName) {
        try {
            SendClass sendClass = new SendClass(Library.FILEUPLOAD, fileName);
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(clientPath + fileName);
            while (fis.available() > 0) {
                buffer = new byte[1024];
                int bytesRead = fis.read(buffer);
                sendClass.clearBuffer();
                sendClass.setBuffer(buffer);
                os.writeObject(sendClass);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
