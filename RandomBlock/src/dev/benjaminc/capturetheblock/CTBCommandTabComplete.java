package dev.benjaminc.capturetheblock;

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
		
		switch(args.length) {
//		case 0: {
//			options = possible;
//		} break;
		case 1: {
			possible.add(Keys.COMMAND_CTB_TEAM); // add join leave list remove
			possible.add(Keys.COMMAND_CTB_SCORE);
			if(sender.hasPermission(Keys.PERMISSION_CONTROL)) { 
				possible.add(Keys.COMMAND_CTB_START);
				possible.add(Keys.COMMAND_CTB_END);
				possible.add(Keys.COMMAND_CTB_RESET);
				possible.add(Keys.COMMAND_CTB_BLOCKS);
				possible.add(Keys.COMMAND_CTB_ALLBLOCKS);
			}
			options = getPossibleCompletes(possible, args[0]);
		} break;
		case 2: {
			switch(args[0]) {
			case Keys.COMMAND_CTB_TEAM: {
				possible.add(Keys.COMMAND_CTB_TEAM_JOIN); // add join leave list remove
				possible.add(Keys.COMMAND_CTB_TEAM_LEAVE); // add join leave list remove
				possible.add(Keys.COMMAND_CTB_TEAM_LIST);
				if(sender.hasPermission(Keys.PERMISSION_CONTROL)) { 
					possible.add(Keys.COMMAND_CTB_TEAM_ADD);
					possible.add(Keys.COMMAND_CTB_TEAM_REMOVE);
					possible.add(Keys.COMMAND_CTB_TEAM_CLEAR);
					possible.add(Keys.COMMAND_CTB_TEAM_ADDALL);
				}
			} break;
			}
			options = getPossibleCompletes(possible, args[1]);
		} break;
		case 3: {
			switch(args[0]) {
			case Keys.COMMAND_CTB_TEAM: {
				switch(args[1]) {
				case Keys.COMMAND_CTB_TEAM_CLEAR:
				case Keys.COMMAND_CTB_TEAM_REMOVE:
				case Keys.COMMAND_CTB_TEAM_JOIN: {
					possible.addAll(plugin.getAllTeams().keySet());
				} break;
				case Keys.COMMAND_CTB_TEAM_LEAVE: {
					possible.addAll(getAllPlayers());
				} break;
				}
			} break;
			}
			options = getPossibleCompletes(possible, args[2]);
		}
		case 4: {
			switch(args[0]) {
			case Keys.COMMAND_CTB_TEAM: {
				switch(args[1]) {
				case Keys.COMMAND_CTB_TEAM_JOIN: {
					possible.addAll(getAllPlayers());
				} break;
				}
			} break;
			}
			options = getPossibleCompletes(possible, args[2]);
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
			if(part.equals(s.substring(0, Math.min(part.length(), s.length())))) {
				options.add(s);
			}
		}
		return options;
	}
}
