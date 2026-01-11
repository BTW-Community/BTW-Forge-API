package net.minecraftforge.event.entity.player;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.Achievement;

/**
 * When the player receives an achievement. If canceled the player will not receive anything.
 */
@Cancelable
public class AchievementEvent extends PlayerEvent {

    public final Achievement achievement;
    public AchievementEvent(EntityPlayer player, Achievement achievement)
    {
        super(player);
        this.achievement = achievement;
    }
}