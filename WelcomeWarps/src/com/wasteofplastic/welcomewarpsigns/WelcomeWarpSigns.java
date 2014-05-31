package com.wasteofplastic.welcomewarpsigns;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author ben
 * Main WelcomeWarpSigns class - provides an island minigame in a sea of acid
 */
public class WelcomeWarpSigns extends JavaPlugin {
    // This plugin
    private static WelcomeWarpSigns plugin;
    // Where warps are stored
    public YamlConfiguration welcomeWarps;
    // Map of all warps stored as warp owner's UUID as string, warp sign Location as String
    private HashMap<String, String> warpList = new HashMap<String, String>();
    // Warp names are owner's UUID as string: owner's name when they made the warp
    private HashMap<String, String> warpNames = new HashMap<String, String>();
    // File locations
    String pluginMainDir = getDataFolder().toString();
    /**
     * @return WelcomeWarpSigns object instance
     */
    public static WelcomeWarpSigns getPlugin() {
	return plugin;
    }


    /**
     * Converts a serialized location to a Location
     * @param s - serialized location in format "world:x:y:z"
     * @return Location
     */
    static public Location getLocationString(final String s) {
	if (s == null || s.trim() == "") {
	    return null;
	}
	final String[] parts = s.split(":");
	if (parts.length == 4) {
	    final World w = Bukkit.getServer().getWorld(parts[0]);
	    final int x = Integer.parseInt(parts[1]);
	    final int y = Integer.parseInt(parts[2]);
	    final int z = Integer.parseInt(parts[3]);
	    return new Location(w, x, y, z);
	}
	return null;
    }

    /**
     * Converts a location to a simple string representation
     * 
     * @param l
     * @return
     */
    static public String getStringLocation(final Location l) {
	if (l == null) {
	    return "";
	}
	return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    /**
     * Checks if this location is safe for a player to teleport to. Used by
     * warps and boat exits Unsafe is any liquid or air and also if there's no
     * space
     * 
     * @param l
     *            - Location to be checked
     * @return true if safe, otherwise false
     */
    public static boolean isSafeLocation(final Location l) {
	if (l == null) {
	    return false;
	}
	final Material ground = l.getBlock().getRelative(BlockFace.DOWN).getType();
	// Check the ground
	switch (ground) {
	case AIR:
	case CACTUS:
	case LAVA:
	case STATIONARY_LAVA:
	case SIGN:
	case WALL_SIGN:
	case SIGN_POST:
	case BOAT:
	case STONE_PLATE:
	case WOOD_PLATE:
	case GOLD_PLATE:
	case IRON_PLATE:
	    return false;
	default:
	    break;
	}
	final Block space1 = l.getBlock();
	final Block space2 = l.getBlock().getRelative(BlockFace.UP);
	if (space1.isLiquid() || space2.isLiquid()) {
	    return false;
	}
	if (space1.getType().isSolid()) {
	    // Do a few other checks
	    if (!(space1.getType().equals(Material.SIGN_POST)) && !(space1.getType().equals(Material.WALL_SIGN))) {
		return false;
	    }
	}
	if (space2.getType().isSolid()) {
	    // Do a few other checks
	    if (!(space2.getType().equals(Material.SIGN_POST)) && !(space2.getType().equals(Material.WALL_SIGN))) {
		return false;
	    }
	}
	// Safe
	return true;
    }


    /**
     * Saves a YAML file
     * 
     * @param yamlFile
     * @param fileLocation
     */
    public static void saveYamlFile(YamlConfiguration yamlFile, String fileLocation) {
	File dataFolder = plugin.getDataFolder();
	File file = new File(dataFolder, fileLocation);

	try {
	    yamlFile.save(file);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Loads a YAML file
     * 
     * @param file
     * @return
     */
    public static YamlConfiguration loadYamlFile(String file) {
	File dataFolder = plugin.getDataFolder();
	File yamlFile = new File(dataFolder, file);

	YamlConfiguration config = null;
	if (yamlFile.exists()) {
	    try {
		config = new YamlConfiguration();
		config.load(yamlFile);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    // Create the missing file
	    config = new YamlConfiguration();
	    getPlugin().getLogger().info("No " + file + " found. Creating it...");
	}
	return config;
    }

    /**
     * Creates the warp list if it does not exist
     */
    public void loadWarpList() {
	getLogger().info("Loading warps...");
	// warpList.clear();
	welcomeWarps = loadYamlFile("warps.yml");
	if (welcomeWarps.getConfigurationSection("warps") == null || welcomeWarps.getConfigurationSection("names") == null) {
	    welcomeWarps.createSection("warps");
	    welcomeWarps.createSection("names");
	}
	// Load the locations
	HashMap<String,Object> temp = (HashMap<String, Object>) welcomeWarps.getConfigurationSection("warps").getValues(true);
	for (String s : temp.keySet()) {
	    warpList.put(s, temp.get(s).toString());
	}
	HashMap<String,Object> temp2 = (HashMap<String, Object>) welcomeWarps.getConfigurationSection("names").getValues(true);
	for (String s : temp2.keySet()) {
	    // Convert to lower case just in case someone fiddled with the YML file
	    warpNames.put(s,temp2.get(s).toString().toLowerCase());
	}

    }

    /**
     * Saves the warp lists to file
     */
    public void saveWarpList() {
	getLogger().info("Saving warps...");
	// Warp locations UUID as String:Location as String
	final HashMap<String,Object> warps = new HashMap<String,Object>();
	for (String s : warpList.keySet()) {
	    // We store the UUID
	    warps.put(s,warpList.get(s));
	}
	welcomeWarps.set("warps", warps);

	// Warp names UUID as string: name
	final HashMap<String,String> names = new HashMap<String,String>();
	for (String s : warpNames.keySet()) {
	    // We store the UUID and the name
	    names.put(s, warpNames.get(s).toLowerCase());
	}
	welcomeWarps.set("names", names);

	saveYamlFile(welcomeWarps, "warps.yml");
    }

    /**
     * Stores warps in the warp array
     * 
     * @param player
     * @param loc
     */
    public boolean addWarp(Player player, Location loc) {
	final String locS = getStringLocation(loc);
	final String uuid = player.getUniqueId().toString();
	// Do not allow warps to be in the same location
	if (warpList.containsValue(locS)) {
	    return false;
	}
	// Remove the old warp if it existed
	warpList.remove(uuid);
	warpNames.remove(uuid);
	warpList.put(uuid, locS);
	warpNames.put(uuid, player.getName().toLowerCase());
	saveWarpList();
	return true;
    }

    /**
     * Removes a warp when the welcome sign is destroyed. Called by
     * WarpSigns.java.
     * 
     * @param uuid
     */
    public void removeWarp(Player player) {
	warpList.remove(player.getUniqueId().toString());
	warpNames.remove(player.getUniqueId().toString());
	saveWarpList();
    }

    /**
     * Removes a warp at a location. Called by WarpSigns.java.
     * 
     * @param loc
     */
    public void removeWarp(Location loc) {
	final String locS = getStringLocation(loc);
	getLogger().info("Asked to remove warp at " + locS);
	if (warpList.containsValue(locS)) {
	    String uuid = getKeyByValue(warpList,locS);
	    warpList.remove(uuid);
	    warpNames.remove(uuid);
	    try {
		// Try to tell the player if they are online that their welcome sign was removed
		plugin.getServer().getPlayer(UUID.fromString(uuid)).sendMessage(ChatColor.RED + "Your welcome sign was removed!");
	    } catch (Exception e) {}	
	    saveWarpList();
	}
    }

    /**
     * Removes the warp sign based on the name of the owner. Called by wadmin.
     * Also breaks the sign if it exists
     * @param name
     * @return true if the sign was removed
     */
    public boolean removeWarp(String name) {
	if (!warpNames.containsValue(name.toLowerCase())) {
	    return false;
	}
	String uuid = getKeyByValue(warpNames,name);
	Location signLoc = getLocationString(warpList.get(uuid));
	if (signLoc != null) {
	    // Remove the sign too
	    try {
		if (signLoc.getBlock().getType().equals(Material.SIGN_POST)) {
		    signLoc.getBlock().breakNaturally();
		} 
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	warpList.remove(uuid);
	warpNames.remove(uuid);
	return true;
    }

    /**
     * Returns true if the location supplied is a warp location
     * 
     * @param loc
     * @return true if this location has a warp sign, false if not
     */
    public boolean checkWarp(Location loc) {
	final String locS = getStringLocation(loc);
	if (warpList.containsValue(locS)) {
	    return true;
	}
	return false;
    }

    /*
     * Checks the name of a warp
     * @param name
     * @return true if the warp is know otherwise false
     */
    public boolean checkWarp(String name) {
	if (warpNames.containsValue(name.toLowerCase())) {
	    return true;
	}
	return false;
    }

    /**
     * Lists all the known warps
     * 
     * @return String set of warps
     */
    public Collection<String> listWarps() {
	return warpNames.values();
    }

    /**
     * Provides the location of the warp for player
     * 
     * @param player
     *            - the warp requested
     * @return Location of warp
     */
    public Location getWarp(Player player) {
	if (warpList.containsKey(player.getUniqueId().toString())) {
	    return getLocationString((String) warpList.get(player.getUniqueId().toString()));
	} else {
	    return null;
	}
    }

    /**
     * Gets a warp based on name
     * @param name
     * @return
     */
    public Location getWarp(String name) {
	if (warpNames.containsValue(name)) {
	    return getLocationString(warpList.get(getKeyByValue(warpNames,name)));
	} else {
	    return null;
	}
    }

    /**
     * Loads the various settings from the config.yml file into the plugin
     */
    public void loadPluginConfig() {
	try {
	    getConfig();
	} catch (final Exception e) {
	    e.printStackTrace();
	}
	Settings.worldName = getConfig().getStringList("signworlds");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
	try {
	    saveWarpList();
	} catch (final Exception e) {
	    plugin.getLogger().severe("Something went wrong saving the warp list!");
	    e.printStackTrace();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
	// instance of this plugin
	plugin = this;
	saveDefaultConfig();
	// Metrics
	try {
	    final Metrics metrics = new Metrics(this);
	    metrics.start();
	} catch (final IOException localIOException) {
	}
	loadPluginConfig();
	// Set up commands for this plugin
	getCommand("wwarp").setExecutor(new WWarpCmd(this));
	getCommand("wwadmin").setExecutor(new WAdmin(this));
	// Register events that this plugin uses
	registerEvents();
	// Load warps
	loadWarpList();
    }

    /**
     * Registers events
     */
    public void registerEvents() {
	final PluginManager manager = getServer().getPluginManager();
	// Enables warp signs in WelcomeWarpSigns
	manager.registerEvents(new WarpSignsListener(this), this);
    }

    /**
     * One-to-one relationship, you can return the first matched key
     * @param map
     * @param value
     * @return
     */
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	for (Entry<T, E> entry : map.entrySet()) {
	    if (value.equals(entry.getValue())) {
		return entry.getKey();
	    }
	}
	return null;
    }


    /**
     * Provides a list of names and locations
     * @return Hasmap of names and locations as strings
     */
    public HashMap<String, String> listWarpsAndLocs() {
	HashMap<String, String> list = new HashMap<String, String>();
	// Link the tables together so they are name : location
	for (String uuid : warpList.keySet()) {
	    list.put(warpNames.get(uuid), warpList.get(uuid));
	}
	return list;
    }


}