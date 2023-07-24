package dev.orangeben.capturetheblock;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RandomBlockCommand implements CommandExecutor {

	CTBMain plugin;
	
	public RandomBlockCommand(CTBMain p) {
		plugin = p;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		sender.sendMessage("your random block is: " + plugin.getRandomBlock().name());
		return true;
	}
}
