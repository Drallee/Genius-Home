package me.dralle.home.listeners;

import me.dralle.home.HomePlugin;
import me.dralle.home.utils.HomeUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static me.dralle.home.utils.Utils.ColouredText;
import static me.dralle.home.utils.Utils.getConfigMessage;
import static me.dralle.home.utils.Utils.rep;

public class JoinLeaveListener implements Listener {
    private final HomePlugin plugin;

    public JoinLeaveListener(HomePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (player.hasPermission("genius.homes.admin") && plugin.isUpdateAvailable()) {
            player.sendMessage(ColouredText(rep(getConfigMessage("chat.message.player.update-available"),
                    "%version%", plugin.getLatestVersion(),
                    "%url%", "https://modrinth.com/plugin/genius-homes")));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        
        // Cleanup player utility map
        HomePlugin.removePlayerMenuUtility(player);
        
        // Cleanup teleport tasks if any
        HomeUtils.cancelTeleport(player);
    }
}
