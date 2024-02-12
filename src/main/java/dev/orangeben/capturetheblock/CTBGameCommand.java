package dev.orangeben.capturetheblock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

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
			case Keys.COMMAND_CTB_HELP: {
                // "See the README on github"
                if(sender instanceof Player) {
                    Player p = (Player) sender;
                    BaseComponent[] component = new ComponentBuilder().appendLegacy(plugin.getString("help.title")).append(plugin.getString("help.url")).color(ChatColor.BLUE).underlined(true).event(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getString("help.url"))).create();
                    p.spigot().sendMessage(component);
                } else {
                    sender.sendMessage(plugin.getString("help.title") + " " + plugin.getString("help.url"));
                }
                return true;
            }
            case Keys.COMMAND_CTB_SCORE: {
				sender.sendMessage(plugin.showScoresStr(false));
				return true;
			}
			case Keys.COMMAND_CTB_TEAM: {
				if(args.length >= 2) {
					switch(args[1]) {
					case Keys.COMMAND_CTB_TEAM_JOIN: {
                        // PUT SOMEONE ON A TEAM
						if(args.length >= 3) {
							if(args.length >= 4 && isAdmin) { // If a player was specified
								Player pl = Bukkit.getPlayer(args[3]);
								if(pl != null) {
									boolean success = plugin.joinTeam(pl, args[2]);
									if(!success) {
										sender.sendMessage(plugin.getString("team.error.nonexistant"));
									} else {
										sender.sendMessage(plugin.getString("team.joined.other", args[3], args[2]));
									}
								} else {
									sender.sendMessage(plugin.getString("error.player.nonexistant", args[3]));
								}
							} else { // If the sender is trying to join a team
								if(sender instanceof Player) {
									boolean success = plugin.joinTeam((Player) sender, args[2]);
									if(!success) {
										sender.sendMessage(plugin.getString("team.error.nonexistant"));
									} else {
										sender.sendMessage(plugin.getString("team.joined.self", args[2]));
									}
								} else {
									sender.sendMessage(plugin.getString("error.notplayer"));
								}
							}
						} else {
							sender.sendMessage(plugin.getString("team.error.noname"));
						}
						return true;
					}
					case Keys.COMMAND_CTB_TEAM_LEAVE: {
						// KICK SOMEONE OFF TEAM
						if(args.length >= 3 && isAdmin) { // If a player was specified
							Player pl = Bukkit.getPlayer(args[2]);
							if(pl != null) {
								boolean success = plugin.leaveTeam(pl);
								if(!success) {
									sender.sendMessage(plugin.getString("team.error.leave.other"));
								} else {
                                    sender.sendMessage(plugin.getString("team.left.other", args[2]));
									pl.sendMessage(plugin.getString("team.left.self"));
								}
							} else {
								sender.sendMessage(plugin.getString("error.player.nonexistant", args[2]));
							}
						} else { // If the sender is trying to leave a team
							if(sender instanceof Player) {
								boolean success = plugin.leaveTeam((Player) sender);
								if(!success) {
									sender.sendMessage(plugin.getString("team.error.leave.self"));
								} else {
									sender.sendMessage(plugin.getString("team.left.self"));
								}
							} else {
								sender.sendMessage(plugin.getString("error.notplayer"));
							}
						}
						return true;
					}
					case Keys.COMMAND_CTB_TEAM_LIST: {
                        // LIST TEAMS AND PLAYERS
						List<Team> ts = new ArrayList<Team>();
						if(isAdmin) {
							if(args.length >= 3) {
								if(plugin.getAllTeams().containsKey(args[2])) {
									ts.add(plugin.getAllTeams().get(args[2]));
								} else {
									sender.sendMessage(plugin.getString("team.error.nonexistant"));
								}
							} else {
								ts.addAll(plugin.getAllTeams().values());
							}
						} else {
							if(sender instanceof Player) {
								Team mt = plugin.findTeam(((Player) sender).getUniqueId()); 
                                if(mt != null) {
                                    ts.add(mt);
                                } else {
                                    sender.sendMessage(plugin.getString("team.error.noton"));
                                }
							}
						}
                        if(!ts.isEmpty()) {
                            String msg = "";
                            if(ts.size() > 1) {
                                msg = "CTB Team List:\n";
                            } else {
                                msg = "CTB Team ";
                            }
                            boolean first = true;
                            for(Team t : ts) {
                                if(first) {
                                    first = false;
                                } else {
                                    msg += "\n";
                                }
                                msg += plugin.listTeam(t, true);
                            }
                            sender.sendMessage(msg);
                        }
						return true;
					}
					}
				} else {
                    if(isAdmin) {
                        sender.sendMessage(plugin.getString("error.noaction"));
                    } else {
                        sender.sendMessage(plugin.getString("error.validaction", Keys.COMMAND_CTB_TEAM_JOIN + ", " + Keys.COMMAND_CTB_TEAM_LEAVE + ", & " + Keys.COMMAND_CTB_TEAM_LIST));
                    }
                    return true;
                }
			}
			}
			if(isAdmin) {
				switch(args[0]) {
				case Keys.COMMAND_CTB_START: {
                    // STARTS THE GAME
                    if(plugin.isRunning()) {
                        sender.sendMessage(plugin.getString("game.error.running"));
                    } else {
                        plugin.startGame();
                    }
					return true;
				}
				case Keys.COMMAND_CTB_END: {
                    // ENDS THE GAME
                    if(plugin.isRunning()) {
                        plugin.endGame();
                    } else {
                        sender.sendMessage(plugin.getString("game.error.stopped"));
                    }
					return true;
				}
                case Keys.COMMAND_CTB_ENDAT: {
                    // ENDS THE GAME AT A TIME
					if(args.length >= 2) {
						if(args.length > 2 && args[1].charAt(1) == ':') {
							args[1] = "0" + args[1];
						}
						if(args[1].equals(Keys.COMMAND_CTB_ENDAT_NEVER)) {
							plugin.setEndTime(null);
							sender.sendMessage(plugin.getString("game.endtime.disabled"));
						} else {
							try {
								LocalDateTime ldt = LocalDate.now().atTime(LocalTime.parse(args[1]));
								if(ldt.isAfter(LocalDateTime.now())) {
									plugin.setEndTime(ldt);
									if(plugin.getRoundsLeft() != -1) {
										plugin.setRoundsLeft(1);
									}
									sender.sendMessage(plugin.getString("game.endtime.set", ldt.format(CTBMain.formatter)));
								} else {
									sender.sendMessage(plugin.getString("game.endtime.error.past", args[1]));
								}
								
							} catch(DateTimeParseException e) {
								sender.sendMessage(plugin.getString("game.endtime.error.invalid", args[1]));
							}
						}
					} else {
						sender.sendMessage(plugin.getString("game.endtime.error.missing"));
					}
					return true;
				}
				case Keys.COMMAND_CTB_FINAL: {
                    // SETS THE NUMBER OF REMAINING ROUNDS
					if(args.length >= 2) {
						try {
							int left = Integer.parseInt(args[1]);
							if(left >= -1) {
								plugin.setRoundsLeft(left);
								sender.sendMessage(plugin.getString("game.endct.set", left, StringBank.pluralize(left)));
							} else {
								sender.sendMessage(plugin.getString("game.endct.small", left));
							}
							
						} catch(NumberFormatException e) {
							sender.sendMessage(plugin.getString("error.notnum", args[1]));
						}
					} else {
						sender.sendMessage(plugin.getString("game.endct.missing"));
					}
					return true;
				}
				case Keys.COMMAND_CTB_RESET: {
                    // RESETS GAME
                    if(plugin.isRunning()) {
                        plugin.endGame();
                    }
					plugin.resetScores();
                    sender.sendMessage(plugin.getString("game.reset"));
					return true;
				}
				case Keys.COMMAND_CTB_RELOADCONFIG: {
                    // RELOADS CTB CONFIG
					plugin.reloadMyConfig();
					sender.sendMessage(plugin.getString("game.info.config.reload"));
				} return true;
				case Keys.COMMAND_CTB_BLOCKS: {
                    // SHOWS THE BLOCKS TEAMS ARE GOING FOR
					sender.sendMessage(plugin.showScoresStr(true));
					return true;
				}
				case Keys.COMMAND_CTB_ALLBLOCKS: {
                    // LISTS ALL BLOCKS AND SETS AVAILABLE
					sender.sendMessage(plugin.listAllBlocks());
					return true;
				}
				case Keys.COMMAND_CTB_MOVEON: {
                    // GO TO THE NEXT ROUND
					if(plugin.isRunning()) {
						plugin.startRound();
					} else {
						sender.sendMessage(plugin.getString("game.error.stopped"));
					}
					return true;
				}
				case Keys.COMMAND_CTB_TEAM: {
                    // TEAM CONTROLS
					if(args.length >= 2) {
						switch(args[1]) {
						case Keys.COMMAND_CTB_TEAM_ADD: {
                            // ADD A NEW TEAM
							if(args.length >= 3) {
								plugin.addTeam(args[2]);
								sender.sendMessage(plugin.getString("team.new", args[2]));
							} else {
								sender.sendMessage(plugin.getString("team.error.noname"));
							}
						} break;
						case Keys.COMMAND_CTB_TEAM_REMOVE: {
                            // REMOVE AN EXISTING TEAM
							if(args.length >= 3) {
								plugin.removeTeam(args[2]);
								sender.sendMessage(plugin.getString("team.removed", args[2]));
								return true;
							} else {
								sender.sendMessage(plugin.getString("team.error.noname"));
							}
						} break;
						case Keys.COMMAND_CTB_TEAM_CLEAR: {
                            // REMOVE ALL PLAYERS FROM A TEAM
							if(args.length >= 3) {
								plugin.clearTeam(args[2]);
								sender.sendMessage(plugin.getString("team.cleared", args[2]));
								return true;
							} else {
								sender.sendMessage(plugin.getString("team.error.noname"));
							}
						} break;
						case Keys.COMMAND_CTB_TEAM_SKIP: {
                            // SKIPS A TEAM'S BLOCK AND GIVES THEM A NEW ONE
							Team toSkip = null;

							switch(args.length) {
							case 2: {
								if(sender instanceof Player) {
									toSkip = plugin.findTeam(((Player) sender).getUniqueId());
								} else {
									sender.sendMessage(plugin.getString("team.error.noname"));  
								}
							} break;
							case 3: {
								String teamname = args[2];
								if(plugin.getAllTeams().containsKey(teamname)) {
									toSkip = plugin.getAllTeams().get(teamname);
								} else {
									sender.sendMessage(plugin.getString("team.error.noname"));
								}
							} break;
							default: {
								sender.sendMessage("Error, wrong number of args, expected 2 or 3" );
							} break;
							}
							
							if(toSkip != null) {
								if(!toSkip.hasEveryoneFound()) {
									plugin.regenTeamTargetBlock(toSkip);
								} else {
									sender.sendMessage(plugin.getString("team.error.skip.alreadygot"));
								}
							} else {
								sender.sendMessage("toSkip was null");
							}
						} break;
						case Keys.COMMAND_CTB_TEAM_SCORE: {
                            // SCORE CONTROLS
							switch(args.length) {
                            case 2: {
                                sender.sendMessage(plugin.getString("team.error.noname"));
                            } break;
							case 3: {
                                sender.sendMessage(plugin.getString("team.score.error.action"));
							} break;
							case 4: {
                                sender.sendMessage(plugin.getString("team.score.error.amount"));
							} break;
							default: {
								try {
                                    int amount = Integer.parseInt(args[4]);
									switch(args[3]) {
									case Keys.COMMAND_CTB_TEAM_SCORE_ADD: {
										plugin.getAllTeams().get(args[2]).addScore(amount);
										sender.sendMessage(plugin.getString("team.score.add", amount, args[2]));
									} break;
									case Keys.COMMAND_CTB_TEAM_SCORE_REMOVE: {
										plugin.getAllTeams().get(args[2]).subtractScore(Integer.parseInt(args[4]));
										sender.sendMessage(plugin.getString("team.score.sub", amount, args[2]));
									} break;
									case Keys.COMMAND_CTB_TEAM_SCORE_SET: {
										plugin.getAllTeams().get(args[2]).setScore(Integer.parseInt(args[4]));
										sender.sendMessage(plugin.getString("team.score.set", args[2], amount));
									} break;
									}
								} catch(NumberFormatException e) {
									sender.sendMessage(plugin.getString("error.notnum", args[4]));
								}	
							}
							}
						} break;
						case Keys.COMMAND_CTB_TEAM_ADDALL: {
                            // PUT EVERYONE ON THEIR OWN TEAM
							for(Player p : Bukkit.getOnlinePlayers()) {
								Collection<Player> specs = plugin.getSpectators();
								if(!specs.contains(p) && plugin.findTeam(p.getUniqueId()) == null) {
									if(plugin.findTeam(p.getUniqueId()) != null) {
										plugin.findTeam(p.getUniqueId()).addPerson(p);
									} else {
										plugin.addTeam(p.getName());
										plugin.joinTeam(p, p.getName());
									}
								}
							}
							sender.sendMessage(plugin.getString("team.addall.success"));
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
									sender.sendMessage(plugin.enableSet(args[2]) ? plugin.getString("set.added", args[2]) : plugin.getString("set.nonexistant", args[2]));
								}
							} else {
								sender.sendMessage(plugin.getString("set.specify"));
							}
						} break;
						case Keys.COMMAND_CTB_SET_REMOVE: {
							if(args.length > 2) {
								// TODO add better messages
								for(int i = 2; i < args.length; i++) {
									sender.sendMessage(plugin.disableSet(args[i]) ? plugin.getString("set.removed", args[2]) : plugin.getString("set.nonexistant", args[2]));
								}
							} else {
								sender.sendMessage(plugin.getString("set.specify"));
							}
						} break;
						case Keys.COMMAND_CTB_SET_LIST : {
							if(args.length == 2) {
								sender.sendMessage(plugin.getString("sets.enabled"));
								for(String s : plugin.getEnabledSets()) {
									sender.sendMessage(s);
								}
							} else {
								if(plugin.getAllSets().containsKey(args[2])) {
									sender.sendMessage(plugin.getAllSets().get(args[2]).toString());
								} else {
									sender.sendMessage(plugin.getString("set.nonexistant", args[2]));
								}
							}
						} break;
						case Keys.COMMAND_CTB_SET_LISTALL: {
							sender.sendMessage(plugin.getString("sets.all"));
							for(String s : plugin.getAllSets().keySet()) {
								sender.sendMessage(s);
							}
						} break;
						case Keys.COMMAND_CTB_SET_CLEAR: {
							plugin.clearSets();
							sender.sendMessage(plugin.getString("sets.cleared"));
						} break;
                        default: {
                            sender.sendMessage(plugin.getString("error.validaction"));
                        } break;
						}
					} else {
                        sender.sendMessage(plugin.getString("error.noaction"));
                    }
					return true;
				}
                case Keys.COMMAND_CTB_MARK: {
                    if(!plugin.isRunning()) {
                        sender.sendMessage(plugin.getString("game.error.stopped"));
                        return true;
                    }
                    switch(args.length) {
                    case 1: sender.sendMessage(plugin.getString("mark.specify.type")); break;
                    case 2: {
                        if(args[1].equals(Keys.COMMAND_CTB_MARK_TEAM)) {
                            sender.sendMessage(plugin.getString("mark.specify.who"));    
                        } else {
                            sender.sendMessage(plugin.getString("mark.specify.who"));    
                        }
                    } break;
                    default: {
                        boolean toSet = !(args.length >= 4 && (args[3].equals(Keys.COMMAND_CTB_MARK_NOTFOUND) || args[3].equals("0") || args[3].equalsIgnoreCase("false")));
                        String name = args[2];
                        switch(args[1]) {
                        case Keys.COMMAND_CTB_MARK_TEAM: {
                            if(plugin.getAllTeams().containsKey(name)) {
                                Team t = plugin.getAllTeams().get(name);
                                for(UUID u : t.getAllPeoples().keySet()) {
                                    if(t.isOnline(u)) {
                                        if(toSet) {
                                            plugin.foundBlock(t.getPlayer(u), t);
                                        } else {
                                            plugin.unfoundBlock(t.getPlayer(u), t);
                                        }
                                    } else {
                                        t.setFound(u, toSet);
                                    }
                                }
                                sender.sendMessage(plugin.getString("mark.set.all", name, ((!toSet) ? plugin.getString("mark.not") + " " : "")));
                            } else {
                                sender.sendMessage(plugin.getString("team.error.nonexistant"));
                            }
                        } break;
                        case Keys.COMMAND_CTB_MARK_PLAYER: {
                            for(Team t : plugin.getAllTeams().values()) {
                                Map<UUID, String> peoples = t.getAllPeoples();
                                if(peoples.values().contains(name)) {
                                    for(UUID u : peoples.keySet()) {
                                        if(peoples.get(u).equals(name)) {
                                            if(t.isOnline(u)) {
                                                if(toSet) {
                                                    plugin.foundBlock(t.getPlayer(u), t);
                                                } else {
                                                    plugin.unfoundBlock(t.getPlayer(u), t);
                                                }
                                            } else {
                                                t.setFound(u, toSet);
                                            }
                                            sender.sendMessage(plugin.getString("mark.set.one", name, ((!toSet) ? plugin.getString("mark.not") + " " : "")));
                                            return true;        
                                        }
                                    }
                                }
                            }
                            sender.sendMessage(plugin.getString("error.player.nonexistant", name));
                        }
                        }
                    } break;
                    }
                    return true;
                }
				case Keys.COMMAND_CTB_TOGGLEDEBUGMSG: {
					plugin.setDebugMsgVisable(!plugin.getDebugMsgVisable());
					sender.sendMessage("Debug messages are " + ((plugin.getDebugMsgVisable()) ? "now" : "no longer") + " visable");
					return true;
				}
				}

			}
			sender.sendMessage(plugin.getString("teaerror.validaction.list", acts));
			return true;
		}
        sender.sendMessage(plugin.getString("error.validaction", acts));
		return true;
	}
}
