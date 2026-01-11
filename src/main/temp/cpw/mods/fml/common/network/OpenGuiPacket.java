package cpw.mods.fml.common.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;

public class OpenGuiPacket extends FMLPacket {
    /**
     * The id of the window that the action occurred in.
     */
    private int windowId;
    private int networkId;
    private int modGuiId;
    /**
     * The x coordinate of this ChunkPosition
     */
    private int x;
    /**
     * The y coordinate of this ChunkPosition
     */
    private int y;
    /**
     * The z coordinate of this ChunkPosition
     */
    private int z;

    public OpenGuiPacket() {
        super(FMLPacket.Type.GUIOPEN);
    }

    public byte[] generatePacket(Object... data) {
        ByteArrayDataOutput dat = ByteStreams.newDataOutput();
        dat.writeInt((Integer)data[0]);
        dat.writeInt((Integer)data[1]);
        dat.writeInt((Integer)data[2]);
        dat.writeInt((Integer)data[3]);
        dat.writeInt((Integer)data[4]);
        dat.writeInt((Integer)data[5]);
        return dat.toByteArray();
    }

    public FMLPacket consumePacket(byte[] data) {
        ByteArrayDataInput dat = ByteStreams.newDataInput(data);
        this.windowId = dat.readInt();
        this.networkId = dat.readInt();
        this.modGuiId = dat.readInt();
        this.x = dat.readInt();
        this.y = dat.readInt();
        this.z = dat.readInt();
        return this;
    }

    public void execute(INetworkManager network, FMLNetworkHandler handler, NetHandler netHandler, String userName) {
        EntityPlayer player = netHandler.getPlayer();
        player.openGui(this.networkId, this.modGuiId, player.worldObj, this.x, this.y, this.z);
        player.openContainer.windowId = this.windowId;
    }
}
