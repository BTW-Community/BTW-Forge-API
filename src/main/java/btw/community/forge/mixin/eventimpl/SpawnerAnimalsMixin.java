package btw.community.forge.mixin.eventimpl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.SpawnerAnimals;
import net.minecraft.src.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpawnerAnimals.class)
public class SpawnerAnimalsMixin {
    @WrapOperation(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/EntityLiving;getCanSpawnHere()Z"))
    private boolean forge$wrappedFindChunks(EntityLiving instance, Operation<Boolean> original, WorldServer server, @Local EntityLiving entity, @Local(ordinal = 0) float xPos, @Local(ordinal = 1) float yPos, @Local(ordinal = 2) float zPos) {
        var canSpawn = ForgeEventFactory.canEntitySpawn(entity, server, xPos, yPos, zPos);
        return canSpawn == Event.Result.ALLOW || (canSpawn == Event.Result.DEFAULT && original.call(instance));
    }
}
