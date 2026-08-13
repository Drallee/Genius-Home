package me.dralle.home.menu.subMenu;

import me.dralle.home.HomePlugin;
import me.dralle.home.models.Home;
import me.dralle.home.menu.PaginatedMenu;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.utils.PlayerHeadUtils;
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
        title = getConfiguredTitle("GUI.names.home.list.homes",
                "%name%", name,
                "%displayname%", displayName,
                "%timestamp%", updateTimestamp(),
                "%chat_prefix%", chat_prefix,
                "%current%", current_homes,
                "%max%", max_homes);
        if (!p.getUniqueId().equals(target.getUniqueId())){
            title = getConfiguredTitle("GUI.names.home.others.list",
                    "%name%", name,
                    "%displayname%", displayName,
                    "%timestamp%", updateTimestamp(),
                    "%target%", target_name,
                    "%chat_prefix%", chat_prefix,
                    "%current%", current_homes,
                    "%max%", max_homes);

        }
        return title;
    }

    @Override
    public int getSlots() {
        return getConfiguredSlots(6);
    }

    @Override
    protected String getMenuId() {
        return "home-menu";
    }

    @Override
    public void handleMenuItems(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        OfflinePlayer target = playerMenuUtility.getTarget();
        List<Home> homes = playerMenuUtility.getPlayerHomes();

        if (e.getCurrentItem() == null) return;

        switch (getAction(e)){
            case "previous-page":
                    if (page > 0){
                        page--;
                        super.open();
                    } else {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-first-page")));
                    }
                break;
            case "next-page":
                    if ((indexes + 1) < homes.size()){
                        page++;
                        super.open();
                    } else {
                        p.sendMessage(ColouredText(getConfigMessage("GUI.general.already-last-page")));
                    }
                break;
            case "create-home":
                if (p.getUniqueId().equals(target.getUniqueId())) {
                    HomePlugin.getHomeTextInputService().openCreateHomeInput(p, playerMenuUtility);
                }
                break;
            case "close":
                p.closeInventory();
                break;
        }

        Integer contentIndex = getClickedContentIndex(e.getRawSlot());
        if (contentIndex != null) {
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

                    homeItem = withAction(createItemFromConfig(icon, 1, "&b" + homeName, lore), "home-item");
                    
                    if(icon.equalsIgnoreCase("PLAYER_HEAD") && !skullMeta.equalsIgnoreCase("none")){
                        inventory.setItem(getContentSlots().get(i), PlayerHeadUtils.changePlayerHeadSkinByString(skullMeta, homeItem));
                    }else {
                        inventory.setItem(getContentSlots().get(i), homeItem);
                    }
               }
            }
        }

        if (p.getUniqueId().equals(target.getUniqueId())) {
            int maxHomes = getMaxHomes(target);
            if (maxHomes == -1 || homes.size() < maxHomes) {
                setButton("create-home", "create-home");
            }
        }
    }
}
