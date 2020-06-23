package dev.benjaminc.capturetheblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Team {
	
	private Material target;
	private boolean givenup;
	private String name;
	private List<Player> peoples;
	private Map<UUID, String> uuids;
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
    		found &= foundBlock.get(u);
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
		return uuids.get(u);
	}
}
