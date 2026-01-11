package cpw.mods.fml.common.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.InjectedModContainer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.EnumGameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public class FMLNetworkHandler {
    private static final int FML_HASH = Hashing.murmur3_32().hashString("FML").asInt();
    private static final int PROTOCOL_VERSION = 2;
    private static final FMLNetworkHandler INSTANCE = new FMLNetworkHandler();
    static final int LOGIN_RECEIVED = 1;
    static final int CONNECTION_VALID = 2;
    static final int FML_OUT_OF_DATE = -1;
    static final int MISSING_MODS_OR_VERSIONS = -2;
    private Map<NetLoginHandler, Integer> loginStates = Maps.newHashMap();
    private Map<ModContainer, NetworkModHandler> networkModHandlers = Maps.newHashMap();
    private Map<Integer, NetworkModHandler> networkIdLookup = Maps.newHashMap();

    public static void handlePacket250Packet(Packet250CustomPayload packet, INetworkManager network, NetHandler handler) {
        String target = packet.channel;
        if (target.startsWith("MC|")) {
            handler.handleVanilla250Packet(packet);
        }

        if (target.equals("FML")) {
            instance().handleFMLPacket(packet, network, handler);
        } else {
            NetworkRegistry.instance().handleCustomPacket(packet, network, handler);
        }

    }

    public static void onConnectionEstablishedToServer(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
        NetworkRegistry.instance().clientLoggedIn(clientHandler, manager, login);
    }

    private void handleFMLPacket(Packet250CustomPayload packet, INetworkManager network, NetHandler netHandler) {
        FMLPacket pkt = FMLPacket.readPacket(network, packet.data);
        if (pkt != null) {
            String userName = "";
            if (netHandler instanceof NetLoginHandler) {
                userName = ((NetLoginHandler)netHandler).clientUsername;
            } else {
                EntityPlayer pl = netHandler.getPlayer();
                if (pl != null) {
                    userName = pl.getCommandSenderName();
                }
            }

            pkt.execute(network, this, netHandler, userName);
        }
    }

    public static void onConnectionReceivedFromClient(NetLoginHandler netLoginHandler, MinecraftServer server, SocketAddress address, String userName) {
        instance().handleClientConnection(netLoginHandler, server, address, userName);
    }

    private void handleClientConnection(NetLoginHandler netLoginHandler, MinecraftServer server, SocketAddress address, String userName) {
        if (!this.loginStates.containsKey(netLoginHandler)) {
            if (this.handleVanillaLoginKick(netLoginHandler, server, address, userName)) {
                FMLLog.fine("Connection from %s rejected - no FML packet received from client", userName);
                netLoginHandler.completeConnection("You don't have FML installed, you cannot connect to this server");
            } else {
                FMLLog.fine("Connection from %s was closed by vanilla minecraft", userName);
            }
        } else {
            switch((Integer)this.loginStates.get(netLoginHandler)) {
                case -2:
                    netLoginHandler.completeConnection("The server requires mods that are absent or out of date on your client");
                    this.loginStates.remove(netLoginHandler);
                    break;
                case -1:
                    netLoginHandler.completeConnection("Your client is not running a new enough version of FML to connect to this server");
                    this.loginStates.remove(netLoginHandler);
                    break;
                case 0:
                default:
                    netLoginHandler.completeConnection("There was a problem during FML negotiation");
                    this.loginStates.remove(netLoginHandler);
                    break;
                case 1:
                    String modKick = NetworkRegistry.instance().connectionReceived(netLoginHandler, netLoginHandler.myTCPConnection);
                    if (modKick != null) {
                        netLoginHandler.completeConnection(modKick);
                        this.loginStates.remove(netLoginHandler);
                        return;
                    }

                    if (!this.handleVanillaLoginKick(netLoginHandler, server, address, userName)) {
                        this.loginStates.remove(netLoginHandler);
                        return;
                    }

                    NetLoginHandler.func_72531_a(netLoginHandler, false);
                    netLoginHandler.myTCPConnection.addToSendQueue(this.getModListRequestPacket());
                    this.loginStates.put(netLoginHandler, 2);
                    break;
                case 2:
                    netLoginHandler.completeConnection((String)null);
                    this.loginStates.remove(netLoginHandler);
            }

        }
    }

    private boolean handleVanillaLoginKick(NetLoginHandler netLoginHandler, MinecraftServer server, SocketAddress address, String userName) {
        ServerConfigurationManager playerList = server.getConfigurationManager();
        String kickReason = playerList.allowUserToConnect(address, userName);
        if (kickReason != null) {
            netLoginHandler.completeConnection(kickReason);
        }

        return kickReason == null;
    }

    public static void handleLoginPacketOnServer(NetLoginHandler handler, Packet1Login login) {
        if (login.clientEntityId == FML_HASH) {
            if (login.dimension == 2) {
                FMLLog.finest("Received valid FML login packet from %s", handler.myTCPConnection.getSocketAddress());
                instance().loginStates.put(handler, 1);
            } else if (login.dimension != 2) {
                FMLLog.finest("Received incorrect FML (%x) login packet from %s", login.dimension, handler.myTCPConnection.getSocketAddress());
                instance().loginStates.put(handler, -1);
            }
        } else {
            FMLLog.fine("Received invalid login packet (%x, %x) from %s", login.clientEntityId, login.dimension, handler.myTCPConnection.getSocketAddress());
        }

    }

    static void setHandlerState(NetLoginHandler handler, int state) {
        instance().loginStates.put(handler, state);
    }

    public static FMLNetworkHandler instance() {
        return INSTANCE;
    }

    public static Packet1Login getFMLFakeLoginPacket() {
        FMLCommonHandler.instance().getSidedDelegate().setClientCompatibilityLevel((byte)0);
        Packet1Login fake = new Packet1Login();
        fake.clientEntityId = FML_HASH;
        fake.dimension = 2;
        fake.gameType = EnumGameType.NOT_SET;
        fake.terrainType = WorldType.worldTypes[0];
        return fake;
    }

    public Packet250CustomPayload getModListRequestPacket() {
        return PacketDispatcher.getPacket("FML", FMLPacket.makePacket(FMLPacket.Type.MOD_LIST_REQUEST));
    }

    public void registerNetworkMod(NetworkModHandler handler) {
        this.networkModHandlers.put(handler.getContainer(), handler);
        this.networkIdLookup.put(handler.getNetworkId(), handler);
    }

    public boolean registerNetworkMod(ModContainer container, Class<?> networkModClass, ASMDataTable asmData) {
        NetworkModHandler handler = new NetworkModHandler(container, networkModClass, asmData);
        if (handler.isNetworkMod()) {
            this.registerNetworkMod(handler);
        }

        return handler.isNetworkMod();
    }

    public NetworkModHandler findNetworkModHandler(Object mc) {
        if (mc instanceof InjectedModContainer) {
            return (NetworkModHandler)this.networkModHandlers.get(((InjectedModContainer)mc).wrappedContainer);
        } else if (mc instanceof ModContainer) {
            return (NetworkModHandler)this.networkModHandlers.get(mc);
        } else {
            return mc instanceof Integer ? (NetworkModHandler)this.networkIdLookup.get(mc) : (NetworkModHandler)this.networkModHandlers.get(FMLCommonHandler.instance().findContainerFor(mc));
        }
    }

    public Set<ModContainer> getNetworkModList() {
        return this.networkModHandlers.keySet();
    }

    public static void handlePlayerLogin(EntityPlayerMP player, NetServerHandler netHandler, INetworkManager manager) {
        NetworkRegistry.instance().playerLoggedIn(player, netHandler, manager);
        GameRegistry.onPlayerLogin(player);
    }

    public Map<Integer, NetworkModHandler> getNetworkIdMap() {
        return this.networkIdLookup;
    }

    public void bindNetworkId(String key, Integer value) {
        Map<String, ModContainer> mods = Loader.instance().getIndexedModList();
        NetworkModHandler handler = this.findNetworkModHandler(mods.get(key));
        if (handler != null) {
            handler.setNetworkId(value);
            this.networkIdLookup.put(value, handler);
        }

    }

    public static void onClientConnectionToRemoteServer(NetHandler netClientHandler, String server, int port, INetworkManager networkManager) {
        NetworkRegistry.instance().connectionOpened(netClientHandler, server, port, networkManager);
    }

    public static void onClientConnectionToIntegratedServer(NetHandler netClientHandler, MinecraftServer server, INetworkManager networkManager) {
        NetworkRegistry.instance().connectionOpened(netClientHandler, server, networkManager);
    }

    public static void onConnectionClosed(INetworkManager manager, EntityPlayer player) {
        NetworkRegistry.instance().connectionClosed(manager, player);
    }

    public static void openGui(EntityPlayer player, Object mod, int modGuiId, World world, int x, int y, int z) {
        ModContainer mc = FMLCommonHandler.instance().findContainerFor(mod);
        if (mc == null) {
            NetworkModHandler nmh = instance().findNetworkModHandler(mod);
            if (nmh == null) {
                FMLLog.warning("A mod tried to open a gui on the server without being a NetworkMod");
                return;
            }

            mc = nmh.getContainer();
        }

        if (player instanceof EntityPlayerMP) {
            NetworkRegistry.instance().openRemoteGui(mc, (EntityPlayerMP)player, modGuiId, world, x, y, z);
        } else if (FMLCommonHandler.instance().getSide().equals(Side.CLIENT)) {
            NetworkRegistry.instance().openLocalGui(mc, player, modGuiId, world, x, y, z);
        } else {
            FMLLog.fine("Invalid attempt to open a local GUI on a dedicated server. This is likely a bug. GUIID: %s,%d", mc.getModId(), modGuiId);
        }

    }

    public static Packet getEntitySpawningPacket(Entity entity) {
        EntityRegistry.EntityRegistration er = EntityRegistry.instance().lookupModSpawn(entity.getClass(), false);
        if (er == null) {
            return null;
        } else {
            return er.usesVanillaSpawning() ? null : PacketDispatcher.getPacket("FML", FMLPacket.makePacket(FMLPacket.Type.ENTITYSPAWN, er, entity, instance().findNetworkModHandler(er.getContainer())));
        }
    }

    public static void makeEntitySpawnAdjustment(int entityId, EntityPlayerMP player, int serverX, int serverY, int serverZ) {
        Packet250CustomPayload pkt = PacketDispatcher.getPacket("FML", FMLPacket.makePacket(FMLPacket.Type.ENTITYSPAWNADJUSTMENT, entityId, serverX, serverY, serverZ));
        player.playerNetServerHandler.sendPacketToPlayer(pkt);
    }

    public static InetAddress computeLocalHost() throws IOException {
        InetAddress add = null;
        List<InetAddress> addresses = Lists.newArrayList();
        InetAddress localHost = InetAddress.getLocalHost();
        Iterator i$ = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();

        while(i$.hasNext()) {
            NetworkInterface ni = (NetworkInterface)i$.next();
            if (!ni.isLoopback() && ni.isUp()) {
                addresses.addAll(Collections.list(ni.getInetAddresses()));
                if (addresses.contains(localHost)) {
                    add = localHost;
                    break;
                }
            }
        }

        if (add == null && !addresses.isEmpty()) {
            i$ = addresses.iterator();

            while(i$.hasNext()) {
                InetAddress addr = (InetAddress)i$.next();
                if (addr.getAddress().length == 4) {
                    add = addr;
                    break;
                }
            }
        }

        if (add == null) {
            add = localHost;
        }

        return add;
    }

    public static Packet3Chat handleChatMessage(NetHandler handler, Packet3Chat chat) {
        return NetworkRegistry.instance().handleChat(handler, chat);
    }

    public static void handlePacket131Packet(NetHandler handler, Packet131MapData mapData) {
        if (!(handler instanceof NetServerHandler) && mapData.itemID == Item.map.itemID) {
            FMLCommonHandler.instance().handleTinyPacket(handler, mapData);
        } else {
            NetworkRegistry.instance().handleTinyPacket(handler, mapData);
        }

    }

    public static int getCompatibilityLevel() {
        return 2;
    }

    public static boolean vanillaLoginPacketCompatibility() {
        return FMLCommonHandler.instance().getSidedDelegate().getClientCompatibilityLevel() == 0;
    }
}
