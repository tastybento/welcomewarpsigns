package com.wasteofplastic.welcomewarpsigns;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WWarpCmd implements CommandExecutor {
    private WelcomeWarpSigns plugin;


    public WWarpCmd(WelcomeWarpSigns plugin) {
	this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (!(sender instanceof Player)) {
	    sender.sendMessage("/wwarp is for players only. Try /wwadmin?");
	    return true;
	}
	final Player player = (Player) sender;
	if (!Settings.worldName.isEmpty() && !Settings.worldName.contains(player.getWorld().getName())) {
	    // Wrong world
	    player.sendMessage(ChatColor.RED + "Welcome Warp Signs are not available in this world!");
	    return true;
	}
	switch (args.length) {
	case 0:
	    if (player.hasPermission("welcomewarpsigns.use")) {
		// Step through warp table
		Collection<String> warpList = plugin.listWarps();
		if (warpList.isEmpty()) {
		    player.sendMessage(ChatColor.YELLOW + "There are no warps available yet!");
		    if (player.hasPermission("welcomewarpsigns.add")) {
			player.sendMessage(ChatColor.YELLOW + "Create a warp by placing a sign with [WELCOME] at the top.");
		    }
		    return true;
		} else {
		    String wlist = "";
		    for (String w : warpList) {
			if (wlist.isEmpty()) {
			    wlist = w;
			} else {
			    wlist += ", " + w;

			}
		    }
		    player.sendMessage(ChatColor.YELLOW + "The following warps are available: " + ChatColor.WHITE + wlist);
		    if (plugin.getWarp(player)==null && player.hasPermission("welcomewarpsigns.add")) {
			player.sendMessage(ChatColor.YELLOW + "Create a warp by placing a sign with [WELCOME] at the top.");
		    }
		    return true;
		}
	    }
	case 1:
	    // Warp somewhere command
	    if (player.hasPermission("welcomewarpsigns.use")) {
		final Collection<String> warpList = plugin.listWarps();
		if (warpList.isEmpty()) {
		    player.sendMessage(ChatColor.YELLOW + "There are no warps available yet!");
		    if (player.hasPermission("welcomewarpsigns.add")) {
			player.sendMessage(ChatColor.YELLOW + "Create a warp by placing a sign with [WELCOME] at the top.");
		    }
		    return true;
		} else if (!plugin.checkWarp(args[0].toLowerCase())) {
		    player.sendMessage(ChatColor.RED + "That warp doesn't exist.");
		    return true;
		} else {
		    // Warp exists!
		    final Location warpSpot = plugin.getWarp(args[0].toLowerCase());
		    // Check if the warp spot is safe
		    if (warpSpot == null) {
			player.sendMessage(ChatColor.RED + "That warp is not ready yet! Try again later!");
			plugin.getLogger().warning("Null warp found, owned by " + args[0]);
			return true;
		    }
		    if (!(WelcomeWarpSigns.isSafeLocation(warpSpot))) {
			player.sendMessage(ChatColor.RED + "That warp is not safe right now! Try again later!");
			plugin.getLogger().warning("Unsafe warp found at " + warpSpot.toString() + " owned by " + args[0]);
			return true;
		    } else {
			final Location actualWarp = new Location(warpSpot.getWorld(), warpSpot.getBlockX() + 0.5D, warpSpot.getBlockY(),
				warpSpot.getBlockZ() + 0.5D);
			player.teleport(actualWarp);
			player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1F, 1F);
			return true;
		    }
		}
	    }
	    return false;

	}
	return false;
    }

}
