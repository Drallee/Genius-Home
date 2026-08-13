package me.dralle.home.menu.subMenu;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.PaginatedMenu;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.models.Home;
import me.dralle.home.utils.HomeUtils;
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
        return getConfiguredTitle("GUI.names.home.sounds.title",
                "%name%", name,
                "%displayname%", displayName,
                "%timestamp%", updateTimestamp(),
                "%chat_prefix%", chat_prefix,
                "%home%", homeName,
                "%current%", current_homes,
                "%max%", max_homes);
    }

    @Override
    public int getSlots() {
        return getConfiguredSlots(6);
    }

    @Override
    protected String getMenuId() {
        return "sound-menu";
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
        switch (getAction(e)) {
            case "previous-page":
                    if (page == 0) {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-first-page")));
                    } else {
                        page = page - 1;
                        super.open();
                    }
                break;
            case "next-page":
                    if (!((indexes + 1) >= sounds.size())) {
                        page = page + 1;
                        super.open();
                    } else {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-last-page")));
                    }
                break;
            case "back":
                    new HomeSettingsMenu(playerMenuUtility).open();
                break;
            case "close":
                p.closeInventory();
                break;
            case "sound-no-permission":
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                break;
            default:
                if (getClickedContentIndex(e.getRawSlot()) != null) {
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
        addMenuBorderSounds();
        List<Map<?, ?>> sounds = HomePlugin.getSoundsConfig().getMapList("sounds.list");
        setButton("back", "back");

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
                    item = withAction(createItem("BARRIER", 1, displayName, getConfigMessage("GUI.names.home.settings.change-sound-no-permission-individual")), "sound-no-permission");
                } else {
                    item = withAction(createItemFromConfig(material, 1, displayName, getConfigMessageList("GUI.names.home.sounds.item-lore")), "sound-item");
                }
                inventory.setItem(getContentSlots().get(i), item);
            }
        }
    }
}
