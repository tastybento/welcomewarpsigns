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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.wasteofplastic.wwarps.Settings;
import com.wasteofplastic.wwarps.WWarps;
import com.wasteofplastic.wwarps.util.Util;

public class WarpCmd implements CommandExecutor, TabCompleter {
    private final WWarps plugin;
    private Sound batTakeOff;
    /**
     * Constructor
     * 
     * @param aSkyBlock
     */
    public WarpCmd(WWarps aSkyBlock) {
        // Plugin instance
        this.plugin = aSkyBlock;
        // Get the sounds
        for (Sound sound: Sound.values()) {
            if (sound.toString().contains("TAKEOFF")) {
                batTakeOff = sound;
            }
        }
    }

    /**
     * One-to-one relationship, you can return the first matched key
     * 
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

    /*
     * (non-Javadoc)
     * @see
     * org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender
     * , org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        if (!(sender instanceof Player)) {
            return false;
        }
        final Player player = (Player) sender;
        final UUID playerUUID = player.getUniqueId();
        if (label.equalsIgnoreCase("wwarps")) {
            if (player.hasPermission(Settings.PERMPREFIX + "use")) {
                if (Settings.useWarpPanel) {
                    // Step through warp table
                    Set<UUID> warpList = plugin.getWarpSignsListener().listWarps();
                    if (warpList.isEmpty()) {
                        player.sendMessage(ChatColor.YELLOW + plugin.myLocale().warpserrorNoWarpsYet);
                        player.sendMessage(ChatColor.YELLOW + plugin.myLocale().warpswarpTip);
                        return true;
                    } else {
                        // Try the warp panel
                        int panelNum;
                        try {
                            panelNum = Integer.valueOf(split[0]) - 1;
                        } catch (Exception e) {
                            panelNum = 0;
                        }
                        player.openInventory(plugin.getWarpPanel().getWarpPanel(panelNum));
                        return true;
                    }
                } else {
                    // Step through warp table
                    Collection<UUID> warpList = plugin.getWarpSignsListener().listWarps();
                    if (warpList.isEmpty()) {
                        player.sendMessage(ChatColor.YELLOW + plugin.myLocale().warpserrorNoWarpsYet);
                        player.sendMessage(ChatColor.YELLOW + plugin.myLocale().warpswarpTip);
                        return true;
                    } else {
                        if (Settings.useWarpPanel) {
                            // Try the warp panel
                            player.openInventory(plugin.getWarpPanel().getWarpPanel(0));
                        } else {
                            Boolean hasWarp = false;
                            StringBuilder wlist = new StringBuilder();
                            for (UUID w : warpList) {
                                if (wlist.length() == 0) {
                                    wlist = new StringBuilder(plugin.getServer().getOfflinePlayer(w).getName());
                                } else {
                                    wlist.append(", ").append(plugin.getServer().getOfflinePlayer(w).getName());
                                }
                                if (w.equals(playerUUID)) {
                                    hasWarp = true;
                                }
                            }
                            player.sendMessage(ChatColor.YELLOW + plugin.myLocale().warpswarpsAvailable + ": " + ChatColor.WHITE + wlist);
                            if (!hasWarp && (player.hasPermission(Settings.PERMPREFIX + "addwarp"))) {
                                player.sendMessage(ChatColor.YELLOW + plugin.myLocale().warpswarpTip);
                            }
                        }
                        return true;
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + plugin.myLocale().errorNoPermission);
                return true;
            }
        }
        if (label.equalsIgnoreCase("wwarp")) {
            switch (split.length) {
            case 1:
                // Warp somewhere command
                if (!player.hasPermission(Settings.PERMPREFIX + "use")) {
                    player.sendMessage(ChatColor.RED + plugin.myLocale().errorNoPermission);
                    return true;
                }
                final Set<UUID> warpList = plugin.getWarpSignsListener().listWarps();
                if (warpList.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + plugin.myLocale().warpserrorNoWarpsYet);
                    if (player.hasPermission(Settings.PERMPREFIX + "add")) {
                        player.sendMessage(ChatColor.YELLOW + plugin.myLocale().warpswarpTip);
                    } else {
                        player.sendMessage(ChatColor.RED + plugin.myLocale().errorNoPermission);
                    }
                    return true;
                } else {
                    // Check if this is part of a name
                    UUID foundWarp = null;
                    for (UUID warp : warpList) {
                        if (plugin.getServer().getOfflinePlayer(warp).getName().toLowerCase().startsWith(split[0].toLowerCase())) {
                            foundWarp = warp;
                            break;
                        }
                    }
                    if (foundWarp == null) {
                        player.sendMessage(ChatColor.RED + plugin.myLocale().warpserrorDoesNotExist);
                        return true;
                    } else {
                        // Warp exists!
                        final Location warpSpot = plugin.getWarpSignsListener().getWarp(foundWarp);
                        // Check if the warp spot is safe
                        if (warpSpot == null) {
                            player.sendMessage(ChatColor.RED + plugin.myLocale().warpserrorNotReadyYet);
                            plugin.getLogger().warning("Null warp found, owned by " + plugin.getServer().getOfflinePlayer(foundWarp).getName());
                            return true;
                        }
                        // Find out which direction the warp is facing
                        Block b = warpSpot.getBlock();
                        if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
                            Sign sign = (Sign) b.getState();
                            org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
                            BlockFace directionFacing = s.getFacing();
                            Location inFront = b.getRelative(directionFacing).getLocation();
                            Location oneDown = b.getRelative(directionFacing).getRelative(BlockFace.DOWN).getLocation();
                            if ((WWarps.isSafeLocation(inFront))) {
                                warpPlayer(player, inFront, foundWarp, directionFacing);
                                return true;
                            } else if (b.getType().equals(Material.WALL_SIGN) && WWarps.isSafeLocation(oneDown)) {
                                // Try one block down if this is a wall sign
                                warpPlayer(player, oneDown, foundWarp, directionFacing);
                                return true;
                            }
                        } else {
                            // Warp has been removed
                            player.sendMessage(ChatColor.RED + plugin.myLocale().warpserrorDoesNotExist);
                            plugin.getWarpSignsListener().removeWarp(warpSpot);
                            return true;
                        }
                        if (!(WWarps.isSafeLocation(warpSpot))) {
                            player.sendMessage(ChatColor.RED + plugin.myLocale().warpserrorNotSafe);
                            // WALL_SIGN's will always be unsafe if the place in front is obscured.
                            if (b.getType().equals(Material.SIGN_POST)) {
                                plugin.getLogger().warning(
                                        "Unsafe warp found at " + warpSpot.toString() + " owned by " + plugin.getServer().getOfflinePlayer(foundWarp).getName());
                            }
                            return true;
                        } else {
                            final Location actualWarp = new Location(warpSpot.getWorld(), warpSpot.getBlockX() + 0.5D, warpSpot.getBlockY(),
                                    warpSpot.getBlockZ() + 0.5D);
                            player.teleport(actualWarp);			    
                            player.getWorld().playSound(player.getLocation(), batTakeOff, 1F, 1F);
                            return true;
                        }
                    }
                }
            default:
                player.performCommand("wwarps");
                return true;
            } 
        } 
        return false;
    }


    /**
     * Warps a player to a spot in front of a sign
     * @param player
     * @param inFront
     * @param foundWarp
     * @param directionFacing
     */
    private void warpPlayer(Player player, Location inFront, UUID foundWarp, BlockFace directionFacing) {
        // convert blockface to angle
        float yaw = Util.blockFaceToFloat(directionFacing);
        final Location actualWarp = new Location(inFront.getWorld(), inFront.getBlockX() + 0.5D, inFront.getBlockY(),
                inFront.getBlockZ() + 0.5D, yaw, 30F);
        player.teleport(actualWarp);
        player.getWorld().playSound(player.getLocation(), batTakeOff, 1F, 1F);
        Player warpOwner = plugin.getServer().getPlayer(foundWarp);
        if (warpOwner != null && !warpOwner.equals(player)) {
            warpOwner.sendMessage(plugin.myLocale().warpsPlayerWarped.replace("[name]", player.getDisplayName()));
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        final List<String> options = new ArrayList<>();
        if (!(sender instanceof Player)) {
            //plugin.getLogger().info("DEBUG: not a player");
            return options;
        }
        if (!label.equalsIgnoreCase("wwarp")) {
            //plugin.getLogger().info("DEBUG: not right command");
            return options;
        }
        final Player player = (Player) sender;
        if (!player.hasPermission(Settings.PERMPREFIX + "use")) {
            //plugin.getLogger().info("DEBUG: not right permission");
            return options;
        }
        String lastArg = (args.length != 0 ? args[args.length - 1] : "");
        //plugin.getLogger().info("DEBUG: args length = " + args.length);
        switch (args.length) {
        case 1: 
            final Set<UUID> warpList = plugin.getWarpSignsListener().listWarps();
            //plugin.getLogger().info("DEBUG: warp list = " + warpList);
            for (UUID warp : warpList) {
                //plugin.getLogger().info("DEBUG: adding " + plugin.getServer().getOfflinePlayer(warp).getName());
                options.add(plugin.getServer().getOfflinePlayer(warp).getName());
            }
            break;
        default:
            break;
        }
        return Util.tabLimit(options, lastArg);
    }
}
