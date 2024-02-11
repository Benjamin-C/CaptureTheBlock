package dev.orangeben.capturetheblock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class BlockSet {
	
	/** The plugin this list is working in */
    private CTBMain plugin;
	
    /** The name of the list */
	private String name;
    /** The list of all blocks that are on this list */
	private List<Material> addBlocks;
    /** The lists that should also be included in this list */
	private List<String> addSet;
	
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
		
	}

    /**
     * Gets the name of the set
     * @return the name of the set
     */
	public String getName() {
		return name;
	}

    /**
     * Gets the blocks added directly to this set. Does not include blocks added only though other sets
     * @return The list of blocks
     */
	public List<Material> getBlocks() {
		return addBlocks;
	}

    /**
     * Gets the sets that are included with this set
     * @return the list of other sets
     */
	public List<String> getAdd() {
		return addSet;
	}

    /**
     * Adds a block to the list of included blocks
     * @param m The material type of the block to add
     */
	public void addBlock(Material m) {
		addBlocks.add(m);
	}
	
    /**
     * Adds another set to the list of sets to include
     * @param set The name of the other set
     */
	public void addSet(String set) {
		addSet.add(set);
	}

    /**
     * Removes a block to the list of included blocks. The block might still be included through included sets.
     * @param m The material type of the block to remove
     */
	public void removeBlock(Material m) {
		addBlocks.remove(m);
	}
	
    /**
     * Removes a set from the list of included sets. Blocks from that set might still be included through other included sets or included blocks.
     * Adds another set to the list of sets to include
     * @param set The name of the other set
     */
	public void removeSet(String set) {
		addSet.remove(set);
	}

    /**
     * Gets all blocks from the included blocks and all included sets
     * @return All the blocks
     */
	public List<Material> getAllBlocks() {
		return getAllBlocks(null);
	}
    /**
     * Gets all blocks from the included sets. Does not add blocks from sets named in usedSets, assuming they are included from somewhere else.
     * @param usedSets The list of previously used sets
     * @return All the blocks except some that shouldn't be
     */
	private List<Material> getAllBlocks(List<String> usedSets) {
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
		}
		return new ArrayList<Material>(mat);
	}
	
    /**
     * Shows a string representation of the set including all direct blocks and subsets
     * @return The string representation
     */
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
