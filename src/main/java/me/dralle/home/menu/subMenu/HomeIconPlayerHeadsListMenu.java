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
        return getConfiguredTitle("GUI.names.home.icons.title-heads",
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
        return "player-heads-menu";
    }

    @Override
    public void handleMenuItems(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Home home = playerMenuUtility.getPlayerHome();
        String homeName = home.getHomeName();
        ArrayList<String> icons = getHomeIconsPlayerHeads();

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
                        new HomeIconListMenu(playerMenuUtility).open();
                    } else {
                        new HomeListMenu(playerMenuUtility).open();
                    }
                break;
            case "close":
                p.closeInventory();
                break;
        }
        if (getClickedContentIndex(e.getRawSlot()) != null) {
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

    @Override
    public void setMenuItems() {
        addMenuBorderPlayerHeads();
        setButton("back", "back");
        int slotIndex = 0;
        ItemStack head = withAction(changePlayerHeadSkinByString("MHF_steve", createItem("PLAYER_HEAD", 1, getConfigMessage("GUI.names.home.icons.heads-item"), getConfigMessage("GUI.names.home.icons.heads-item-lore"))), "head-item");
        ItemStack head_alex = withAction(changePlayerHeadSkinByString("MHF_alex", createItem("PLAYER_HEAD", 1, getConfigMessage("GUI.names.home.icons.alex-item"), getConfigMessage("GUI.names.home.icons.alex-item-lore"))), "head-item");
        inventory.setItem(getContentSlots().get(slotIndex++), head);
        inventory.setItem(getContentSlots().get(slotIndex++), head_alex);
        ArrayList<String> icons = getHomeIconsPlayerHeads();
        if(icons != null && !icons.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage() - slotIndex; i++) {
                int indexLoop = getMaxItemsPerPage() * page + i;
                if(indexLoop >= icons.size()) break;
                if (icons.get(indexLoop) != null){
                    ItemStack item = createItem("PLAYER_HEAD", 1, rep(getConfigMessage("GUI.names.home.icons.generic-icon-name"), "%icon%", icons.get(indexLoop)), getConfigMessage("GUI.names.home.icons.generic-icon-lore"));
                    inventory.setItem(getContentSlots().get(slotIndex + i), withAction(changePlayerHeadSkinByString(icons.get(indexLoop), item), "head-item"));
                }
            }
        }
    }
}
