package dev.benjaminc.capturetheblock;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class CTBEvent implements Listener {
	
	private CTBMain plugin;
	
	public CTBEvent(CTBMain p) {
		plugin = p;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
//		UUID u = p.getUniqueId();
		Material m = p.getLocation().getBlock().getType();
		Material n = p.getLocation().subtract(0, 1, 0).getBlock().getType();
		Team t = plugin.findTeam(p);
		if(t != null) {
			Material tgt = t.getTarget();
			if((m == tgt || n == tgt) && !t.hasEveryoneFound()) {
				plugin.foundBlock(p, t);
			}
		}
	}
}
