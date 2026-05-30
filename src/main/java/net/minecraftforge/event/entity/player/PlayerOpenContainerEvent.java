package net.minecraftforge.event.entity.player;

import cpw.mods.fml.common.eventhandler.ForgeEvent.HasResult;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Container;

@HasResult
public class PlayerOpenContainerEvent extends PlayerEvent
{

    public final boolean canInteractWith;

    /**
     * This event is fired when a player attempts to view a container during
     * player tick.
     * 
     * setResult ALLOW to allow the container to stay open
     * setResult DENY to force close the container (denying access)
     * 
     * DEFAULT is vanilla behaviour
     * 
     */

    public PlayerOpenContainerEvent(EntityPlayer player, Container openContainer)
    {
        super(player);
        this.canInteractWith = openContainer.canInteractWith(player);
    }
}