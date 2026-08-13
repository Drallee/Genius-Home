package me.dralle.home.input;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.Menu;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static me.dralle.home.utils.Utils.ColouredText;

public class TextInputService {
    private final HomePlugin plugin;
    private final Set<UUID> submittedInputs = ConcurrentHashMap.newKeySet();

    public TextInputService(HomePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(TextInputRequest request) {
        Player player = request.getPlayer();
        UUID playerId = player.getUniqueId();
        submittedInputs.remove(playerId);

        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(request.getTitle())
                .text(request.getInitialText())
                .itemLeft(Menu.createItem(request.getItemMaterial(), 1, request.getInitialText()))
                .onClickAsync((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return CompletableFuture.completedFuture(Collections.emptyList());
                    }

                    return request.getValidator().apply(stateSnapshot.getPlayer(), stateSnapshot.getText())
                            .thenApply(result -> {
                                if (!result.isValid()) {
                                    stateSnapshot.getPlayer().playSound(stateSnapshot.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                                    return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(ColouredText(result.getErrorMessage())));
                                }

                                submittedInputs.add(playerId);
                                return java.util.Arrays.asList(
                                        AnvilGUI.ResponseAction.close(),
                                        AnvilGUI.ResponseAction.run(() -> request.getOnConfirm().accept(result.getValue()))
                                );
                            });
                })
                .onClose(stateSnapshot -> {
                    if (submittedInputs.remove(playerId)) {
                        return;
                    }
                    if (request.getOnCancel() != null) {
                        plugin.getServer().getScheduler().runTask(plugin, request.getOnCancel());
                    }
                })
                .open(player);
    }

    public void clearSessions() {
        submittedInputs.clear();
    }
}
