package me.dralle.home.listeners;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.Menu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {
    public MenuListener(HomePlugin plugin) {
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onMenuDrag(InventoryDragEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu menu) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) {
                return;
            }

            menu.handleMenuItems(e);
        }
    }
}
