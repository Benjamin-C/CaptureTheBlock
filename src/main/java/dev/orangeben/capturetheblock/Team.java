package dev.orangeben.capturetheblock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import peterTimer.Timer;

public class Team {
	
	// data to save
    /** The name of the team */
	private String name;
    /** All of the players on the team, even if they're offline */
	private Map<UUID, String> uuids;
    /** The team's score */
	private int score;
    /** The team's color */
	private Color color;
    /** The number of rounds the team has been in */
    private int roundCount;
	
	// data to not save
    /** The block type the team is attempting to find */
	private Material target;
    /** If the team has given up */
	private boolean givenup;
    /** All of the online players on the team */
	private Map<UUID, Player> peoples;
    /** If each player has found their block */
	private Map<UUID, Boolean> foundBlock;
    /** The plugin this team is currently in */
    private CTBMain plugin;
	
	public Team(String name, CTBMain plugin) {
		this.name = name;
        this.plugin = plugin;

		peoples = new HashMap<UUID, Player>();

		uuids = new HashMap<UUID, String>();
		foundBlock = new HashMap<UUID, Boolean>();
	}
	
    /**
     * Updates the team's time bars, updating the remaining time and team status info
     * @param timer The timer to update in
     * @param titleprefix The prefix to go in the timer title
     */
	public void updateTimeBars(Timer timer, String titleprefix) {
		if(timer != null) {
			String got_str = "";
			int gotnum = 0;
			for(Player p : peoples.values()) {
				if(foundBlock.get(p.getUniqueId()) == true) {
					gotnum++;
				}
			}
			if(gotnum == 0) {
				got_str += plugin.getString("color.missed");
			} else if(gotnum < peoples.size()) {
				got_str += plugin.getString("color.got");
			} else {
				got_str += plugin.getString("color.got");
			}
			got_str += gotnum + "/" + peoples.size();
			timer.setTitle(plugin.getString("color.main") + titleprefix + plugin.getString("color.missed") + target + " " + got_str + plugin.getString("color.main"), name);
			timer.setTitle(plugin.getString("color.main") + titleprefix + ((gotnum == peoples.size()) ? plugin.getString("color.got") : plugin.getString("color.got.some")) + target + " " + got_str + plugin.getString("color.main"), name + Keys.BOSSBAR_GOTBLOCK_SUFFIX);
		}
	}

    /**
     * Gets the name of the team
     * @return the team name
     */
    public String getName() {
		return name;
	}

    /**
     * Sets the name of the team
     * @param newName The new team name
     */
	public void setName(String newName) {
		name = newName;
	}

    /**
     * Gets the team's color
     * @return the color
     */
	public Color getColor() {
		return color;
	}
    /**
     * Sets the team color
     * @param color the new color
     */
	public void setColor(Color color) {
		this.color = color;
	}

    /**
     * Gets the number of rounds the team has been in
     * @return The number of rounds
     */
    public int getRoundCount() {
        return roundCount;
    }
    /**
     * Sets the number of rounds the team has been in
     * @param newCount the new number of rounds
     */
    public void setRoundCount(int newCount) {
        roundCount = newCount;
    }
    /**
     * Increments the number of rounds the team has been in
     */
    public void incrementRoundCount() {
        roundCount++;
    }


	
    // ___       __   __   ___ ___ 
    // |   /\  |__) / _` |__   |  
    // |  /~~\ |  \ \__> |___  |  
    //    

    /**
     * Gets the block the team is going for
     * @return The block type
     */
	public Material getTarget() {
		return target;
	}

    /**
     * Sets the block the team is going for
     * @param target The new block type
     */
	public void setTarget(Material target) {
		this.target = target;
	}

    /**
     * Gets wether the team has given up on this block or not
     * @return
     */
	public boolean isGivenup() {
		return givenup;
	}

    /**
     * Sets if the team has given up on this block
     * @param givenup
     */
	public void setGivenup(boolean givenup) {
		this.givenup = givenup;
	}

    //  __   __   __   __   ___ 
    // /__` /  ` /  \ |__) |__  
    // .__/ \__, \__/ |  \ |___ 
    // 

    /**
     * If the team would score a point if the round ended right now
     * @return If the team would get a point
     */
    public boolean hasScored() {
		if(peoples.size() < 1) {
			return false;
		}
    	return hasEveryoneFound();
	}

    /**
     * Checks if a specific player has found their block
     * @param pl the UUID of the player
     * @return if they have found their block
     */
    public boolean hasFound(UUID pl) {
		return foundBlock.get(pl);
	}

    /**
     * If everyone who is online has found their block
     * @return If everyone has found their block
     */
	public boolean hasEveryoneFound() {
		for(UUID u : peoples.keySet()) {
			if(!foundBlock.get(u)) {
				return false;
    		}
		}
		return true;
	}

    /**
     * If any online player has found their block
     * @return If anyone has found their block
     */
    public boolean hasAnyoneFound() {
		for(UUID u : peoples.keySet()) {
			if(foundBlock.get(u)) {
				return true;
    		}
		}
		return false;
	}

    /**
     * Sets if a player has found their block. Does not perform any other possibly desired actions.
     * @param uuid The UUID of the player
     * @param found If they have found their block
     */
	public void setFound(UUID uuid, boolean found) {
		foundBlock.put(uuid, found);
	}

    /**
     * Marks everyone has having not found their block regardless of if they are online right now
     */
    public void clearFound() {
		for(UUID u : foundBlock.keySet()) {
			foundBlock.put(u, false);
		}
	}

    /**
     * Sets the team's score
     * @param score The new score
     */
    public void setScore(int score) {
		this.score = score;
	}
    /**
     * Gets the team's score
     * @return The team's score
     */
	public int getScore() {
		return score;
	}
    /**
     * Adds to the team's score
     * @param add The amount to add
     */
	public void addScore(int add) {
		score += add;
	}
    /**
     * Subtracts from the team's score
     * @param sub The amount to subtract
     */
	public void subtractScore(int sub) {
		score -= sub;
	}

    // __                 ___  __   __  
    // |__) |     /\  \ / |__  |__) /__` 
    // |    |___ /~~\  |  |___ |  \ .__/     
    //

    /**
     * Gets the collection of all online Players
     * @return
     */
	public Collection<Player> getOnlinePeoples() {
		return peoples.values();
	}

    /**
     * Gets the map of UUID to players for all online players
     * @return
     */
    public Map<UUID, Player> getPeoples() {
        return peoples;
    }
	
    /**
     * Gets all people who are part of the team wether or not they are online
     * @return
     */
	public Map<UUID, String> getAllPeoples() {
		return uuids;
	}

    /**
     * Checks if a player is online
     * @param u The UUID of the player to check
     * @return If the player is online
     */
	public boolean isOnline(UUID u) {
        return peoples.containsKey(u);
	}
	
    /**
     * Adds a new player to the team. The player must be online
     * @param p the Player to add
     */
	public void addPerson(Player p) {
		peoples.put(p.getUniqueId(), p);
		uuids.put(p.getUniqueId(), p.getName());
		foundBlock.put(p.getUniqueId(), false);
	}
    /**
     * Adds a new player to the team. The player must be offline
     * @param u the UUID of the player
     * @param name the name of the player
     */
	public void addPerson(UUID u, String name) {
		uuids.put(u, name);
		foundBlock.put(u, false);
	}
	
    /**
     * Removes a player from the team
     * @param p the player to remove
     */
	public void removePerson(Player p) {
		peoples.remove(p.getUniqueId());
		uuids.remove(p.getUniqueId());
	}
	/**
     * Marks a player as offline
     * @param p the player
     */
	public void disconnectPerson(Player p) {
		peoples.remove(p.getUniqueId());
	}
    /**
     * Marks a player as online
     * @param p the player
     */
	public void reconnectPerson(Player p) {
		peoples.put(p.getUniqueId(), p);
		if(uuids.containsKey(p.getUniqueId()) ) {
			uuids.put(p.getUniqueId(), p.getName());
		}
	}
	
    /**
     * Gets a player by UUID
     * @param u The UUID to search for
     * @return the Player, or null if they aren't on the team or are offline
     */
    public Player getPlayer(UUID u) {
        return peoples.get(u);
    }

    /**
     * Checks if a player is a member of the team, ignoring their online status
     * @param u The UUID of the player
     * @return if the player is a member of the team
     */
	public boolean isMember(UUID u) {
		return uuids.containsKey(u);
	}
	
    /**
     * Sends a message to everyone on the team
     * @param msg the message to send
     */
	public void sendMessage(String msg) {
		sendMessage(msg, null);
	}
	
    /**
     * Sends a message to everyone on the team except the sender
     * @param msg the message to send
     * @param sender the Player who sent the message
     */
	public void sendMessage(String msg, Player sender) {
		for(Player p : peoples.values()) {
			if(p != sender) {
				p.sendMessage(msg);
			}
		}
	}
	
    /**
     * Sends a title message to everyone on the team
     * @param ttl the title text
     * @param sub the subtitle text
     * @param fadein the number of ticks to fade in over
     * @param hold the number of ticks to stay on for
     * @param fadeout the number of ticks to fade out over
     */
	public void sendTitle(String ttl, String sub, int fadein, int hold, int fadeout) {
		sendTitle(ttl, sub, fadein, hold, fadeout, null);
	}
    /**
     * Sends a title message to everyone on the team, except the message sender
     * @param ttl the title text
     * @param sub the subtitle text
     * @param fadein the number of ticks to fade in over
     * @param hold the number of ticks to stay on for
     * @param fadeout the number of ticks to fade out over
     * @param sender the Player who sent the message
     */
	public void sendTitle(String ttl, String sub, int fadein, int hold, int fadeout, Player sender) {
		for(Player p : peoples.values()) {
			if(p != sender) {
				p.sendTitle(ttl, sub, fadein, hold, fadeout);
			}
		}
	}

    /**
     * Removes all players from the team
     */
	public void clearPlayers() {
		peoples.clear();
		uuids.clear();
		foundBlock.clear();
	}
	
    /**
     * Gets a player's name from the team cache. Useful for getting info on offline players
     * @param u the UUID of the player
     * @return the player's name
     */
	public String getPlayerName(UUID u) {
		if(uuids.get(u) != null) {
			return uuids.get(u);
		} else {
			OfflinePlayer op = Bukkit.getOfflinePlayer(u);
			if(op != null) {
				return op.getName();
			} else {
				return u.toString();
			}
		}
		
	}

    @Override
    /**
     * Gets the team as a string. Just shows the team's name.
     * @return the team as a string
     */
	public String toString() {
		return "Team[" + name + "]";
	}
}
