package me.dralle.home.menu.subMenu;

import me.dralle.home.menu.Menu;
import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.models.Home;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import static me.dralle.home.utils.Utils.*;

public class HomeRenameAnvil extends Menu {
    private String pendingName;

    public HomeRenameAnvil(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    public String getPendingName() {
        return pendingName;
    }

    public void setPendingName(String pendingName) {
        this.pendingName = pendingName;
    }

    @Override
    public String getMenuName() {
        Home home = playerMenuUtility.getPlayerHome();
        return ColouredText(rep(getConfigMessage("GUI.names.home.rename.title"), "%home%", home.getHomeName()));
    }

    @Override
    public int getSlots() {
        return 0; // Not used for Anvil
    }

    @Override
    public void open() {
        Player p = playerMenuUtility.getOwner();
        org.bukkit.OfflinePlayer target = playerMenuUtility.getTarget();
        boolean isOwner = p.getUniqueId().equals(target.getUniqueId());

        if (isOwner) {
            if (getConfigCheck("settings.homes.rename-permission") && !p.hasPermission("genius.homes.rename")) {
                p.sendMessage(ColouredText(getErrorMessagePermission()));
                return;
            }
        } else {
            if (!p.hasPermission("genius.homes.others.settings.rename") && !p.hasPermission("genius.others.settings")) {
                p.sendMessage(ColouredText(getErrorMessagePermission()));
                return;
            }
        }
        inventory = Bukkit.createInventory(this, InventoryType.ANVIL, getMenuName());
        this.setMenuItems();
        playerMenuUtility.getOwner().openInventory(inventory);
    }

    @Override
    public void handleMenuItems(InventoryClickEvent e) {
        // Handled in MenuListener for InventoryType.ANVIL
    }

    @Override
    public void setMenuItems() {
        Home home = playerMenuUtility.getPlayerHome();
        String rawName = StripColouredText(home.getHomeName());
        pendingName = null;
        ItemStack nameTag = createItemFromConfig("NAME_TAG", 1, rawName, getConfigMessageList("GUI.names.home.rename.item-lore"));
        inventory.setItem(0, nameTag);
    }
}
