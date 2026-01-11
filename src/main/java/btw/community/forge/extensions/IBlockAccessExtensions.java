package btw.community.forge.extensions;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import org.jetbrains.annotations.Nullable;

public interface IBlockAccessExtensions {

    default boolean setBlock(int x, int y, int z, Block blockIn, int metadataIn, int flags) {
        throw new IllegalStateException("how did this not get overriden by mixin? what???");
    }

    default boolean setBlock(int x, int y, int z, Block blockIn) {
        throw new IllegalStateException("how did this not get overriden by mixin? what???");
    }

    default @Nullable Block getBlock(int x, int y, int z) {
        throw new IllegalStateException("how did this not get overriden by mixin? what???");
    }

    default TileEntity getTileEntity(int x, int y, int z) {
        throw new IllegalStateException("how did this not get overriden by mixin? what???");
    }
}
