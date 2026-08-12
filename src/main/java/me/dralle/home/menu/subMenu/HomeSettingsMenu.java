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
        return ColouredText(rep(getConfigMessage("GUI.names.home.settings.title"),
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
    public void handleMenuItems(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        OfflinePlayer target = playerMenuUtility.getTarget();
        Home home = playerMenuUtility.getPlayerHome();
        String homeName = home.getHomeName();

        if (e.getCurrentItem() == null) return;

        boolean isOwner = p.getUniqueId().equals(target.getUniqueId());

        switch (e.getCurrentItem().getType()) {
            case ITEM_FRAME -> {
                if (isOwner || p.hasPermission("genius.homes.others.settings.change.icons") || p.hasPermission("genius.others.settings")) {
                    new HomeIconListMenu(playerMenuUtility).open();
                } else {
                    p.sendMessage(ColouredText(getErrorMessagePermission()));
                }
            }
            case NAME_TAG -> {
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1f, 1f);
                p.sendMessage(ColouredText(getConfigMessage("chat.prefix.error") + " &cRenaming via GUI is temporarily disabled."));
            }
            case TNT -> {
                if (isOwner || p.hasPermission("genius.homes.others.settings.delete") || p.hasPermission("genius.others.settings")) {
                    new HomeConfirmDeleteMenu(playerMenuUtility).open();
                } else {
                    p.sendMessage(ColouredText(getErrorMessagePermission()));
                }
            }
            case ENDER_PEARL -> {
                if (!isOwner && !p.hasPermission("genius.homes.others.teleport")) {
                    p.sendMessage(ColouredText(getErrorMessagePermission()));
                    return;
                }
                teleportToHome(p, target, home);
            }
            case NOTE_BLOCK -> {
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
            case RED_BANNER -> {
                if (isOwner || p.hasPermission("genius.homes.others.settings.new.location") || p.hasPermission("genius.others.settings")) {
                    new HomeConfirmNewLocation(playerMenuUtility).open();
                } else {
                    p.sendMessage(ColouredText(getErrorMessagePermission()));
                }
            }
            case ARROW -> new HomeListMenu(playerMenuUtility).open();
            case BARRIER -> {
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

        ItemStack info_item = createItemFromConfig(homeIconType, 1 , rep(getConfigMessage("GUI.names.home.settings.info-name"), "%home%", homeName),
                getConfigMessageList("GUI.names.home.settings.info-lore").stream().map(s -> rep(s, "%x%", x, "%y%", y, "%z%", z, "%world%", world)).toList());
        ItemStack delete_button = createItem("TNT", 1 , getConfigMessage("GUI.names.home.settings.delete"), getConfigMessage("GUI.names.home.settings.delete-lore"));
        ItemStack set_new_location = createItemFromConfig("RED_BANNER", 1 , getConfigMessage("GUI.names.home.settings.set-location"),
                getConfigMessageList("GUI.names.home.settings.set-location-lore").stream().map(s -> rep(s, "%x%", x, "%y%", y, "%z%", z, "%world%", world, "%new_x%", playerLocationX, "%new_y%", playerLocationY, "%new_z%", playerLocationZ, "%new_world%", playerWorld)).toList());
        ItemStack change_icon = createItem("ITEM_FRAME", 1, getConfigMessage("GUI.names.home.settings.change-icon"), getConfigMessage("GUI.names.home.settings.change-icon-lore"));
        ItemStack change_name = createItem("BARRIER", 1, getConfigMessage("GUI.names.home.settings.change-name"),
                "&7Current: &b" + homeName,
                "",
                "&cTemporarily disabled");
        
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

        ItemStack teleport_button = createItemFromConfig("ENDER_PEARL", 1 , getConfigMessage("GUI.names.home.settings.teleport"), teleportLore);
        
        String soundDisplayName = getSoundDisplayName(home.getSound());
        ItemStack change_teleport_sound_button = createItemFromConfig("NOTE_BLOCK", 1 , getConfigMessage("GUI.names.home.settings.change-sound"),
                getConfigMessageList("GUI.names.home.settings.change-sound-lore").stream().map(s -> rep(s, "%sound%", soundDisplayName)).toList());
        if (!HomePlugin.getSoundsConfig().getBoolean("sounds.enabled")) {
            change_teleport_sound_button = createItem("BARRIER", 1, getConfigMessage("GUI.names.home.settings.change-sound"), "&cThis feature is disabled");
        } else if (getConfigCheck("settings.homes.sound-permission") && !p.hasPermission("genius.homes.sound")) {
            change_teleport_sound_button = createItem("BARRIER", 1, getConfigMessage("GUI.names.home.settings.change-sound"),
                    getConfigMessage("GUI.names.home.settings.change-sound-no-permission"), "&7Current: &b" + soundDisplayName);
        }
        ItemStack back_button = createItem("ARROW", 1 , getConfigMessage("GUI.general.back"), getConfigMessage("GUI.general.back-lore-homes"));
        ItemStack close_button = createItem("BARRIER", 1, getConfigMessage("GUI.general.close"));
        if(homeIconType.equals("PLAYER_HEAD")){
            if(!Objects.equals(homeSkullMeta, "none")){
                inventory.setItem(4, changePlayerHeadSkinByString(homeSkullMeta, info_item));
            }else{
                inventory.setItem(4, info_item);
            }
        }else{
            inventory.setItem(4, info_item);
        }

        inventory.setItem(20, teleport_button);
        inventory.setItem(29, set_new_location);

        inventory.setItem(22, change_icon);
        inventory.setItem(31, change_name);

        inventory.setItem(24, delete_button);
        inventory.setItem(33, change_teleport_sound_button);

        inventory.setItem(45, back_button);
        inventory.setItem(49, close_button);

        setFillerGlass();
    }
}
