/**
 * 
 */
package com.wasteofplastic.welcomewarpsigns;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

/**
 * Handles warping in AcidIsland Players can add one sign
 * 
 * @author ben
 * 
 */
public class WarpSignsListener implements Listener {
    private final WelcomeWarpSigns plugin;

    public WarpSignsListener(WelcomeWarpSigns acidIsland) {
	plugin = acidIsland;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onSignPopped(BlockPhysicsEvent e) {
	// Block b = e.getBlock().getRelative(BlockFace.UP);
	Block b = e.getBlock();
	if (!Settings.worldName.isEmpty() && !Settings.worldName.contains(b.getWorld().getName())) {
	    // Wrong world
	    return;
	}
	// plugin.getLogger().info("Block type " + b.getType().toString());
	if (plugin.checkWarp(b.getLocation())) {
	    //plugin.getLogger().info("Known warp location!");
	    // This is the sign block - check to see if it is still a sign
	    if (b.getType().equals(Material.SIGN_POST)) {
		// Check to see if it is still attached
		MaterialData m = b.getState().getData();
		BlockFace face = BlockFace.DOWN; // Most of the time it's going
		// to be down
		if (m instanceof Attachable) {
		    face = ((Attachable) m).getAttachedFace();
		}
		if (b.getRelative(face).getType().isSolid()) {
		    //plugin.getLogger().info("Attached to some solid block");
		} else {
		    /*
		     * } MaterialData m = b.getState().getData(); BlockFace face
		     * = BlockFace.DOWN; if (m instanceof Attachable) { face =
		     * ((Attachable) m).getAttachedFace(); }
		     */
		    // b.getRelative(face);
		    //plugin.getLogger().info("Not attached!");
		    plugin.removeWarp(b.getLocation());
		    //plugin.getLogger().info("Warp removed");
		}
	    }
	}
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onSignBreak(BlockBreakEvent e) {
	Block b = e.getBlock();
	Player player = e.getPlayer();
	if (Settings.worldName.contains(b.getWorld().getName()) || Settings.worldName.isEmpty()) {
	    if (b.getType().equals(Material.SIGN_POST)) {
		Sign s = (Sign) b.getState();
		if (s != null) {
		    if (s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "[WELCOME]")) {
			// Do a quick check to see if this sign location is in
			// the list of warp signs
			if (plugin.checkWarp(s.getLocation())) {
			    // Welcome sign detected - check to see if it is
			    // this player's sign
			    final Location playerSignLoc = plugin.getWarp(player);
			    if (playerSignLoc != null) {
				if (playerSignLoc.equals(s.getLocation())) {
				    // This is the player's sign, so allow it to
				    // be destroyed
				    player.sendMessage(ChatColor.GREEN + "Welcome sign removed");
				    plugin.removeWarp(player);
				} else {
				    player.sendMessage(ChatColor.RED + "You can only remove your own Welcome Sign");
				    e.setCancelled(true);
				}
			    } else {
				// Someone else's sign because this player has
				// none registered
				player.sendMessage(ChatColor.RED + "You can only remove your own Welcome Sign");
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
	//plugin.getLogger().info("SignChangeEvent called");
	String title = e.getLine(0);
	Player player = e.getPlayer();
	if (Settings.worldName.contains(player.getWorld().getName()) || Settings.worldName.isEmpty()) {
		plugin.getLogger().info("Correct world");
	    	if (e.getBlock().getType().equals(Material.SIGN_POST)) {
		plugin.getLogger().info("The first line of the sign says " + title);
		if (title.equalsIgnoreCase("[WELCOME]")) {
		    plugin.getLogger().info("Welcome sign detected");
		    // Welcome sign detected - check permissions
		    if (!(player.hasPermission("welcomewarpsigns.add"))) {
			player.sendMessage(ChatColor.RED + "You do not have permission to place Welcome Signs yet");
			return;
		    }
		    // Check if the sign is in an acceptable location
		    if (!WelcomeWarpSigns.isSafeLocation(e.getBlock().getLocation())) {
			player.sendMessage(ChatColor.RED + "Sorry! That is not a safe place to put a Welcome Sign!");
			    e.setLine(0, ChatColor.RED + "[WELCOME]");
			    return;
		    }
		    // Check if the player already has a sign
		    final Location oldSignLoc = plugin.getWarp(player);
		    if (oldSignLoc == null) {
			//plugin.getLogger().info("Player does not have a sign already");
			// First time the sign has been placed or this is a new sign
			if (plugin.addWarp(player, e.getBlock().getLocation())) {
			    player.sendMessage(ChatColor.GREEN + "Welcome sign placed successfully!");
			    e.setLine(0, ChatColor.GREEN + "[WELCOME]");
			} else {
			    player.sendMessage(ChatColor.RED + "Sorry! There is a sign already in that location!");
			    e.setLine(0, ChatColor.RED + "[WELCOME]");
			}
		    } else {
			//plugin.getLogger().info("Player already has a Sign");
			// A sign already exists. Check if it still there and if so,
			// deactivate it
			Block oldSignBlock = oldSignLoc.getBlock();
			if (oldSignBlock.getType().equals(Material.SIGN_POST)) {
			    // The block is still a sign
			    //plugin.getLogger().info("The block is still a sign");
			    Sign oldSign = (Sign) oldSignBlock.getState();
			    if (oldSign != null) {
				//plugin.getLogger().info("Sign block is a sign");
				if (oldSign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "[WELCOME]")) {
				    //plugin.getLogger().info("Old sign had a green welcome");
				    oldSign.setLine(0, ChatColor.RED + "[WELCOME]");
				    oldSign.update();
				    player.sendMessage(ChatColor.RED + "Deactivating old sign.");
				    plugin.removeWarp(player);
				}
			    }
			}
			// Set up the warp
			if (plugin.addWarp(player, e.getBlock().getLocation())) {
			    player.sendMessage(ChatColor.GREEN + "Welcome sign placed successfully!");
			    e.setLine(0, ChatColor.GREEN + "[WELCOME]");
			} else {
			    player.sendMessage(ChatColor.RED + "Sorry! There is a sign already in that location!");
			    e.setLine(0, ChatColor.RED + "[WELCOME]");
			}
		    }
		}
	    }
	}
    }

}
