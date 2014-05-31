/**
 * 
 */
package com.wasteofplastic.welcomewarpsigns;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author ben
 *
 */
public class WAdmin implements CommandExecutor {
    private WelcomeWarpSigns plugin;
    /**
     * 
     */
    public WAdmin(WelcomeWarpSigns plugin) {
	this.plugin = plugin;
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (sender instanceof Player) {
	    // Check permissions
	    if (!sender.hasPermission("welcomewarpsigns.admin")) {
		sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
		return true;
	    }
	}
	switch (args.length) {
	// Switch on arguments 
	case 1:
	    // List
	    if (args[0].equalsIgnoreCase("list")) {
		// Step through warp table
		HashMap<String,String> warpList = plugin.listWarpsAndLocs();
		if (warpList.isEmpty()) {
		    sender.sendMessage(ChatColor.YELLOW + "There are no warps created yet!");
		} else {
		    sender.sendMessage(ChatColor.YELLOW + "[Warp Name] - [World: X:Y:Z location]");
		    for (String name : warpList.keySet()) {
			sender.sendMessage(ChatColor.AQUA + name + ChatColor.GRAY + " - " + ChatColor.WHITE + warpList.get(name));
		    }
		}
		return true;
	    }
	    if (args[0].equalsIgnoreCase("remove")) {
		sender.sendMessage(ChatColor.YELLOW + "/wwadmin remove <name> - removed warp, deactivates sign");
		return true;
	    }
	case 2:
	    // Remove warp command
	    if (args[0].equalsIgnoreCase("remove")) {
		if (!plugin.checkWarp(args[1].toLowerCase())) {
		    sender.sendMessage(ChatColor.RED + "That warp doesn't exist.");
		    return true;
		} else {
		    // Remove warp
		    if (plugin.removeWarp(args[1].toLowerCase())) {
			sender.sendMessage(ChatColor.GREEN + "Warp removed.");
		    } else {
			sender.sendMessage(ChatColor.RED + "Warp could not be found. Check warps.yml for corruption.");
		    }
		    return true;
		}
	    }
	    sender.sendMessage(ChatColor.RED + "Unknown command");
	default:
	    // No argument so print the help
	    sender.sendMessage(ChatColor.YELLOW + "[Welcome Warp Signs Help]");
	    sender.sendMessage(ChatColor.YELLOW + "/wwadmin list - lists all known warps and their locations");
	    sender.sendMessage(ChatColor.YELLOW + "/wwadmin remove <name> - removed warp, deactivates sign");
	    return true;
	}
    }
}
