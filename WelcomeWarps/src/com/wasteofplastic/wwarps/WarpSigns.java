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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.wasteofplastic.wwarps.util.Util;

/**
 * Handles warping in ASkyBlock Players can add one sign
 * 
 * @author tastybento
 * 
 */
public class WarpSigns implements Listener {
    private final WWarps plugin;
    // Map of all warps stored as player, warp sign Location
    private final HashMap<UUID, Location> warpList;
    // Where warps are stored
    private YamlConfiguration welcomeWarps;

    /**
     * @param plugin
     */
    public WarpSigns(WWarps plugin) {
	this.plugin = plugin;
	this.warpList = new HashMap<>();
    }

    /**
     * Checks to see if a sign has been broken
     * @param e
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onSignBreak(BlockBreakEvent e) {
	Block b = e.getBlock();
	Player player = e.getPlayer();
	if (Settings.worldName.isEmpty() || Settings.worldName.contains(b.getWorld().getName())) {
	    if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
		Sign s = (Sign) b.getState();
		if (s != null) {
		    //plugin.getLogger().info("DEBUG: sign found at location " + s.toString());
		    if (s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + plugin.myLocale().warpswelcomeLine)) {
			// Do a quick check to see if this sign location is in
			//plugin.getLogger().info("DEBUG: welcome sign");
			// the list of warp signs
			if (warpList.containsValue(s.getLocation())) {
			    //plugin.getLogger().info("DEBUG: warp sign is in list");
			    // Welcome sign detected - check to see if it is
			    // this player's sign
			    if ((warpList.containsKey(player.getUniqueId()) && warpList.get(player.getUniqueId()).equals(s.getLocation()))) {
				// Player removed sign
				removeWarp(s.getLocation());
			    } else if (player.isOp()  || player.hasPermission(Settings.PERMPREFIX + "admin")) {
				// Op or mod removed sign
				player.sendMessage(ChatColor.GREEN + plugin.myLocale().warpsremoved);
				removeWarp(s.getLocation());
			    } else {
				// Someone else's sign - not allowed
				player.sendMessage(ChatColor.RED + plugin.myLocale().warpserrorNoRemove);
				e.setCancelled(true);
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * Event handler for Sign Changes
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onSignWarpCreate(SignChangeEvent e) {
	//plugin.getLogger().info("DEBUG: SignChangeEvent called");
	String title = e.getLine(0);
	Player player = e.getPlayer();
	//plugin.getLogger().info("DEBUG: The first line of the sign says " + title);
	if (title.equalsIgnoreCase(plugin.myLocale().warpswelcomeLine)) {
	    //plugin.getLogger().info("DEBUG: Welcome sign detected");
	    if (!Settings.worldName.isEmpty() && !Settings.worldName.contains(player.getWorld().getName())) {
		//plugin.getLogger().info("DEBUG: Incorrect world");
		player.sendMessage(ChatColor.RED + plugin.myLocale().errorWrongWorld);
		return;
	    }
	    // Welcome sign detected - check permissions
	    if (!player.hasPermission(Settings.PERMPREFIX + "add")) {
		player.sendMessage(ChatColor.RED + plugin.myLocale().warpserrorNoPerm);
		return;
	    }
	    //plugin.getLogger().info("DEBUG: has permission");
	    // Check if the player already has a sign
	    final Location oldSignLoc = getWarp(player.getUniqueId());
	    if (oldSignLoc == null) {
		//plugin.getLogger().info("DEBUG: Player does not have a sign already");
		// First time the sign has been placed or this is a new
		// sign
		if (addWarp(player.getUniqueId(), e.getBlock().getLocation())) {
		    player.sendMessage(ChatColor.GREEN + plugin.myLocale().warpssuccess);
		    e.setLine(0, ChatColor.GREEN + plugin.myLocale().warpswelcomeLine);
		    for (int i = 1; i<4; i++) {
			e.setLine(i, ChatColor.translateAlternateColorCodes('&', e.getLine(i)));
		    }
		} else {
		    player.sendMessage(ChatColor.RED + plugin.myLocale().warpserrorDuplicate);
		    e.setLine(0, ChatColor.RED + plugin.myLocale().warpswelcomeLine);
		    for (int i = 1; i<4; i++) {
			e.setLine(i, ChatColor.translateAlternateColorCodes('&', e.getLine(i)));
		    }
		}
	    } else {
		//plugin.getLogger().info("DEBUG: Player already has a Sign");
		// A sign already exists. Check if it still there and if
		// so,
		// deactivate it
		Block oldSignBlock = oldSignLoc.getBlock();
		if (oldSignBlock.getType().equals(Material.SIGN_POST) || oldSignBlock.getType().equals(Material.WALL_SIGN)) {
		    // The block is still a sign
		    //plugin.getLogger().info("DEBUG: The block is still a sign");
		    Sign oldSign = (Sign) oldSignBlock.getState();
		    if (oldSign != null) {
			//plugin.getLogger().info("DEBUG: Sign block is a sign");
			if (oldSign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + plugin.myLocale().warpswelcomeLine)) {
			    //plugin.getLogger().info("DEBUG: Old sign had a green welcome");
			    oldSign.setLine(0, ChatColor.RED + plugin.myLocale().warpswelcomeLine);
			    oldSign.update();
			    player.sendMessage(ChatColor.RED + plugin.myLocale().warpsdeactivate);
			    removeWarp(player.getUniqueId());
			}
		    }
		}
		// Set up the warp
		if (addWarp(player.getUniqueId(), e.getBlock().getLocation())) {
		    player.sendMessage(ChatColor.GREEN + plugin.myLocale().warpssuccess);
		    e.setLine(0, ChatColor.GREEN + plugin.myLocale().warpswelcomeLine);
		} else {
		    player.sendMessage(ChatColor.RED + plugin.myLocale().warpserrorDuplicate);
		    e.setLine(0, ChatColor.RED + plugin.myLocale().warpswelcomeLine);
		}
	    }
	}
    }

    /**
     * Saves the warp lists to file
     */
    public void saveWarpList(boolean reloadPanel) {
	if (warpList == null || welcomeWarps == null) {
	    return;
	}
	//plugin.getLogger().info("Saving warps...");
	final HashMap<String, Object> warps = new HashMap<>();
	for (UUID p : warpList.keySet()) {
	    warps.put(p.toString(), Util.getStringLocation(warpList.get(p)));
	}
	welcomeWarps.set("warps", warps);
	Util.saveYamlFile(welcomeWarps, "warps.yml");
	// Update the warp panel - needs to be done 1 tick later so that the sign
	// text will be updated.
	if (reloadPanel) {
	    // This is not done on shutdown
	    if (plugin.getWarpPanel() != null) {
		plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getWarpPanel().updatePanel());
	    }
	}
	//plugin.getLogger().info("End of saving warps");
    }

    /**
     * Creates the warp list if it does not exist
     */
    public void loadWarpList() {
	plugin.getLogger().info("Loading warps...");
	// warpList.clear();
	welcomeWarps = Util.loadYamlFile("warps.yml");
	if (welcomeWarps.getConfigurationSection("warps") == null) {
	    welcomeWarps.createSection("warps"); // This is only used to create
	    // the warp.yml file so forgive
	    // this code
	}
	HashMap<String, Object> temp = (HashMap<String, Object>) welcomeWarps.getConfigurationSection("warps").getValues(true);
	for (String s : temp.keySet()) {
	    try {
		UUID playerUUID = UUID.fromString(s);
		//plugin.getLogger().info("DEBUG: Player UUID =  " + playerUUID);
		//plugin.getLogger().info("DEBUG: text loc = " + (String) temp.get(s));
		Location l = Util.getLocationString((String) temp.get(s));
		//plugin.getLogger().info("DEBUG: Loading warp at " + l);
		Block b = l.getBlock();
		// Check that a warp sign is still there
		if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
		    warpList.put(playerUUID, l);
		} else {
		    plugin.getLogger().warning("Warp at location " + temp.get(s) + " has no sign - removing.");
		}
	    } catch (Exception e) {
		plugin.getLogger().severe("Problem loading warp at location " + temp.get(s) + " - removing.");
		e.printStackTrace();
	    }
	}
    }

    /**
     * Stores warps in the warp array
     * 
     * @param player
     * @param loc
     */
    public boolean addWarp(UUID player, Location loc) {
	// Do not allow warps to be in the same location
	if (warpList.containsValue(loc)) {
	    return false;
	}
	// Remove the old warp if it existed
	if (warpList.containsKey(player)) {
	    warpList.remove(player);
	}
	warpList.put(player, loc);
	saveWarpList(true);
	return true;
    }

    /**
     * Removes a warp when the welcome sign is destroyed. Called by
     * WarpSigns.java.
     * 
     * @param uuid
     */
    public void removeWarp(UUID uuid) {
	if (warpList.containsKey(uuid)) {
	    popSign(warpList.get(uuid));
	    warpList.remove(uuid);
	}
	saveWarpList(true);
    }

    /**
     * Changes the sign to red if it exists
     * @param loc
     */
    private void popSign(Location loc) {
	Block b = loc.getBlock();
	if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
	    Sign s = (Sign) b.getState();
	    if (s != null) {
		if (s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + plugin.myLocale().warpswelcomeLine)) {
		    s.setLine(0, ChatColor.RED + plugin.myLocale().warpswelcomeLine);
		    s.update();
		}
	    }
	}
    }

    /**
     * Removes a warp at a location. Called by WarpSigns.java.
     * 
     * @param loc
     */
    public void removeWarp(Location loc) {
	//plugin.getLogger().info("Asked to remove warp at " + loc);
	popSign(loc);
	Iterator<Entry<UUID, Location>> it = warpList.entrySet().iterator();
	while (it.hasNext()) {
	    Entry<UUID, Location> en = it.next();
	    if (en.getValue().equals(loc)) {
		// Inform player
		Player p = plugin.getServer().getPlayer(en.getKey());
		if (p != null) {
		    // Inform the player
		    p.sendMessage(ChatColor.RED + plugin.myLocale().warpssignRemoved);
		} else {
		    plugin.getMessages().setMessage(en.getKey(), ChatColor.RED + plugin.myLocale().warpssignRemoved);
		}
		it.remove();
	    }
	}
	saveWarpList(true);
    }

    /**
     * Lists all the known warps
     * 
     * @return String set of warps
     */
    public Set<UUID> listWarps() {
	return warpList.keySet();
    }

    /**
     * @return Sorted list of warps with most recent players listed first
     */
    public Collection<UUID> listSortedWarps() {
	// Bigger value of time means a more recent login
	TreeMap<Long, UUID> map = new TreeMap<>();
	for (UUID uuid : warpList.keySet()) {
	    map.put(plugin.getServer().getOfflinePlayer(uuid).getLastPlayed(), uuid);
	}
		return map.descendingMap().values();
    }
    /**
     * Provides the location of the warp for player or null if one is not found
     * 
     * @param player
     *            - the warp requested
     * @return Location of warp
     */
    public Location getWarp(UUID player) {
		return warpList.getOrDefault(player, null);
    }

    /**
     * @param location
     * @return Name of warp owner
     */
    public String getWarpOwner(Location location) {
	for (UUID playerUUID : warpList.keySet()) {
	    if (location.equals(warpList.get(playerUUID))) {
		return plugin.getServer().getOfflinePlayer(playerUUID).getName();
	    }
	}
	return "";
    }

}