package me.dralle.home.menu.subMenu;

import me.dralle.home.menu.PaginatedMenu;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.models.Home;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

import static me.dralle.home.utils.Utils.*;
import static me.dralle.home.utils.HomeUtils.*;

public class HomeIconListMenu extends PaginatedMenu {

    public HomeIconListMenu(PlayerMenuUtility playerMenuUtility) {
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
        return getConfiguredTitle("GUI.names.home.icons.title",
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
        return "icon-menu";
    }

    @Override
    public void open() {
        Player p = playerMenuUtility.getOwner();
        OfflinePlayer target = playerMenuUtility.getTarget();
        boolean isOwner = p.getUniqueId().equals(target.getUniqueId());

        if (!isOwner && !p.hasPermission("genius.homes.others.settings.change.icons") && !p.hasPermission("genius.others.settings")) {
            p.sendMessage(ColouredText(getErrorMessagePermission()));
            return;
        }
        super.open();
    }

    @Override
    public void handleMenuItems(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Home home = playerMenuUtility.getPlayerHome();
        String homeName = home.getHomeName();
        ArrayList<String> icons = getHomeIcons();

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
                    if (!((indexes + 1) >= icons.size())) {
                        page = page + 1;
                        super.open();
                    } else {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-last-page")));
                    }
                break;
            case "back":
                    if (playerMenuUtility.getPlayerHome() != null) {
                        new HomeSettingsMenu(playerMenuUtility).open();
                    } else {
                        new HomeListMenu(playerMenuUtility).open();
                    }
                break;
            case "close":
                p.closeInventory();
                break;
        }
        
        Integer actualIndex = getClickedContentIndex(e.getRawSlot());
        if (actualIndex != null) {

            if (actualIndex >= icons.size()) return;
            String materialName = icons.get(actualIndex);

            if(!materialName.equals("PLAYER_HEAD")){
                String skullMeta = "none";
                setHomeIcon(p, home, materialName, skullMeta, () -> {
                    playerMenuUtility.updateHomeIconByName(homeName, materialName, skullMeta);
                    if (playerMenuUtility.getPlayerHome() != null) {
                        new HomeSettingsMenu(playerMenuUtility).open();
                    } else {
                        new HomeListMenu(playerMenuUtility).open();
                    }
                });
            }else{
                new HomeIconPlayerHeadsListMenu(playerMenuUtility).open();
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorderHomeIcons();
        ArrayList<String> icons = getHomeIcons();
        setButton("back", "back");
        if(icons != null && !icons.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                int indexLoop = getMaxItemsPerPage() * page + i;
                if(indexLoop >= icons.size()) break;
                String materialName = icons.get(indexLoop);
                if (materialName != null){
                    String displayName = getIconDisplayName(materialName);
                    ItemStack item = withAction(createItem(materialName, 1, rep(getConfigMessage("GUI.names.home.icons.generic-icon-name"), "%icon%", displayName), getConfigMessage("GUI.names.home.icons.generic-icon-lore")), "icon-item");
                    inventory.setItem(getContentSlots().get(i), item);
                }
            }
        }
    }
}
