package NIO;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.Iterator;

public class NIOServer implements Runnable {

    ServerSocketChannel srv;
    Selector selector;
    String fileName;
    int i = 0;

    @Override
    public void run() {
        try {
            srv = ServerSocketChannel.open();
            srv.bind(new InetSocketAddress(8189));
            System.out.println("server started!");
            srv.configureBlocking(false);
            selector = Selector.open();
            srv.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select(); // block
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isAcceptable()) {
                        SocketChannel channel = ((ServerSocketChannel) key.channel())
                                .accept();
                        System.out.println("Client accepted");
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        SocketChannel channel = (SocketChannel) key.channel();
                        int cnt = channel.read(buffer);
                        if (cnt == -1) {
                            System.out.println("client leave chat!");
                            channel.close();
                        }
                        buffer.flip();
                        StringBuilder msg = new StringBuilder();
                        if (i == 0){
                            byte buf;
                            while (buffer.hasRemaining()) {
                                buf = buffer.get();
                                if (buf == 124 && i == 1) {
                                    fileName = msg.toString();
                                    buffer.compact();
                                    FileSave(buffer, channel, cnt);
                                    break;
                                }
                                msg.append((char) buf);
                                if (msg.toString().equals("./upload")){
                                    msg.setLength(0);
                                    i++;
                                }
                            }
                        }
                        //channel.close();
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private void FileSave(ByteBuffer buffer, SocketChannel channel, int cnt) throws IOException {
        RandomAccessFile aFile = new RandomAccessFile("./common/src/main/resources/" + fileName, "rw");
        FileChannel inChannel = aFile.getChannel();
        inChannel.position(0);
        while(cnt > 0){
            buffer.flip();
            while(buffer.hasRemaining()){
                int bytesWritten = inChannel.write(buffer);;
            }
            buffer.clear();
            cnt = channel.read(buffer);
        }
        aFile.close();
        i = 0;
        channel.write(ByteBuffer.wrap("File saved successfully|".getBytes()));
    }

    public static void main(String[] args) {
        new Thread(new NIOServer()).start();
    }
}
