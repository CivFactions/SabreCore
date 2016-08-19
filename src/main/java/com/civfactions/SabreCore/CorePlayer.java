package com.civfactions.SabreCore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.civfactions.SabreApi.SabrePlayer;
import com.civfactions.SabreApi.SabreApi;
import com.civfactions.SabreApi.data.ConfigurationObject;
import com.civfactions.SabreApi.util.Guard;
import com.civfactions.SabreApi.util.Permission;
import com.civfactions.SabreCore.data.ClassStorage;

/**
 * Represents a player that has joined the server and may or may not be online
 * @author GFQ
 */
public class CorePlayer implements SabrePlayer {
	
	private final SabreApi sabreApi;
	private final PlayerManager pm;
	
	// Unique ID of the player
	private final UUID uid;
	
	// The player's display name
	private String name;
	
	// The Bukkit player instance
	private Player bukkitPlayer;
	
	// Login and play time data
	private Date firstLogin;
	private Date lastLogin;
	private long playTime;
	private boolean banned;
	private String banMessage;
	
	// Pending offline messages for the player
	private List<String> offlineMessages;
	
	// Whether the player is vanished or not
	private boolean vanished;
	
	// The player's bed location
	private Location bedLocation;
	
	// Other players that are being ignored by this player
	private final Set<SabrePlayer> ignoredPlayers;
	
	
	private final ClassStorage dataStore;
	
	
	/**
	 * Creates a new SabrePlayer instance
	 * @param uid The player's ID
	 * @param name The player's display name
	 */
	public CorePlayer(SabreApi sabreApi, PlayerManager playerManager, ClassStorage dataStore, UUID uid, String name) {
		Guard.ArgumentNotNull(playerManager, "playerManager");
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNullOrEmpty(name, "name");
		
		this.sabreApi = sabreApi;
		this.pm = playerManager;
		this.uid = uid;
		this.name = name;
		this.firstLogin = sabreApi.getTimeNow();
		this.lastLogin = sabreApi.getTimeNow();
		this.playTime = 0;
		this.banned = false;
		this.banMessage = "";
		this.offlineMessages = new ArrayList<String>();
		this.vanished = false;
		this.ignoredPlayers = new HashSet<SabrePlayer>();
		
		this.dataStore = dataStore;
	}
	
	/**
	 * Gets the Sabre API
	 * @return The Sabre API
	 */
	public SabreApi getSabreApi() {
		return sabreApi;
	}
	
	
	/**
	 * Gets the player ID
	 * @return The player ID
	 */
	@Override
	public UUID getUniqueId() {
		return uid;
	}
	
	
	/**
	 * Gets the player name
	 * @return The player name
	 */
	@Override
	public String getName() {
		return this.name;
	}
	
	
	/**
	 * Sets the player name
	 * @param name The player name
	 */
	public void setName(String name) {
		Guard.ArgumentNotNullOrEmpty(name, "name");
		
		this.name = name;
		
		if (bukkitPlayer != null) {
			bukkitPlayer.setDisplayName(name);
			bukkitPlayer.setCustomName(name);
			bukkitPlayer.setPlayerListName(name);
		}
	}
	
	
	/**
	 * Gets the Bukkit player instance
	 * @return The Bukkit player instance
	 */
	@Override
	public Player getBukkitPlayer() {
		return this.bukkitPlayer;
	}
	
	
	/**
	 * Sets the player instance
	 * @param bukkitPlayer The player instance
	 */
	@Override
	public void setBukkitPlayer(Player bukkitPlayer) {
		this.bukkitPlayer = bukkitPlayer;
	}
	
	
	/**
	 * Gets whether the player is online
	 * @return true if the player is online
	 */
	@Override
	public boolean isOnline() {
		return bukkitPlayer != null && bukkitPlayer.isOnline();
	}
	
	
	/**
	 * Gets the first login time
	 * @return The first login time
	 */
	@Override
	public Date getFirstLogin() {
		return this.firstLogin;
	}
	
	
	/**
	 * Sets the first login time
	 * @param firstLogin The first login time
	 */
	public void setFirstLogin(Date firstLogin) {
		Guard.ArgumentNotNull(firstLogin, "firstLogin");
		
		this.firstLogin = firstLogin;
	}
	
	
	/**
	 * Gets the last login time
	 * @return The first login time
	 */
	@Override
	public Date getLastLogin() {
		return this.lastLogin;
	}
	
	
	/**
	 * Sets the last login time
	 * @param lastLogin The first login time
	 */
	public void setLastLogin(Date lastLogin) {
		Guard.ArgumentNotNull(lastLogin, "lastLogin");
		
		this.lastLogin = lastLogin;
	}
	
	
	/**
	 * Gets the number of days since last login
	 * @return The number of days since last login
	 */
	@Override
	public int getDaysSinceLastLogin() {
		Date now = sabreApi.getTimeNow();
		long timeDiff = now.getTime() - lastLogin.getTime();
		long diffDays = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
		return (int)diffDays;
	}
	
	
	/**
	 * Gets the total play time
	 * @return The total play time
	 */
	@Override
	public long getPlaytime() {
		return this.playTime;
	}
	
	
	/**
	 * Sets the total play time
	 * @param lastLogin The total play time
	 */
	public void setPlaytime(long playTime) {
		this.playTime = playTime;
	}
	
	
	/**
	 * Adds to the total play time
	 * @param lastLogin The total play time
	 */
	public void addPlayTime(long playTime) {
		this.playTime += playTime;
	}
	
	
	/**
	 * Gets the ban status
	 * @param banned Whether the player is banned
	 */
	@Override
	public boolean getBanned() {
		return this.banned;
	}

	
	/**
	 * Sets the ban status
	 * @return banned true if the player is banned
	 */
	public void setBanned(boolean banned) {
		this.banned = banned;
	}
	
	
	/**
	 * Gets the ban message
	 * @param The ban message
	 */
	@Override
	public String getBanMessage() {
		return this.banMessage;
	}
	
	
	/**
	 * Sets the ban message
	 * @return banMessage the ban message
	 */
	public void setBanMessage(String banMessage) {
		Guard.ArgumentNotNull(banMessage, "banMessage");
		
		this.banMessage = banMessage;
	}
	
	/**
	 * Sends a message to an online player
	 * @param str
	 * @param args
	 */
	@Override
	public void msg(String str, Object... args) {
		if (str == null || str == "") {
			return; // Silently ignore null or empty strings
		}
		
		String msg = sabreApi.formatText(str, args);
		if (this.isOnline()) {
			this.getBukkitPlayer().sendMessage(msg);
		}
	}
	
	
	/**
	 * Gets the distance from another player
	 * @param p The player to check
	 * @return The distance the players are away from each other
	 */
	@Override
	public int getDistanceFrom(SabrePlayer other) {
		Guard.ArgumentNotNull(other, "other");
		
		if (!isOnline() || !other.isOnline()) {
			return -1;
		}
		
		Location myLocation = bukkitPlayer.getLocation();
		Location otherLocation = other.getBukkitPlayer().getLocation();
		int dx = otherLocation.getBlockX() - myLocation.getBlockX();
		int dz = otherLocation.getBlockZ() - myLocation.getBlockZ();
		
		return (int)Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
	}
	
	
	/**
	 * Adds an offline message for the player
	 * @param message The message to add
	 */
	public void addOfflineMessage(String message) {
		Guard.ArgumentNotNullOrEmpty(message, "message");
		
		this.offlineMessages.add(message);
	}
	
	
	/**
	 * Gets the admin vanished status
	 * @return true if the player is vanished
	 */
	@Override
	public boolean getVanished() {
		return this.vanished;
	}

	
	/**
	 * Sets the vanished status
	 * @param vanished The new vanished state
	 */
	@Override
	public void setVanished(boolean vanished) {
		this.vanished = vanished;
	}
	
	
	/**
	 * Gets the player bed location
	 * @return the bed location
	 */
	@Override
	public Location getBedLocation() {
		return this.bedLocation;
	}

	
	/**
	 * Gets the bed location
	 * @param bedLocation The bed location
	 */
	public void setBedLocation(Location bedLocation) {
		this.bedLocation = bedLocation;
	}
	
	
	/**
	 * Sets the ignored state of a player
	 * @param sp The player to set
	 * @param ignored The ignored status
	 */
	@Override
	public void setIgnored(SabrePlayer sp, boolean ignored) {
		Guard.ArgumentNotNull(sp, "sp");
		
		if (ignored && !ignoredPlayers.contains(sp)) {
			ignoredPlayers.add(sp);
		} else {
			ignoredPlayers.remove(sp);
		}
	}
	
	
	/**
	 * Gets whether a player is ignored
	 * @return Whether the player is ignored
	 */
	@Override
	public boolean isIgnoring(SabrePlayer sp) {
		Guard.ArgumentNotNull(sp, "sp");
		
		return ignoredPlayers.contains(sp);
	}
	
	
	/**
	 * Gets whether the player has admin permissions
	 */
	@Override
	public boolean isAdmin() {
		return isOnline() && bukkitPlayer.hasPermission(Permission.ADMIN_NODE);
	}
	
	/**
	 * Teleports the player to a location
	 * @param l The location
	 * @return True if successful
	 */
	@Override
	public boolean teleport(Location location) {
		Guard.ArgumentNotNull(location, "location");
		
		if (isOnline()) {
			return bukkitPlayer.teleport(location);
		}
		return false;
	}
	
	@Override
	public String getConfigurationKey() {
		return "players";
	}
	
	@Override
	public void loadConfiguration(ConfigurationObject config) {
		
	}
	
	@Override
	public void saveConfiguration(ConfigurationObject config) {
		
	}

	@Override
	public <T> T getDataValue(String key) {
		return dataStore.getDataValue(key);
	}

	@Override
	public <T> void setDataValue(String key, T value) {
		dataStore.setDataValue(key, value);
	}
}
