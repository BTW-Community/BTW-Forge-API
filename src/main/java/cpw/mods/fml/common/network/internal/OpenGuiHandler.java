package cpw.mods.fml.common.network.internal;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.internal.FMLMessage.OpenGui;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.Level;

public class OpenGuiHandler extends SimpleChannelInboundHandler<OpenGui> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OpenGui msg) throws Exception
    {
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        player.openGui(msg.modId, msg.modGuiId, player.worldObj, msg.x, msg.y, msg.z);
        player.openContainer.windowId = msg.windowId;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log(Level.ERROR, cause, "OpenGuiHandler exception");
        super.exceptionCaught(ctx, cause);
    }

}