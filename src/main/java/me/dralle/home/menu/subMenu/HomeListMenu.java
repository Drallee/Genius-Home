package me.dralle.home.menu.subMenu;

import me.dralle.home.models.Home;
import me.dralle.home.menu.PaginatedMenu;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.utils.PlayerHeadUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

import static me.dralle.home.utils.Utils.*;
import static me.dralle.home.utils.HomeUtils.*;

public class HomeListMenu extends PaginatedMenu {

    public HomeListMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        Player p = playerMenuUtility.getOwner();
        OfflinePlayer target = playerMenuUtility.getTarget();
        String name = p.getName();
        String target_name = target.getName();
        String displayName = p.getDisplayName();
        String chat_prefix = getConfigMessage("chat.prefix.home");
        int max_homes = getMaxHomes(target);
        int current_homes = playerMenuUtility.getPlayerHomes().size();
        String title;
        title = ColouredText(rep(getConfigMessage("GUI.names.home.list.homes"),
                "%name%", name,
                "%displayname%", displayName,
                "%timestamp%", updateTimestamp(),
                "%chat_prefix%", chat_prefix,
                "%current%", current_homes,
                "%max%", max_homes));
        if (!p.getUniqueId().equals(target.getUniqueId())){
            title = ColouredText(rep(getConfigMessage("GUI.names.home.others.list"),
                    "%name%", name,
                    "%displayname%", displayName,
                    "%timestamp%", updateTimestamp(),
                    "%target%", target_name,
                    "%chat_prefix%", chat_prefix,
                    "%current%", current_homes,
                    "%max%", max_homes));

        }
        return title;
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenuItems(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        OfflinePlayer target = playerMenuUtility.getTarget();
        List<Home> homes = playerMenuUtility.getPlayerHomes();

        if (e.getCurrentItem() == null) return;

        switch (e.getCurrentItem().getType()){
            case ARROW:
                String displayName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                if (displayName.equalsIgnoreCase(StripColouredText(getConfigMessage("GUI.general.previous-page")))){
                    if (page > 0){
                        page--;
                        super.open();
                    } else {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-first-page")));
                    }
                } else if (displayName.equalsIgnoreCase(StripColouredText(getConfigMessage("GUI.general.next-page")))){
                    if ((indexes + 1) < homes.size()){
                        page++;
                        super.open();
                    } else {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-last-page")));
                    }
                }
                break;
            case WHITE_BED:
                if(homes.isEmpty()){
                    setHome(p, "Default", "RED_BED", "none", () -> {
                        playerMenuUtility.setPlayerHomes(getPlayerHomesList(p, p, "PHL"));
                        new HomeListMenu(playerMenuUtility).open();
                    });
                }
                break;
            case BARRIER:
                p.closeInventory();
                break;
        }

        if (e.getRawSlot() >= 10 && e.getRawSlot() <= 43) {
            String itemDisplayName = StripColouredText(e.getCurrentItem().getItemMeta().getDisplayName());
            Home selectedHome = playerMenuUtility.getHomeByName(itemDisplayName);
            if (selectedHome != null) {
                boolean isOwner = p.getUniqueId().equals(target.getUniqueId());
                switch (e.getClick()) {
                    case LEFT -> {
                        if (!isOwner && !p.hasPermission("genius.homes.others.teleport")) {
                            p.sendMessage(ColouredText(getErrorMessagePermission()));
                            return;
                        }
                        teleportToHome(p, target, selectedHome);
                    }
                    case RIGHT -> {
                        boolean canSettings = isOwner || p.hasPermission("genius.others.settings") || 
                                p.hasPermission("genius.homes.others.settings.delete") ||
                                p.hasPermission("genius.homes.others.settings.rename") ||
                                p.hasPermission("genius.homes.others.settings.change.sounds") ||
                                p.hasPermission("genius.homes.others.settings.new.location") ||
                                p.hasPermission("genius.homes.others.settings.change.icons");
                                
                        if (canSettings) {
                            playerMenuUtility.setHomeToChange(selectedHome);
                            new HomeSettingsMenu(playerMenuUtility).open();
                        } else {
                            p.sendMessage(ColouredText(getErrorMessagePermission()));
                        }
                    }
                    case SHIFT_RIGHT -> {
                        if (isOwner || p.hasPermission("genius.others.settings") || p.hasPermission("genius.homes.others.settings.delete")) {
                            playerMenuUtility.setHomeToChange(selectedHome);
                            new HomeConfirmDeleteMenu(playerMenuUtility).open();
                        } else {
                            p.sendMessage(ColouredText(getErrorMessagePermission()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorderDefault();
        Player p = playerMenuUtility.getOwner();
        OfflinePlayer target = playerMenuUtility.getTarget();
        List<Home> homes = playerMenuUtility.getPlayerHomes();

        if(!homes.isEmpty()) {
            for(int i = 0; i < getMaxItemsPerPage(); i++) {
                int indexLoop = getMaxItemsPerPage() * page + i;
                if(indexLoop >= homes.size()) break;
                Home home = homes.get(indexLoop);
                if (home != null){
                    String homeName = home.getHomeName();
                    String icon = home.getIconType();
                    String skullMeta = home.getSkullMeta();
                    ItemStack homeItem;
                    List<String> lore = new ArrayList<>();
                    if (p.getUniqueId().equals(target.getUniqueId())) {
                        lore.addAll(getConfigMessageList("GUI.general.home-item-lore-own"));
                    } else {
                        lore.addAll(getConfigMessageList("GUI.general.home-item-lore-other"));
                    }

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
                        lore.addAll(getConfigMessageList("GUI.general.home-item-cost-lore").stream()
                                .map(s -> rep(s, "%amount%", amount, "%currency%", finalCurrency)).toList());
                    }

                    int time = getCooldownTime(p);
                    if (time > 0) {
                        lore.addAll(getConfigMessageList("GUI.general.home-item-cooldown-lore").stream()
                                .map(s -> rep(s, "%time%", time)).toList());
                    }

                    homeItem = createItemFromConfig(icon, 1, "&b" + homeName, lore);
                    
                    if(icon.equalsIgnoreCase("PLAYER_HEAD") && !skullMeta.equalsIgnoreCase("none")){
                        inventory.addItem(PlayerHeadUtils.changePlayerHeadSkinByString(skullMeta, homeItem));
                    }else {
                        inventory.addItem(homeItem);
                    }
               }
            }
        } else {
            if (p.getUniqueId().equals(target.getUniqueId())) {
                inventory.setItem(22, createItem("WHITE_BED", 1, getConfigMessage("GUI.names.home.list.create"), getConfigMessage("GUI.names.home.list.create-lore")));
            }
        }
    }
}
