package cpw.mods.fml.common.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.NetHandler;

public class EntitySpawnAdjustmentPacket extends FMLPacket {
    /**
     * "The entity ID, in this case it's the player ID."
     */
    public int entityId;
    public int serverX;
    public int serverY;
    public int serverZ;

    public EntitySpawnAdjustmentPacket() {
        super(FMLPacket.Type.ENTITYSPAWNADJUSTMENT);
    }

    public byte[] generatePacket(Object... data) {
        ByteArrayDataOutput dat = ByteStreams.newDataOutput();
        dat.writeInt((Integer)data[0]);
        dat.writeInt((Integer)data[1]);
        dat.writeInt((Integer)data[2]);
        dat.writeInt((Integer)data[3]);
        return dat.toByteArray();
    }

    public FMLPacket consumePacket(byte[] data) {
        ByteArrayDataInput dat = ByteStreams.newDataInput(data);
        this.entityId = dat.readInt();
        this.serverX = dat.readInt();
        this.serverY = dat.readInt();
        this.serverZ = dat.readInt();
        return this;
    }

    public void execute(INetworkManager network, FMLNetworkHandler handler, NetHandler netHandler, String userName) {
        FMLCommonHandler.instance().adjustEntityLocationOnClient(this);
    }
}
