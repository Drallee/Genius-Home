package me.dralle.home.menu.subMenu;

import me.dralle.home.menu.PaginatedMenu;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.models.Home;
import org.bukkit.ChatColor;
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
        return ColouredText(rep(getConfigMessage("GUI.names.home.icons.title"),
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
                    if (!((indexes + 1) >= icons.size())) {
                        page = page + 1;
                        super.open();
                    } else {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-last-page")));
                    }
                } else if (StripColouredText(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase(StripColouredText(getConfigMessage("GUI.general.back")))) {
                    if (playerMenuUtility.getPlayerHome() != null) {
                        new HomeSettingsMenu(playerMenuUtility).open();
                    } else {
                        new HomeListMenu(playerMenuUtility).open();
                    }
                }
                break;
            case BARRIER:
                p.closeInventory();
                break;
        }
        
        // Handle icon selection
        int slot = e.getRawSlot();
        if ((slot >= 10 && slot <= 16) || (slot >= 19 && slot <= 25) || (slot >= 28 && slot <= 34) || (slot >= 37 && slot <= 43)) {
            int row = slot / 9;
            int col = slot % 9;
            int indexInPage = (row - 1) * 7 + (col - 1);
            int actualIndex = page * getMaxItemsPerPage() + indexInPage;

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
        ItemStack back_button = createItem("ARROW", 1 , getConfigMessage("GUI.general.back"), getConfigMessage("GUI.general.back-lore-homes"));
        inventory.setItem(45, back_button);
        if(icons != null && !icons.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                int indexLoop = getMaxItemsPerPage() * page + i;
                if(indexLoop >= icons.size()) break;
                String materialName = icons.get(indexLoop);
                if (materialName != null){
                    String displayName = getIconDisplayName(materialName);
                    ItemStack item = createItem(materialName, 1, rep(getConfigMessage("GUI.names.home.icons.generic-icon-name"), "%icon%", displayName), getConfigMessage("GUI.names.home.icons.generic-icon-lore"));
                    inventory.addItem(item);
                }
            }
        }
    }
}
