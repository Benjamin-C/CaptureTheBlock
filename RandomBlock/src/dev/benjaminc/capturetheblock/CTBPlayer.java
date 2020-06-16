package dev.benjaminc.capturetheblock;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CTBPlayer {
	
	private Material target;
	private boolean found;
	private boolean givenup;
	private String name;
	
	private List<Player> peoples;

	public CTBPlayer() {
		peoples = new ArrayList<Player>();
	}
	
	public Material getTarget() {
		return target;
	}

	public void setTarget(Material target) {
		this.target = target;
	}

	public boolean isFound() {
		return found;
	}

	public void setFound(boolean found) {
		this.found = found;
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
	
	public void addPerson(Player Player) {
		peoples.add(Player);
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
	
}
