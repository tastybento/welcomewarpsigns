/*******************************************************************************
 * This file is part of ASkyBlock.
 *
 *     ASkyBlock is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ASkyBlock is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ASkyBlock.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.wasteofplastic.wwarps;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SimpleAttachableMaterialData;
import org.bukkit.material.TrapDoor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.wasteofplastic.wwarps.commands.AdminCmd;
import com.wasteofplastic.wwarps.commands.WarpCmd;
import com.wasteofplastic.wwarps.listeners.JoinLeaveEvents;
import com.wasteofplastic.wwarps.panels.WarpPanel;

/**
 * @author tastybento
 *         Main ASkyBlock class - provides an island minigame in a sea of acid
 */
public class WWarps extends JavaPlugin {
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
		// TODO: improve the safe location finding.
		//Bukkit.getLogger().info("DEBUG: " + l.toString());
		final Block ground = l.getBlock().getRelative(BlockFace.DOWN);
		final Block space1 = l.getBlock();
		final Block space2 = l.getBlock().getRelative(BlockFace.UP);
		//Bukkit.getLogger().info("DEBUG: ground = " + ground.getType());
		//Bukkit.getLogger().info("DEBUG: space 1 = " + space1.getType());
		//Bukkit.getLogger().info("DEBUG: space 2 = " + space2.getType());
		// Portals are not "safe"
		if (space1.getType() == Material.PORTAL || ground.getType() == Material.PORTAL || space2.getType() == Material.PORTAL
				|| space1.getType() == Material.ENDER_PORTAL || ground.getType() == Material.ENDER_PORTAL || space2.getType() == Material.ENDER_PORTAL) {
			return false;
		}
		// If ground is AIR, then this is either not good, or they are on slab,
		// stair, etc.
		if (ground.getType() == Material.AIR) {
			// Bukkit.getLogger().info("DEBUG: air");
			return false;
		}
		// liquid may be unsafe
		if (ground.getType().equals(Material.STATIONARY_LAVA) || ground.getType().equals(Material.LAVA)
				|| space1.getType().equals(Material.STATIONARY_LAVA) || space1.getType().equals(Material.LAVA)
				|| space2.getType().equals(Material.STATIONARY_LAVA) || space2.getType().equals(Material.LAVA)) {
			// Lava check only
			// Bukkit.getLogger().info("DEBUG: lava");
			return false;
		}

		MaterialData md = ground.getState().getData();
		if (md instanceof SimpleAttachableMaterialData) {
			//Bukkit.getLogger().info("DEBUG: trapdoor/button/tripwire hook etc.");
			if (md instanceof TrapDoor) {
				TrapDoor trapDoor = (TrapDoor) md;
				if (trapDoor.isOpen()) {
					//Bukkit.getLogger().info("DEBUG: trapdoor open");
					return false;
				}
			} else {
				return false;
			}
			//Bukkit.getLogger().info("DEBUG: trapdoor closed");
		}
		if (ground.getType().equals(Material.CACTUS) || ground.getType().equals(Material.BOAT) || ground.getType().equals(Material.FENCE)
				|| ground.getType().equals(Material.NETHER_FENCE) || ground.getType().equals(Material.SIGN_POST) || ground.getType().equals(Material.WALL_SIGN)) {
			// Bukkit.getLogger().info("DEBUG: cactus");
			return false;
		}
		// Check that the space is not solid
		// The isSolid function is not fully accurate (yet) so we have to
		// check
		// a few other items
		// isSolid thinks that PLATEs and SIGNS are solid, but they are not
		return (!space1.getType().isSolid() || space1.getType().equals(Material.SIGN_POST) || space1.getType().equals(Material.WALL_SIGN)) && (!space2.getType().isSolid() || space2.getType().equals(Material.SIGN_POST) || space2.getType().equals(Material.WALL_SIGN));
	}
    // Localization Strings
    private final HashMap<String,Locale> availableLocales = new HashMap<>();
    // Listeners
    private WarpSigns warpSignsListener;
	// Warp panel
    private WarpPanel warpPanel;
    // Messages
    private Messages messages;
    private static WWarps plugin;


    /**
     * @return the messages
     */
    public Messages getMessages() {
	if (messages == null) {
	    messages = new Messages(this);
	}
	return messages;
    }

    /**
     * @return the warpPanel
     */
    public WarpPanel getWarpPanel() {
	if (warpPanel == null) {
	    // Probably due to a reload
	    warpPanel = new WarpPanel(this);
	    getServer().getPluginManager().registerEvents(warpPanel, this);
	}
	return warpPanel;
    }


    /**
     * @return the warpSignsListener
     */
    public WarpSigns getWarpSignsListener() {
	return warpSignsListener;
    }

    /**
     * Loads the various settings from the config.yml file into the plugin
     */
    public void loadPluginConfig() {
	// getLogger().info("*********************************************");
	try {
	    getConfig();
	} catch (final Exception e) {
	    e.printStackTrace();
	}
	//CompareConfigs.compareConfigs();
	// Get the localization strings
	//getLocale();
	// Add this to the config
	// Default is locale.yml
	availableLocales.put("locale", new Locale(this, "locale"));
	// Debug
	Settings.debug = getConfig().getInt("debug", 0);
	// Settings from config.yml
	Settings.useWarpPanel = getConfig().getBoolean("usewarppanel", true);
	Settings.worldName = getConfig().getStringList("signworlds");
	// All done
	}

    /**
     * @return System locale
     */
    public Locale myLocale() {
	return availableLocales.get("locale");
    }

    /*
     * (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
	try {
	    // Save the warps and do not reload the panel
	    if (warpSignsListener != null) {
		warpSignsListener.saveWarpList(false);
	    }
	} catch (final Exception e) {
	    getLogger().severe("Something went wrong saving files!");
	    e.printStackTrace();
	}
    }

    /*
     * (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
	plugin = this;
	saveDefaultConfig();
	// Load all the configuration of the plugin and localization strings
	loadPluginConfig();
	// Set up commands for this plugin
		WarpCmd warpCmd = new WarpCmd(this);
	AdminCmd adminCmd = new AdminCmd(this);

	getCommand("wwarp").setExecutor(warpCmd);
	getCommand("wwarp").setTabCompleter(warpCmd);	
	getCommand("wwadmin").setExecutor(adminCmd);
	// Register events
	registerEvents();
	// Have to do this a tick later to wait for all the worlds to load
	getServer().getScheduler().runTask(this, () -> {
    // Load warps
    getWarpSignsListener().loadWarpList();
    // Load the warp panel
    warpPanel = new WarpPanel(plugin);
    getServer().getPluginManager().registerEvents(warpPanel, plugin);
    });
    }

    /**
     * Registers events
     */
    public void registerEvents() {
	final PluginManager manager = getServer().getPluginManager();
	// Events for when a player joins or leaves the server
	manager.registerEvents(new JoinLeaveEvents(this), this);
	// Enables warp signs in ASkyBlock
	warpSignsListener = new WarpSigns(this);
	manager.registerEvents(warpSignsListener, this);
    }

    public static WWarps getPlugin() {
	return plugin;
    }

}
