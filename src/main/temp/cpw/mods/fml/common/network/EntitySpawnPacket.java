package cpw.mods.fml.common.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.common.registry.IThrowableEntity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import net.minecraft.src.DataWatcher;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.NetHandler;
import net.minecraft.src.MathHelper;

public class EntitySpawnPacket extends FMLPacket {
    public int networkId;
    public int modEntityId;
    /**
     * "The entity ID, in this case it's the player ID."
     */
    public int entityId;
    public double scaledX;
    public double scaledY;
    public double scaledZ;
    public float scaledYaw;
    public float scaledPitch;
    public float scaledHeadYaw;
    /**
     * Metadata of the block.
     */
    public List metadata;
    public int throwerId;
    public double speedScaledX;
    public double speedScaledY;
    public double speedScaledZ;
    public ByteArrayDataInput dataStream;
    public int rawX;
    public int rawY;
    public int rawZ;

    public EntitySpawnPacket() {
        super(FMLPacket.Type.ENTITYSPAWN);
    }

    public byte[] generatePacket(Object... data) {
        EntityRegistry.EntityRegistration er = (EntityRegistry.EntityRegistration)data[0];
        Entity ent = (Entity)data[1];
        NetworkModHandler handler = (NetworkModHandler)data[2];
        ByteArrayDataOutput dat = ByteStreams.newDataOutput();
        dat.writeInt(handler.getNetworkId());
        dat.writeInt(er.getModEntityId());
        dat.writeInt(ent.entityId);
        dat.writeInt(MathHelper.floor_double(ent.posX * 32.0D));
        dat.writeInt(MathHelper.floor_double(ent.posY * 32.0D));
        dat.writeInt(MathHelper.floor_double(ent.posZ * 32.0D));
        dat.writeByte((byte)((int)(ent.rotationYaw * 256.0F / 360.0F)));
        dat.writeByte((byte)((int)(ent.rotationPitch * 256.0F / 360.0F)));
        if (ent instanceof EntityLiving) {
            dat.writeByte((byte)((int)(((EntityLiving)ent).rotationYawHead * 256.0F / 360.0F)));
        } else {
            dat.writeByte(0);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            ent.getDataWatcher().writeWatchableObjects(dos);
        } catch (IOException var17) {
        }

        dat.write(bos.toByteArray());
        if (ent instanceof IThrowableEntity) {
            Entity owner = ((IThrowableEntity)ent).getThrower();
            dat.writeInt(owner == null ? ent.entityId : owner.entityId);
            double maxVel = 3.9D;
            double mX = ent.motionX;
            double mY = ent.motionY;
            double mZ = ent.motionZ;
            if (mX < -maxVel) {
                mX = -maxVel;
            }

            if (mY < -maxVel) {
                mY = -maxVel;
            }

            if (mZ < -maxVel) {
                mZ = -maxVel;
            }

            if (mX > maxVel) {
                mX = maxVel;
            }

            if (mY > maxVel) {
                mY = maxVel;
            }

            if (mZ > maxVel) {
                mZ = maxVel;
            }

            dat.writeInt((int)(mX * 8000.0D));
            dat.writeInt((int)(mY * 8000.0D));
            dat.writeInt((int)(mZ * 8000.0D));
        } else {
            dat.writeInt(0);
        }

        if (ent instanceof IEntityAdditionalSpawnData) {
            ((IEntityAdditionalSpawnData)ent).writeSpawnData(dat);
        }

        return dat.toByteArray();
    }

    public FMLPacket consumePacket(byte[] data) {
        ByteArrayDataInput dat = ByteStreams.newDataInput(data);
        this.networkId = dat.readInt();
        this.modEntityId = dat.readInt();
        this.entityId = dat.readInt();
        this.rawX = dat.readInt();
        this.rawY = dat.readInt();
        this.rawZ = dat.readInt();
        this.scaledX = (double)this.rawX / 32.0D;
        this.scaledY = (double)this.rawY / 32.0D;
        this.scaledZ = (double)this.rawZ / 32.0D;
        this.scaledYaw = (float)dat.readByte() * 360.0F / 256.0F;
        this.scaledPitch = (float)dat.readByte() * 360.0F / 256.0F;
        this.scaledHeadYaw = (float)dat.readByte() * 360.0F / 256.0F;
        ByteArrayInputStream bis = new ByteArrayInputStream(data, 27, data.length - 27);
        DataInputStream dis = new DataInputStream(bis);

        try {
            this.metadata = DataWatcher.readWatchableObjects(dis);
        } catch (IOException var6) {
        }

        dat.skipBytes(data.length - bis.available() - 27);
        this.throwerId = dat.readInt();
        if (this.throwerId != 0) {
            this.speedScaledX = (double)dat.readInt() / 8000.0D;
            this.speedScaledY = (double)dat.readInt() / 8000.0D;
            this.speedScaledZ = (double)dat.readInt() / 8000.0D;
        }

        this.dataStream = dat;
        return this;
    }

    public void execute(INetworkManager network, FMLNetworkHandler handler, NetHandler netHandler, String userName) {
        NetworkModHandler nmh = handler.findNetworkModHandler(this.networkId);
        ModContainer mc = nmh.getContainer();
        EntityRegistry.EntityRegistration registration = EntityRegistry.instance().lookupModSpawn(mc, this.modEntityId);
        if (registration != null && registration.getEntityClass() != null) {
            FMLCommonHandler.instance().spawnEntityIntoClientWorld(registration, this);
        } else {
            FMLLog.log(Level.WARNING, "Missing mod entity information for %s : %d", mc.getModId(), this.modEntityId);
        }
    }
}
