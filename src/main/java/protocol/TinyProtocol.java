package protocol;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import io.netty.channel.*;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import nl.xanderwander.customentityapi.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import protocol.Reflection.*;

/**
 * Modified by fren_gor to support 1.17+ servers
 * Modified by XanderWander to be even smaller
 *
 * @author Kristian
 */
public abstract class TinyProtocol {
	private static final AtomicInteger ID = new AtomicInteger(0);
	private static final MethodInvoker getPlayerHandle = Reflection.getMethod("{obc}.entity.CraftPlayer", "getHandle");
	private static final Class<?> playerConnectionClass = Reflection.getUntypedClass("{nms.server.network}.PlayerConnection");
	private static final Class<?> networkManagerClass = Reflection.getUntypedClass("{nms.network}.NetworkManager");
	private static final FieldAccessor<?> getConnection = Reflection.getField("{nms.server.level}.EntityPlayer", null, playerConnectionClass);
	private static final FieldAccessor<?> getManager = Reflection.getField(playerConnectionClass, null, networkManagerClass);
	private static final FieldAccessor<Channel> getChannel = Reflection.getField(networkManagerClass, Channel.class, 0);
	private static final Class<?> minecraftServerClass = Reflection.getUntypedClass("{nms.server}.MinecraftServer");
	private static final Class<?> serverConnectionClass = Reflection.getUntypedClass("{nms.server.network}.ServerConnection");
	private static final FieldAccessor<?> getMinecraftServer = Reflection.getField("{obc}.CraftServer", minecraftServerClass, 0);
	private static final FieldAccessor<?> getServerConnection = Reflection.getField(minecraftServerClass, serverConnectionClass, 0);
	private static final FieldAccessor<List> getChannelFutures = Reflection.getField(serverConnectionClass, List.class, 0);
	private static final FieldAccessor<List> getNetworkMarkers = Reflection.getField(serverConnectionClass, List.class, 1);
	private static final Class<?> PACKET_LOGIN_IN_START = Reflection.getMinecraftClass("PacketLoginInStart", "network.protocol.login");
	private static final FieldAccessor<String> getPlayerName = Reflection.getField(PACKET_LOGIN_IN_START, String.class, 0);
	private final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
	private Listener listener;
	private final Set<Channel> uninjectedChannels = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
	private List<Object> networkManagers;
	private final List<Channel> serverChannels = new ArrayList<>();
	private ChannelInboundHandlerAdapter serverChannelHandler;
	private ChannelInitializer<Channel> beginInitProtocol;
	private ChannelInitializer<Channel> endInitProtocol;
	private final String handlerName;
	protected volatile boolean closed = false, injected = false;
	protected final Plugin plugin;
	
	public TinyProtocol(final Plugin plugin) {
		this.plugin = plugin;
		this.handlerName = getHandlerName();
		registerBukkitEvents();
		try {
			registerChannelHandler();
			registerPlayers(plugin);
			injected = true;
		} catch (IllegalArgumentException ex) {
			plugin.getLogger().info("[protocol.protocol.TinyProtocol] Delaying server channel injection due to late bind.");
			new BukkitRunnable() {
				@Override
				public void run() {
					registerChannelHandler();
					registerPlayers(plugin);
					injected = true;
					plugin.getLogger().info("[protocol.protocol.TinyProtocol] Late bind injection successful.");
				}
			}.runTask(plugin);
		}
	}
	
	private void createServerChannelHandler() {
		endInitProtocol = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				try {
					synchronized (networkManagers) {
						if (!closed) {
							channel.eventLoop().submit(() -> injectChannelInternal(channel));
						}
					}
				} catch (Exception e) {
					plugin.getLogger().log(Level.SEVERE, "Cannot inject incoming channel " + channel, e);
				}
			}
		};
		beginInitProtocol = new ChannelInitializer<>() {
			@Override
			protected void initChannel(Channel channel) {
				channel.pipeline().addLast(endInitProtocol);
			}
		};
		serverChannelHandler = new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) {
				Channel channel = (Channel) msg;
				channel.pipeline().addFirst(beginInitProtocol);
				ctx.fireChannelRead(msg);
			}
		};
	}

	private void registerBukkitEvents() {
		listener = new Listener() {
			@EventHandler(priority = EventPriority.LOWEST)
			public void onPlayerLogin(PlayerLoginEvent e) {
				if (closed)
					return;
				Channel channel = getChannel(e.getPlayer());
				if (!uninjectedChannels.contains(channel)) {
					injectPlayer(e.getPlayer());
				}
			}
			@EventHandler
			public void onPluginDisable(PluginDisableEvent e) {
				if (e.getPlugin().equals(plugin)) {
					close();
				}
			}
		};
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
	}
	
	@SuppressWarnings("unchecked")
	private void registerChannelHandler() {
		FieldAccessor<?> t = Reflection.getField("{obc}.CraftServer", minecraftServerClass, 0);
		Object mcServer = t.get(Bukkit.getServer());
		Object serverConnection = getServerConnection.get(mcServer);
		networkManagers = getNetworkMarkers.get(serverConnection);
		List<ChannelFuture> futures = getChannelFutures.get(serverConnection);
		createServerChannelHandler();
		synchronized (futures) {
			for (ChannelFuture item : futures) {
				Channel serverChannel = item.channel();
				serverChannels.add(serverChannel);
				serverChannel.pipeline().addFirst(serverChannelHandler);
			}
		}
	}
	
	private void unregisterChannelHandler() {
		if (serverChannelHandler == null)
			return;
		for (Channel serverChannel : serverChannels) {
			final ChannelPipeline pipeline = serverChannel.pipeline();
			serverChannel.eventLoop().execute(() -> {
				try {
					pipeline.remove(serverChannelHandler);
				} catch (NoSuchElementException ignored) {}
			});
		}
	}
	
	private void registerPlayers(Plugin plugin) {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			injectPlayer(player);
		}
	}

	public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
		return packet;
	}

	public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
		return packet;
	}
	
	public void sendPacket(Player player, Object packet) {
		sendPacket(getChannel(player), packet);
	}

	public void sendPacket(Channel channel, Object packet) {
		channel.pipeline().writeAndFlush(packet);
	}

	public void receivePacket(Player player, Object packet) {
		receivePacket(getChannel(player), packet);
	}

	public void receivePacket(Channel channel, Object packet) {
		channel.pipeline().context("encoder").fireChannelRead(packet);
	}
	
	protected String getHandlerName() {
		return "tiny-" + plugin.getName() + "-" + ID.incrementAndGet();
	}

	public void injectPlayer(Player player) {
		injectChannelInternal(getChannel(player)).player = player;
	}

	private PacketInterceptor injectChannelInternal(Channel channel) {
		try {
			PacketInterceptor interceptor = (PacketInterceptor) channel.pipeline().get(handlerName);
			if (interceptor == null) {
				interceptor = new PacketInterceptor();
				channel.pipeline().addBefore("packet_handler", handlerName, interceptor);
				uninjectedChannels.remove(channel);
			}
			return interceptor;
		} catch (IllegalArgumentException e) {
			return (PacketInterceptor) channel.pipeline().get(handlerName);
		}
	}
	
	public Channel getChannel(Player player) {
		Channel channel = channelLookup.get(player.getName());
		if (channel == null) {
			Object connection = getConnection.get(getPlayerHandle.invoke(player));
			Object manager = getManager.get(connection);
			channelLookup.put(player.getName(), channel = getChannel.get(manager));
		}
		return channel;
	}
	
	public void uninjectPlayer(Player player) {
		uninjectChannel(getChannel(player));
	}

	public void uninjectChannel(final Channel channel) {
		if (!closed) {
			uninjectedChannels.add(channel);
		}
		channel.eventLoop().execute(() -> channel.pipeline().remove(handlerName));
	}

	public final void close() {
		if (!closed) {
			closed = true;
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				uninjectPlayer(player);
			}
			HandlerList.unregisterAll(listener);
			unregisterChannelHandler();
		}
	}
	
	private final class PacketInterceptor extends ChannelDuplexHandler {
		public volatile Player player;
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			final Channel channel = ctx.channel();
			handleLoginStart(channel, msg);
			try {
				msg = onPacketInAsync(player, channel, msg);
			} catch (Exception e) {
				plugin.getLogger().log(Level.SEVERE, "Error in onPacketInAsync().", e);
			}
			if (msg != null) {
				super.channelRead(ctx, msg);
			}
		}
		
		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			try {
				msg = onPacketOutAsync(player, ctx.channel(), msg);
			} catch (Exception e) {
				plugin.getLogger().log(Level.SEVERE, "Error in onPacketOutAsync().", e);
			}
			if (msg != null) {
				super.write(ctx, msg, promise);
			}
		}
		
		private void handleLoginStart(Channel channel, Object packet) {
			if (PACKET_LOGIN_IN_START.isInstance(packet)) {
				String name = getPlayerName.get(packet);
				channelLookup.put(name, channel);
			}
		}
	}
}


