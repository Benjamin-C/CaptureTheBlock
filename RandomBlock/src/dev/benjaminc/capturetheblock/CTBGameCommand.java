package dev.benjaminc.capturetheblock;

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
						String msg = "";
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
				case Keys.COMMAND_CTB_END: {
					plugin.endRound();
					return true;
				}
				case Keys.COMMAND_CTB_RESET: {
					plugin.endRound();
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
							sender.sendMessage("Enabled Sets");
							for(String s : plugin.getEnabledSets()) {
								sender.sendMessage(s);
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
