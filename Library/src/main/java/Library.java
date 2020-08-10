import java.io.*;

public interface Library {
    public static byte[] buffer = new byte[1024];

    public static void fileRead(DataOutputStream os, String currentFile) throws IOException {
        FileInputStream fis = new FileInputStream(currentFile);
        while (fis.available() > 0) {
            int bytesRead = fis.read(buffer);
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
    }

    public static void fileSave(DataOutputStream os, DataInputStream is, String fileName, long fileLength) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        try(FileOutputStream fos = new FileOutputStream(file)) {
            long test = fileLength/1024;
            if (test == 0) for (long i = 0; i < 1; i++) {
                int bytesRead = is.read(buffer);
                fos.write(buffer, 0, bytesRead);
            }
            else for (long i = 0; i <= test; i++) {
                int bytesRead = is.read(buffer);
                fos.write(buffer, 0, bytesRead);
            }
        }
        os.writeUTF("OK");
        os.flush();
    }

}
