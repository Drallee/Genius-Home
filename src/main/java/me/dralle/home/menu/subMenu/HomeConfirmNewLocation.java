package me.dralle.home.menu.subMenu;

import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.menu.Menu;
import me.dralle.home.models.Home;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static me.dralle.home.utils.PlayerHeadUtils.changePlayerHeadSkinByString;
import static me.dralle.home.utils.Utils.*;
import static me.dralle.home.utils.HomeUtils.*;

public class HomeConfirmNewLocation extends Menu {

    public HomeConfirmNewLocation(PlayerMenuUtility playerMenuUtility) {
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
        return ColouredText(rep(getConfigMessage("GUI.names.home.location-confirm.title"),
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
        return 45;
    }

    @Override
    public void open() {
        Player p = playerMenuUtility.getOwner();
        OfflinePlayer target = playerMenuUtility.getTarget();
        boolean isOwner = p.getUniqueId().equals(target.getUniqueId());

        if (!isOwner && !p.hasPermission("genius.homes.others.settings.new.location") && !p.hasPermission("genius.others.settings")) {
            p.sendMessage(ColouredText(getErrorMessagePermission()));
            return;
        }
        super.open();
    }

    @Override
    public void handleMenuItems(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Home home = playerMenuUtility.getPlayerHome();
        Location playerLocation = p.getLocation();
        if (e.getCurrentItem() == null) return;
        switch (e.getCurrentItem().getType()){
            case LIME_CONCRETE -> {
                changeHomeLocation(p, home, playerLocation, () -> {
                    playerMenuUtility.setPlayerHomes(getPlayerHomesList(playerMenuUtility.getTarget(), p, "CNL"));
                    playerMenuUtility.setHomeToChange(home);
                    new HomeSettingsMenu(playerMenuUtility).open();
                });
            }
            case RED_CONCRETE, ARROW -> new HomeSettingsMenu(playerMenuUtility).open();
            case BARRIER -> e.getWhoClicked().closeInventory();
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
        Location playerLocation = p.getLocation();
        String x = df.format(location.getX());
        String y = df.format(location.getY());
        String z = df.format(location.getZ());
        String world = location.getWorld().getName();

        String new_x = df.format(playerLocation.getX());
        String new_y = df.format(playerLocation.getY());
        String new_z = df.format(playerLocation.getZ());
        String new_world = playerLocation.getWorld().getName();

        ItemStack info_item = createItemFromConfig(homeIconType, 1 , rep(getConfigMessage("GUI.names.home.settings.info-name"), "%home%", homeName),
                getConfigMessageList("GUI.names.home.location-confirm.info-lore").stream().map(s -> rep(s, "%x%", x, "%y%", y, "%z%", z, "%world%", world, "%new_x%", new_x, "%new_y%", new_y, "%new_z%", new_z, "%new_world%", new_world)).toList());
        ItemStack confirm_button = createItemFromConfig("LIME_CONCRETE", 1, getConfigMessage("GUI.names.home.location-confirm.confirm"), getConfigMessageList("GUI.names.home.location-confirm.confirm-lore"));
        ItemStack cancel_button = createItem("RED_CONCRETE", 1, getConfigMessage("GUI.names.home.location-confirm.cancel"), "");
        ItemStack back_button = createItem("ARROW", 1 , getConfigMessage("GUI.general.back"), getConfigMessage("GUI.general.back-lore-settings"));
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
        inventory.setItem(20, confirm_button);
        inventory.setItem(24, cancel_button);
        inventory.setItem(39, back_button);
        inventory.setItem(40, close_button);

        setFillerGlass();
    }
}
