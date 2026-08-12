package me.dralle.home.utils;

import me.dralle.home.HomePlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class LanguageManager {
    private static final String DEFAULT_LANGUAGE = "en_US";

    private final HomePlugin plugin;
    private FileConfiguration defaultLanguage;
    private FileConfiguration activeLanguage;
    private String activeLanguageCode;

    public LanguageManager(HomePlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        defaultLanguage = FileUtil.loadFile("language/" + DEFAULT_LANGUAGE + ".yml", "language/" + DEFAULT_LANGUAGE + ".yml");

        activeLanguageCode = HomePlugin.getHomeConfig().getString("language", DEFAULT_LANGUAGE);
        if (activeLanguageCode == null || activeLanguageCode.isBlank()) {
            activeLanguageCode = DEFAULT_LANGUAGE;
        }

        if (DEFAULT_LANGUAGE.equalsIgnoreCase(activeLanguageCode)) {
            activeLanguage = defaultLanguage;
            return;
        }

        String activePath = "language/" + activeLanguageCode + ".yml";
        if (!FileUtil.exists(activePath)) {
            plugin.getLogger().warning("Language '" + activeLanguageCode + "' does not exist. Falling back to " + DEFAULT_LANGUAGE + ".");
            activeLanguage = defaultLanguage;
            return;
        }

        activeLanguage = FileUtil.loadFile(activePath, activePath);
    }

    public String getString(String path) {
        if (activeLanguage != null && activeLanguage.isString(path)) {
            return activeLanguage.getString(path);
        }
        if (defaultLanguage != null && defaultLanguage.isString(path)) {
            if (!DEFAULT_LANGUAGE.equalsIgnoreCase(activeLanguageCode)) {
                plugin.getLogger().warning("Missing language message '" + path + "' in " + activeLanguageCode + ".yml. Using en_US.yml.");
            }
            return defaultLanguage.getString(path);
        }
        plugin.getLogger().warning("Missing language message '" + path + "' in active language and en_US.yml.");
        return "&cMissing message: " + path;
    }

    public List<String> getStringList(String path) {
        if (activeLanguage != null && activeLanguage.isList(path)) {
            return activeLanguage.getStringList(path);
        }
        if (defaultLanguage != null && defaultLanguage.isList(path)) {
            if (!DEFAULT_LANGUAGE.equalsIgnoreCase(activeLanguageCode)) {
                plugin.getLogger().warning("Missing language message list '" + path + "' in " + activeLanguageCode + ".yml. Using en_US.yml.");
            }
            return defaultLanguage.getStringList(path);
        }
        plugin.getLogger().warning("Missing language message list '" + path + "' in active language and en_US.yml.");
        return new ArrayList<>();
    }

    public FileConfiguration getDefaultLanguage() {
        return defaultLanguage;
    }

    public FileConfiguration getActiveLanguage() {
        return activeLanguage;
    }
}
