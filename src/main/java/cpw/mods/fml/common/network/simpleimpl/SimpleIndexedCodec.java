package cpw.mods.fml.common.network.simpleimpl;

import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class SimpleIndexedCodec extends FMLIndexedMessageToMessageCodec<IMessage> {
    @Override
    public void encodeInto(ChannelHandlerContext ctx, IMessage msg, ByteBuf target) throws Exception
    {
        msg.toBytes(target);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, IMessage msg)
    {
        msg.fromBytes(source);
    }

}