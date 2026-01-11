package net.minecraftforge.common;

import net.minecraft.src.*;

import java.io.File;

//Class used internally to provide the world specific data directories.

public class WorldSpecificSaveHandler implements ISaveHandler
{
    private WorldServer world;
    private ISaveHandler parent;
    private File dataDir;

    public WorldSpecificSaveHandler(WorldServer world, ISaveHandler parent)
    {
        this.world = world;
        this.parent = parent;
        dataDir = new File(((AnvilChunkLoader)world.theChunkProviderServer.currentChunkLoader).chunkSaveLocation, "data");
        dataDir.mkdirs();
    }

    @Override public WorldInfo loadWorldInfo() { return parent.loadWorldInfo(); }
    @Override public void checkSessionLock() throws MinecraftException { parent.checkSessionLock(); }
    @Override public IChunkLoader getChunkLoader(WorldProvider var1) { return parent.getChunkLoader(var1); }
    @Override public void saveWorldInfoWithPlayer(WorldInfo var1, NBTTagCompound var2) { parent.saveWorldInfoWithPlayer(var1, var2); }
    @Override public void saveWorldInfo(WorldInfo var1){ parent.saveWorldInfo(var1); }
    @Override public IPlayerFileData getSaveHandler() { return parent.getSaveHandler(); }
    @Override public void flush() { parent.flush(); }
    @Override public String getWorldDirectoryName() { return parent.getWorldDirectoryName(); }

    @Override
    public void loadModSpecificData(WorldServer worldServer) {
        parent.loadModSpecificData(worldServer);
    }

    @Override
    public void saveModSpecificData(WorldServer worldServer) {
        parent.saveModSpecificData(worldServer);
    }

//    @Override public File getWorldDirectory() { return parent.getWorldDirectory(); }

    /**
     * Gets the file location of the given map
     */
    @Override
    public File getMapFileFromName(String name)
    {
        return new File(dataDir, name + ".dat");
    }

}