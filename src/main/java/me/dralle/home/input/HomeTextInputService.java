package me.dralle.home.input;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.menu.subMenu.HomeListMenu;
import me.dralle.home.menu.subMenu.HomeSettingsMenu;
import me.dralle.home.models.Home;
import me.dralle.home.utils.HomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.dralle.home.utils.Utils.ColouredText;
import static me.dralle.home.utils.Utils.getConfigMessage;
import static me.dralle.home.utils.Utils.getErrorMessagePermission;
import static me.dralle.home.utils.Utils.rep;

public class HomeTextInputService {
    private final HomePlugin plugin;
    private final TextInputService textInputService;
    private final HomeNameValidator validator;

    public HomeTextInputService(HomePlugin plugin, TextInputService textInputService, HomeNameValidator validator) {
        this.plugin = plugin;
        this.textInputService = textInputService;
        this.validator = validator;
    }

    public void openCreateHomeInput(Player player, PlayerMenuUtility playerMenuUtility) {
        openCreateHomeInput(player, () -> {
            playerMenuUtility.setPlayerHomes(HomeUtils.getPlayerHomesList(player, player, "createHomeInputCancel"));
            new HomeListMenu(playerMenuUtility).open();
        }, () -> {
            playerMenuUtility.setPlayerHomes(HomeUtils.getPlayerHomesList(player, player, "createHomeInputSuccess"));
            new HomeListMenu(playerMenuUtility).open();
        });
    }

    public void openCreateHomeInput(Player player) {
        openCreateHomeInput(player, null, null);
    }

    private void openCreateHomeInput(Player player, Runnable onCancel, Runnable onSuccess) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int max = HomeUtils.getMaxHomes(player);
            int current = HomeUtils.getCurrentHomes(player, player);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (max != -1 && current >= max) {
                    player.sendMessage(ColouredText(rep(getConfigMessage("chat.message.error.player.command.max-homes"),
                            "%max%", max,
                            "%chat_prefix%", getConfigMessage("chat.prefix.error"))));
                    return;
                }

                textInputService.open(TextInputRequest.builder(player)
                        .title(ColouredText(getConfigMessage("text-input.home.create.title")))
                        .initialText(getConfigMessage("text-input.home.create.initial-text"))
                        .itemMaterial("NAME_TAG")
                        .validator((inputPlayer, input) -> CompletableFuture.supplyAsync(
                                () -> validator.validate(inputPlayer, input, HomeNameValidator.Mode.CREATE, null)))
                        .onConfirm(homeName -> HomeUtils.setHome(player, homeName, "RED_BED", "none", onSuccess))
                        .onCancel(onCancel)
                        .build());
            });
        });
    }

    public void openRenameHomeInput(Player player, PlayerMenuUtility playerMenuUtility) {
        OfflinePlayer target = playerMenuUtility.getTarget();
        Home home = playerMenuUtility.getPlayerHome();
        if (home == null) {
            return;
        }

        boolean isOwner = player.getUniqueId().equals(target.getUniqueId());
        if (isOwner) {
            if (me.dralle.home.utils.Utils.getConfigCheck("settings.homes.rename-permission") && !player.hasPermission("genius.homes.rename")) {
                player.sendMessage(ColouredText(getErrorMessagePermission()));
                return;
            }
        } else if (!player.hasPermission("genius.homes.others.settings.rename") && !player.hasPermission("genius.others.settings")) {
            player.sendMessage(ColouredText(getErrorMessagePermission()));
            return;
        }

        String oldName = home.getHomeName();
        textInputService.open(TextInputRequest.builder(player)
                .title(ColouredText(rep(getConfigMessage("text-input.home.rename.title"), "%home%", oldName)))
                .initialText(oldName)
                .itemMaterial("NAME_TAG")
                .validator((inputPlayer, input) -> CompletableFuture.supplyAsync(
                        () -> validator.validate(target, input, HomeNameValidator.Mode.RENAME, oldName)))
                .onConfirm(newName -> HomeUtils.changeHomeName(player, target, oldName, newName, () -> {
                    List<Home> updatedHomes = HomeUtils.getPlayerHomesList(target, player, "AnvilGUIRename");
                    playerMenuUtility.setPlayerHomes(updatedHomes);
                    Home updatedHome = null;
                    for (Home updated : updatedHomes) {
                        if (updated.getHomeName().equalsIgnoreCase(newName)) {
                            updatedHome = updated;
                            break;
                        }
                    }
                    if (updatedHome != null) {
                        playerMenuUtility.setHomeToChange(updatedHome);
                        new HomeSettingsMenu(playerMenuUtility).open();
                    } else {
                        new HomeListMenu(playerMenuUtility).open();
                    }
                }))
                .onCancel(() -> new HomeSettingsMenu(playerMenuUtility).open())
                .build());
    }
}
