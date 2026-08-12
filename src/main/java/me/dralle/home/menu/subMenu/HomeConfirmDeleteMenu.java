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
        return getConfiguredTitle("GUI.names.home.delete-confirm.title",
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
        return getConfiguredSlots(5);
    }

    @Override
    protected String getMenuId() {
        return "delete-confirm-menu";
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
        switch (getAction(e)){
            case "confirm-delete":
                deleteHome(p, playerMenuUtility.getPlayerHome(), () -> {
                    playerMenuUtility.setPlayerHomes(getPlayerHomesList(playerMenuUtility.getTarget(), p, "CDM"));
                    new HomeListMenu(playerMenuUtility).open();
                });
                break;
            case "cancel":
            case "back":
                new HomeSettingsMenu(playerMenuUtility).open();
                break;
            case "close":
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
        ItemStack info_item = createConfiguredItem("buttons.info", "info", "%home%", homeName, "%x%", x, "%y%", y, "%z%", z, "%world%", world);
        Integer infoSlot = me.dralle.home.HomePlugin.getMenuConfigManager().getSlot(getMenuId(), "buttons.info.slot", getSlots(), 4);
        if(homeIconType.equals("PLAYER_HEAD")){
            if(!Objects.equals(homeSkullMeta, "none")){
                inventory.setItem(infoSlot, changePlayerHeadSkinByString(homeSkullMeta, info_item));
            }else{
                inventory.setItem(infoSlot, info_item);
            }
        }else{
            inventory.setItem(infoSlot, info_item);
        }
        setButton("confirm", "confirm-delete");
        setButton("cancel", "cancel");
        setButton("back", "back");
        setButton("close", "close");

        setFillerGlass();
    }
}
