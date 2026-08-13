package me.dralle.home.commands.homeCommands;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.utils.HomeUtils;
import org.bukkit.Bukkit;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.dralle.home.utils.Utils.*;

public class DelHomeCommand implements CommandExecutor, TabCompleter {
    private final HomePlugin plugin;

    public DelHomeCommand(HomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return null;

        if (args.length == 1) {
            List<me.dralle.home.models.Home> homes = HomeUtils.getPlayerHomesList(player, player, "tabComplete");
            return homes.stream()
                    .map(me.dralle.home.models.Home::getHomeName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ColouredText(getConfigMessage("chat.message.error.player.command.only-players")));
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(ColouredText(rep(getConfigMessage("chat.message.error.player.command.usage"), "%usage%", "/delhome <name>")));
            return true;
        }

        String homeName = args[0];
        PlayerMenuUtility pmu = HomePlugin.getPlayerMenuUtility(p);
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<me.dralle.home.models.Home> homes = HomeUtils.getPlayerHomesList(p, p, "delhome");
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                pmu.setPlayerHomes(homes);
                me.dralle.home.models.Home home = pmu.getHomeByName(homeName);

                if (home == null) {
                    p.sendMessage(ColouredText(rep(getConfigMessage("chat.message.error.player.home.not-found"), "%home%", homeName, "%chat_prefix%", getConfigMessage("chat.prefix.error"))));
                    return;
                }

                HomeUtils.deleteHome(p, home);
            });
        });
        return true;
    }
}
