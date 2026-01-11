package cpw.mods.fml.common.network;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class NetworkRegistry {
    private static final NetworkRegistry INSTANCE = new NetworkRegistry();
    private Multimap<Player, String> activeChannels = ArrayListMultimap.create();
    private Multimap<String, IPacketHandler> universalPacketHandlers = ArrayListMultimap.create();
    private Multimap<String, IPacketHandler> clientPacketHandlers = ArrayListMultimap.create();
    private Multimap<String, IPacketHandler> serverPacketHandlers = ArrayListMultimap.create();
    private Set<IConnectionHandler> connectionHandlers = Sets.newLinkedHashSet();
    private Map<ModContainer, IGuiHandler> serverGuiHandlers = Maps.newHashMap();
    private Map<ModContainer, IGuiHandler> clientGuiHandlers = Maps.newHashMap();
    private List<IChatListener> chatListeners = Lists.newArrayList();

    public static NetworkRegistry instance() {
        return INSTANCE;
    }

    byte[] getPacketRegistry(Side side) {
        return Joiner.on('\u0000').join(Iterables.concat(Arrays.asList("FML"), this.universalPacketHandlers.keySet(), side.isClient() ? this.clientPacketHandlers.keySet() : this.serverPacketHandlers.keySet())).getBytes(Charsets.UTF_8);
    }

    public boolean isChannelActive(String channel, Player player) {
        return this.activeChannels.containsEntry(player, channel);
    }

    public void registerChannel(IPacketHandler handler, String channelName) {
        if (Strings.isNullOrEmpty(channelName) || channelName != null && channelName.length() > 16) {
            FMLLog.severe("Invalid channel name '%s' : %s", channelName, Strings.isNullOrEmpty(channelName) ? "Channel name is empty" : "Channel name is too long (16 chars is maximum)");
            throw new RuntimeException("Channel name is invalid");
        } else {
            this.universalPacketHandlers.put(channelName, handler);
        }
    }

    public void registerChannel(IPacketHandler handler, String channelName, Side side) {
        if (side == null) {
            this.registerChannel(handler, channelName);
        } else if (Strings.isNullOrEmpty(channelName) || channelName != null && channelName.length() > 16) {
            FMLLog.severe("Invalid channel name '%s' : %s", channelName, Strings.isNullOrEmpty(channelName) ? "Channel name is empty" : "Channel name is too long (16 chars is maximum)");
            throw new RuntimeException("Channel name is invalid");
        } else {
            if (side.isClient()) {
                this.clientPacketHandlers.put(channelName, handler);
            } else {
                this.serverPacketHandlers.put(channelName, handler);
            }

        }
    }

    void activateChannel(Player player, String channel) {
        this.activeChannels.put(player, channel);
    }

    void deactivateChannel(Player player, String channel) {
        this.activeChannels.remove(player, channel);
    }

    public void registerConnectionHandler(IConnectionHandler handler) {
        this.connectionHandlers.add(handler);
    }

    public void registerChatListener(IChatListener listener) {
        this.chatListeners.add(listener);
    }

    /**
     * Called when a player successfully logs in. Reads player data from disk and inserts the player into the world.
     */
    void playerLoggedIn(EntityPlayerMP player, NetServerHandler netHandler, INetworkManager manager) {
        this.generateChannelRegistration(player, netHandler, manager);
        Iterator i$ = this.connectionHandlers.iterator();

        while(i$.hasNext()) {
            IConnectionHandler handler = (IConnectionHandler)i$.next();
            handler.playerLoggedIn((Player)player, netHandler, manager);
        }

    }

    String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
        Iterator i$ = this.connectionHandlers.iterator();

        String kick;
        do {
            if (!i$.hasNext()) {
                return null;
            }

            IConnectionHandler handler = (IConnectionHandler)i$.next();
            kick = handler.connectionReceived(netHandler, manager);
        } while(Strings.isNullOrEmpty(kick));

        return kick;
    }

    void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager networkManager) {
        Iterator i$ = this.connectionHandlers.iterator();

        while(i$.hasNext()) {
            IConnectionHandler handler = (IConnectionHandler)i$.next();
            handler.connectionOpened(netClientHandler, server, port, networkManager);
        }

    }

    void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager networkManager) {
        Iterator i$ = this.connectionHandlers.iterator();

        while(i$.hasNext()) {
            IConnectionHandler handler = (IConnectionHandler)i$.next();
            handler.connectionOpened(netClientHandler, server, networkManager);
        }

    }

    void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
        this.generateChannelRegistration(clientHandler.getPlayer(), clientHandler, manager);
        Iterator i$ = this.connectionHandlers.iterator();

        while(i$.hasNext()) {
            IConnectionHandler handler = (IConnectionHandler)i$.next();
            handler.clientLoggedIn(clientHandler, manager, login);
        }

    }

    void connectionClosed(INetworkManager manager, EntityPlayer player) {
        Iterator i$ = this.connectionHandlers.iterator();

        while(i$.hasNext()) {
            IConnectionHandler handler = (IConnectionHandler)i$.next();
            handler.connectionClosed(manager);
        }

        this.activeChannels.removeAll(player);
    }

    void generateChannelRegistration(EntityPlayer player, NetHandler netHandler, INetworkManager manager) {
        Packet250CustomPayload pkt = new Packet250CustomPayload();
        pkt.channel = "REGISTER";
        pkt.data = this.getPacketRegistry(player instanceof EntityPlayerMP ? Side.SERVER : Side.CLIENT);
        pkt.length = pkt.data.length;
        manager.addToSendQueue(pkt);
    }

    void handleCustomPacket(Packet250CustomPayload packet, INetworkManager network, NetHandler handler) {
        if ("REGISTER".equals(packet.channel)) {
            this.handleRegistrationPacket(packet, (Player)handler.getPlayer());
        } else if ("UNREGISTER".equals(packet.channel)) {
            this.handleUnregistrationPacket(packet, (Player)handler.getPlayer());
        } else {
            this.handlePacket(packet, network, (Player)handler.getPlayer());
        }

    }

    private void handlePacket(Packet250CustomPayload packet, INetworkManager network, Player player) {
        String channel = packet.channel;
        Iterator i$ = Iterables.concat(this.universalPacketHandlers.get(channel), player instanceof EntityPlayerMP ? this.serverPacketHandlers.get(channel) : this.clientPacketHandlers.get(channel)).iterator();

        while(i$.hasNext()) {
            IPacketHandler handler = (IPacketHandler)i$.next();
            handler.onPacketData(network, packet, player);
        }

    }

    private void handleRegistrationPacket(Packet250CustomPayload packet, Player player) {
        List<String> channels = this.extractChannelList(packet);
        Iterator i$ = channels.iterator();

        while(i$.hasNext()) {
            String channel = (String)i$.next();
            this.activateChannel(player, channel);
        }

    }

    private void handleUnregistrationPacket(Packet250CustomPayload packet, Player player) {
        List<String> channels = this.extractChannelList(packet);
        Iterator i$ = channels.iterator();

        while(i$.hasNext()) {
            String channel = (String)i$.next();
            this.deactivateChannel(player, channel);
        }

    }

    private List<String> extractChannelList(Packet250CustomPayload packet) {
        String request = new String(packet.data, Charsets.UTF_8);
        List<String> channels = Lists.newArrayList(Splitter.on('\u0000').split(request));
        return channels;
    }

    public void registerGuiHandler(Object mod, IGuiHandler handler) {
        ModContainer mc = FMLCommonHandler.instance().findContainerFor(mod);
        if (mc == null) {
            mc = Loader.instance().activeModContainer();
            FMLLog.log(Level.WARNING, "Mod %s attempted to register a gui network handler during a construction phase", mc.getModId());
        }

        NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(mc);
        if (nmh == null) {
            FMLLog.log(Level.FINE, "The mod %s needs to be a @NetworkMod to register a Networked Gui Handler", mc.getModId());
        } else {
            this.serverGuiHandlers.put(mc, handler);
        }

        this.clientGuiHandlers.put(mc, handler);
    }

    void openRemoteGui(ModContainer mc, EntityPlayerMP player, int modGuiId, World world, int x, int y, int z) {
        IGuiHandler handler = (IGuiHandler)this.serverGuiHandlers.get(mc);
        NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(mc);
        if (handler != null && nmh != null) {
            Container container = (Container)handler.getServerGuiElement(modGuiId, player, world, x, y, z);
            if (container != null) {
                player.incrementWindowID();
                player.closeContainer();
                int windowId = player.currentWindowId;
                Packet250CustomPayload pkt = new Packet250CustomPayload();
                pkt.channel = "FML";
                pkt.data = FMLPacket.makePacket(FMLPacket.Type.GUIOPEN, windowId, nmh.getNetworkId(), modGuiId, x, y, z);
                pkt.length = pkt.data.length;
                player.playerNetServerHandler.sendPacketToPlayer(pkt);
                player.openContainer = container;
                player.openContainer.windowId = windowId;
                player.openContainer.addCraftingToCrafters(player);
            }
        }

    }

    void openLocalGui(ModContainer mc, EntityPlayer player, int modGuiId, World world, int x, int y, int z) {
        IGuiHandler handler = (IGuiHandler)this.clientGuiHandlers.get(mc);
        FMLCommonHandler.instance().showGuiScreen(handler.getClientGuiElement(modGuiId, player, world, x, y, z));
    }

    public Packet3Chat handleChat(NetHandler handler, Packet3Chat chat) {
        Side s = Side.CLIENT;
        if (handler instanceof NetServerHandler) {
            s = Side.SERVER;
        }

        IChatListener listener;
        for(Iterator i$ = this.chatListeners.iterator(); i$.hasNext(); chat = s.isClient() ? listener.clientChat(handler, chat) : listener.serverChat(handler, chat)) {
            listener = (IChatListener)i$.next();
        }

        return chat;
    }

    public void handleTinyPacket(NetHandler handler, Packet131MapData mapData) {
        NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(Integer.valueOf(mapData.itemID));
        if (nmh == null) {
            FMLLog.info("Received a tiny packet for network id %d that is not recognised here", mapData.itemID);
        } else {
            if (nmh.hasTinyPacketHandler()) {
                nmh.getTinyPacketHandler().handle(handler, mapData);
            } else {
                FMLLog.info("Received a tiny packet for a network mod that does not accept tiny packets %s", nmh.getContainer().getModId());
            }

        }
    }
}
