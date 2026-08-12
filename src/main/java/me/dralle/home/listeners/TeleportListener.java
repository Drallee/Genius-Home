package me.dralle.home.listeners;

import me.dralle.home.HomePlugin;
import me.dralle.home.utils.HomeUtils;
import me.dralle.home.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TeleportListener implements Listener {
    private final HomePlugin plugin;

    public TeleportListener(HomePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!Utils.getConfigCheck("settings.homes.teleport.cooldown.cancel-on-move")) return;
        
        Player player = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();

        if (to == null) return;

        // Check if the player moved block-wise
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            HomeUtils.cancelTeleport(player);
        }
    }
}
