package net.minecraftforge.client.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.src.ChatMessageComponent;

@Cancelable
public class ClientChatReceivedEvent extends Event
{
    public ChatMessageComponent message;
    public ClientChatReceivedEvent(ChatMessageComponent message)
    {
        this.message = message;
    }
}