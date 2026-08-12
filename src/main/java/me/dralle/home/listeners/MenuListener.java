package me.dralle.home.listeners;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.Menu;
import me.dralle.home.menu.subMenu.HomeRenameAnvil;
import me.dralle.home.menu.subMenu.HomeSettingsMenu;
import me.dralle.home.utils.HomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static me.dralle.home.utils.Utils.*;

public class MenuListener implements Listener {
    private final HomePlugin plugin;
    private final NamespacedKey renameKey;

    public MenuListener(HomePlugin plugin) {
        this.plugin = plugin;
        this.renameKey = new NamespacedKey(plugin, "rename_text");
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        if (e.getInventory().getHolder() instanceof HomeRenameAnvil anvil) {
            AnvilInventory inventory = e.getInventory();

            Bukkit.getScheduler().runTask(plugin, () -> {
                inventory.setRepairCost(0);
            });

            ItemStack firstItem = inventory.getItem(0);
            if (firstItem != null && firstItem.getType() != org.bukkit.Material.AIR) {
                String renameText = getLiveRenameText(e.getView());
                String resolvedName = resolveResultName(renameText, firstItem.hasItemMeta() ? firstItem.getItemMeta().getDisplayName() : null);
                anvil.setPendingName(resolvedName);

                ItemStack result = firstItem.clone();
                org.bukkit.inventory.meta.ItemMeta meta = result.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ColouredText(resolvedName));
                    meta.getPersistentDataContainer().set(renameKey, PersistentDataType.STRING, resolvedName);
                    meta.setLore(null);
                    result.setItemMeta(meta);
                }

                e.setResult(result);
                inventory.setRepairCost(0);
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onMenuDrag(InventoryDragEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent e){
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu menu) {
            
            if (menu instanceof HomeRenameAnvil anvil) {
                if (e.getRawSlot() == 0 || e.getRawSlot() == 1) {
                    e.setCancelled(true);
                    return;
                }
            } else {
                e.setCancelled(true);
            }

            if (menu instanceof HomeRenameAnvil anvil) {
                if (e.getRawSlot() == 2) {
                    e.setCancelled(true);
                    Player p = (Player) e.getWhoClicked();
                    int currentLevel = p.getLevel();
                    String oldName = anvil.getPlayerMenuUtility().getPlayerHome().getHomeName();

                    if (e.getInventory() instanceof AnvilInventory anvilInv) {
                        anvilInv.setRepairCost(0);
                    }

                    String rawNewName = extractRenameText(anvil, e);

                    if (rawNewName != null) {
                        rawNewName = rawNewName.trim();
                    }

                    if (rawNewName == null || rawNewName.isEmpty()) {
                        String errorPrefix = getConfigMessage("chat.prefix.error");
                        p.sendMessage(ColouredText(errorPrefix + " &cPlease type a new name in the anvil."));
                        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                        if (p.getLevel() < currentLevel) p.setLevel(currentLevel);
                        return;
                    }
                    
                    final String cleanNewName = rawNewName;

                    if (StripColouredText(cleanNewName).equalsIgnoreCase(StripColouredText(oldName))) {
                        p.sendMessage(ColouredText(getConfigMessage("chat.prefix.error") + " &cNew name matches old name."));
                        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                        return;
                    }

                    org.bukkit.OfflinePlayer target = anvil.getPlayerMenuUtility().getTarget();
                    HomeUtils.changeHomeName(p, target, oldName, cleanNewName, () -> {
                        // Restore level if taken (async callback might need this)
                        if (p.getLevel() < currentLevel) {
                            p.setLevel(currentLevel);
                        }
                        
                        // Refresh home list in PMU and reopen settings
                        List<me.dralle.home.models.Home> updatedHomes = HomeUtils.getPlayerHomesList(target, p, "AnvilRename");
                        anvil.getPlayerMenuUtility().setPlayerHomes(updatedHomes);
                        
                        // We need to find the home again because the name changed
                        me.dralle.home.models.Home updatedHome = null;
                        for (me.dralle.home.models.Home h : updatedHomes) {
                            if (h.getHomeName().equalsIgnoreCase(cleanNewName)) {
                                updatedHome = h;
                                break;
                            }
                        }

                        if (updatedHome != null) {
                            anvil.getPlayerMenuUtility().setHomeToChange(updatedHome);
                            new HomeSettingsMenu(anvil.getPlayerMenuUtility()).open();
                        } else {
                            // Fallback if home not found for some reason
                            new me.dralle.home.menu.subMenu.HomeListMenu(anvil.getPlayerMenuUtility()).open();
                        }
                    });
                }
                return;
            }

            if (e.getCurrentItem() == null) {
                return;
            }

            menu.handleMenuItems(e);
        }
    }

    private String extractRenameText(HomeRenameAnvil anvil, InventoryClickEvent e) {
        String liveRenameText = getLiveRenameText(e.getView());
        if (liveRenameText != null && !liveRenameText.trim().isEmpty()) {
            return liveRenameText;
        }

        String pendingName = anvil.getPendingName();
        if (pendingName != null && !pendingName.trim().isEmpty()) {
            return pendingName;
        }

        if (e.getInventory() instanceof AnvilInventory anvilInv) {
            String renameText = anvilInv.getRenameText();
            if (renameText != null && !renameText.trim().isEmpty()) {
                return renameText;
            }
        }

        ItemStack resultItem = e.getCurrentItem();
        if (resultItem == null) {
            resultItem = e.getInventory().getItem(2);
        }
        if (resultItem != null && resultItem.hasItemMeta()) {
            String pdcName = resultItem.getItemMeta().getPersistentDataContainer().get(renameKey, PersistentDataType.STRING);
            if (pdcName != null && !pdcName.trim().isEmpty()) {
                return pdcName;
            }
            if (resultItem.getItemMeta().hasDisplayName()) {
                return resultItem.getItemMeta().getDisplayName();
            }
        }

        return null;
    }

    private String getLiveRenameText(org.bukkit.inventory.InventoryView view) {
        if (view instanceof AnvilView anvilView) {
            String renameText = anvilView.getRenameText();
            if (renameText != null) {
                return renameText;
            }
        }
        return null;
    }

    private String resolveResultName(String renameText, String fallbackDisplayName) {
        if (renameText != null && !renameText.trim().isEmpty()) {
            return renameText.trim();
        }
        if (fallbackDisplayName != null && !fallbackDisplayName.trim().isEmpty()) {
            return StripColouredText(fallbackDisplayName);
        }
        return "";
    }
}
