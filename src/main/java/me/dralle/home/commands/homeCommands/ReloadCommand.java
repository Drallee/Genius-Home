package me.dralle.home.commands.homeCommands;

import me.dralle.home.HomePlugin;
import me.dralle.home.utils.HomeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static me.dralle.home.utils.Utils.*;

public class ReloadCommand implements CommandExecutor {
    private final HomePlugin plugin;

    public ReloadCommand(HomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("genius.homes.admin")) {
            sender.sendMessage(ColouredText(getErrorMessagePermission()));
            return true;
        }

        plugin.reloadConfigs();
        HomeUtils.clearCache();
        sender.sendMessage(ColouredText(getConfigMessage("chat.message.error.player.command.config-reloaded")));
        return true;
    }
}
