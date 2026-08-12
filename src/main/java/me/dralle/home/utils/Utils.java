package me.dralle.home.utils;

import me.dralle.home.HomePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    public static String ColouredText(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String StripColouredText(String message){
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
            // Support old format as well for compatibility if needed, but the target is %var%
            message = message.replace(entry.getKey().replace("%", "<").replace("%", ">"), entry.getValue());
        }
        return message;
    }

    private static String defaultIfNull(@Nullable String value, String defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static String updateTimestamp(){
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
        if (HomePlugin.getMessagesConfig().isSet(path)) {
            return HomePlugin.getMessagesConfig().getString(path);
        }

        // Fallback for missing messages
        switch (path) {
            case "chat.prefix.home": return "&9&lHOME &7》";
            case "chat.prefix.error": return "&4&lERROR &7》";
            case "chat.message.player.home.amount": return "%chat_prefix% &eYou currently have &b%current%&7/&c%max% &ehome(s)";
            case "chat.message.player.home.set": return "%chat_prefix% &aYour home %home% has been set to: &cx: &b%x% &cy: &e%y% &cz: &b%z%";
            case "chat.message.player.home.deleted": return "%chat_prefix% &cDeleting your home %home%";
            case "chat.message.player.home.name-changed": return "%chat_prefix% &aYour &e%old% &aname has been changed to &b%new%";
            case "chat.message.player.home.location-updated": return "%chat_prefix% &aHome location updated!";
            case "chat.message.player.home.sound-changed": return "%chat_prefix% &aTeleport sound has been changed for your home &b%home%";
            case "chat.message.player.home.icon-changed": return "%chat_prefix% &aIcon has been changed for your home &b%home%";
            case "chat.message.player.home.teleporting": return "%chat_prefix% &eTeleporting you to your home &b%home%";
            case "chat.message.player.home.teleporting-other": return "%chat_prefix% &eTeleporting you to &b%target%'s&r &ehome &b%home%";
            case "chat.message.player.home.others.amount": return "%chat_prefix% &b%target% &ecurrently have &b%current% &ehome(s)";
            case "chat.message.error.player.teleport.cooldown": return "%chat_prefix% &eTeleporting in &b%time% &eseconds... Don't move!";
            case "chat.message.error.player.teleport.cancelled": return "%chat_prefix% &cTeleportation cancelled due to movement.";
            case "chat.message.error.player.teleport.insufficient-funds": return "%chat_prefix% &cInsufficient funds! You need %amount% %currency%.";
            case "chat.message.error.player.teleport.insufficient-xp": return "%chat_prefix% &cInsufficient XP! You need %amount% XP.";
            case "chat.message.error.player.teleport.insufficient-levels": return "%chat_prefix% &cInsufficient levels! You need %amount% levels.";
            case "chat.message.error.player.teleport.insufficient-items": return "%chat_prefix% &cInsufficient items! You need %amount% %item%.";
            case "chat.message.error.player.teleport.paid": return "%chat_prefix% &aYou paid %amount% %currency% to teleport.";
            case "chat.message.error.player.teleport.paid-xp": return "%chat_prefix% &aYou paid %amount% XP to teleport.";
            case "chat.message.error.player.teleport.paid-levels": return "%chat_prefix% &aYou paid %amount% levels to teleport.";
            case "chat.message.error.player.teleport.paid-item": return "%chat_prefix% &aYou paid %amount% %item% to teleport.";
            case "chat.message.error.player.teleport.location-not-found": return "%chat_prefix% &cHome location not found!";
            case "chat.message.error.player.teleport.unsafe-location": return "%chat_prefix% &cTeleport destination is unsafe!";
            case "chat.message.error.player.command.player-not-found": return "%chat_prefix% &cPlayer does't exist.";
            case "chat.message.error.player.command.no-permission": return "%chat_prefix% &cYou do not have the right permission.";
            case "chat.message.error.player.command.only-players": return "&cOnly players can use this command.";
            case "chat.message.error.player.command.usage": return "&cUsage: %usage%";
            case "chat.message.error.player.command.max-homes": return "%chat_prefix% &cYou have reached the maximum amount of homes (%max%)";
            case "chat.message.error.player.command.config-reloaded": return "&aConfiguration reloaded and home cache cleared!";
            case "chat.message.error.player.home.not-found": return "%chat_prefix% &cSeems like you don't have a home named %home%";
            case "chat.message.error.player.home.already-exists": return "%chat_prefix% &cSeems like you already have a home named %home%";
            case "chat.message.error.player.home.rename-usage": return "&cTo change the name of your home, use &e/home-rename %home% (new name)";
            case "chat.message.error.player.home.others.not-found": return "%chat_prefix% &cSeems like %target% doesn't have a home named %home%";
            case "GUI.general.back": return "&aGo back";
            case "GUI.general.back-lore-homes": return "&7To your home(s)";
            case "GUI.general.back-lore-settings": return "&7To your home settings";
            case "GUI.general.close": return "&cClose";
            case "GUI.general.previous-page": return "&aPrevious page";
            case "GUI.general.next-page": return "&aNext page";
            case "GUI.general.page-lore": return "&eCurrent page&7: &b%current%&7/&c%total%";
            case "GUI.general.already-first-page": return "&7You are already on the first page.";
            case "GUI.general.already-last-page": return "You are on the last page.";
            case "GUI.names.home.list.homes": return "&rYour home(s) [&b%current%&7/&c%max%&r]";
            case "GUI.names.home.list.create": return "&aCreate a home";
            case "GUI.names.home.list.create-lore": return "&7Click to set a home";
            case "GUI.names.home.settings.title": return "&rYour home settings (&b%home%&r)";
            case "GUI.names.home.settings.info-name": return "&b%home%";
            case "GUI.names.home.settings.delete": return "&cDelete home";
            case "GUI.names.home.settings.delete-lore": return "&eClick to delete";
            case "GUI.names.home.settings.set-location": return "&eSet new location";
            case "GUI.names.home.settings.change-icon": return "&eChange icon";
            case "GUI.names.home.settings.change-icon-lore": return "&7Click to change icon";
            case "GUI.names.home.settings.change-name": return "&eChange Name";
            case "GUI.names.home.settings.rename.no-permission": return "&cYou do not have permission to rename your home";
            case "GUI.names.home.rename.title": return "&rChange Name: &b%home%";
            case "GUI.names.home.settings.teleport": return "&aTeleport";
            case "GUI.names.home.settings.teleport-lore": return "&7CLick to teleport";
            case "GUI.names.home.settings.change-sound": return "&eChange sound effect";
            case "GUI.names.home.settings.change-sound-no-permission": return "&cYou do not have permission to change sound";
            case "GUI.names.home.settings.change-sound-no-permission-individual": return "&cYou do not have permission to use this sound";
            case "GUI.names.home.sounds.title": return "&rHome sounds list";
            case "GUI.names.home.delete-confirm.title": return "&rWanna delete your home (&b%home%&r)?";
            case "GUI.names.home.delete-confirm.confirm": return "&aConfirm";
            case "GUI.names.home.delete-confirm.cancel": return "&cCancel";
            case "GUI.names.home.location-confirm.title": return "&rWanna set new location (&b%home%&r)?";
            case "GUI.names.home.location-confirm.confirm": return "&aConfirm";
            case "GUI.names.home.location-confirm.cancel": return "&cCancel";
            case "GUI.names.home.icons.title": return "&rHome icons list";
            case "GUI.names.home.icons.title-heads": return "&rHome icons list (Player Heads)";
            case "GUI.names.home.icons.heads-item": return "&bPLAYER_HEAD";
            case "GUI.names.home.icons.heads-item-lore": return "&aClick to change Icon";
            case "GUI.names.home.icons.alex-item": return "&bALEX";
            case "GUI.names.home.icons.alex-item-lore": return "&aClick to change Icon";
            case "GUI.names.home.icons.generic-icon-name": return "&b%icon%";
            case "GUI.names.home.icons.generic-icon-lore": return "&aClick to change Icon";
            case "GUI.names.home.others.list": return "&r%target%'s home(s) [&b%current%&r]";
            case "database.connection.sqlite": return "&aSuccessfully connected to SQLite database";
            case "database.connection.mysql": return "&aSuccessfully connected to MySQL database";
            case "database.connection.error": return "&4Error establishing database connection or creating table";
            case "database.connection.table-created": return "&eTable '&a%table%&e' created successfully.";
            case "database.connection.table-exists": return "&cTable '&4%table%&c' already exists.";
            case "database.connection.cache-cleared": return "&eHome cache cleared!";
            case "chat.message.player.update-available": return "&eA new update is available for &bGenius-Homes&e! Version: &b%version%&e. Download it at: &b%url%";
        }

        console("&4&lERROR &8>> config path not found '" + path + "'", true);
        return "&cError";
    }

    public static List<String> getConfigMessageList(String path) {
        if (HomePlugin.getMessagesConfig().isSet(path)) {
            return HomePlugin.getMessagesConfig().getStringList(path);
        }

        // Fallback for missing message lists
        switch (path) {
            case "GUI.general.home-item-lore-own":
                return List.of("&aLeft click&7:", "  &7To teleport", "&eRight click&7:", "  &7For settings", "&cShift right click&7:", "  &7To delete your home");
            case "GUI.general.home-item-lore-other":
                return List.of("&aLeft click&7:", "  &7To teleport");
            case "GUI.general.home-item-cost-lore":
                return List.of("", "&eTeleport Cost&7:", "  &7- &a%amount% %currency%");
            case "GUI.general.home-item-cooldown-lore":
                return List.of("", "&eTeleport Cooldown&7:", "  &7- &a%time% seconds");
            case "GUI.names.home.settings.info-lore":
                return List.of("&eLocation&7:", "  &eX&7: &b%x%", "  &eY&7: &b%y%", "  &eZ&7: &b%z%", "  &eWorld&7: &b%world%");
            case "GUI.names.home.settings.set-location-lore":
                return List.of("&eLocation&7:", "  &eX&7: &c%x% &7→ &a%new_x%", "  &eY&7: &c%y% &7→ &a%new_y%", "  &eZ&7: &c%z% &7→ &a%new_z%", "  &eWorld&7: &c%world% &7→ &a%new_world%");
            case "GUI.names.home.settings.change-name-lore":
                return List.of("&7Current: &b%home%", "", "&eClick to change the name of your home");
            case "GUI.names.home.rename.item-lore":
                return List.of("&eType the new name above", "&eand click the result to confirm");
            case "GUI.names.home.settings.change-sound-lore":
                return List.of("&7Current: &b%sound%", "", "&7Click to change sound effect");
            case "GUI.names.home.sounds.item-lore":
                return List.of("&aLeft click&7:", "  &7To apply to home", "&eRight click&7:", "  &7To listen to the sound");
            case "GUI.names.home.delete-confirm.confirm-lore":
                return List.of("&7Your home will be deleted and", "&c&lCANNOT be restored&7.");
            case "GUI.names.home.location-confirm.info-lore":
                return List.of("&eLocation&7:", "  &eX&7: &c%x% &7→ &a%new_x%", "  &eY&7: &c%y% &7→ &a%new_y%", "  &eZ&7: &c%z% &7→ &a%new_z%", "  &eWorld&7: &c%world% &7→ &a%new_world%");
            case "GUI.names.home.location-confirm.confirm-lore":
                return List.of("&7Your home will change it's location and", "&c&lCANNOT be restored&7.");
        }

        console("&4&lERROR &8>> config path not found '" + path + "'", true);
        return new ArrayList<>();
    }

    public static int getConfigNumber(String path){
        if(HomePlugin.getHomeConfig().isSet(path)){
            return HomePlugin.getHomeConfig().getInt(path);
        }
        console("&4&lERROR &8>> &cconfig path not found '"+path+"'", true);
        return 0;
    }

    public static Boolean getConfigCheck(String path){
        if(HomePlugin.getHomeConfig().isSet(path)){
            return HomePlugin.getHomeConfig().getBoolean(path);
        }
        console("&4&lERROR &8>> &cconfig path not found '"+path+"'", true);
        return false;
    }

    public static void msp(Player p, String message, boolean colouredText){
        if(colouredText){
            message = ChatColor.translateAlternateColorCodes('&', message);
        }
        p.sendMessage(message);
    }

    public static void console(String message, boolean colouredText, boolean isDebug){
        if(isDebug && !getConfigCheck("settings.debug")) return;
        if(colouredText){
            message = ChatColor.translateAlternateColorCodes('&', message);
        }
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }

    public static void console(String message, boolean colouredText){
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
