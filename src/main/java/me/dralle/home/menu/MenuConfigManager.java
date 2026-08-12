package me.dralle.home.menu;

import me.dralle.home.HomePlugin;
import me.dralle.home.utils.FileUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuConfigManager {
    private static final List<String> DEFAULT_MENUS = List.of(
            "home-menu",
            "home-settings-menu",
            "delete-confirm-menu",
            "new-location-confirm-menu",
            "icon-menu",
            "player-heads-menu",
            "sound-menu",
            "rename-menu"
    );

    private final HomePlugin plugin;
    private final Map<String, FileConfiguration> menus = new HashMap<>();

    public MenuConfigManager(HomePlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        menus.clear();
        for (String menuId : DEFAULT_MENUS) {
            menus.put(menuId, FileUtil.loadFile("menus/" + menuId + ".yml", "menus/" + menuId + ".yml"));
            validate(menuId);
        }
    }

    public FileConfiguration getConfig(String menuId) {
        FileConfiguration config = menus.get(menuId);
        if (config == null) {
            plugin.getLogger().warning("Menu config '" + menuId + "' was not loaded. Using empty defaults.");
        }
        return config;
    }

    public int getRows(String menuId, int fallbackRows) {
        FileConfiguration config = getConfig(menuId);
        int rows = config == null ? fallbackRows : config.getInt("rows", fallbackRows);
        if (rows < 1 || rows > 6) {
            plugin.getLogger().warning("menus/" + menuId + ".yml has invalid rows value '" + rows + "'. Using " + fallbackRows + ".");
            return fallbackRows;
        }
        return rows;
    }

    public List<Integer> getSlots(String menuId, String path, int inventorySize, List<Integer> fallback) {
        FileConfiguration config = getConfig(menuId);
        if (config == null || !config.isList(path)) {
            return fallback;
        }

        List<Integer> slots = parseSlots(config.getList(path), inventorySize, menuId, path);
        return slots.isEmpty() ? fallback : slots;
    }

    public Integer getSlot(String menuId, String path, int inventorySize, Integer fallback) {
        FileConfiguration config = getConfig(menuId);
        if (config == null || !config.isSet(path)) {
            return fallback;
        }

        int slot = config.getInt(path, fallback == null ? -1 : fallback);
        if (slot < 0 || slot >= inventorySize) {
            plugin.getLogger().warning("menus/" + menuId + ".yml path '" + path + "' uses invalid slot " + slot + ".");
            return fallback;
        }
        return slot;
    }

    private void validate(String menuId) {
        FileConfiguration config = getConfig(menuId);
        if (config == null) return;

        int inventorySize = getRows(menuId, 6) * 9;
        validateItem(menuId, "filler");

        ConfigurationSection buttons = config.getConfigurationSection("buttons");
        if (buttons != null) {
            for (String key : buttons.getKeys(false)) {
                validateItem(menuId, "buttons." + key);
                Integer slot = getSlot(menuId, "buttons." + key + ".slot", inventorySize, null);
                if (slot == null && buttons.getConfigurationSection(key).isSet("slot")) {
                    plugin.getLogger().warning("menus/" + menuId + ".yml button '" + key + "' has no valid slot and will not render.");
                }
            }
        }
    }

    private void validateItem(String menuId, String path) {
        FileConfiguration config = getConfig(menuId);
        if (config == null || !config.isConfigurationSection(path)) return;

        String materialName = config.getString(path + ".material", "BARRIER");
        if (Material.matchMaterial(materialName) == null) {
            plugin.getLogger().warning("menus/" + menuId + ".yml path '" + path + ".material' uses invalid material '" + materialName + "'. BARRIER will be used.");
        }
    }

    private List<Integer> parseSlots(List<?> values, int inventorySize, String menuId, String path) {
        List<Integer> slots = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof Integer integer) {
                addSlot(slots, integer, inventorySize, menuId, path);
            } else if (value instanceof String string) {
                parseSlotString(slots, string, inventorySize, menuId, path);
            }
        }
        return slots;
    }

    private void parseSlotString(List<Integer> slots, String value, int inventorySize, String menuId, String path) {
        String trimmed = value.trim();
        if (trimmed.contains("-")) {
            String[] parts = trimmed.split("-", 2);
            try {
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                for (int slot = Math.min(start, end); slot <= Math.max(start, end); slot++) {
                    addSlot(slots, slot, inventorySize, menuId, path);
                }
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("menus/" + menuId + ".yml path '" + path + "' contains invalid slot range '" + value + "'.");
            }
            return;
        }

        try {
            addSlot(slots, Integer.parseInt(trimmed), inventorySize, menuId, path);
        } catch (NumberFormatException ex) {
            plugin.getLogger().warning("menus/" + menuId + ".yml path '" + path + "' contains invalid slot '" + value + "'.");
        }
    }

    private void addSlot(List<Integer> slots, int slot, int inventorySize, String menuId, String path) {
        if (slot < 0 || slot >= inventorySize) {
            plugin.getLogger().warning("menus/" + menuId + ".yml path '" + path + "' contains out-of-range slot " + slot + ".");
            return;
        }
        if (!slots.contains(slot)) {
            slots.add(slot);
        }
    }
}
