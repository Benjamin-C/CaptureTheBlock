package dev.benjaminc.capturetheblock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CTBGameCommand implements CommandExecutor {

	CTBMain plugin;
	
	public CTBGameCommand(CTBMain p) {
		plugin = p;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		boolean isAdmin = sender.hasPermission(Keys.PERMISSION_CONTROL);
		String acts = Keys.COMMAND_CTB_SCORE;
		if(sender.hasPermission(Keys.PERMISSION_CONTROL)) {
			acts += ", " + Keys.COMMAND_CTB_START + ", "
					+ Keys.COMMAND_CTB_END + ", "
					+ Keys.COMMAND_CTB_RESET + ", "
					+ Keys.COMMAND_CTB_BLOCKS + ", "
					+ Keys.COMMAND_CTB_ALLBLOCKS;
		}
		
		if(args.length >= 1) {
			switch(args[0]) {
			case Keys.COMMAND_CTB_SCORE: {
				sender.sendMessage(plugin.showScoresStr(false));
				return true;
			}
			case Keys.COMMAND_CTB_TEAM: {
				if(args.length >= 2) {
					switch(args[1]) {
					case Keys.COMMAND_CTB_TEAM_JOIN: {
						if(args.length >= 3) {
							if(args.length >= 4 && isAdmin) { // If a player was specified
								Player pl = Bukkit.getPlayer(args[3]);
								if(pl != null) {
									boolean success = plugin.joinTeam(pl, args[2]);
									if(!success) {
										sender.sendMessage("That is not a team!");
									} else {
										sender.sendMessage(args[3] + " is now on " + args[2]);
									}
								} else {
									sender.sendMessage(args[3] + " is not a player and can not join the team");
								}
							} else { // If the sender is trying to join a team
								if(sender instanceof Player) {
									boolean success = plugin.joinTeam((Player) sender, args[2]);
									if(!success) {
										sender.sendMessage("That is not a team!");
									} else {
										sender.sendMessage("You have joined " + args[2]);
									}
								} else {
									sender.sendMessage("You are not a player, so you can not join a team");
								}
							}
						} else {
							sender.sendMessage("You need to specify what you want to do");
						}
						return true;
					}
					case Keys.COMMAND_CTB_TEAM_LEAVE: {
						// KICK SOMEONE ELSE OFF TEAM HERE
						if(args.length >= 3 && isAdmin) { // If a player was specified
							Player pl = Bukkit.getPlayer(args[2]);
							if(pl != null) {
								boolean success = plugin.leaveTeam(pl);
								if(!success) {
									sender.sendMessage("That player can not leave a team");
								} else {
									sender.sendMessage(args[2] + " has been removed from their team.");
								}
							} else {
								sender.sendMessage(args[2] + " is not a player and can not join the team");
							}
						} else { // If the sender is trying to leave a team
							if(sender instanceof Player) {
								boolean success = plugin.leaveTeam((Player) sender);
								if(!success) {
									sender.sendMessage("You can't leave a team!");
								} else {
									sender.sendMessage("You have been removed from your team");
								}
							} else {
								sender.sendMessage("You are not a player, so you can not leave a team");
							}
						}
						return true;
					}
					case Keys.COMMAND_CTB_TEAM_LIST: {
						List<Team> ts = new ArrayList<Team>();
						if(isAdmin) {
							ts.addAll(plugin.getAllTeams().values());
						} else {
							if(sender instanceof Player) {
								ts.add(plugin.findTeam((Player) sender));
							}
						}
						String msg = "CTB teams:\n";
						boolean first = true;
						for(Team t : ts) {
							if(first) {
								first = false;
							} else {
								msg += "\n";
							}
							msg += plugin.listTeam(t);
						}
						sender.sendMessage(msg);
						return true;
					}
					}
				}
			}
			}
			if(isAdmin) {
				switch(args[0]) {
				case Keys.COMMAND_CTB_START: {
					plugin.startGame();
					return true;
				}
				case Keys.COMMAND_CTB_ENDALT:
				case Keys.COMMAND_CTB_END: {
					plugin.endGame();
					return true;
				}
				case Keys.COMMAND_CTB_TOGGLEDEBUGMSG: {
					plugin.setDebugMsgVisable(!plugin.getDebugMsgVisable());
					sender.sendMessage("Debug messages are " + ((plugin.getDebugMsgVisable()) ? "now" : "no longer") + " visable");
					return true;
				}
				case Keys.COMMAND_CTB_FINAL: {
					if(args.length >= 2) {
						try {
							int left = Integer.parseInt(args[1]);
							if(left >= -1) {
								plugin.setRoundsLeft(left);
								sender.sendMessage("Ending after " + args[1] + " more rounds");
							} else {
								sender.sendMessage(args[1] + " is not a valid number of rounds. Number must be >= -1");
							}
							
						} catch(NumberFormatException e) {
							sender.sendMessage(args[1] + " is not a valid number of rounds");
						}
					} else {
						sender.sendMessage("Please specify a number of rounds");
					}
					return true;
				}
				case Keys.COMMAND_CTB_ENDAT: {
					if(args.length >= 2) {
						if(args.length > 2 && args[1].charAt(1) == ':') {
							args[1] = "0" + args[1];
						}
						if(args[1].equals("null")) {
							plugin.setEndTime(null);
							sender.sendMessage("End at time disabled");
						} else {
							try {
								LocalDateTime ldt = LocalDate.now().atTime(LocalTime.parse(args[1]));
								if(ldt.isAfter(LocalDateTime.now())) {
									plugin.setEndTime(ldt);
									if(plugin.getRoundsLeft() != -1) {
										plugin.setRoundsLeft(1);
									}
									sender.sendMessage("Continuing until just after " + ldt.format(CTBMain.formatter));
								} else {
									sender.sendMessage(args[1] + " is in the past, try a time in the future");
								}
								
							} catch(DateTimeParseException e) {
								sender.sendMessage(args[1] + " is not a valid end time");
							}
						}
					} else {
						sender.sendMessage("Please specify a time to end");
					}
					return true;
				}
				case Keys.COMMAND_CTB_RESET: {
					plugin.endGame();
					plugin.resetScores();
					return true;
				}
				case Keys.COMMAND_CTB_RELOADCONFIG: {
					plugin.reloadMyConfig();
					sender.sendMessage("Config reloaded. The game changes will take effect next new block.");
				} return true;
				case Keys.COMMAND_CTB_BLOCKS: {
					sender.sendMessage(plugin.showScoresStr(true));
					return true;
				}
				case Keys.COMMAND_CTB_ALLBLOCKS: {
					sender.sendMessage(plugin.listAllBlocks());
				}
				case Keys.COMMAND_CTB_TEAM: {
					if(args.length >= 2) {
						switch(args[1]) {
						case Keys.COMMAND_CTB_TEAM_ADD: {
							if(args.length >= 3) {
								plugin.addTeam(args[2]);
								sender.sendMessage("Team " + args[2] + " has been added");
							} else {
								sender.sendMessage("Please specify a team name to add");
							}
						} break;
						case Keys.COMMAND_CTB_TEAM_REMOVE: {
							if(args.length >= 3) {
								plugin.removeTeam(args[2]);
								sender.sendMessage("Team " + args[2] + " has been removed");
								return true;
							} else {
								sender.sendMessage("Please specify a team name to remove");
							}
						} break;
						case Keys.COMMAND_CTB_TEAM_CLEAR: {
							if(args.length >= 3) {
								plugin.clearTeam(args[2]);
								sender.sendMessage("Team " + args[2] + " has been removed");
								return true;
							} else {
								sender.sendMessage("Please specify a team name to remove");
							}
						} break;
						case Keys.COMMAND_CTB_TEAM_SCORE: {
							switch(args.length) {
							case 3: {
								sender.sendMessage("Please specify a team name to remove");
							} break;
							case 4: {
								sender.sendMessage("Please specify a score action <add|remove|set>");
							} break;
							default: {
								try {
									switch(args[3]) {
									case Keys.COMMAND_CTB_TEAM_SCORE_ADD: {
										plugin.getAllTeams().get(args[2]).addScore(Integer.parseInt(args[4]));
										sender.sendMessage("Added " + args[4] + " points to " + args[2]);
									} break;
									case Keys.COMMAND_CTB_TEAM_SCORE_REMOVE: {
										plugin.getAllTeams().get(args[2]).subtractScore(Integer.parseInt(args[4]));
										sender.sendMessage("Subtracted " + args[4] + " points from " + args[2]);
									} break;
									case Keys.COMMAND_CTB_TEAM_SCORE_SET: {
										plugin.getAllTeams().get(args[2]).setScore(Integer.parseInt(args[4]));
										sender.sendMessage("Set " + args[2] + "'s score to " + args[4]);
									} break;
									}
								} catch(NumberFormatException e) {
									sender.sendMessage(args[4] + " is not a valid number");
								}
								
							}
							}
						} break;
						case Keys.COMMAND_CTB_TEAM_ADDALL: {
							for(Player p : Bukkit.getOnlinePlayers()) {
								Collection<Player> specs = plugin.getSpectators();
								if(!specs.contains(p) && plugin.findTeam(p) == null) {
									if(plugin.findTeam(p.getUniqueId()) != null) {
										plugin.findTeam(p.getUniqueId()).addPerson(p);
									} else {
										plugin.addTeam(p.getName());
										plugin.joinTeam(p, p.getName());
									}
								}
							}
							sender.sendMessage("All players not on a team have been added to a team");
						} break;
						}
						return true;
					}
				}
				case Keys.COMMAND_CTB_SET: {
					if(args.length > 1) {
						switch(args[1]) {
						case Keys.COMMAND_CTB_SET_ADD: {
							if(args.length > 2) {
								// TODO add better messages
								for(int i = 2; i < args.length; i++) {
									sender.sendMessage(plugin.enableSet(args[2]) ? "It worked" : "That set " + args[2] + " doesn't exist");
								}
							} else {
								sender.sendMessage("Please specify a set");
							}
						} break;
						case Keys.COMMAND_CTB_SET_REMOVE: {
							if(args.length > 2) {
								// TODO add better messages
								for(int i = 2; i < args.length; i++) {
									sender.sendMessage(plugin.disableSet(args[i]) ? "It worked" : "That set " + args[i] + " doesn't exist");
								}
							} else {
								sender.sendMessage("Please specify a set");
							}
						} break;
						case Keys.COMMAND_CTB_SET_LIST : {
//							sender.sendMessage("Listing sets");
//							sender.sendMessage("l:" + args.length);
//							for(int i = 0; i < args.length; i++) {
//								sender.sendMessage(args[i]);
//							}
//							sender.sendMessage(":l");
							if(args.length == 2) {
								sender.sendMessage("Enabled Sets");
								for(String s : plugin.getEnabledSets()) {
									sender.sendMessage(s);
								}
							} else {
								if(plugin.getAllSets().containsKey(args[2])) {
									sender.sendMessage(plugin.getAllSets().get(args[2]).toString());
								} else {
									sender.sendMessage("Set " + args[2] + " does not exist.");
								}
							}
						} break;
						case Keys.COMMAND_CTB_SET_LISTALL: {
							sender.sendMessage("All sets:");
							for(String s : plugin.getAllSets().keySet()) {
								sender.sendMessage(s);
							}
						} break;
						case Keys.COMMAND_CTB_SET_CLEAR: {
							plugin.clearSets();
							sender.sendMessage("Cleared");
						} break;
						}
					}
					return true;
				}
				}
			}
			sender.sendMessage("Please specify a valid action. Valid actions are " + acts);
			return true;
		}
		sender.sendMessage("Please specify an action for the game. Actions are " + acts);
		return true;
	}
}
