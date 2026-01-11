package net.minecraftforge.client.event.sound;

import net.minecraft.src.ISound;
import net.minecraft.src.SoundCategory;
import net.minecraft.src.SoundManager;

/***
 * Raised when the SoundManager tries to play a normal sound.
 * 
 * If you return null from this function it will prevent the sound from being played,
 * you can return a different entry if you want to change the sound being played.
 */
//TODO: Replace PlaySoundEvent in 1.8
public class PlaySoundEvent17 extends SoundEvent
{
    public final String name;
    public final ISound sound;
    public final SoundCategory category;
    public ISound result;

    public PlaySoundEvent17(SoundManager manager, ISound sound, SoundCategory category)
    { 
        super(manager);
        this.sound = sound;
        this.category = category;
        this.name = sound.getPositionedSoundLocation().getResourcePath();
        this.result = sound;
    }
}