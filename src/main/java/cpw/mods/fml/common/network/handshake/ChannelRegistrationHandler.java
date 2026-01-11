package cpw.mods.fml.common.network.handshake;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.src.NetworkManager;
import org.apache.logging.log4j.Level;

import java.util.Set;

public class ChannelRegistrationHandler extends SimpleChannelInboundHandler<FMLProxyPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception
    {
        Side side = msg.getTarget();
        NetworkManager manager = msg.getOrigin();
        if (msg.channel().equals("REGISTER") || msg.channel().equals("UNREGISTER"))
        {
            byte[] data = new byte[msg.payload().readableBytes()];
            msg.payload().readBytes(data);
            String channels = new String(data,Charsets.UTF_8);
            String[] split = channels.split("\0");
            Set<String> channelSet = ImmutableSet.copyOf(split);
            FMLCommonHandler.instance().fireNetRegistrationEvent(manager, channelSet, msg.channel(), side);
        }
        else
        {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log(Level.ERROR, cause, "ChannelRegistrationHandler exception");
        super.exceptionCaught(ctx, cause);
    }
}