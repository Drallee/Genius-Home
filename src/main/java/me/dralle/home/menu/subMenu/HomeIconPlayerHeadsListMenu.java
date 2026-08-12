package me.dralle.home.menu.subMenu;

import me.dralle.home.menu.PaginatedMenu;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.models.Home;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Objects;

import static me.dralle.home.utils.PlayerHeadUtils.changePlayerHeadSkinByString;
import static me.dralle.home.utils.Utils.*;
import static me.dralle.home.utils.HomeUtils.*;

public class HomeIconPlayerHeadsListMenu extends PaginatedMenu {

    public HomeIconPlayerHeadsListMenu(PlayerMenuUtility playerMenuUtility) {
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
        return ColouredText(rep(getConfigMessage("GUI.names.home.icons.title-heads"),
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
        Home home = playerMenuUtility.getPlayerHome();
        String homeName = home.getHomeName();
        ArrayList<String> icons = getHomeIconsPlayerHeads();

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
                        new HomeIconListMenu(playerMenuUtility).open();
                    } else {
                        new HomeListMenu(playerMenuUtility).open();
                    }
                }
                break;
            case BARRIER:
                p.closeInventory();
                break;
        }
        switch (e.getRawSlot()) {
            case 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43 -> {
                String item = StripColouredText(e.getCurrentItem().getType().toString());
                String skullMetaFinal = "none";
                if(e.getCurrentItem().getType().toString().equals("PLAYER_HEAD")){
                    SkullMeta skull = (SkullMeta) e.getCurrentItem().getItemMeta();
                    if (skull != null) {
                        if (skull.getPlayerProfile() != null) {
                            skullMetaFinal = skull.getPlayerProfile().getName();
                        } else if (skull.getOwningPlayer() != null) {
                            skullMetaFinal = skull.getOwningPlayer().getName();
                        }
                    }
                }
                final String finalSkullMeta = skullMetaFinal;
                setHomeIcon(p, home, item, finalSkullMeta, () -> {
                    playerMenuUtility.updateHomeIconByName(homeName, item, finalSkullMeta);
                    if (playerMenuUtility.getPlayerHome() != null) {
                        new HomeSettingsMenu(playerMenuUtility).open();
                    } else {
                        new HomeListMenu(playerMenuUtility).open();
                    }
                });
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorderPlayerHeads();
        ItemStack back_button = createItem("ARROW", 1 , getConfigMessage("GUI.general.back"), getConfigMessage("GUI.general.back-lore-homes"));
        inventory.setItem(45, back_button);
        ItemStack head = changePlayerHeadSkinByString("MHF_steve", createItem("PLAYER_HEAD", 1, getConfigMessage("GUI.names.home.icons.heads-item"), getConfigMessage("GUI.names.home.icons.heads-item-lore")));
        ItemStack head_alex = changePlayerHeadSkinByString("MHF_alex", createItem("PLAYER_HEAD", 1, getConfigMessage("GUI.names.home.icons.alex-item"), getConfigMessage("GUI.names.home.icons.alex-item-lore")));
        inventory.addItem(head);
        inventory.addItem(head_alex);
        ArrayList<String> icons = getHomeIconsPlayerHeads();
        if(icons != null && !icons.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                int indexLoop = getMaxItemsPerPage() * page + i;
                if(indexLoop >= icons.size()) break;
                if (icons.get(indexLoop) != null){
                    ItemStack item = createItem("PLAYER_HEAD", 1, rep(getConfigMessage("GUI.names.home.icons.generic-icon-name"), "%icon%", icons.get(indexLoop)), getConfigMessage("GUI.names.home.icons.generic-icon-lore"));
                    inventory.addItem(changePlayerHeadSkinByString(icons.get(indexLoop), item));
                }
            }
        }
    }
}
