import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.CharsetUtil;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SerialHandler extends ChannelInboundHandlerAdapter {

    private static ConcurrentLinkedDeque<SocketChannel> channels = new ConcurrentLinkedDeque<>();
    private String name;
    private static int cnt = 0;
    private ByteBuf buffer;
    SendClass sendClass;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        cnt++;
        name = "user#" + cnt;
        System.out.println("Client: " + name + " connected!");
        channels.add((SocketChannel) ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // disconnect
        System.out.println("client: " + name + " leave");
        channels.remove((SocketChannel) ctx.channel());
        cnt--;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Library.SelectFunction(msg, ctx);
    }
}