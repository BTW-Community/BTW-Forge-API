package btw.community.forge.mixin.extensions;

import btw.community.forge.extensions.IBlockAccessExtensions;
import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class WorldExtensionsMixin implements IBlockAccessExtensions {
    @Shadow
    public abstract int getBlockId(int var1, int var2, int var3);

    @Shadow public abstract TileEntity getBlockTileEntity(int var1, int var2, int var3);

    @Shadow public abstract boolean setBlock(int par1, int par2, int par3, int par4, int par5, int par6);

    @Override
    public boolean setBlock(int x, int y, int z, Block blockIn, int metadataIn, int flags) {
        int id;
        if (blockIn == null) {
            id = 0;
        }
        else {
            id = blockIn.blockID;
        }
        return setBlock(x, y, z, id, metadataIn, flags);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return Block.blocksList[getBlockId(x, y, z)];
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return getBlockTileEntity(x, y, z);
    }
}
