package dev.orangeben.capturetheblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
	private String name;
	private Map<UUID, String> uuids;
	private int score;
	private Color color;
	
	// data to not save
	private Material target;
	private boolean givenup;
	private Map<UUID, Player> peoples;
	private Map<UUID, Boolean> foundBlock;
	
	public Team(String name) {
		this.name = name;
		peoples = new HashMap<UUID, Player>();

		uuids = new HashMap<UUID, String>();
		foundBlock = new HashMap<UUID, Boolean>();
	}
	
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
				got_str += Strings.COLOR_MISSED;
			} else if(gotnum < peoples.size()) {
				got_str += Strings.COLOR_SOME_GOT;
			} else {
				got_str += Strings.COLOR_GOT;
			}
			got_str += gotnum + "/" + peoples.size();
			timer.setTitle(Strings.COLOR_MAIN + titleprefix + Strings.COLOR_MISSED + target + " " + got_str + Strings.COLOR_MAIN, name);
			timer.setTitle(Strings.COLOR_MAIN + titleprefix + ((gotnum == peoples.size()) ? Strings.COLOR_GOT : Strings.COLOR_SOME_GOT) + target + " " + got_str + Strings.COLOR_MAIN, name + Keys.BOSSBAR_GOTBLOCK_SUFFIX);
		}
	}
	
	public Material getTarget() {
		return target;
	}

	public void setTarget(Material target) {
		this.target = target;
	}

	public boolean hasScored() {
		if(peoples.size() < 1) {
			return false;
		}
    	return hasEveryoneFound();
	}
	public boolean hasEveryoneFound() {
		for(UUID u : peoples.keySet()) {
			if(!foundBlock.get(u)) {
				return false;
    		}
		}
		return true;
	}
    public boolean hasAnyoneFound() {
		for(UUID u : peoples.keySet()) {
			if(foundBlock.get(u)) {
				return true;
    		}
		}
		return false;
	}

	public void setFound(UUID uuid, boolean found) {
		foundBlock.put(uuid, found);
	}

    public void clearFound() {
		for(UUID u : foundBlock.keySet()) {
			foundBlock.put(u, false);
		}
	}

	public boolean isGivenup() {
		return givenup;
	}

	public void setGivenup(boolean givenup) {
		this.givenup = givenup;
	}

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
	
	public Map<UUID, String> getAllPeoples() {
		return uuids;
	}

	public void setScore(int score) {
		this.score = score;
	}
	public int getScore() {
		return score;
	}
	public void addScore(int add) {
		score += add;
	}
	public void subtractScore(int sub) {
		score -= sub;
	}
	public boolean isOnline(UUID u) {
        return peoples.containsKey(u);
	}
	
	// public void setPeoples(List<Player> peoples) {
	// 	this.peoples = peoples;
	// }
	
	public void addPerson(Player p) {
		peoples.put(p.getUniqueId(), p);
		uuids.put(p.getUniqueId(), p.getName());
		foundBlock.put(p.getUniqueId(), false);
	}
	public void addPerson(UUID u) {
		uuids.put(u, null);
		foundBlock.put(u, false);
	}
	public void addPerson(UUID u, String name) {
		uuids.put(u, name);
		foundBlock.put(u, false);
	}
	
	public void removePerson(Player p) {
		peoples.remove(p.getUniqueId());
		uuids.remove(p.getUniqueId());
	}
	
	public void disconnectPerson(Player p) {
		peoples.remove(p.getUniqueId());
	}
	public void reconnectPerson(Player p) {
		peoples.put(p.getUniqueId(), p);
		if(uuids.containsKey(p.getUniqueId()) ) {
			uuids.put(p.getUniqueId(), p.getName());
		}
	}
	
	public String getName() {
		return name;
	}

    public Player getPlayer(UUID u) {
        return peoples.get(u);
    }

	public boolean isMember(UUID u) {
		return uuids.containsKey(u);
	}
	
	public void setName(String newName) {
		name = newName;
	}
	
	public void sendMessage(String msg) {
		sendMessage(msg, null);
	}
	
	public void sendMessage(String msg, Player sender) {
		for(Player p : peoples.values()) {
			if(p != sender) {
				p.sendMessage(msg);
			}
		}
	}
	
	public void sendTitle(String ttl, String sub, int fadein, int hold, int fadeout) {
		sendTitle(ttl, sub, fadein, hold, fadeout, null);
	}
	public void sendTitle(String ttl, String sub, int fadein, int hold, int fadeout, Player sender) {
		for(Player p : peoples.values()) {
			if(p != sender) {
				p.sendTitle(ttl, sub, fadein, hold, fadeout);
			}
		}
	}
	
	@Override
	public String toString() {
		return "Team[" + name + "]";
	}

	public boolean hasFound(Player pl) {
		return hasFound(pl.getUniqueId());
	}

    public boolean hasFound(UUID pl) {
		return foundBlock.get(pl);
	}

	public void clearPlayers() {
		peoples.clear();
		uuids.clear();
		foundBlock.clear();
	}
	
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

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}	
}
