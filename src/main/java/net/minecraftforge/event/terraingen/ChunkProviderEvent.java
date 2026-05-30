package net.minecraftforge.event.terraingen;

import cpw.mods.fml.common.eventhandler.ForgeEvent;
import net.minecraft.src.Block;
import net.minecraft.src.World;
import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.IChunkProvider;

public class ChunkProviderEvent extends ForgeEvent
{

    public final IChunkProvider chunkProvider;

    public ChunkProviderEvent(IChunkProvider chunkProvider)
    {
        this.chunkProvider = chunkProvider;
    }

    /**
     * This event is fired when a chunks blocks are replaced by a biomes top and
     * filler blocks.
     *
     * You can set the result to DENY to prevent the default replacement.
     */
    public static class ReplaceBiomeBlocks extends ChunkProviderEvent
    {
        public final int chunkX;
        public final int chunkZ;
        public final Block[] blockArray;
        public final byte[] metaArray; // CAN BE NULL
        public final BiomeGenBase[] biomeArray;
        public final World world; // CAN BE NULL

        @Deprecated // TODO: Remove in 1.8
        public ReplaceBiomeBlocks(IChunkProvider chunkProvider, int chunkX, int chunkZ, Block[] blockArray, BiomeGenBase[] biomeArray)
        {
            this(chunkProvider, chunkX, chunkZ, blockArray, new byte[256], biomeArray, null);
        }

        @Deprecated // TODO: Remove in 1.8
        public ReplaceBiomeBlocks(IChunkProvider chunkProvider, int chunkX, int chunkZ, Block[] blockArray, byte[] metaArray, BiomeGenBase[] biomeArray)
        {
            this(chunkProvider, chunkZ, chunkZ, blockArray, metaArray, biomeArray, null);
        }

        public ReplaceBiomeBlocks(IChunkProvider chunkProvider, int chunkX, int chunkZ, Block[] blockArray, byte[] metaArray, BiomeGenBase[] biomeArray, World world)
        {
            super(chunkProvider);
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.blockArray = blockArray;
            this.biomeArray = biomeArray;
            this.metaArray = metaArray;
            this.world = world;
        }

        @Override
        public boolean hasResult() {
            return true;
        }
    }

    /**
     * This event is fired before a chunks terrain noise field is initialized.
     *
     * You can set the result to DENY to substitute your own noise field.
     */
    public static class InitNoiseField extends ChunkProviderEvent
    {
        public double[] noisefield;
        public final int posX;
        public final int posY;
        public final int posZ;
        public final int sizeX;
        public final int sizeY;
        public final int sizeZ;

        public InitNoiseField(IChunkProvider chunkProvider, double[] noisefield, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ)
        {
            super(chunkProvider);
            this.noisefield = noisefield;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        @Override
        public boolean hasResult() {
            return true;
        }
    }
}