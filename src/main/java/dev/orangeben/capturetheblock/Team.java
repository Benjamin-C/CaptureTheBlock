package dev.orangeben.capturetheblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.peter.petertimer.Timer;

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
    /** The number of fails the team has in a row */
	private int streak;

	// data to not save
    /** The block type the team is attempting to find, and which players have found it */
	private Map<Material, List<UUID>> targets;
    /** If the team has given up */
	private boolean givenup;
    /** All of the online players on the team */
	private Map<UUID, Player> peoples;
    /** The plugin this team is currently in */
    private CTBMain plugin;
    /** RNG source */
    private Random rand;
    /** Game timer for current game */
    private Timer timer;
	
	public Team(String name, CTBMain plugin) {
		this.name = name;
        this.plugin = plugin;

        rand = new Random(System.nanoTime());

		peoples = new HashMap<UUID, Player>();

		uuids = new HashMap<UUID, String>();
        targets = new HashMap<Material, List<UUID>>();
	}
	
    /**
     * Sets the timer the team uses
     * @param timer the new timer
     */
    public void setTimer(Timer timer) {
        this.timer = timer;
        for(UUID u : peoples.keySet()) {
            if(this.timer.getBar(u.toString()) == null) {
                this.timer.addBar(u.toString(), "");
                this.timer.addPlayer(peoples.get(u), u.toString());
            }
        }
    }

    /**
     * Updates the team's time bars, updating the remaining time and team status info
     * @param timer The timer to update in
     * @param titleprefix The prefix to go in the timer title
     */
	public void updateTimeBars(String titleprefix) {
		if(timer != null) {
            String ctstr = "";

            // Select color
            int gotnum = 0;
            if(hasEveryoneFoundAll()) {
                ctstr += plugin.getString("color.got");
                gotnum = peoples.size();
            } else {
                int somenum = 0;
                for(UUID p : peoples.keySet()) {
                    if(hasFoundAll(p)) {
                        gotnum++;
                    }
                    if(hasFoundAny(p)) {
                        somenum++;
                    }
                }
                if(somenum > 0) {
                    ctstr += plugin.getString("color.got.some");
                } else {
                    ctstr += plugin.getString("color.missed");
                }
            }
            ctstr += "(" + gotnum + "/" + peoples.size() + ")";

            for(Player p : peoples.values()) {
                String matsstr = "";
                boolean firstMat = true;
                for(Material m : targets.keySet()) {
                    if(firstMat) {
                        firstMat = false;
                    } else {
                        matsstr += plugin.getString("color.main") + ", ";
                    }
                    if(hasFound(p.getUniqueId(), m)) {
                        matsstr += plugin.getString("color.got") + plugin.matStr(m);
                    } else {
                        if(hasAnyoneFound(m)) {
                            matsstr += plugin.getString("color.got.some") + plugin.matStr(m);
                        } else {
                            matsstr += plugin.getString("color.missed") + plugin.matStr(m);
                        }
                    }
                }
                timer.setTitle(plugin.getString("color.main") + titleprefix + matsstr + " " + ctstr + plugin.getString("color.main"), p.getUniqueId().toString());
            }

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
	public List<Material> getTargets() {
        List<Material> res = new ArrayList<Material>();
        for(Material m : targets.keySet()) {
            res.add(m);
        }
		return res;
	}

    /**
     * Gets the nubmer of targets the team has
     * @return The number of targets
     */
    public int getTargetCount() {
        return targets.size();
    }

    /**
     * Adds a target to the team's target set
     * @param tgt the new target to add
     */
    public void addTarget(Material tgt) {
        targets.put(tgt, new ArrayList<UUID>());
    }

    /**
     * Removes a target from the team
     * @param tgt The target to remove
     * @return If the target existed or not
     */
    public boolean removeTarget(Material tgt) {
        if(targets.containsKey(tgt)) {
            targets.remove(tgt);
            return true;
        }
        return false;
    }

    /**
     * Clears the target list
     */
    public void clearTargets() {
        targets.clear();
    }

    /**
     * Gets wether the team has given up on this block or not
     * @return
     */
	public boolean isGivenup() {
		return givenup;
	}

    /**
     * Sets if the team has given up on these blocks
     * @param givenup
     */
	public void setGivenup(boolean givenup) {
		this.givenup = givenup;
	}

    /**
     * Gets a string of all materials the team needs
     * @return
     */
    public String getTargetString() {
        if(targets.size() > 0) {
            boolean firstMat = true;
            String matsstr = "";
            for(Material m : targets.keySet()) {
                if(firstMat) {
                    firstMat = false;
                } else {
                    matsstr += ", ";
                }
                matsstr += plugin.matStr(m);
            }
            return matsstr;
        } else {
            return plugin.getString("block.target.null");
        }
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
    	return hasEveryoneFoundAll();
	}

    /**
     * Checks if a specific player has found of their blocks
     * @param pl the UUID of the player
     * @return A map of blocks to found state
     */
    public Map<Material, Boolean> hasFound(UUID pl) {
		Map<Material, Boolean> map = new HashMap<Material, Boolean>();
        for(Material m : targets.keySet()) {
            map.put(m, targets.get(m).contains(pl));
        }
        return map;
	}

    /**
     * Checks if a player has found a specific block
     * @param pl the UUID of the player
     * @param tgt The specific block to check
     * @return True if they've found it, false if they haven't or it isn't a target
     */
    public boolean hasFound(UUID pl, Material tgt) {
        if(targets.containsKey(tgt)) {
            return targets.get(tgt).contains(pl);
        }
        return false;
    }

    /**
     * Checks if a player has found all of their targets
     * @param pl the UUID of the player
     * @return True if the player has, False if they haven't or they have no targets
     */
    public boolean hasFoundAll(UUID pl) {
        boolean all = targets.size() > 0;
        for(Material m : targets.keySet()) {
            all &= targets.get(m).contains(pl);
        }
        return all;
    }

        /**
     * Checks if a player has found any of their targets
     * @param pl the UUID of the player
     * @return True if the player has, False if they haven't or they have no targets
     */
    public boolean hasFoundAny(UUID pl) {
        for(Material m : targets.keySet()) {
            if(targets.get(m).contains(pl)) {
                return true;
            };
        }
        return false;
    }

    /**
     * If everyone who is online has found the given block
     * @param m The material to check for
     * @return If everyone has found their block
     */
	public boolean hasEveryoneFound(Material m) {
		for(UUID u : peoples.keySet()) {
			if(!hasFound(u, m)) {
				return false;
    		}
		}
		return true;
	}

    /**
     * If any online player has found the given block
     * @return If anyone has found their block
     */
    public boolean hasAnyoneFound(Material m) {
		for(UUID u : peoples.keySet()) {
			if(hasFound(u, m)) {
				return true;
    		}
		}
		return false;
	}

    /**
     * If any online player has found any block
     * @return If anyone has found their block
     */
    public boolean hasAnyoneFoundAny() {
		for(UUID u : peoples.keySet()) {
			if(hasFoundAny(u)) {
				return true;
    		}
		}
		return false;
	}

    /**
     * If all online player have found all their blocks
     * @return If everyone has found all targets
     */
    public boolean hasEveryoneFoundAll() {
        for(UUID u : peoples.keySet()) {
			if(!hasFoundAll(u)) {
				return false;
    		}
		}
		return true;
    }

    /**
     * Sets if a player has found their block. Does not perform any other possibly desired actions.
     * @param uuid The UUID of the player
     * @param found If they have found their block
     */
	public void setFound(UUID uuid, Material m, boolean found) {
        if(targets.containsKey(m)) {
            if(found) {
                targets.get(m).add(uuid);
            } else {
                targets.get(m).remove(uuid);
            }
        }
	}

    /**
     * Marks everyone has having not found their block regardless of if they are online right now
     */
    public void clearFound() {
        for(Material m : targets.keySet()) {
            targets.get(m).clear();
        }
	}

    /**
     * Gets the number of blocks a given player has found
     * @param p The UUID of the player
     * @return The number of found blocks
     */
    public int getFoundCount(UUID p) {
        int n = 0;
        for(List<UUID> l : targets.values()) {
            if(l.contains(p)) {
                n++;
            }
        }
        return n;
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

    /**
     * Adds a fail to the team's count
     */
    public void addStreak() {
        addStreak(1);
    }

    public void addStreak(int amount) {
        streak += amount;
    }

    public void subtractStreak(int amount) {
        streak -= amount;
    }

    /**
     * Gets the number of fails
     * @return the number of fails in a row
     */
    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public void checkStreak() {
        if(streak > 0) {
            if(!hasEveryoneFoundAll()) {
                streak = -1;
            } else {
                streak++;
            }
        } else if(hasEveryoneFoundAll()) {
            streak = 1;
        } else {
            streak--;
        }

        List<StreakReward> possible = new ArrayList<StreakReward>();
        for(StreakReward s : plugin.getRewards().values()) {
            if(((s.getStreakLength() > 0 && streak > 0) || (s.getStreakLength() <= 0 && streak <= 0)) && Math.abs(s.getStreakLength()) <= Math.abs(streak)) {
                possible.add(s);
            }
        }
        if(possible.size() > 0) {
            for(Player p : getOnlinePeoples()) {
                if(streak > 0) {
                    if(hasFoundAll(p.getUniqueId())) {
                        possible.get(rand.nextInt(possible.size())).giveTo(p);
                    }
                } else {
                    if(!hasFoundAll(p.getUniqueId())) {
                        possible.get(rand.nextInt(possible.size())).giveTo(p);
                    }
                }
            }
        }
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
	}
    /**
     * Adds a new player to the team. The player must be offline
     * @param u the UUID of the player
     * @param name the name of the player
     */
	public void addPerson(UUID u, String name) {
		uuids.put(u, name);
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
        if(timer != null) {
            timer.removeBar(p.getUniqueId().toString());
        }
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
        if(timer != null) {
            timer.addBar(p.getUniqueId().toString(), "");
            timer.addPlayer(p, p.getUniqueId().toString());
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
        // TODO remove from title bars
        for(UUID id : peoples.keySet()) {
            timer.removeBar(id.toString());
        }
		peoples.clear();
		uuids.clear();
        clearFound();
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
