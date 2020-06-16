package dev.benjaminc.capturetheblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import net.md_5.bungee.api.ChatColor;

public class CTBMain extends JavaPlugin {
	
	/** The {@link List} of all {@link Material} that could be selected.
	 * This is populated from the config. */
	private List<Material> blocks;
	
	/** The {@link Map} of {@link UUID} to {@link Material} of what is assigned to the players */
	private Map<UUID, Material> assignedBlock;
	/** The {@link Map} of {@link UUID} to {@link Boolean} of if players have found their block */
	private Map<UUID, Boolean> foundBlock;
	
	/** the {@link ChatColor} of the main text */
	private ChatColor maincolor = ChatColor.LIGHT_PURPLE;
	/** The {@link ChatColor} of the accented text */
	private ChatColor accentcolor = ChatColor.AQUA;
	/** the {@link ChatColor} if you found the block */
	private ChatColor gotcolor = ChatColor.GREEN;
	/** The {@link ChatColor} if you missed the block */
	private ChatColor missedcolor = ChatColor.RED;
	/** The {@link ChatColor} reset */
	private ChatColor colorreset = ChatColor.RESET;
	
	/** the final int ticks per second the server is expected to have */
	public static final int TPS = 20;
	/** The {@link BukkitScheduler} used to schedult tasks in the future */
	private BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
	/** The int id of the current game timer */
	private int timerid;
	/** The int time in seconds the game runs for. Loaded from config. */
	private int roundtime = 300;
	/** The int warning time before the round is over in seconds */
	private int roundwarn = 10;
	
	/** the {@link Random} used for random numbers */
	private Random rand;
	
	/** The {@link ScoreboardManager} to manage the scoreboards */
	private ScoreboardManager sbm;
	/** The {@link Scoreboard} that the game score is stored on */
	private Scoreboard board;
	
	private FileConfiguration cfg = this.getConfig();
	// Fired when plugin is first enabled
	
	// -----------------------------------------------
	// CONFIG
	// -----------------------------------------------
	
	/**
	 * Reloads the config file of the plugin.
	 */
	public void reloadMyConfig() {
		reloadConfig();
		loadMyConfig();
	}
	/**
	 * Loads my config file
	 */
	public void loadMyConfig() {
    	cfg = this.getConfig();
    	roundtime = cfg.getInt(Keys.CONFIG_ROUND_TIME);
    	roundwarn = cfg.getInt(Keys.CONFIG_WARN_TIME);
    	List<?> blkstr = cfg.getList(Keys.CONFIG_BLOCK_LIST);
    	blocks = new ArrayList<Material>();
    	for(Object o : blkstr) {
    		if(o instanceof String) {
    			blocks.add(Material.valueOf((String) o));
    		}
    	}
    	saveDefaultConfig();
	}
    
	// -----------------------------------------------
	// PLUGIN
	// -----------------------------------------------
	
	/**
	 * Fired with the plugin is enabled
	 */
    @Override
    public void onEnable() {
    	loadMyConfig();
    	sbm = Bukkit.getScoreboardManager();
    	board = sbm.getMainScoreboard();
    	rand = new Random();
    	assignedBlock = new HashMap<UUID, Material>();
    	foundBlock = new HashMap<UUID, Boolean>();
    	getServer().getPluginManager().registerEvents(new CTBEvent(this), this);
    	
    	this.getCommand(Keys.COMMAND_RANDOM_BLOCK_NAME).setExecutor(new RandomBlockCommand(this));
    	this.getCommand(Keys.COMMAND_CTB_NAME).setExecutor(new CTBGameCommand(this));
    	this.getCommand(Keys.COMMAND_CTB_NAME).setTabCompleter(new CTBCommandTabComplete());
    	
    	if(board.getObjective(Keys.SCORE_NAME) == null) {
    		board.registerNewObjective(Keys.SCORE_NAME, "dummy", Keys.SCORE_NAME);
    	}
    	board.getObjective(Keys.SCORE_NAME).setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }
    
    /*
     *  Fired when plugin is disabled
     */
    @Override
    public void onDisable() {
    	board.clearSlot(DisplaySlot.PLAYER_LIST);
    }
    
	// -----------------------------------------------
	// BLOCKS
	// -----------------------------------------------
    
    /**
     * Gets a random block to use
     * @return the {@link Material} of the random block from the blocks list.
     */
    protected Material getRandomBlock() {
    	return blocks.get(rand.nextInt(blocks.size()));
    }
    /**
     * Assignes a block to a player
     * @param uuid the {@link UUID} of the player
     * @param mat the {@link Material} of the block
     */
    protected void assignBlock(UUID uuid, Material mat) {
    	assignedBlock.put(uuid, mat);
    	foundBlock.put(uuid, false);
    }
    /**
     * Gets the block assigned to a player
     * @param uuid the {@link UUID} of the player
     * @return the {@link Material} assigned to the player
     */
    protected Material getBlock(UUID uuid) {
    	if(assignedBlock.containsKey(uuid)) {
    		return assignedBlock.get(uuid);
    	}
    	return null;
    }
    /**
     * Marks that a player found their block and sends the message
     * Also starts the next round if everyone has found their block
     * @param p	the {@link Player} who found their block
     */
    protected void foundBlock(Player p) {
    	foundBlock.put(p.getUniqueId(), true);
    	Score s = getScore(p.getName());
    	s.setScore(s.getScore() + 1);
    	for(Player player : Bukkit.getOnlinePlayers()) {
            if(player == p){
            	p.sendMessage(maincolor + Strings.YOU_FOUND_BLOCK + colorreset);
            } else {
            	player.sendMessage(maincolor + p.getName() + " " + Strings.THEY_FOUND_BLOCK + maincolor);
            }
        }
    	
    	
    	if(hasEveryoneFoundBlock()) {
			startRound();
		}
    }
    /**
     * Checks if a player has found their block
     * @param uuid the {@link UUID} of the player
     * @return boolean of if the player found their block
     */
    protected boolean hasFoundBlock(UUID uuid) {
    	if(foundBlock.containsKey(uuid)) {
    		return foundBlock.get(uuid);
    	}
    	return false;
    }
    /**
     * Checks if everyone has found their block
     * @return the boolean of if everyone has found their block
     */
    protected boolean hasEveryoneFoundBlock() {
    	boolean found = true;
    	for(UUID u : foundBlock.keySet()) {
    		found &= foundBlock.get(u);
    	}
    	return found;
    }
    /**
     * Lists all blocks that can be selected
     * @return the String of all blocks, indexed and seperated by newlines.
     */
    public String listAllBlocks() {
    	String l = "";
    	for(int i = 0; i < blocks.size(); i++) {
    		l += i + " " + blocks.get(i) + "\n";
    	}
    	return l;
    }
    
	// -----------------------------------------------
	// GAME CONTROL
	// -----------------------------------------------
    
    /**
     * Prints the start of game messages, and starts a round
     */
    protected void startGame() {
    	Bukkit.broadcastMessage(maincolor + Strings.GAME_BEGUN + colorreset);
		Bukkit.broadcastMessage(maincolor + Strings.GAME_INFO + colorreset);
		startRound();
    }
    /**
     * Starts a round of the game
     */
    protected void startRound() {
    	stopTimer();
    	showScores(true);
    	
    	for(Player pl : getPlayers()) {
    		Material mat = getRandomBlock();
			pl.sendMessage(maincolor + Strings.YOUR_SCORE_IS + " " + board.getObjective(Keys.SCORE_NAME).getScore(pl.getName()).getScore() + colorreset);
			pl.sendMessage(maincolor + Strings.NOW_STAND_ON + " " + accentcolor + mat.name() + colorreset);
			pl.sendTitle(maincolor + Strings.FIND + " " + accentcolor + mat.name() + colorreset, maincolor + Strings.YOU_HAVE + " " + accentcolor + roundtime + maincolor + " " + Strings.SECONDS + "." + colorreset, 20, 200, 20);
			assignBlock(pl.getUniqueId(), mat);
		}
    	for(Player pl : getSpectators()) {
    		pl.sendTitle(maincolor + Strings.STARTING_ROUND + colorreset, null, 20, 200, 20);
    	}
    	for(Player pl : getTeammates()) {
    		pl.sendTitle(maincolor + Strings.STARTING_ROUND + colorreset, null, 20, 200, 20);
    	}
    	CTBMain me = this;
        timerid = scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override public void run() {
            	Bukkit.broadcastMessage(maincolor + "" + roundwarn + " " + Strings.SECONDS + "!" + colorreset);
            	for(Player pl : getServer().getOnlinePlayers()) {
            		pl.sendTitle(maincolor + "" + roundwarn + colorreset, (hasFoundBlock(pl.getUniqueId()) || isSpectator(pl)) ? null : Strings.BETTER_HURRY + "!", 0, 20, 5);
            	}
            	timerid = scheduler.scheduleSyncDelayedTask(me, new Runnable() {
                	@Override public void run() {
						startRound();
					}
                }, roundwarn*TPS);
            }
        }, (roundtime-roundwarn)*TPS);
    }
    /**
     * Stops the game timer, if it is running
     */
    private void stopTimer() {
    	if(scheduler.isQueued(timerid)) {
    		scheduler.cancelTask(timerid);
    	}
    }
    /**
     * Ends a round by stopping the timer, broadcasting a game over message, and showing the scores
     */
    public void endRound() {
    	stopTimer();
    	Bukkit.broadcastMessage(maincolor + Strings.GAME_OVER + colorreset);
    	showScores(true);
	}
    
	// -----------------------------------------------
	// SCORE
	// -----------------------------------------------
    
    /**
     * Gets the score of a player
     * @param player the {@link Player} to check
     * @return the {@link Score} score
     */
    protected Score getScore(String player) {
    	return board.getObjective(Keys.SCORE_NAME).getScore(player);
    }
    /**
     * Gets a string representation of the scores
     * @param showBlocks the boolean of wether to show the currently assigned block or not
     * @return the String of the scores
     */
    public String showScoresStr(boolean showBlocks) {
    	Map<UUID, Integer> scoremap = new HashMap<UUID, Integer>();
    	for(Player p : getPlayers()) {
    		scoremap.put(p.getUniqueId(), getScore(p.getName()).getScore());
    	}
    	Map<UUID, String> msgmap = new HashMap<UUID, String>();
    	for(Player p : getPlayers()) {
    		UUID uuid = p.getUniqueId();
    		
    		String name = p.getName();
    		int sc = getScore(name).getScore();
    		String scstr = ((hasFoundBlock(uuid)) ? gotcolor : missedcolor) + "" + sc + "-" + name + ((showBlocks) ? ": " + ((getBlock(uuid) != null) ? getBlock(uuid).name() : Strings.NOTHING) : "") + colorreset;
    		msgmap.put(uuid, scstr);
    	}
    	
    	// Add some fake scores for testing
//    	for(int i = 0; i < 3; i++) {
//    		UUID u = UUID.randomUUID();
//    		scoremap.put(u, i+3);
//    		msgmap.put(u, cc + "FakePlayer" + (i+3) + cr);
//    	}
    	
    	Object uuids[] = scoremap.keySet().toArray();
    	boolean sorted = false;
    	while(!sorted) {
    		sorted = true;
    		for(int i = 0; i < uuids.length-1; i++) {
    			if(scoremap.get(uuids[i]) < scoremap.get(uuids[i+1])) {
    				Object uu = uuids[i];
    				uuids[i] = uuids[i+1];
    				uuids[i+1] = uu;
    				sorted = false;
    			}
    		}
    	}
    	
    	String scorestr = maincolor + Strings.PLAYER_SCORES + "\n" + colorreset;
    	for(int i = 0; i < uuids.length; i++) {
    		scorestr += msgmap.get(uuids[i]) + "\n";
    	}
    	scorestr += maincolor + "--- --- --- --- ---\n" + colorreset;
    	return scorestr;
    }
    
    /**
     * Sends the scores to all players.
     * @param showBlocks the boolean to show assigned blocks, always shows assigned blocks to spectators
     */
    protected void showScores(boolean showBlocks) {
    	String plstr = showScoresStr(showBlocks);
    	for(Player p : getPlayers()) {
    		p.sendMessage(plstr);
    	}
    	for(Player p : getTeammates()) {
    		p.sendMessage(plstr);
    	}
    	String spstr = showScoresStr(true);
    	for(Player p : getSpectators()) {
    		p.sendMessage(spstr);
    	}
    }
    /**
     * Resets everyone's scores
     */
    protected void resetScores() {
    	for(Player p : getPlayers()) {
    		board.getObjective(Keys.SCORE_NAME).getScore(p.getName()).setScore(0);
    	}
    }
    
	// -----------------------------------------------
	// PLAYERS
	// -----------------------------------------------
    
    /**
     * Gets all players in the CTB game. This is not the same as {@link Bukkit#getOnlinePlayers()} because it omitts spectators
     * @return the {@link Collection} of all {@link Player} in the game
     */
    protected Collection<Player> getTeammates() {
    	List<Player> peoples = new ArrayList<Player>();
    	for(Player p : Bukkit.getOnlinePlayers()) {
    		if(!p.hasPermission(Keys.PERMISSION_TEAMMATE)) {
    			peoples.add(p);
    		}
    	}
    	return peoples;
    }
    /**
     * Gets all players in the CTB game. This is not the same as {@link Bukkit#getOnlinePlayers()} because it omitts spectators
     * @return the {@link Collection} of all {@link Player} in the game
     */
    protected Collection<Player> getPlayers() {
    	List<Player> peoples = new ArrayList<Player>();
    	for(Player p : Bukkit.getOnlinePlayers()) {
    		if(!p.hasPermission(Keys.PERMISSION_SPECTATE) && !p.hasPermission(Keys.PERMISSION_TEAMMATE)) {
    			peoples.add(p);
    		}
    	}
    	return peoples;
    }
    /**
     * Gets all spectators to the CTB game. This does not mean that their gamemode is spectator
     * @return the {@link Collection} of all {@link Player} who are spectating the game
     */
    protected Collection<Player> getSpectators() {
    	List<Player> peoples = new ArrayList<Player>();
    	for(Player p : Bukkit.getOnlinePlayers()) {
    		if(p.hasPermission(Keys.PERMISSION_SPECTATE)) {
    			peoples.add(p);
    		}
    	}
    	return peoples;
    }
    /**
     * Checks if a player is spectating the game This does not mean the player is in spectator mode
     * @param p the {@link Player} to check
     * @return the boolean of is the player is a spectator
     */
    protected boolean isSpectator(Player p) {
    	return p.hasPermission(Keys.PERMISSION_SPECTATE);
    }
}
