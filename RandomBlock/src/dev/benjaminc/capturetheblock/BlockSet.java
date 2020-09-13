package dev.benjaminc.capturetheblock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class BlockSet {
	
	// TODO allow removing of blocks
	
	private CTBMain plugin;
	
	private String name;
	private List<Material> addBlocks;
//	private List<Material> removeBlocks;
	private List<String> addSet;
//	private List<BlockSet> removeSet;
	
	public BlockSet(CTBMain plugin, String name, List<Material> addBlocks, List<String> addSet) {
		super();
		this.name = name;
		this.plugin = plugin;
		if(addBlocks == null) {
			this.addBlocks = new ArrayList<Material>();
		} else {
			this.addBlocks = addBlocks;
		}
		if(addSet == null) {
			this.addSet = new ArrayList<String>();
		} else {
			this.addSet = addSet;
		}
		
//		this.remove = remove;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Material> getBlocks() {
		return addBlocks;
	}

	public void setBlocks(List<Material> blocks) {
		this.addBlocks = blocks;
	}

	public List<String> getAdd() {
		return addSet;
	}

	public void setAdd(List<String> add) {
		this.addSet = add;
	}

	public void addBlock(Material m) {
		addBlocks.add(m);
	}
	
	public void addSet(String set) {
		addSet.add(set);
	}
//	public List<BlockSet> getRemove() {
//		return remove;
//	}

//	public void setRemove(List<BlockSet> remove) {
//		this.remove = remove;
//	}

	public List<Material> getAllBlocks() {
		return getAllBlocks(null);
	}
	public List<Material> getAllBlocks(List<String> usedSets) {
		if(usedSets == null) {
			usedSets = new ArrayList<String>();
		}
		Set<Material> mat = new HashSet<Material>();
		if(addBlocks != null) {
			mat.addAll(addBlocks);
		}
		if(!usedSets.contains(name)) {
			usedSets.add(name);
			if(addSet != null) {
				for(String s : addSet) {
					if(plugin.getAllSets().containsKey(s)) {
						mat.addAll(plugin.getAllSets().get(s).getAllBlocks(usedSets));
					} else {
						plugin.sendDebugMessage("Error loading " + name + ": Subet " + s + " does not exist");
					}
				}
			}
//			if(remove != null) {
//				for(BlockSet s : remove) {
//					mat.removeAll(s.getAllBlocks(usedSets));
//				}
//			}
		}
		return new ArrayList<Material>(mat);
	}
	
	@Override
	public String toString() {
		String s = ChatColor.RED + name + ChatColor.RESET + "\n";
		for(String st : addSet) {
			s += ChatColor.AQUA + "  " + st + "\n"; 
		}
		for(Material m : addBlocks) {
			s += ChatColor.GREEN + "  " + m + "\n"; 
		}
		return s.substring(0,s.length()-("\n").length());
	}
}
