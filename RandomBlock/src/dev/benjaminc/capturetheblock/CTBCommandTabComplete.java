package dev.benjaminc.capturetheblock;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CTBCommandTabComplete implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lavel, String[] args) {
		List<String> options = new ArrayList<String>();
		
		List<String> possible = new ArrayList<String>();
		if(sender.hasPermission(Keys.PERMISSION_CONTROL)) { 
			possible.add(Keys.COMMAND_CTB_START);
			possible.add(Keys.COMMAND_CTB_END);
			possible.add(Keys.COMMAND_CTB_RESET);
			possible.add(Keys.COMMAND_CTB_BLOCKS);
			possible.add(Keys.COMMAND_CTB_ALLBLOCKS);
		}
		
		possible.add(Keys.COMMAND_CTB_SCORE);
		
		switch(args.length) {
		case 0: {
			options = possible;
		} break;
		case 1: {
			options = getPossibleCompletes(possible, args[0]);
		} break;
		}
		
		return options;
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
