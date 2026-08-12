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

public class HomeConfirmDeleteMenu extends Menu {

    public HomeConfirmDeleteMenu(PlayerMenuUtility playerMenuUtility) {
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
        return ColouredText(rep(getConfigMessage("GUI.names.home.delete-confirm.title"),
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

        if (!isOwner && !p.hasPermission("genius.homes.others.settings.delete") && !p.hasPermission("genius.others.settings")) {
            p.sendMessage(ColouredText(getErrorMessagePermission()));
            return;
        }
        super.open();
    }

    @Override
    public void handleMenuItems(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        switch (e.getCurrentItem().getType()){
            case LIME_CONCRETE:
                deleteHome(p, playerMenuUtility.getPlayerHome(), () -> {
                    playerMenuUtility.setPlayerHomes(getPlayerHomesList(playerMenuUtility.getTarget(), p, "CDM"));
                    new HomeListMenu(playerMenuUtility).open();
                });
                break;
            case RED_CONCRETE:
            case ARROW:
                new HomeSettingsMenu(playerMenuUtility).open();
                break;
            case BARRIER:
                e.getWhoClicked().closeInventory();
                break;
        }
    }

    @Override
    public void setMenuItems() {
        Home home = playerMenuUtility.getPlayerHome();

        String homeName = home.getHomeName();
        String homeIconType = home.getIconType();
        String homeSkullMeta = home.getSkullMeta();

        Location location = home.getLocation();
        String x = df.format(location.getX());
        String y = df.format(location.getY());
        String z = df.format(location.getZ());
        String world = location.getWorld().getName();
        ItemStack info_item = createItemFromConfig(homeIconType, 1 , rep(getConfigMessage("GUI.names.home.settings.info-name"), "%home%", homeName),
                getConfigMessageList("GUI.names.home.settings.info-lore").stream().map(s -> rep(s, "%x%", x, "%y%", y, "%z%", z, "%world%", world)).toList());
        ItemStack confirm_button = createItemFromConfig("LIME_CONCRETE", 1, getConfigMessage("GUI.names.home.delete-confirm.confirm"), getConfigMessageList("GUI.names.home.delete-confirm.confirm-lore"));
        ItemStack cancel_button = createItem("RED_CONCRETE", 1, getConfigMessage("GUI.names.home.delete-confirm.cancel"), "");
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
