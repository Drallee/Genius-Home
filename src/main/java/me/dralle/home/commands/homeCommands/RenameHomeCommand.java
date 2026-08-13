package me.dralle.home.commands.homeCommands;

import me.dralle.home.HomePlugin;
import me.dralle.home.input.HomeNameValidator;
import me.dralle.home.input.TextInputValidationResult;
import me.dralle.home.utils.HomeUtils;
import org.bukkit.Bukkit;
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

public class RenameHomeCommand implements CommandExecutor, TabCompleter {
    private final HomePlugin plugin;

    public RenameHomeCommand(HomePlugin plugin) {
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

        if (args.length < 2) {
            p.sendMessage(ColouredText(rep(getConfigMessage("chat.message.error.player.command.usage"), "%usage%", "/home-rename <old> <new>")));
            return true;
        }

        if (getConfigCheck("settings.homes.rename-permission") && !p.hasPermission("genius.homes.rename")) {
            p.sendMessage(ColouredText(getErrorMessagePermission()));
            return true;
        }

        String oldName = args[0];
        String newName = args[1];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TextInputValidationResult validation = new HomeNameValidator().validate(p, newName, HomeNameValidator.Mode.RENAME, oldName);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!validation.isValid()) {
                    p.sendMessage(ColouredText(rep(validation.getErrorMessage(), "%chat_prefix%", getConfigMessage("chat.prefix.error"))));
                    return;
                }
                HomeUtils.changeHomeName(p, oldName, validation.getValue());
            });
        });
        return true;
    }
}
