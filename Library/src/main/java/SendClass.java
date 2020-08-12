import java.io.Serializable;

public class SendClass implements Serializable {

    private String command;
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

    public SendClass(String command, String fileName){
        this.command = command;
        this.fileName = fileName;
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


}
