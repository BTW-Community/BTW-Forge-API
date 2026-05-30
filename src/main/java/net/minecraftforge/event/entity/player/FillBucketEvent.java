package net.minecraftforge.event.entity.player;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.ForgeEvent;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.World;

@Cancelable
@ForgeEvent.HasResult
public class FillBucketEvent extends PlayerEvent
{
    /**
     * This event is fired when a player attempts to use a Empty bucket, it 
     * can be canceled to completely prevent any further processing.
     * 
     * If you set the result to 'ALLOW', it means that you have processed 
     * the event and wants the basic functionality of adding the new 
     * ItemStack to your inventory and reducing the stack size to process.
     * setResult(ALLOW) is the same as the old setHandeled();
     */

    public final ItemStack current;
    public final World world;
    public final MovingObjectPosition target;

    public ItemStack result;

    public FillBucketEvent(EntityPlayer player, ItemStack current, World world, MovingObjectPosition target)
    {
        super(player);
        this.current = current;
        this.world = world;
        this.target = target;
    }
}