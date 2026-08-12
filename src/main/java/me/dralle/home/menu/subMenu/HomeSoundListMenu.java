package me.dralle.home.menu.subMenu;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.PaginatedMenu;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.models.Home;
import me.dralle.home.utils.HomeUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.dralle.home.utils.Utils.*;

public class HomeSoundListMenu extends PaginatedMenu {

    public HomeSoundListMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        Player p = playerMenuUtility.getOwner();
        OfflinePlayer target = playerMenuUtility.getTarget();
        String name = p.getName();
        String displayName = p.getDisplayName();
        String chat_prefix = getConfigMessage("chat.prefix.home");
        Home home = playerMenuUtility.getPlayerHome();
        String homeName = home.getHomeName();
        int max_homes = HomeUtils.getMaxHomes(target);
        int current_homes = playerMenuUtility.getPlayerHomes().size();
        return ColouredText(rep(getConfigMessage("GUI.names.home.sounds.title"),
                "%name%", name,
                "%displayname%", displayName,
                "%timestamp%", updateTimestamp(),
                "%chat_prefix%", chat_prefix,
                "%home%", homeName,
                "%current%", current_homes,
                "%max%", max_homes));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void open() {
        Player p = playerMenuUtility.getOwner();
        OfflinePlayer target = playerMenuUtility.getTarget();
        boolean isOwner = p.getUniqueId().equals(target.getUniqueId());

        if (isOwner) {
            if (getConfigCheck("settings.homes.sound-permission") && !p.hasPermission("genius.homes.sound")) {
                p.sendMessage(ColouredText(getErrorMessagePermission()));
                return;
            }
        } else {
            if (!p.hasPermission("genius.homes.others.settings.change.sounds") && !p.hasPermission("genius.others.settings")) {
                p.sendMessage(ColouredText(getErrorMessagePermission()));
                return;
            }
        }
        super.open();
    }

    @Override
    public void handleMenuItems(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Home home = playerMenuUtility.getPlayerHome();
        List<Map<?, ?>> sounds = HomePlugin.getSoundsConfig().getMapList("sounds.list");

        if (e.getCurrentItem() == null) return;
        switch (Objects.requireNonNull(e.getCurrentItem()).getType()) {
            case ARROW:
                if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase(StripColouredText(getConfigMessage("GUI.general.previous-page")))) {
                    if (page == 0) {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-first-page")));
                    } else {
                        page = page - 1;
                        super.open();
                    }
                } else if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase(StripColouredText(getConfigMessage("GUI.general.next-page")))) {
                    if (!((indexes + 1) >= sounds.size())) {
                        page = page + 1;
                        super.open();
                    } else {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-last-page")));
                    }
                } else if (StripColouredText(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase(StripColouredText(getConfigMessage("GUI.general.back")))) {
                    new HomeSettingsMenu(playerMenuUtility).open();
                }
                break;
            case BARRIER:
                if (e.getRawSlot() >= 10 && e.getRawSlot() <= 43) {
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                } else {
                    p.closeInventory();
                }
                break;
            default:
                if (e.getRawSlot() >= 10 && e.getRawSlot() <= 43) {
                    // Let's just find it by display name from the config list.
                    String clickedDisplayName = StripColouredText(e.getCurrentItem().getItemMeta().getDisplayName());
                    for (Map<?, ?> soundMap : sounds) {
                        String dsName = StripColouredText(ColouredText((String) soundMap.get("display_name")));
                        if (dsName.equalsIgnoreCase(clickedDisplayName)) {
                            String soundName = (String) soundMap.get("name");

                            OfflinePlayer target = playerMenuUtility.getTarget();
                            boolean hasSoundPerm;
                            if (target.isOnline() && target.getPlayer() != null) {
                                hasSoundPerm = target.getPlayer().hasPermission("genius.homes.sounds." + soundName.toLowerCase());
                            } else {
                                hasSoundPerm = p.hasPermission("genius.homes.sounds." + soundName.toLowerCase());
                            }

                            if (getConfigCheck("settings.homes.per-sound-permission") && !hasSoundPerm) {
                                p.sendMessage(ColouredText(getConfigMessage("GUI.names.home.settings.change-sound-no-permission-individual")));
                                return;
                            }

                            if (e.isLeftClick()) {
                                HomeUtils.setHomeSound(p, home, soundName, () -> {
                                    home.setSound(soundName);
                                    new HomeSettingsMenu(playerMenuUtility).open();
                                });
                            } else if (e.isRightClick()) {
                                try {
                                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                                    p.playSound(p.getLocation(), sound, 1f, 1f);
                                } catch (IllegalArgumentException ex) {
                                    // Fallback or ignore if sound name is invalid for Bukkit version
                                }
                            }
                            return;
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void setMenuItems() {
        getSoundsIndexes();
        // Menu Border logic similar to addMenuBorderHomeIcons but for sounds
        if(page != 0){
            String displayName = getConfigMessage("GUI.general.previous-page");
            String lore = rep(getConfigMessage("GUI.general.page-lore"), "%current%", (page + 1), "%total%", pages);
            inventory.setItem(48, createItem("ARROW", 1, displayName, lore));
        }
        List<Map<?, ?>> sounds = HomePlugin.getSoundsConfig().getMapList("sounds.list");
        if (!((indexes + 1) >= sounds.size())){
            String displayName = getConfigMessage("GUI.general.next-page");
            String lore = rep(getConfigMessage("GUI.general.page-lore"), "%current%", (page + 1), "%total%", pages);
            inventory.setItem(50, createItem("ARROW", 1, displayName, lore));
        }
        inventory.setItem(49, createItem("BARRIER", 1, getConfigMessage("GUI.general.close")));
        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
        inventory.setItem(17, FILLER_GLASS);
        inventory.setItem(18, FILLER_GLASS);
        inventory.setItem(26, FILLER_GLASS);
        inventory.setItem(27, FILLER_GLASS);
        inventory.setItem(35, FILLER_GLASS);
        inventory.setItem(36, FILLER_GLASS);
        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }

        ItemStack back_button = createItem("ARROW", 1 , getConfigMessage("GUI.general.back"), getConfigMessage("GUI.general.back-lore-settings"));
        inventory.setItem(45, back_button);

        if(!sounds.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                int indexLoop = getMaxItemsPerPage() * page + i;
                if(indexLoop >= sounds.size()) break;
                Map<?, ?> soundMap = sounds.get(indexLoop);
                String soundName = (String) soundMap.get("name");
                String displayName = (String) soundMap.get("display_name");
                String material = (String) soundMap.get("material");
                
                ItemStack item;
                OfflinePlayer target = playerMenuUtility.getTarget();
                boolean hasSoundPerm;
                if (target.isOnline() && target.getPlayer() != null) {
                    hasSoundPerm = target.getPlayer().hasPermission("genius.homes.sounds." + soundName.toLowerCase());
                } else {
                    hasSoundPerm = playerMenuUtility.getOwner().hasPermission("genius.homes.sounds." + soundName.toLowerCase());
                }

                if (getConfigCheck("settings.homes.per-sound-permission") && !hasSoundPerm) {
                    item = createItem("BARRIER", 1, displayName, getConfigMessage("GUI.names.home.settings.change-sound-no-permission-individual"));
                } else {
                    item = createItemFromConfig(material, 1, displayName, getConfigMessageList("GUI.names.home.sounds.item-lore"));
                }
                inventory.addItem(item);
            }
        }
    }
}
