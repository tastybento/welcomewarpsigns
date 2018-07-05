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

package com.wasteofplastic.wwarps.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wasteofplastic.wwarps.Settings;
import com.wasteofplastic.wwarps.WWarps;

/**
 * This class handles admin commands
 * 
 */
public class AdminCmd implements CommandExecutor {
    private final WWarps plugin;
    public AdminCmd(WWarps aSkyBlock) {
	this.plugin = aSkyBlock;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender
     * , org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
	// Console commands
	Player player;
	if (sender instanceof Player) {
	    player = (Player) sender;
	    if (!player.hasPermission(Settings.PERMPREFIX + "admin")) {
		player.sendMessage(ChatColor.RED + plugin.myLocale().errorNoPermission);
		return true;
	    }
	}
	if (split.length == 0) {
	    sender.sendMessage(plugin.myLocale().adminHelpHelp);
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " reload:" + ChatColor.WHITE + " " + plugin.myLocale().adminHelpreload);
	    return true;
	}
	if (split.length == 1) {
	    if (split[0].equalsIgnoreCase("reload")) {
		// reload
		plugin.reloadConfig();
		plugin.loadPluginConfig();
		sender.sendMessage(ChatColor.YELLOW + plugin.myLocale().reloadconfigReloaded);
		return true;
	    } else {
		sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownCommand);
		return true;
	    }
	} 
	sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownCommand);
	return true;
    }
}