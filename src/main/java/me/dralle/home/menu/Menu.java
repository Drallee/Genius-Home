package me.dralle.home.menu;

import me.dralle.home.HomePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.dralle.home.utils.Utils.*;

public abstract class Menu implements InventoryHolder {

    protected HomePlugin plugin;
    protected PlayerMenuUtility playerMenuUtility;
    protected Inventory inventory;
    protected ItemStack FILLER_GLASS;
    protected NamespacedKey actionKey;

    public Menu(PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
        this.plugin = playerMenuUtility.getPlugin();
        this.actionKey = new NamespacedKey(plugin, "menu_action");
        this.FILLER_GLASS = createConfiguredItem("filler", null);
    }

    public PlayerMenuUtility getPlayerMenuUtility() {
        return playerMenuUtility;
    }

    public abstract String getMenuName();
    public abstract int getSlots();
    public abstract void handleMenuItems(InventoryClickEvent e);
    public abstract void setMenuItems();
    protected abstract String getMenuId();

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        this.setMenuItems();
        playerMenuUtility.getOwner().openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setFillerGlass(){
        FileConfiguration config = getMenuConfig();
        if (config != null && !config.getBoolean("filler.enabled", true)) {
            return;
        }

        List<Integer> slots = HomePlugin.getMenuConfigManager().getSlots(getMenuId(), "filler.slots", getSlots(), List.of());
        if (slots.isEmpty()) {
            for (int i = 0; i < getSlots(); i++) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, FILLER_GLASS);
                }
            }
            return;
        }

        for (Integer slot : slots) {
            if (slot >= 0 && slot < getSlots() && inventory.getItem(slot) == null) {
                inventory.setItem(slot, FILLER_GLASS);
            }
        }
    }

    protected FileConfiguration getMenuConfig() {
        return HomePlugin.getMenuConfigManager().getConfig(getMenuId());
    }

    protected String getConfiguredTitle(String fallbackLanguagePath, Object... replacements) {
        FileConfiguration config = getMenuConfig();
        String title = config == null ? null : config.getString("title");
        if (title == null || title.isBlank()) {
            title = getConfigMessage(fallbackLanguagePath);
        }
        return ColouredText(rep(title, replacements));
    }

    protected int getConfiguredSlots(int fallbackRows) {
        return HomePlugin.getMenuConfigManager().getRows(getMenuId(), fallbackRows) * 9;
    }

    protected List<Integer> getContentSlots() {
        return HomePlugin.getMenuConfigManager().getSlots(getMenuId(), "content-slots", getSlots(), defaultContentSlots());
    }

    protected List<Integer> defaultContentSlots() {
        return Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    }

    protected void setButton(String buttonId, String action, Object... replacements) {
        FileConfiguration config = getMenuConfig();
        if (config != null && !config.getBoolean("buttons." + buttonId + ".enabled", true)) {
            return;
        }

        Integer slot = HomePlugin.getMenuConfigManager().getSlot(getMenuId(), "buttons." + buttonId + ".slot", getSlots(), null);
        if (slot == null) {
            plugin.getLogger().warning("menus/" + getMenuId() + ".yml button '" + buttonId + "' has no valid slot.");
            return;
        }
        inventory.setItem(slot, createConfiguredItem("buttons." + buttonId, action, replacements));
    }

    protected ItemStack createConfiguredItem(String path, @Nullable String action, Object... replacements) {
        FileConfiguration config = getMenuConfig();
        String material = config == null ? "BARRIER" : config.getString(path + ".material", "BARRIER");
        int amount = config == null ? 1 : Math.max(1, config.getInt(path + ".amount", 1));
        String name = config == null ? "" : config.getString(path + ".name", "");
        List<String> lore = config == null ? new ArrayList<>() : config.getStringList(path + ".lore");

        List<String> resolvedLore = new ArrayList<>();
        for (String loreLine : lore) {
            String resolvedLine = rep(loreLine, replacements);
            resolvedLore.addAll(Arrays.asList(resolvedLine.split("\\R", -1)));
        }

        ItemStack item = createItemFromConfig(material, amount, rep(name, replacements), resolvedLore);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            if (config != null && config.isInt(path + ".custom-model-data")) {
                itemMeta.setCustomModelData(config.getInt(path + ".custom-model-data"));
            }
            if (action != null) {
                itemMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
            }
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    protected String getAction(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) {
            return "";
        }
        String action = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
        return action == null ? "" : action;
    }

    protected ItemStack withAction(ItemStack item, String action) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
            item.setItemMeta(itemMeta);
        }
        return item;
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
        Material mat = Material.matchMaterial(materialString);
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
