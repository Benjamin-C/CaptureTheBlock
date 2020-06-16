package dev.benjaminc.capturetheblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	
	// CONFIGURED VALUES
	/** The {@link List} of all {@link Material} that could be selected. */
	private List<Material> blocks;
	/** The int time in seconds the game runs for. Loaded from config. */
	private int roundtime = 300;
	/** The int warning time before the round is over in seconds */
	private int roundwarn = 10;
	
	/** The {@link List} of {@link Team} in the game */
	private Map<String, Team> teams;
	
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
    	teams = new HashMap<String, Team>();
//    	assignedBlock = new HashMap<UUID, Material>();
//    	foundBlock = new HashMap<UUID, Boolean>();
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
     * Marks that a player found their block and sends the message
     * Also starts the next round if everyone has found their block
     * @param p	the {@link Player} who found their block
     */
    protected void foundBlock(Player pl) {
    	Team p = findTeam(pl);
    	Score s = getScore(p.getName());
    	s.setScore(s.getScore() + 1);
    	pl.sendMessage(maincolor + Strings.YOU_FOUND_BLOCK + colorreset);
    	sendAllMsg(maincolor + p.getName() + " " + Strings.THEY_FOUND_BLOCK + maincolor);
    	
    	if(hasEveryoneFoundBlock()) {
			startRound();
		}
    }
    
    // TODO add javadoc
    protected Team findTeam(Player p) {
    	for(Team t : teams.values()) {
    		if(t.ifContainsPlayer(p)) {
    			return t;
    		}
    	}
    	return null;
    }
    /**
     * Checks if everyone has found their block
     * @return the boolean of if everyone has found their block
     */
    protected boolean hasEveryoneFoundBlock() {
    	boolean found = true;
    	for(Team p : teams.values()) {
    		found &= p.hasEveryoneFound();
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
    	sendAllMsg(maincolor + Strings.GAME_BEGUN + colorreset);
		sendAllMsg(maincolor + Strings.GAME_INFO + colorreset);
		startRound();
    }
    /**
     * Starts a round of the game
     */
    protected void startRound() {
    	stopTimer();
    	showScores(true);
    	
    	for(Team t : teams.values()) {
    		Material mat = getRandomBlock();
			t.sendMessage(maincolor + Strings.YOUR_SCORE_IS + " " + getScore(t.getName()).getScore() + colorreset);
			t.sendMessage(maincolor + Strings.NOW_STAND_ON + " " + accentcolor + mat.name() + colorreset);
			t.sendTitle(maincolor + Strings.FIND + " " + accentcolor + mat.name() + colorreset, maincolor + Strings.YOU_HAVE + " " + accentcolor + roundtime + maincolor + " " + Strings.SECONDS + "." + colorreset, 20, 200, 20);
			t.setTarget(mat);
		}
    	for(Player pl : getSpectators()) {
    		pl.sendTitle(maincolor + Strings.STARTING_ROUND + colorreset, null, 20, 200, 20);
    	}
    	CTBMain me = this;
        timerid = scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override public void run() {
            	sendAllMsg(maincolor + "" + roundwarn + " " + Strings.SECONDS + "!" + colorreset);
            	for(Player p : getSpectators()) {
            		p.sendTitle(maincolor + "" + roundwarn + colorreset, null, 0, 20, 5);
            	}
            	for(Team t : teams.values()) {
            		t.sendTitle(maincolor + "" + roundwarn + colorreset, t.hasEveryoneFound() ? null : Strings.BETTER_HURRY + "!", 0, 20, 5);
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
    	sendAllMsg(maincolor + Strings.GAME_OVER + colorreset);
    	showScores(true);
	}
    
	// -----------------------------------------------
	// SCORE
	// -----------------------------------------------
    
    /**
     * Gets the score of a player
     * @param teamname the {@link String} name of the team to check
     * @return the {@link Score} score
     */
    protected Score getScore(String teamname) {
    	return board.getObjective(Keys.SCORE_NAME).getScore(teamname);
    }
    /**
     * Gets a string representation of the scores
     * @param showBlocks the boolean of wether to show the currently assigned block or not
     * @return the String of the scores
     */
    public String showScoresStr(boolean showBlocks) {
    	Map<String, Integer> scoremap = new HashMap<String, Integer>();
    	for(Team t : teams.values()) {
    		scoremap.put(t.getName(), getScore(t.getName()).getScore());
    	}
    	Map<String, String> msgmap = new HashMap<String, String>();
    	for(Team t : teams.values()) {
    		String name = t.getName();
    		int sc = getScore(name).getScore();
    		String scstr = (t.hasEveryoneFound() ? gotcolor : missedcolor) + "" + sc + "-" + name + ((showBlocks) ? ": " + ((t.getTarget() != null) ? t.getTarget().name() : Strings.NOTHING) : "") + colorreset;
    		msgmap.put(name, scstr);
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
    	for(Team t : teams.values()) {
    		t.sendMessage(plstr);
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
    	for(Team t : teams.values()) {
    		board.getObjective(Keys.SCORE_NAME).getScore(t.getName()).setScore(0);
    	}
    }
    
	// -----------------------------------------------
	// PLAYERS
	// -----------------------------------------------
    
    // TODO add javadoc
    protected boolean addTeam(String name) {
    	if(!teams.containsKey(name)) {
    		teams.put(name, new Team(name));
    		return true;
    	}
    	return false;
    }
    // TODO add javadoc
    protected boolean removeTeam(String name) {
    	if(teams.containsKey(name)) {
    		teams.remove(name);
    		return true;
    	}
    	return false;
    }
    // TODO add javadoc
    protected boolean joinTeam(Player p, String name) {
    	if(teams.containsKey(name)) {
    		teams.get(name).addPerson(p);
    		return true;
    	}
    	return false;
    }
    // TODO add javadoc
    protected Map<String, Team> getAllTeams() {
    	return teams;
    }
    // TODO add javadoc
    protected String listTeam(Team t) {
    	String team = t.getName();
    	for(Player p : t.getPeoples()) {
    		team += "\n  " + p.getName();
    	}
    	return team;
    }
    
    // TODO javadoc
    protected boolean leaveTeam(Player p) {
    	Team t = findTeam(p);
    	if(t != null) {
    		t.removePerson(p);
    		return true;
    	}
    	return false;
    }
    
    // TODO add javadoc
    protected void sendAllMsg(String msg) {
    	for(Team t : teams.values()) {
    		t.sendMessage(msg);
    	}
    	for(Player p : getSpectators()) {
    		p.sendMessage(msg);
    	}
    }
    
    // TODO add javadoc
    protected void sendAllTitle(String ttl, String sub, int fadein, int hold, int fadeout) {
    	for(Team t : teams.values()) {
    		t.sendTitle(ttl, sub, fadein, hold, fadeout);
    	}
    	for(Player p : getSpectators()) {
    		p.sendTitle(ttl, sub, fadein, hold, fadeout);
    	}
    }
    
    /**
     * Gets all spectators to the CTB game. This does not mean that their gamemode is spectator
     * @return the {@link Collection} of all {@link Player} who are spectating the game
     */
    protected Collection<Player> getSpectators() {
    	List<Player> peoples = new ArrayList<Player>();
    	for(Player p : Bukkit.getOnlinePlayers()) {
    		if(isSpectator(p)) {
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
