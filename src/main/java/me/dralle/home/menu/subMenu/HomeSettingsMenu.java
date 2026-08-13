package me.dralle.home.menu.subMenu;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.Menu;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.models.Home;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.dralle.home.utils.PlayerHeadUtils.changePlayerHeadSkinByString;
import static me.dralle.home.utils.Utils.*;
import static me.dralle.home.utils.HomeUtils.*;

public class HomeSettingsMenu extends Menu {

    public HomeSettingsMenu(PlayerMenuUtility playerMenuUtility) {
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
        int max_homes = getMaxHomes(target);
        int current_homes = playerMenuUtility.getPlayerHomes().size();
        return getConfiguredTitle("GUI.names.home.settings.title",
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
        return "home-settings-menu";
    }

    @Override
    public void handleMenuItems(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        OfflinePlayer target = playerMenuUtility.getTarget();
        Home home = playerMenuUtility.getPlayerHome();
        String homeName = home.getHomeName();

        if (e.getCurrentItem() == null) return;

        boolean isOwner = p.getUniqueId().equals(target.getUniqueId());

        switch (getAction(e)) {
            case "change-icon" -> {
                if (isOwner || p.hasPermission("genius.homes.others.settings.change.icons") || p.hasPermission("genius.others.settings")) {
                    new HomeIconListMenu(playerMenuUtility).open();
                } else {
                    p.sendMessage(ColouredText(getErrorMessagePermission()));
                }
            }
            case "rename-home" -> HomePlugin.getHomeTextInputService().openRenameHomeInput(p, playerMenuUtility);
            case "delete-home" -> {
                if (isOwner || p.hasPermission("genius.homes.others.settings.delete") || p.hasPermission("genius.others.settings")) {
                    new HomeConfirmDeleteMenu(playerMenuUtility).open();
                } else {
                    p.sendMessage(ColouredText(getErrorMessagePermission()));
                }
            }
            case "teleport-home" -> {
                if (!isOwner && !p.hasPermission("genius.homes.others.teleport")) {
                    p.sendMessage(ColouredText(getErrorMessagePermission()));
                    return;
                }
                teleportToHome(p, target, home);
            }
            case "change-sound" -> {
                if ((isOwner || p.hasPermission("genius.homes.others.settings.change.sounds") || p.hasPermission("genius.others.settings")) && HomePlugin.getSoundsConfig().getBoolean("sounds.enabled")) {
                    if (isOwner && getConfigCheck("settings.homes.sound-permission") && !p.hasPermission("genius.homes.sound")) {
                        p.sendMessage(ColouredText(getErrorMessagePermission()));
                        return;
                    }
                    new HomeSoundListMenu(playerMenuUtility).open();
                } else if (HomePlugin.getSoundsConfig().getBoolean("sounds.enabled")) {
                    p.sendMessage(ColouredText(getErrorMessagePermission()));
                }
            }
            case "set-location" -> {
                if (isOwner || p.hasPermission("genius.homes.others.settings.new.location") || p.hasPermission("genius.others.settings")) {
                    new HomeConfirmNewLocation(playerMenuUtility).open();
                } else {
                    p.sendMessage(ColouredText(getErrorMessagePermission()));
                }
            }
            case "back" -> new HomeListMenu(playerMenuUtility).open();
            case "close" -> e.getWhoClicked().closeInventory();
            case "disabled-rename" -> HomePlugin.getHomeTextInputService().openRenameHomeInput(p, playerMenuUtility);
            case "disabled-sound" -> p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            default -> {
                if (e.getCurrentItem().getType() == org.bukkit.Material.BARRIER) {
                if (e.getSlot() == 31) {
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1f, 1f);
                } else if (e.getSlot() == 33) {
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                } else {
                    e.getWhoClicked().closeInventory();
                }
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        Player p = playerMenuUtility.getOwner();
        Home home = playerMenuUtility.getPlayerHome();

        String homeName = home.getHomeName();
        String homeIconType = home.getIconType();
        String homeSkullMeta = home.getSkullMeta();

        Location location = home.getLocation();
        String playerLocationX = df.format(p.getLocation().getX());
        String playerLocationY = df.format(p.getLocation().getY());
        String playerLocationZ = df.format(p.getLocation().getZ());
        String playerWorld = p.getLocation().getWorld().getName();
        String x = df.format(location.getX());
        String y = df.format(location.getY());
        String z = df.format(location.getZ());
        String world = location.getWorld().getName();

        ItemStack info_item = createConfiguredItem("buttons.info", "info", "%home%", homeName, "%x%", x, "%y%", y, "%z%", z, "%world%", world);
        
        List<String> teleportLore = new ArrayList<>(getConfigMessageList("GUI.names.home.settings.teleport-lore"));
        if (getConfigCheck("settings.homes.teleport.cost.enabled")) {
            String costType = getConfigString("settings.homes.teleport.cost.type");
            String currency = "";
            int amount = getConfigNumber("settings.homes.teleport.cost.amount");
            if (costType.equalsIgnoreCase("VAULT")) {
                currency = me.dralle.home.utils.EconomyUtils.getCurrencyName();
            } else if (costType.equalsIgnoreCase("XP")) {
                currency = "XP";
            } else if (costType.equalsIgnoreCase("LEVEL")) {
                currency = "levels";
            } else if (costType.equalsIgnoreCase("ITEM")) {
                currency = getConfigString("settings.homes.teleport.cost.item");
            }
            
            final String finalCurrency = currency;
            teleportLore.addAll(getConfigMessageList("GUI.general.home-item-cost-lore").stream()
                    .map(s -> rep(s, "%amount%", amount, "%currency%", finalCurrency)).toList());
        }

        int time = getCooldownTime(p);
        if (time > 0) {
            teleportLore.addAll(getConfigMessageList("GUI.general.home-item-cooldown-lore").stream()
                    .map(s -> rep(s, "%time%", time)).toList());
        }

        ItemStack teleport_button = createConfiguredItem("buttons.teleport", "teleport-home", "%teleport_details%", String.join("\n", teleportLore));
        
        String soundDisplayName = getSoundDisplayName(home.getSound());
        ItemStack change_teleport_sound_button = createConfiguredItem("buttons.change-sound", "change-sound", "%sound%", soundDisplayName);
        if (!HomePlugin.getSoundsConfig().getBoolean("sounds.enabled")) {
            change_teleport_sound_button = withAction(createItem("BARRIER", 1, getConfigMessage("GUI.names.home.settings.change-sound"), "&cThis feature is disabled"), "disabled-sound");
        } else if (getConfigCheck("settings.homes.sound-permission") && !p.hasPermission("genius.homes.sound")) {
            change_teleport_sound_button = withAction(createItem("BARRIER", 1, getConfigMessage("GUI.names.home.settings.change-sound"),
                    getConfigMessage("GUI.names.home.settings.change-sound-no-permission"), "&7Current: &b" + soundDisplayName), "disabled-sound");
        }

        Integer infoSlot = HomePlugin.getMenuConfigManager().getSlot(getMenuId(), "buttons.info.slot", getSlots(), 4);
        inventory.setItem(infoSlot, homeIconType.equals("PLAYER_HEAD") && !Objects.equals(homeSkullMeta, "none") ? changePlayerHeadSkinByString(homeSkullMeta, info_item) : info_item);
        inventory.setItem(HomePlugin.getMenuConfigManager().getSlot(getMenuId(), "buttons.teleport.slot", getSlots(), 20), teleport_button);
        setButton("set-location", "set-location", "%x%", x, "%y%", y, "%z%", z, "%world%", world, "%new_x%", playerLocationX, "%new_y%", playerLocationY, "%new_z%", playerLocationZ, "%new_world%", playerWorld);
        setButton("change-icon", "change-icon");
        setButton("rename", "rename-home", "%home%", homeName);
        setButton("delete", "delete-home");
        inventory.setItem(HomePlugin.getMenuConfigManager().getSlot(getMenuId(), "buttons.change-sound.slot", getSlots(), 33), change_teleport_sound_button);
        setButton("back", "back");
        setButton("close", "close");

        setFillerGlass();
    }
}
