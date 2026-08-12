package me.dralle.home.utils;

import me.dralle.home.HomePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static Plugin plugin = HomePlugin.getPlugin(HomePlugin.class);
    public static final DecimalFormat df = new DecimalFormat("0.00");

    public static String chat(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String ColouredText(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String StripColouredText(String message) {
        return ChatColor.stripColor(ColouredText(message));
    }

    public static String rep(@NotNull String message, Object... replacements) {
        Map<String, String> replacementMap = new HashMap<>();
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length && replacements[i] instanceof String) {
                replacementMap.put((String) replacements[i], String.valueOf(replacements[i + 1]));
            }
        }
        for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
            message = message.replace(entry.getKey().replace("%", "<").replace("%", ">"), entry.getValue());
        }
        return message;
    }

    public static String updateTimestamp() {
        LocalDateTime currentTime = LocalDateTime.now();
        return dtf.format(currentTime);
    }

    public static String getConfigString(String path) {
        if (HomePlugin.getHomeConfig().isSet(path)) {
            return HomePlugin.getHomeConfig().getString(path);
        }
        console("&4&lERROR &8>> &cconfig path not found '" + path + "'", true);
        return "";
    }

    public static String getConfigMessage(String path) {
        return HomePlugin.getLanguageManager().getString(path);
    }

    public static List<String> getConfigMessageList(String path) {
        return HomePlugin.getLanguageManager().getStringList(path);
    }

    public static int getConfigNumber(String path) {
        if (HomePlugin.getHomeConfig().isSet(path)) {
            return HomePlugin.getHomeConfig().getInt(path);
        }
        console("&4&lERROR &8>> &cconfig path not found '" + path + "'", true);
        return 0;
    }

    public static Boolean getConfigCheck(String path) {
        if (HomePlugin.getHomeConfig().isSet(path)) {
            return HomePlugin.getHomeConfig().getBoolean(path);
        }
        console("&4&lERROR &8>> &cconfig path not found '" + path + "'", true);
        return false;
    }

    public static void msp(Player p, String message, boolean colouredText) {
        if (colouredText) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }
        p.sendMessage(message);
    }

    public static void console(String message, boolean colouredText, boolean isDebug) {
        if (isDebug && !getConfigCheck("settings.debug")) return;
        if (colouredText) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }

    public static void console(String message, boolean colouredText) {
        console(message, colouredText, false);
    }

    public static String getErrorMessagePermission() {
        return rep(
                getConfigMessage("chat.message.error.player.command.no-permission"),
                "%timestamp%", updateTimestamp(),
                "%chat_prefix%", getConfigMessage("chat.prefix.error"));
    }

    public static String getErrorMessagePlayerNotFound() {
        return rep(
                getConfigMessage("chat.message.error.player.command.player-not-found"),
                "%timestamp%", updateTimestamp(),
                "%chat_prefix%", getConfigMessage("chat.prefix.error"));
    }
}
