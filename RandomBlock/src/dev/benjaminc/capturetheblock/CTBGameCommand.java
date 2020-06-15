package dev.benjaminc.capturetheblock;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CTBGameCommand implements CommandExecutor {

	CTBMain plugin;
	
	public CTBGameCommand(CTBMain p) {
		plugin = p;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String acts = Keys.COMMAND_CTB_SCORE;
		if(sender.hasPermission(Keys.PERMISSION_CONTROL)) {
			acts += ", " + Keys.COMMAND_CTB_START + ", "
					+ Keys.COMMAND_CTB_END + ", "
					+ Keys.COMMAND_CTB_RESET + ", "
					+ Keys.COMMAND_CTB_BLOCKS + ", "
					+ Keys.COMMAND_CTB_ALLBLOCKS;
		}
		if(args.length >= 1) {
			if(args[0].equals(Keys.COMMAND_CTB_SCORE)) {
				sender.sendMessage(plugin.showScoresStr(false));
				return true;
			}
			if(sender.hasPermission(Keys.PERMISSION_CONTROL)) {
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
				case Keys.COMMAND_CTB_BLOCKS: {
					sender.sendMessage(plugin.showScoresStr(true));
					return true;
				}
				case Keys.COMMAND_CTB_ALLBLOCKS: {
					sender.sendMessage(plugin.listAllBlocks());
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
