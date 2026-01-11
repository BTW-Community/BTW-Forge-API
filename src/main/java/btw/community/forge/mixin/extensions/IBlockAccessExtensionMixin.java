package btw.community.forge.mixin.extensions;

import btw.community.forge.extensions.IBlockAccessExtensions;
import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IBlockAccess.class)
public abstract interface IBlockAccessExtensionMixin extends IBlockAccessExtensions {
    @Shadow public abstract int getBlockId(int var1, int var2, int var3);

    @Shadow public abstract TileEntity getBlockTileEntity(int var1, int var2, int var3);
    @Override
    public default boolean setBlock(int x, int y, int z, Block blockIn, int metadataIn, int flags) {
        return false;
    }

    @Override
    public default Block getBlock(int x, int y, int z) {
        return Block.blocksList[getBlockId(x, y, z)];
    }

    @Override
    public default TileEntity getTileEntity(int x, int y, int z) {
        return getBlockTileEntity(x, y, z);
    }
}
