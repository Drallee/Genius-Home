package me.dralle.home.menu;

import me.dralle.home.HomePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static me.dralle.home.utils.Utils.*;

public abstract class Menu implements InventoryHolder {

    protected HomePlugin plugin;
    protected PlayerMenuUtility playerMenuUtility;
    protected Inventory inventory;
    protected ItemStack FILLER_GLASS = createItem("GRAY_STAINED_GLASS_PANE", 1, "");

    public Menu(PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
        this.plugin = playerMenuUtility.getPlugin();
    }

    public PlayerMenuUtility getPlayerMenuUtility() {
        return playerMenuUtility;
    }

    public abstract String getMenuName();
    public abstract int getSlots();
    public abstract void handleMenuItems(InventoryClickEvent e);
    public abstract void setMenuItems();

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        this.setMenuItems();
        playerMenuUtility.getOwner().openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setFillerGlass(){
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null){
                inventory.setItem(i, FILLER_GLASS);
            }
        }
    }

    public static ItemStack createItem(@NotNull String materialString, int amount, @NotNull String displayName, @Nullable String... lore){
        Material mat = Material.getMaterial(materialString);
        if (mat == null) mat = Material.BARRIER;
        ItemStack item = new ItemStack(mat, amount);
        List<String> loreArray = new ArrayList<>();

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ColouredText(displayName));
            if (lore != null) {
                for (String s : lore){
                    loreArray.add(ColouredText(s));
                }
                itemMeta.setLore(loreArray);
            }
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    public static ItemStack createItemFromConfig(@NotNull String materialString, int amount, @NotNull String displayName, @NotNull List<String> lore){
        Material mat = Material.getMaterial(materialString);
        if (mat == null) mat = Material.BARRIER;
        ItemStack item = new ItemStack(mat, amount);
        List<String> loreArray = new ArrayList<>();

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ColouredText(displayName));
            for (String s : lore){
                loreArray.add(ColouredText(s));
            }
            itemMeta.setLore(loreArray);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(itemMeta);
        }

        return item;
    }
}
