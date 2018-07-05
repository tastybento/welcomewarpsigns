package com.wasteofplastic.wwarps;

import java.util.*;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.wasteofplastic.wwarps.util.Util;

/**
 * Handles offline messaging to players and teams
 * 
 * @author tastybento
 * 
 */
public class Messages {
    private final WWarps plugin;
    // Offline Messages
    private final HashMap<UUID, List<String>> messages = new HashMap<>();
    private YamlConfiguration messageStore;

    
    /**
     * @param plugin
     */
    public Messages(WWarps plugin) {
	this.plugin = plugin;
    }
    
    /**
     * Returns what messages are waiting for the player or null if none
     * 
     * @param playerUUID
     * @return
     */
    public List<String> getMessages(UUID playerUUID) {
		return messages.get(playerUUID);
    }

    /**
     * Clears any messages for player
     * 
     * @param playerUUID
     */
    public void clearMessages(UUID playerUUID) {
	messages.remove(playerUUID);
    }

    public void saveMessages() {
	if (messageStore == null) {
	    return;
	}
	plugin.getLogger().info("Saving offline messages...");
	try {
	    // Convert to a serialized string
	    final HashMap<String, Object> offlineMessages = new HashMap<>();
	    for (UUID p : messages.keySet()) {
		offlineMessages.put(p.toString(), messages.get(p));
	    }
	    // Convert to YAML
	    messageStore.set("messages", offlineMessages);
	    Util.saveYamlFile(messageStore, "messages.yml");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public boolean loadMessages() {
	plugin.getLogger().info("Loading offline messages...");
	try {
	    messageStore = Util.loadYamlFile("messages.yml");
	    if (messageStore.getConfigurationSection("messages") == null) {
		messageStore.createSection("messages"); // This is only used to
							// create
	    }
	    HashMap<String, Object> temp = (HashMap<String, Object>) messageStore.getConfigurationSection("messages").getValues(true);
	    for (String s : temp.keySet()) {
		List<String> messageList = messageStore.getStringList("messages." + s);
		if (!messageList.isEmpty()) {
		    messages.put(UUID.fromString(s), messageList);
		}
	    }
	    return true;
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }

    /**
     * Provides the messages for the player
     * 
     * @param playerUUID
     * @return List of messages
     */
    public List<String> get(UUID playerUUID) {
	return messages.get(playerUUID);
    }

    /**
     * Stores a message for player
     * 
     * @param playerUUID
     * @param playerMessages
     */
    public void put(UUID playerUUID, List<String> playerMessages) {
	messages.put(playerUUID, playerMessages);

    }


    /**
     * Sets a message for the player to receive next time they login
     * 
     * @param message
     * @return true if player is offline, false if online
     */
    public boolean setMessage(UUID playerUUID, String message) {
	// getLogger().info("DEBUG: received message - " + message);
	Player player = plugin.getServer().getPlayer(playerUUID);
	// Check if player is online
	if (player != null) {
	    if (player.isOnline()) {
		// player.sendMessage(message);
		return false;
	    }
	}
	// Player is offline so store the message
	// getLogger().info("DEBUG: player is offline - storing message");
	List<String> playerMessages = get(playerUUID);
	if (playerMessages != null) {
	    playerMessages.add(message);
	} else {
	    playerMessages = new ArrayList<>(Collections.singletonList(message));
	}
	put(playerUUID, playerMessages);
	return true;
    }

}
