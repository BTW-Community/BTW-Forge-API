package net.minecraftforge.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.ForgeEvent;
import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.EntityPlayerMP;

/**
 * ServerChatEvent is fired whenever a C01PacketChatMessage is processed. <br>
 * This event is fired via {@link ForgeHooks#onServerChatEvent(net.minecraft.network.NetServerHandler, String, ChatComponentTranslation)}, 
 * which is executed by the NetServerHandler#processChatMessage(net.minecraft.network.play.client.C01PacketChatMessage)<br>
 * <br>
 * {@link #username} contains the username of the player sending the chat message.<br>
 * {@link #message} contains the message being sent.<br>
 * {@link #player} the instance of EntityPlayerMP for the player sending the chat message.<br>
 * {@link #component} contains the instance of ChatComponentTranslation for the sent message.<br>
 * <br>
 * This event is {@link Cancelable}. <br>
 * If this event is canceled, the chat message is never distributed to all clients.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
@Cancelable
public class ServerChatEvent extends ForgeEvent
{
    public final String message, username;
    public final EntityPlayerMP player;
    public ChatMessageComponent component;
    public ServerChatEvent(EntityPlayerMP player, String message, ChatMessageComponent component)
    {
        super();
        this.message = message;
        this.player = player;
        this.username = player.username;
        this.component = component;
    }
}