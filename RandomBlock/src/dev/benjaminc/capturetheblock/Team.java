package dev.benjaminc.capturetheblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Team {
	
	// data to save
	private String name;
	private Map<UUID, String> uuids;
	private int score;
	private Color color;
	
	// data to not save
	private Material target;
	private boolean givenup;
	private List<Player> peoples;
	private Map<UUID, Boolean> foundBlock;
	
	public Team(String name) {
		this.name = name;
		peoples = new ArrayList<Player>();
		uuids = new HashMap<UUID, String>();
		foundBlock = new HashMap<UUID, Boolean>();
	}
	
	public Material getTarget() {
		return target;
	}

	public void setTarget(Material target) {
		this.target = target;
	}

	public boolean hasEveryoneFound() {
		boolean found = true;
    	for(UUID u : foundBlock.keySet()) {
    		if(!foundBlock.get(u)) {
    			if(Bukkit.getServer().getPlayer(u) != null) {
    				found = false;
    			}
    		}
    	}
    	return found;
	}

	public void setFound(UUID uuid, boolean found) {
		foundBlock.put(uuid, found);
	}

	public boolean isGivenup() {
		return givenup;
	}

	public void setGivenup(boolean givenup) {
		this.givenup = givenup;
	}

	public List<Player> getOnlinePeoples() {
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
		for(Player p : peoples) {
			if(p.getUniqueId().equals(u)) {
				return true;
			}
		}
		return false;
	}
	
	public void setPeoples(List<Player> peoples) {
		this.peoples = peoples;
	}
	
	public void addPerson(Player p) {
		peoples.add(p);
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
		peoples.remove(p);
		uuids.remove(p.getUniqueId());
	}
	
	public void disconnectPerson(Player p) {
		peoples.remove(p);
	}
	public void reconnectPerson(Player p) {
		peoples.add(p);
		if(uuids.containsKey(p.getUniqueId()) ) {
			uuids.put(p.getUniqueId(), p.getName());
		}
	}
	
	public String getName() {
		return name;
	}
	
	public boolean ifContainsPlayer(Player p) {
		return peoples.contains(p);
	}
	public boolean ifContainsUUID(UUID u) {
		return uuids.containsKey(u);
	}
	
	public void setName(String newName) {
		name = newName;
	}
	
	public void sendMessage(String msg) {
		sendMessage(msg, null);
	}
	
	public void sendMessage(String msg, Player sender) {
		for(Player p : peoples) {
			if(p != sender) {
				p.sendMessage(msg);
			}
		}
	}
	
	public void sendTitle(String ttl, String sub, int fadein, int hold, int fadeout) {
		sendTitle(ttl, sub, fadein, hold, fadeout, null);
	}
	public void sendTitle(String ttl, String sub, int fadein, int hold, int fadeout, Player sender) {
		for(Player p : peoples) {
			if(p != sender) {
				p.sendTitle(ttl, sub, fadein, hold, fadeout);
			}
		}
	}
	
	@Override
	public String toString() {
		return "Team[" + name + "]";
	}

	public void clearFound() {
		for(UUID u : foundBlock.keySet()) {
			foundBlock.put(u, false);
		}
	}

	public boolean hasFound(Player pl) {
		return foundBlock.get(pl.getUniqueId());
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
