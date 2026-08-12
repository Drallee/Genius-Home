package me.dralle.home.commands.homeCommands;

import me.dralle.home.HomePlugin;
import me.dralle.home.utils.HomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.dralle.home.utils.Utils.*;

public class SetHomeCommand implements CommandExecutor {
    private final HomePlugin plugin;

    public SetHomeCommand(HomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ColouredText(getConfigMessage("chat.message.error.player.command.only-players")));
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(ColouredText(rep(getConfigMessage("chat.message.error.player.command.usage"), "%usage%", "/sethome <name>")));
            return true;
        }

        String homeName = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int max = HomeUtils.getMaxHomes(p);
            int current = HomeUtils.getCurrentHomes(p, p);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (max != -1 && current >= max) {
                    p.sendMessage(ColouredText(rep(getConfigMessage("chat.message.error.player.command.max-homes"), "%max%", max, "%chat_prefix%", getConfigMessage("chat.prefix.error"))));
                    return;
                }

                HomeUtils.setHome(p, homeName, "RED_BED", "none");
            });
        });
        return true;
    }
}
