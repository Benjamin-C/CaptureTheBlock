package dev.orangeben.capturetheblock;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CTBCommandTabComplete implements TabCompleter {
	
	CTBMain plugin;
	
	public CTBCommandTabComplete(CTBMain plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lavel, String[] args) {
		List<String> options = new ArrayList<String>();
		
		List<String> possible = new ArrayList<String>();
		
		boolean isAdmin = sender.hasPermission(Keys.PERMISSION_CONTROL);
		
		switch(args.length) {
//		case 0: {
//			options = possible;
//		} break;
		case 1: {
			possible.add(Keys.COMMAND_CTB_TEAM); // add join leave list remove
			possible.add(Keys.COMMAND_CTB_SCORE);
			if(isAdmin) { 
				possible.add(Keys.COMMAND_CTB_START);
				possible.add(Keys.COMMAND_CTB_END);
				possible.add(Keys.COMMAND_CTB_RESET);
				possible.add(Keys.COMMAND_CTB_SET);
				possible.add(Keys.COMMAND_CTB_RELOADCONFIG);
				possible.add(Keys.COMMAND_CTB_BLOCKS);
				possible.add(Keys.COMMAND_CTB_ALLBLOCKS);
				possible.add(Keys.COMMAND_CTB_FINAL);
				possible.add(Keys.COMMAND_CTB_ENDAT);
				possible.add(Keys.COMMAND_CTB_TOGGLEDEBUGMSG);
				possible.add(Keys.COMMAND_CTB_MOVEON);
                possible.add(Keys.COMMAND_CTB_MARK);
			}
			options = getPossibleCompletes(possible, args[0]);
		} break;
		case 2: {
			switch(args[0]) {
			case Keys.COMMAND_CTB_TEAM: {
				possible.add(Keys.COMMAND_CTB_TEAM_JOIN); // add join leave list remove
				possible.add(Keys.COMMAND_CTB_TEAM_LEAVE); // add join leave list remove
				possible.add(Keys.COMMAND_CTB_TEAM_LIST);
				if(isAdmin) { 
					possible.add(Keys.COMMAND_CTB_TEAM_ADD);
					possible.add(Keys.COMMAND_CTB_TEAM_REMOVE);
					possible.add(Keys.COMMAND_CTB_TEAM_CLEAR);
					possible.add(Keys.COMMAND_CTB_TEAM_SCORE);
					possible.add(Keys.COMMAND_CTB_TEAM_ADDALL);
					possible.add(Keys.COMMAND_CTB_TEAM_SKIP);
				}
			} break;
			case Keys.COMMAND_CTB_SET: {
				possible.add(Keys.COMMAND_CTB_SET);
				possible.add(Keys.COMMAND_CTB_SET_ADD);
				possible.add(Keys.COMMAND_CTB_SET_REMOVE);
				possible.add(Keys.COMMAND_CTB_SET_LIST);
				possible.add(Keys.COMMAND_CTB_SET_LISTALL);
				possible.add(Keys.COMMAND_CTB_SET_CLEAR);
			} break;
			case Keys.COMMAND_CTB_FINAL: {
				possible.add("0");
			} break;
			case Keys.COMMAND_CTB_ENDAT: {
				possible.add("10:00");
				possible.add("null");
			} break;
            case Keys.COMMAND_CTB_MARK: {
                possible.add(Keys.COMMAND_CTB_MARK_PLAYER);
                possible.add(Keys.COMMAND_CTB_MARK_TEAM);
            }
			}
			options = getPossibleCompletes(possible, args[1]);
		} break;
		case 3: {
			switch(args[0]) {
			case Keys.COMMAND_CTB_TEAM: {
				switch(args[1]) {
				case Keys.COMMAND_CTB_TEAM_SCORE:
				case Keys.COMMAND_CTB_TEAM_CLEAR:
				case Keys.COMMAND_CTB_TEAM_REMOVE:
				case Keys.COMMAND_CTB_TEAM_JOIN: {
					possible.addAll(plugin.getAllTeams().keySet());
				} break;
				case Keys.COMMAND_CTB_TEAM_LEAVE: {
					possible.addAll(getAllPlayers());
				} break;
				case Keys.COMMAND_CTB_TEAM_SKIP:
				case Keys.COMMAND_CTB_TEAM_LIST: {
					if(isAdmin) {
						possible.addAll(plugin.getAllTeams().keySet());
					}
				} break;
				}
				options = getPossibleCompletes(possible, args[2]);
			} break;
			case Keys.COMMAND_CTB_SET: {
				switch(args[1]) {
				case Keys.COMMAND_CTB_SET_ADD: {
					possible.addAll(plugin.getDisabledSets());
				} break;
				case Keys.COMMAND_CTB_SET_REMOVE: {
					possible.addAll(plugin.getEnabledSets());
				} break;
				case Keys.COMMAND_CTB_SET_LIST: {
					possible.addAll(plugin.getAllSets().keySet());
				} break;
				}
				options = getPossibleCompletes(possible, args[2]);
			} break;
            case Keys.COMMAND_CTB_MARK: {
                switch(args[1]) {
                case Keys.COMMAND_CTB_MARK_PLAYER: {
                    for(Team t : plugin.getAllTeams().values()) {
                        possible.addAll(t.getAllPeoples().values());
                    }
                    options = getPossibleCompletes(possible, args[2]);
                } break;
                case Keys.COMMAND_CTB_MARK_TEAM: {
                    possible.addAll(plugin.getAllTeams().keySet());
                    options = getPossibleCompletes(possible, args[2]);
                } break;
                }
            }
			}
		} break;
		case 4: {
			switch(args[0]) {
			case Keys.COMMAND_CTB_TEAM: {
				switch(args[1]) {
				case Keys.COMMAND_CTB_TEAM_JOIN: {
					possible.addAll(getAllPlayers());
				} break;
				case Keys.COMMAND_CTB_TEAM_SCORE: {
					possible.add(Keys.COMMAND_CTB_TEAM_SCORE_ADD);
					possible.add(Keys.COMMAND_CTB_TEAM_SCORE_REMOVE);
					possible.add(Keys.COMMAND_CTB_TEAM_SCORE_SET);
				} break;
				}
			} break;
            case Keys.COMMAND_CTB_MARK: {
                possible.add(Keys.COMMAND_CTB_MARK_FOUND);
                possible.add(Keys.COMMAND_CTB_MARK_NOTFOUND);
            } break;
			}
			options = getPossibleCompletes(possible, args[3]);
		}
		}
		return options;
	}

	private List<String> getAllPlayers() {
		List<String> l = new ArrayList<String>();
		for(Player p : Bukkit.getOnlinePlayers()) {
			l.add(p.getName());
		}
		return l;
	}
	private List<String> getPossibleCompletes(List<String> possible, String part) {
		List<String> options = new ArrayList<String>();
		for(String s : possible) {
			if(part.equalsIgnoreCase(s.substring(0, Math.min(part.length(), s.length())))) {
				options.add(s);
			}
		}
		return options;
	}
}
