package cpw.mods.fml.common.network;

import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;

public interface IPacketHandler {
    void onPacketData(INetworkManager iNetworkManager, Packet250CustomPayload packet250CustomPayload, Player player);
}
