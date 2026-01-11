package net.minecraftforge.client.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.util.IChatComponent;

@Cancelable
public class ClientChatReceivedEvent extends Event
{
    public IChatComponent message;
    public ClientChatReceivedEvent(IChatComponent message)
    {
        this.message = message;
    }
}