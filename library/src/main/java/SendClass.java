import java.io.Serializable;

public class SendClass implements Serializable {

    private String command;
    private String user;
    private String filePath;
    private String fileName;
    private String delFile;
    private byte[] buffer;

    public SendClass(String command){
        this.command = command;
    }

    public SendClass(String command, byte[] buffer){
        this.command = command;
        this.buffer = buffer;
    }

    public SendClass(String command, String user){
        this.command = command;
        this.user = user;
    }

    public SendClass(String command, String user, String fileName){
        this.command = command;
        this.fileName = fileName;
        this.user = user;
    }

    public SendClass(String command, String user, String fileName, String delFile){
        this.command = command;
        this.user = user;
        this.fileName = fileName;
        this.delFile = delFile;
    }

    public SendClass(String command, String user, String fileName, String delFile, String filePath){
        this.command = command;
        this.user = user;
        this.fileName = fileName;
        this.delFile = delFile;
        this.filePath = filePath;
    }

    public String getCommand() {
        return command;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDelFile() {
        return delFile;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDelFile(String delFile) {
        this.delFile = delFile;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public void clearBuffer(){this.buffer = null;}

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
