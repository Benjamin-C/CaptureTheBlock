package dev.orangeben.capturetheblock;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import peterTimer.TimeRunnable;
import peterTimer.Timer;

// TODO switch player timers when changing teams
// TODO change title bar wording after you find block
public class CTBMain extends JavaPlugin {
	
    /** Me, for use in runnables */
	CTBMain me = this;
	
	// CONFIGURED VALUES
	/** The {@link List} of all {@link Material} that could be selected. */
	private Map<String, BlockSet> allSets;
	// TODO add javadoc
	private List<Material> activeBlocks;
	// TODO add javadoc
	private List<String> enabledSets;
	
	/** The int time in seconds the game runs for. Loaded from config. */
	private int roundtime = 300;
	/** The int warning time before the round is over in seconds */
	private int roundwarn = 10;
	
	// TODO add javadoc
	private int roundcount = 0;
	/** The {@link List} of {@link Team} in the game */
	private Map<String, Team> teams;
	
	/** the final int ticks per second the server is expected to have. Should never change */
	public static final int TPS = 20;
	// TODO add javadoc
	private Timer gameTimer;
	private int messageTaskId;
	
	/** the {@link Random} used for random numbers */
	private Random rand;
	
	/** The number of rounds left in the game after the current round. -1 to disable */
    private int roundsLeft = -1;
    /** The time at which the current round will be the final round */
	private LocalDateTime endTime = null;
	
    /** Prefix for timer titles */
	private String titlePrefix = "";
	
//	/** The {@link ScoreboardManager} to manage the scoreboards */
//	private ScoreboardManager sbm;
//	/** The {@link Scoreboard} that the game score is stored on */
//	private Scoreboard board;
	
    /** If the game is running */
	private boolean running = false;
	
    /** ISO date time formatter */
	public static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
	
    /** Should the plugin show more debug messages */
	private boolean debugmsgvisable = true;
	
    /** The time title messages will fade in for in ticks */
	private static final int TITLE_FADEIN = 10;
    /** The time title messages will stay for in ticks */
	private static final int TITLE_HOLD = 100;
    /** The time title messages will fade out for in ticks */
	private static final int TITLE_FADEOUT = 10;
	
    /**
     * Sets weather or not debug messages should be shown
     * @param visable New state
     */
	public void setDebugMsgVisable(boolean visable) {
		debugmsgvisable = visable;
	}
	
    /** Gets wether or not debug messages are shown */
	public boolean getDebugMsgVisable() {
		return debugmsgvisable;
	}

    /**
     * If the game is currently running
     * @return the current running status of the game
     */
	public boolean isRunning() {
		return running;
	}
	
    /** Plugin configuration file */
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
    	saveDefaultConfig();
    	
    	loadAllTeams();
    	for(Player p : Bukkit.getOnlinePlayers()) {
    		reconnectPlayer(p);
    	}
    	
    	loadAllBlocks();
    	
    	sendDebugMessage("I_WANT_A_MELLON");
	}
	
    /**
     * Gets all sets available to the game
     * @return all the sets available
     */
	public Map<String, BlockSet> getAllSets() {
		return allSets;
	}
	
	/**
     * Loads all block sets from their files
     */
	public void loadAllBlocks() {
		allSets = new HashMap<String, BlockSet>();
    	File folder = getDataFolder();
    	for(File f : folder.listFiles((File tf, String name) -> name.endsWith(Keys.FILE_BLOCKLIST_SUFFIX))) {
    		try {
	    		YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
	    		String lname = f.getName().substring(0, f.getName().indexOf("."));
	    		
	    		List<?> blkstr = c.getList(Keys.CONFIG_BLOCK_LIST);
	        	BlockSet set = new BlockSet(this, lname, null, null);
	        	for(Object o : blkstr) {
	        		if(o instanceof String) {
	        			String s = ((String) o).toUpperCase();
	        			if(s.startsWith("INCLUDE_")) {
	        				set.addSet(s.substring(s.indexOf("_") + 1, s.length()).toLowerCase());
	        			} else {
	        				set.addBlock(Material.valueOf(s));
	        			}
	        		}
	        	}
	        	allSets.put(lname, set);
    		} catch(Exception e) {
    			e.printStackTrace();
    			sendDebugMessage("Could not load file " + f.getName() + ". See log for details.");
    		}
    	}
	}
    
	/**
     * Enables a block set in the game
     * @param name the name of the set
     * @return if the set existed
     */
	public boolean enableSet(String name) {
		if(allSets.containsKey(name)) {
			if(!enabledSets.contains(name)) {
				enabledSets.add(name);
			}
			updatePossibleBlocks();
			return true;
		} else {
			return false;
		}
	}
	/**
     * Disables a block set in the game
     * @param name the name of the set
     * @return if the set existed
     */
    public boolean disableSet(String name) {
		if(enabledSets.contains(name)) {
			enabledSets.remove(name);
			updatePossibleBlocks();
			return true;
		} else {
			return false;
		}
	}
    /**
     * Updates the list of all possible blocks based on the currently enabled sets
     */
	public void updatePossibleBlocks() {
		activeBlocks.clear();
		for(String s : enabledSets) {
			activeBlocks.addAll(allSets.get(s).getAllBlocks());
		}
	}
	/**
     * Gets the list of all enabled sets
     * @return The list of all enabled sets
     */
	public List<String> getEnabledSets() {
		return enabledSets;
	}
	/**
     * Gets the list of all sets not currently enabled
     * @return The list of disabled sets
     */
	public List<String> getDisabledSets() {
		List<String> setstrs = new ArrayList<String>();
		for(String s : allSets.keySet()) {
			if(!enabledSets.contains(s)) {
				setstrs.add(s);
			}
		}
		return setstrs;
	}

	/**
     * Disable all sets
     */
    public void clearSets() {
		allSets.clear();
	}
	
	// -----------------------------------------------
	// PLUGIN
	// -----------------------------------------------
	
	/**
	 * Fired with the plugin is enabled
	 */
    @Override
    public void onEnable() {
    	rand = new Random();
    	teams = new HashMap<String, Team>();
    	
    	saveResource(Keys.FILE_DEFAULTBLOCKS_NAME, false);
    	
    	enabledSets = new ArrayList<String>();
    	activeBlocks = new ArrayList<Material>();
    	
    	loadMyConfig();

    	getServer().getPluginManager().registerEvents(new CTBEvent(this), this);
    	getCommand(Keys.COMMAND_RANDOM_BLOCK_NAME).setExecutor(new RandomBlockCommand(this));
    	getCommand(Keys.COMMAND_CTB_NAME).setExecutor(new CTBGameCommand(this));
    	getCommand(Keys.COMMAND_CTB_NAME).setTabCompleter(new CTBCommandTabComplete(this));
    }
    
    /*
     *  Fired when plugin is disabled
     */
    @Override
    public void onDisable() {
    	saveAllTeams();
    	if(gameTimer != null) {
    		gameTimer.stop();
    	}
    }
    
	// -----------------------------------------------
	// BLOCKS
	// -----------------------------------------------
    
    /**
     * Gets a random block to use
     * @return the {@link Material} of the random block from the blocks list.
     */
    protected Material getRandomBlock() {
    	if(activeBlocks.size() > 0) {
    		return activeBlocks.get(rand.nextInt(activeBlocks.size()));
    	} else {
    		Exception e = new Exception("There are no active blocks");
    		e.printStackTrace();
    		sendAdminMessage("Something goofed, activeBlocks was empty");
    		return Material.AIR;
    	}
    }

    /**
     * Marks that a player has lost their block and sends the message
     * @param p	the {@link Player} who lost their block
     * @param t the team of that player
     */
    protected void unfoundBlock(Player pl, Team t) {
        if(t.hasFound(pl.getUniqueId())) {
            pl.sendMessage(Strings.COLOR_MAIN + Strings.YOU_LOST_BLOCK + Strings.COLOR_RESET);
            sendAllMsg(Strings.COLOR_MAIN + pl.getName() + " " + Strings.THEY_HAS + " " + Strings.THEY_LOST_BLOCK + Strings.COLOR_RESET);
            if(t.hasScored()) {
                t.addScore(-1);
	    		sendAllMsg(Strings.COLOR_MAIN + t.getName() + " " + Strings.THEY_HAS + " " + Strings.THEY_ALL + " " + Strings.THEY_LOST_BLOCK + Strings.COLOR_RESET);
	    	}
            t.setFound(pl.getUniqueId(), false);
	    	gameTimer.removePlayer(pl, t.getName() + Keys.BOSSBAR_GOTBLOCK_SUFFIX);
	    	gameTimer.addPlayer(pl, t.getName());
	    	t.updateTimeBars(gameTimer, titlePrefix);
    	}
    }

    /**
     * Marks that a player found their block and sends the message
     * Also starts the next round if everyone has found their block
     * @param p	the {@link Player} who found their block
     */
    protected void foundBlock(Player pl, Team t) {
    	if(!t.hasFound(pl.getUniqueId())) {
            pl.sendMessage(Strings.COLOR_MAIN + Strings.YOU_FOUND_BLOCK + Strings.COLOR_RESET);
            sendAllMsg(Strings.COLOR_MAIN + pl.getName() + " " + Strings.THEY_HAS + " " + Strings.THEY_FOUND_BLOCK + Strings.COLOR_RESET);
            t.setFound(pl.getUniqueId(), true);
	    	t.updateTimeBars(gameTimer, titlePrefix);
	    	gameTimer.removePlayer(pl, t.getName());
	    	gameTimer.addPlayer(pl, t.getName() + Keys.BOSSBAR_GOTBLOCK_SUFFIX);
	    	if(t.hasScored()) {
	    		t.addScore(1);
	    		sendAllMsg(Strings.COLOR_MAIN + t.getName() + " " + Strings.THEY_HAS + " " + Strings.THEY_ALL + " " + Strings.THEY_FOUND_BLOCK + Strings.COLOR_RESET);
	    	}
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override public void run() {
					if(hasEveryoneFoundBlock()) {
						startRound();
					}
				}
			});
    	}
    }
    
    /**
     * Finds the team that a {@link Player} is on by their {@link UUID}
     * @param u the {@link UUID} of the {@link Player} to find
     * @return the {@link Team} they are on
     */
    protected Team findTeam(UUID u) {
    	for(Team t : teams.values()) {
    		if(t.isMember(u)) {
    			return t;
    		}
    	}
    	return null;
    }
    /**
     * Checks if all {@link Team} have found their block
     * @return the boolean of if everyone has found their block
     */
    protected boolean hasEveryoneFoundBlock() {
    	boolean found = true;
    	for(Team p : teams.values()) {
    		found &= p.hasEveryoneFound();
    	}
//    	Bukkit.broadcastMessage("Everyone found their block? " + found);
    	return found;
    }
    /**
     * Lists all blocks that can be selected
     * @return the String of all blocks, indexed and seperated by newlines.
     */
    public String listAllBlocks() {
    	String l = "";
    	for(BlockSet b : allSets.values()) {
    		l += b.toString() + "\n";
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
    	sendAllMsg(Strings.COLOR_MAIN + Strings.GAME_BEGUN + Strings.COLOR_RESET);
		sendAllMsg(Strings.COLOR_MAIN + Strings.GAME_INFO + Strings.COLOR_RESET);
		running = true;
		startRound();
    }
    
    /**
     * Generates a new block for a team
     * @param t the team to regen for
     */
    protected void regenTeamTargetBlock(Team t) {
    	Material mat = getRandomBlock();
		t.sendMessage(Strings.COLOR_MAIN + Strings.NOW_STAND_ON + " " + Strings.COLOR_ACCENT + mat.name() + Strings.COLOR_RESET);
		t.sendTitle(Strings.COLOR_MAIN + Strings.FIND + " " + Strings.COLOR_ACCENT + mat.name() + Strings.COLOR_RESET, Strings.COLOR_MAIN + Strings.YOU_HAVE + " " + Strings.COLOR_ACCENT + roundtime + Strings.COLOR_MAIN + " " + Strings.SECONDS + "." + Strings.COLOR_RESET, 20, 200, 20);
		t.setTarget(mat);
		t.clearFound();
		t.updateTimeBars(gameTimer, titlePrefix);
    }
    
    /**
     * Starts a round of the game
     */
    protected void startRound() {
    	if(teams.size() > 0) {
    		if(activeBlocks.size() <= 0) {
    			if(allSets.containsKey(Keys.DEFAULT_SET_NAME)) {
    				activeBlocks.addAll(allSets.get(Keys.DEFAULT_SET_NAME).getAllBlocks());
	    			sendAdminMessage(Strings.NO_SETS_USING_DEFAULT);
    			} else {
    				sendAdminMessage(Strings.NO_SETS_NO_DEFAULT);
    				return;
    			}
    		}
	    	stopTimer();
	    	showScores(true);
	    	boolean cont = false;
	    	if(endTime != null) {
                cont = true;
	    		if(endTime.isBefore(LocalDateTime.now())) {
                    endTime = null;
	    		}
	    	} else if(roundsLeft != 0) {
                if(roundsLeft != -1) {
                    roundsLeft--;
		    	}
		    	cont = true;
			}
			if(cont) {
                for(Team t : teams.values()) {
                    t.incrementRoundCount();
					t.sendMessage(Strings.COLOR_MAIN + Strings.YOUR_SCORE_IS + " " + t.getScore() + Strings.COLOR_RESET);
					regenTeamTargetBlock(t);
					if(roundsLeft != -1) {
						t.sendMessage(Strings.COLOR_MAIN + "There " + ((roundsLeft == 1) ? "is" : "are") + " " + Strings.COLOR_ACCENT + roundsLeft + Strings.COLOR_MAIN + " " + "round" + ((roundsLeft == 1) ? "" : "s") + " left." + Strings.COLOR_RESET);
					}

				}
		    	for(Player pl : getSpectators()) {
		    		pl.sendTitle(Strings.COLOR_MAIN + Strings.STARTING_ROUND + Strings.COLOR_RESET, null, TITLE_FADEIN, TITLE_HOLD, TITLE_FADEOUT);
		    	}
		    	titlePrefix = Strings.FIND_YOUR_BLOCK + " ";
		    	if(roundsLeft == 0) {
		    		titlePrefix = Strings.FINAL_ROUND + " ";
		    		messageTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		    			@Override public void run() {
		    				if(running) {
		    					msgFinalRound();
		    				}
		    			}
		    		},  TITLE_FADEIN+TITLE_HOLD+TITLE_FADEOUT);
		    	}
		    	Map<Integer, TimeRunnable> clbk = new HashMap<Integer, TimeRunnable>();
		    	final int thisroundwarn = roundwarn;
		    	clbk.put(thisroundwarn*TPS, new TimeRunnable() { // Warn the players time is almost up
	//	    		@Override
		    		public void run(Timer timer) {
		    			sendAllMsg(Strings.COLOR_MAIN + "" + thisroundwarn + " " + Strings.SECONDS + "!" + Strings.COLOR_RESET);
		            	for(Player p : getSpectators()) {
		            		p.sendTitle(Strings.COLOR_MAIN + "" + thisroundwarn + Strings.COLOR_RESET, null,  TITLE_FADEIN, TITLE_HOLD, TITLE_FADEOUT);
		            	}
		            	for(Team t : teams.values()) {
		            		t.sendTitle(Strings.COLOR_MAIN + "" + thisroundwarn + Strings.COLOR_RESET, t.hasEveryoneFound() ? null : Strings.BETTER_HURRY + "!", TITLE_FADEIN, TITLE_HOLD, TITLE_FADEOUT);
		            	}
		    		}
		    	});
		    	clbk.put(0, new TimeRunnable() { // When the timer is done
	//	    		@Override
		    		public void run(Timer timer) {
		    			startRound();
		    		}
		    	});

		    	gameTimer = new Timer(roundtime*TPS, Strings.FIND_YOUR_BLOCK + " ", clbk, this);
				
//		    	gameTimer.addAllPlayers();
		    	
		    	String ts = "";
		    	for(Team t : teams.values()) {
		    		ts += " " + t.getName() + ":" + t.getTarget();
		    		gameTimer.addBar(t.getName(), "");
		    		gameTimer.addBar(t.getName() + Keys.BOSSBAR_GOTBLOCK_SUFFIX, "");
		    		t.updateTimeBars(gameTimer, titlePrefix);
		    		gameTimer.addPlayer(new ArrayList<Player>(t.getOnlinePeoples()), t.getName());
		    	}
		    	gameTimer.setTitle(ts, "main");
		    	gameTimer.start();
		    	sendAdminMessage(Strings.GAME_STARTED);
	    	} else {
	    		endGame();
	    	}
    	} else {
    		for(Player p : getAdmins()) {
    			p.sendMessage(Strings.NO_TEAMS);
    		}
    	}
    }

    /**
     * Stops the game timer, if it is running
     */
    private void stopTimer() {
    	if(gameTimer != null) {
    		gameTimer.stop();
    	}
    }
    /**
     * Ends a round by stopping the timer, broadcasting a game over message, and showing the scores
     */
    public void endGame() {
    	stopTimer();
        roundsLeft = -1;
    	Bukkit.getScheduler().cancelTask(messageTaskId);
    	running = false;
    	sendAllTitle(Strings.COLOR_MAIN + Strings.GAME_OVER + Strings.COLOR_RESET, "", TITLE_FADEIN, TITLE_HOLD, TITLE_FADEOUT);
    	sendAllMsg(Strings.COLOR_MAIN + Strings.GAME_OVER + Strings.COLOR_RESET);
    	showScores(true);
	}
    
    /**
     * Sends the message telling all players that it is the final round
     */
    private void msgFinalRound() {
    	for(Team t : teams.values()) {
			t.sendTitle(Strings.COLOR_MAIN + Strings.FINAL_ROUND + Strings.COLOR_RESET, "", TITLE_FADEIN, TITLE_HOLD, TITLE_FADEOUT);
		}
    }
    /**
     * Sets the number of rounds left after this round
     * @param num the number of rounds
     */
    public void setRoundsLeft(int num) {
    	roundsLeft = num;
    	if(roundsLeft == 0 && running) {
    		msgFinalRound();
    	}
    }
    /**
     * Gets the number of rounds left after the current round
     * @return The number of rounds left
     */
    public int getRoundsLeft() {
    	return roundsLeft;
    }
    /**
     * Sets the time for the game to end at. The game will end at the first round end after this time 
     * @param end The game end time
     */
    public void setEndTime(LocalDateTime end) {
    	endTime = end;
    }

	// -----------------------------------------------
	// SCORE
	// -----------------------------------------------
    
    /**
     * Gets a string representation of the scores
     * @param showBlocks the boolean of wether to show the currently assigned block or not
     * @return the String of the scores
     */
    public String showScoresStr(boolean showBlocks) {
    	Map<String, Integer> scoremap = new HashMap<String, Integer>();
    	for(Team t : teams.values()) {
    		scoremap.put(t.getName(), t.getScore());
    	}
    	Map<String, String> msgmap = new HashMap<String, String>();
    	for(Team t : teams.values()) {
    		String name = t.getName();
    		String sc = t.getScore() + "/" + t.getRoundCount();
    		String scstr = (t.hasEveryoneFound() ? Strings.COLOR_GOT : Strings.COLOR_MISSED) + "" + sc + "-" + name + ((showBlocks) ? ": " + ((t.getTarget() != null) ? t.getTarget().name() : Strings.NOTHING) : "") + Strings.COLOR_RESET;
    		msgmap.put(name, scstr);
    	}
    	
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
    	
    	String scorestr = Strings.COLOR_MAIN + Strings.PLAYER_SCORES + "\n" + Strings.COLOR_RESET;
    	for(int i = 0; i < uuids.length; i++) {
    		scorestr += msgmap.get(uuids[i]) + "\n";
    	}
    	scorestr += Strings.COLOR_MAIN + "";
        for(int i = 0; i < Strings.PLAYER_SCORES.length(); i++) {
            scorestr += "-";
        }
        scorestr += "\n" + Strings.COLOR_RESET;
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
    		t.setScore(0);
    	}
    }
    
	// -----------------------------------------------
	// TEAMS & PLAYERS
	// -----------------------------------------------
    
   /**
    * Marks the {@link Player} connected to the server so that they get the timer and requested block
    * @param p the {@link Player} who connected
    */
    protected void reconnectPlayer(Player p) {
		Team t = findTeam(p.getUniqueId());
		if(t != null) {
			t.reconnectPerson(p);
			sendDebugMessage(p.getName() + " joined the game, and was put on team " + t.getName());
			if(gameTimer != null) {
				gameTimer.addPlayer(p, t.getName());
			}
		} else {
			sendDebugMessage(p.getName() + " joined the game and was not on a team");
			if(gameTimer != null) {
				gameTimer.addPlayer(p);
			}
		}
		
    }
    
    /**
     * Marks the {@link Player} disconnected from the server so they don't count against the team
     * @param p the player who is leaving
     */
    public void disconnectPlayer(Player p) {
		Team t = findTeam(p.getUniqueId());
		if(t != null) {
			t.disconnectPerson(p);
			sendDebugMessage(p.getName() + " left the game, and was taken from team " + t.getName());
		} else {
			sendDebugMessage(p.getName() + " left the game and was not on a team");
		}
	}
    
    /**
     * Gets the {@link File} for with info about a {@link Team}
     * @param name the {@link String} name of the team
     * @return the {@link File} for teh team, null if it doesn't exist
     */
    protected File getTeamFile(String name) {
    	return new File(getDataFolder(), name + Keys.FILE_TEAM_SUFFIX);
    }
    
    /**
     * Load all teams from files
     */
    protected void loadAllTeams() {
    	File folder = new File(getDataFolder().getAbsolutePath() + File.separatorChar + Bukkit.getServer().getWorlds().get(0).getName());
    	if(folder.exists()) {
	    	for(File f : folder.listFiles((File tf, String name) -> name.endsWith(Keys.FILE_TEAM_SUFFIX))) {
	    		YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
	        	String tname = c.getString(Keys.TEAM_NAME);
	        	Team t = new Team(tname);
	        	for(String u : c.getStringList(Keys.TEAM_MEMBERS)) {
	        		String pname = null;
	        		String ustr = u;
	        		if(u.contains("{") && u.contains("}")) {
	        			pname = u.substring(u.indexOf("{")+1, u.indexOf("}"));
	        			ustr = u.substring(0, u.indexOf("{"));
	        		}
	        		t.addPerson(UUID.fromString(ustr), pname);
	        	}
	        	t.setScore(c.getInt(Keys.TEAM_SCORE));
	        	t.setColor(c.getColor(Keys.TEAM_COLOR));
                t.setRoundCount(c.getInt(Keys.TEAM_ROUNDCOUNT));
	        	teams.put(tname, t);
	    	}
    	} else {
    		folder.mkdirs();
    	}
    }
    
    /**
     * Save all teams to files
     */
    protected void saveAllTeams() {
    	File folder = new File(getDataFolder().getAbsolutePath() + File.separatorChar + Bukkit.getServer().getWorlds().get(0).getName());
    	if(!folder.exists()) {
    		folder.mkdirs();
    	}
    	for(Team t : teams.values()) {
    		YamlConfiguration c = YamlConfiguration.loadConfiguration(getTeamFile(t.getName()));
    		c.set(Keys.TEAM_NAME, t.getName());
    		String uuids[] = new String[t.getAllPeoples().size()];
    		int i = 0;
    		for(UUID u : t.getAllPeoples().keySet()) {
    			uuids[i++] = u.toString() + "{" + t.getAllPeoples().get(u) + "}";
    		}
    		c.set(Keys.TEAM_MEMBERS, uuids);
    		c.set(Keys.TEAM_SCORE, t.getScore());
    		c.set(Keys.TEAM_COLOR, t.getColor());
    		c.set(Keys.TEAM_ROUNDCOUNT, t.getRoundCount());
    		try {
				c.save(getTeamFile(Bukkit.getServer().getWorlds().get(0).getName() + File.separatorChar + t.getName()));
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /**
     * Creates a new team and add it to the list of teams
     * @param name the {@link String} name of the team
     * @return the boolean of sucess, returns false if the team already exists1
     */
    protected boolean addTeam(String name) {
    	if(!teams.containsKey(name)) {
    		teams.put(name, new Team(name));
    		return true;
    	}
    	return false;
    }
    /**
     * Removes a team and deletes their team config file. May not complete other cleanup properly.
     * @param name the name of the team to remove
     * @return If the team existed and is now removed
     */
    protected boolean removeTeam(String name) {
    	if(teams.containsKey(name)) {
    		teams.remove(name);
    		getTeamFile(name).delete();
    		return true;
    	}
    	return false;
    }
    
    
    /**
     * Clears all players from a team
     * @param name the name of the team
     * @return if the team existed and was cleared
     */
    protected boolean clearTeam(String name) {
    	if(teams.containsKey(name)) {
    		teams.get(name).clearPlayers();
    		return true;
    	}
    	return false;
    }
    
    /**
     * Put a player on a team
     * @param p the player
     * @param name the team
     * @return if the player was added to a team
     */
    protected boolean joinTeam(Player p, String name) {
    	boolean ret = false;
    	for(Team t : teams.values()) {
    		if(t.getName().equals(name)) {
    			t.addPerson(p);
    			ret = true;
    		} else if(t.isMember(p.getUniqueId())) {
    			t.removePerson(p);
    		}
    	}
    	return ret;
    }
    
    /**
     * Gets all teams in the game
     * @return All the teams
     */
    protected Map<String, Team> getAllTeams() {
    	return teams;
    }
    
    /**
     * Gets the string list of a team
     * @param t the team to list
     * @return the listing
     */
    protected String listTeam(Team t) {
        return listTeam(t, false);
    }

    /**
     * Gets the String list of a team
     * @param t the team to list
     * @param showStatus If the listing should show the block found status of the team 
     * @return the listing
     */
    protected String listTeam(Team t, boolean showStatus) {
        if(t != null) {
            String team = "";
            if(showStatus) {
                team += (t.hasEveryoneFound()) ? ChatColor.GREEN : ((t.hasAnyoneFound()) ? ChatColor.GOLD : ChatColor.RED);
            }
            team += t.getName() + " (" + t.getOnlinePeoples().size() + "/" + t.getAllPeoples().size() + ")";
            if(showStatus) {
                team += ChatColor.RESET;
            }

            for(UUID u : t.getAllPeoples().keySet()) {
                if(showStatus) {
                    team += (t.hasFound(u)) ? ChatColor.GREEN : ChatColor.RED;
                }
                team += "\n  " + t.getPlayerName(u);
                if(!t.isOnline(u)) {
                    team += " (offline)";
                }
                if(showStatus) {
                    team += ChatColor.RESET;
                }
            }
            return team;
        } else {
            return "";
        }
    }
    
    /**
     * Removes a player from the team they are on
     * @param p the player to remove
     * @return if the player was removed from a team
     */
    protected boolean leaveTeam(Player p) {
    	Team t = findTeam(p.getUniqueId());
    	if(t != null) {
    		t.removePerson(p);
    		return true;
    	}
    	return false;
    }
    
    /**
     * Sends a message to all team members and spectators
     * @param msg the message to send
     */
    protected void sendAllMsg(String msg) {
    	for(Team t : teams.values()) {
    		t.sendMessage(msg);
    	}
    	for(Player p : getSpectators()) {
    		p.sendMessage(msg);
    	}
    }
    
    /**
     * Sends a title message to all team members and spectators
     * @param ttl The title
     * @param sub The subtitle
     * @param fadein The number of ticks to fade in for
     * @param hold The number of ticks to show the message for
     * @param fadeout The number of ticks to fade out for
     */
    protected void sendAllTitle(String ttl, String sub, int fadein, int hold, int fadeout) {
    	for(Team t : teams.values()) {
    		t.sendTitle(ttl, sub, fadein, hold, fadeout);
    	}
    	for(Player p : getSpectators()) {
    		p.sendTitle(ttl, sub, fadein, hold, fadeout);
    	}
    }
    
    /**
     * Sends a message to all admins
     * @param msg The message to send
     */
    public void sendAdminMessage(String msg) {
    	for(Player p : getAdmins()) {
    		p.sendMessage(msg);
    	}
    }
    
    /**
     * Sends a debug message, only if debug messages is enabled
     * @param msg The message
     */
    public void sendDebugMessage(String msg) {
    	if(debugmsgvisable) {
	    	for(Player p : getAdmins()) {
	    		p.sendMessage(msg);
	    	}
    	}
    }
    /**
     * Gets all admins to the CTB game
     * @return the {@link Collection} of all {@link Player} who are admins
     */
    protected Collection<Player> getAdmins() {
    	List<Player> peoples = new ArrayList<Player>();
    	for(Player p : Bukkit.getOnlinePlayers()) {
    		if(p.hasPermission(Keys.PERMISSION_CONTROL)) {
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
    	return p.hasPermission(Keys.PERMISSION_SPECTATE) && findTeam(p.getUniqueId()) == null;
    }
}
