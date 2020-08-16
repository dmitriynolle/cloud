import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ListViewHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyController.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (((SendClass) msg).getCommand().equals(Library.SERVERFILELIST)){
            NettyController.returnSendObject(msg);
        }
        else if (((SendClass) msg).getCommand().equals(Library.MESSAGE)){
            NettyController.returnSendObject(msg);
        }
        else
            Library.SelectFunction(msg,ctx, 1);
    }
}
