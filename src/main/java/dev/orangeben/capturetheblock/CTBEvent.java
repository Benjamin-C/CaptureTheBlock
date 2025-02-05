package dev.orangeben.capturetheblock;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CTBEvent implements Listener {
	
	private CTBMain plugin;
	
	public CTBEvent(CTBMain p) {
		plugin = p;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(plugin.isRunning()) {
			Player p = e.getPlayer();
			Team t = plugin.findTeam(p.getUniqueId());
			if(t != null) {
				Material m = p.getLocation().getBlock().getType();
				Material n = p.getLocation().subtract(0, 1, 0).getBlock().getType();
                for(Material tgt : t.getTargets()) {
                    if((m == tgt || n == tgt)) {
                        plugin.foundBlock(p, t, tgt);
                    }
                }
			}
		}
	}
	
	@EventHandler
	public void onPlayerConnect(PlayerJoinEvent e) {
		plugin.reconnectPlayer(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent e) {
		plugin.disconnectPlayer(e.getPlayer());
	}
}
