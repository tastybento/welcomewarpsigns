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
package com.wasteofplastic.wwarps.listeners;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.wasteofplastic.wwarps.WWarps;

public class JoinLeaveEvents implements Listener {
    private final WWarps plugin;

    public JoinLeaveEvents(WWarps plugin) {
	this.plugin = plugin;
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
	final Player player = event.getPlayer();
	final UUID playerUUID = player.getUniqueId();
	// Load any messages for the player
	// plugin.getLogger().info("DEBUG: Checking messages for " +
	// player.getName());
	final List<String> messages = plugin.getMessages().getMessages(playerUUID);
	if (messages != null) {
	    // plugin.getLogger().info("DEBUG: Messages waiting!");
	    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(ChatColor.AQUA + plugin.myLocale().newsHeadline);
            int i = 1;
            for (String message : messages) {
            player.sendMessage(i++ + ": " + message);
            }
            // Clear the messages
            plugin.getMessages().clearMessages(playerUUID);
        }, 40L);
	} // else {
	// plugin.getLogger().info("no messages");
	// }
    }
}