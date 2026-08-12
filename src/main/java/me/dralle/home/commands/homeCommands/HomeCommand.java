package me.dralle.home.commands.homeCommands;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.menu.subMenu.HomeListMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import static me.dralle.home.utils.HomeUtils.*;

public class HomeCommand implements CommandExecutor, TabCompleter {
    private final HomePlugin plugin;

    public HomeCommand(HomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return null;

        if (command.getName().equalsIgnoreCase("home")) {
            if (args.length == 1) {
                List<me.dralle.home.models.Home> homes = getPlayerHomesList(player, player, "tabComplete");
                return homes.stream()
                        .map(me.dralle.home.models.Home::getHomeName)
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (command.getName().equalsIgnoreCase("homes")) {
            if (args.length == 1) {
                return null; // Let Bukkit handle player names
            }
        }
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player p)){
            sender.sendMessage(ColouredText(getConfigMessage("chat.message.error.player.command.only-players")));
            return true;
        }

        if(command.getName().equalsIgnoreCase("home")){
            PlayerMenuUtility playerMenuUtility = HomePlugin.getPlayerMenuUtility(p);
            playerMenuUtility.setPlayer(p);
            playerMenuUtility.setTarget(p);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                List<me.dralle.home.models.Home> homes = getPlayerHomesList(p, p, "onCommand");
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    playerMenuUtility.setPlayerHomes(homes);
                    if(args.length < 1){
                        new HomeListMenu(playerMenuUtility).open();
                        return;
                    }

                    String homeName = args[0];
                    me.dralle.home.models.Home home = playerMenuUtility.getHomeByName(homeName);
                    if(home == null){
                        msp(p, rep(getConfigMessage("chat.message.error.player.home.not-found"), "%home%", homeName, "%chat_prefix%", getConfigMessage("chat.prefix.error")), true);
                        return;
                    }
                    teleportToHome(p, p, home);
                });
            });
            return true;
        }

        else if(command.getName().equalsIgnoreCase("homes")){
            PlayerMenuUtility playerMenuUtility = HomePlugin.getPlayerMenuUtility(p);
            playerMenuUtility.setPlayer(p);

            if(args.length < 1){
                playerMenuUtility.setTarget(p);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    List<me.dralle.home.models.Home> homes = getPlayerHomesList(p, p, "onCommand");
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        playerMenuUtility.setPlayerHomes(homes);
                        new HomeListMenu(playerMenuUtility).open();
                    });
                });
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!p.getUniqueId().equals(target.getUniqueId()) && !p.hasPermission("genius.homes.others")) {
                p.sendMessage(ColouredText(getErrorMessagePermission()));
                return true;
            }
            playerMenuUtility.setTarget(target);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                List<me.dralle.home.models.Home> othersHomes = getPlayerHomesList(target, p, "onCommand");
                Bukkit.getScheduler().runTask(plugin, () -> {
                    playerMenuUtility.setPlayerHomes(othersHomes);
                    new HomeListMenu(playerMenuUtility).open();
                });
            });
            return true;
        }
        return true;
    }
}
