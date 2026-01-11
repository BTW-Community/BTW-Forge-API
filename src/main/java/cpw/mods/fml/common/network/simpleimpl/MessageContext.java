package cpw.mods.fml.common.network.simpleimpl;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.src.NetHandlerPlayClient;
import net.minecraft.src.INetHandler;
import net.minecraft.src.NetHandlerPlayServer;

/**
 * Context for the {@link IMessageHandler}
 *
 * @author cpw
 *
 */
public class MessageContext {
    /**
     * The {@link INetHandler} for this message. It could be a client or server handler, depending
     * on the {@link #side} recieved.
     */
    public final INetHandler netHandler;

    /**
     * The Side this message has been received on
     */
    public final Side side;
    /**
     * @param netHandler
     */
    MessageContext(INetHandler netHandler, Side side)
    {
        this.netHandler = netHandler;
        this.side = side;
    }

    public NetHandlerPlayServer getServerHandler()
    {
        return (NetHandlerPlayServer) netHandler;
    }

    public NetHandlerPlayClient getClientHandler()
    {
        return (NetHandlerPlayClient) netHandler;
    }
}