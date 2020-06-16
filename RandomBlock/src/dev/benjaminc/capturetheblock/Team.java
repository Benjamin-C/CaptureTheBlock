package dev.benjaminc.capturetheblock;

import java.util.ArrayList;
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
	private Map<UUID, Boolean> foundBlock;
	
	public Team() {
		peoples = new ArrayList<Player>();
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

	public List<Player> getPeoples() {
		return peoples;
	}

	public void setPeoples(List<Player> peoples) {
		this.peoples = peoples;
	}
	
	public void addPerson(Player p) {
		peoples.add(p);
	}
	
	public void removePerson(Player p) {
		peoples.remove(p);
	}
	public String getName() {
		return name;
	}
	
	public boolean ifContainsPlayer(Player p) {
		return peoples.contains(p);
	}
	
	public void setName(String newName) {
		name = newName;
	}
	
	public void sendMessage(String msg) {
		for(Player p : peoples) {
			p.sendMessage(msg);
		}
	}
	
	public void sendTitle(String ttl, String sub, int fadein, int hold, int fadeout) {
		for(Player p : peoples) {
			p.sendTitle(ttl, sub, fadein, hold, fadeout);
		}
	}
	
}
