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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static Plugin plugin = HomePlugin.getPlugin(HomePlugin.class);
    public static final DecimalFormat df = new DecimalFormat("0.00");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:([^>]+)>(.*?)</gradient>");
    private static final Pattern TAGGED_HEX_PATTERN = Pattern.compile("<(?:color:)?#([A-Fa-f0-9]{6})>");
    private static final Pattern AMP_HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern RAW_HEX_PATTERN = Pattern.compile("(?<![A-Za-z0-9&])#([A-Fa-f0-9]{6})");

    public static String chat(String s) {
        return ColouredText(s);
    }

    public static String ColouredText(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        message = applyGradients(message);
        message = replaceHexPattern(message, TAGGED_HEX_PATTERN);
        message = replaceHexPattern(message, AMP_HEX_PATTERN);
        message = replaceHexPattern(message, RAW_HEX_PATTERN);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String StripColouredText(String message) {
        return ChatColor.stripColor(ColouredText(message));
    }

    private static String applyGradients(String message) {
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            List<String> colors = parseGradientColors(matcher.group(1));
            String replacement = colors.size() >= 2 ? buildGradient(matcher.group(2), colors) : matcher.group();
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static List<String> parseGradientColors(String colorsText) {
        List<String> colors = new ArrayList<>();
        for (String colorPart : colorsText.split(":")) {
            String normalized = colorPart.trim().replace("&", "").replace("#", "");
            if (normalized.matches("[A-Fa-f0-9]{6}")) {
                colors.add(normalized);
            }
        }
        return colors;
    }

    private static String replaceHexPattern(String message, Pattern pattern) {
        Matcher matcher = pattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(toMinecraftHexColor(matcher.group(1))));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String buildGradient(String text, List<String> colors) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        int visibleLength = text.codePointCount(0, text.length());
        if (visibleLength <= 1) {
            return toMinecraftHexColor(colors.get(0)) + text;
        }

        StringBuilder builder = new StringBuilder();
        int segmentCount = colors.size() - 1;
        int index = 0;
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            double ratio = (double) index / (visibleLength - 1);
            double scaledRatio = ratio * segmentCount;
            int segment = Math.min((int) Math.floor(scaledRatio), segmentCount - 1);
            double segmentRatio = scaledRatio - segment;

            String startHex = colors.get(segment);
            String endHex = colors.get(segment + 1);
            int startRed = Integer.parseInt(startHex.substring(0, 2), 16);
            int startGreen = Integer.parseInt(startHex.substring(2, 4), 16);
            int startBlue = Integer.parseInt(startHex.substring(4, 6), 16);
            int endRed = Integer.parseInt(endHex.substring(0, 2), 16);
            int endGreen = Integer.parseInt(endHex.substring(2, 4), 16);
            int endBlue = Integer.parseInt(endHex.substring(4, 6), 16);

            int red = interpolate(startRed, endRed, segmentRatio);
            int green = interpolate(startGreen, endGreen, segmentRatio);
            int blue = interpolate(startBlue, endBlue, segmentRatio);

            builder.append(toMinecraftHexColor(String.format("%02X%02X%02X", red, green, blue)));
            builder.appendCodePoint(codePoint);

            offset += Character.charCount(codePoint);
            index++;
        }
        return builder.toString();
    }

    private static int interpolate(int start, int end, double ratio) {
        return (int) Math.round(start + ((end - start) * ratio));
    }

    private static String toMinecraftHexColor(String hex) {
        String normalized = hex.replace("#", "").replace("&", "");
        StringBuilder builder = new StringBuilder().append(ChatColor.COLOR_CHAR).append('x');
        for (char character : normalized.toCharArray()) {
            builder.append(ChatColor.COLOR_CHAR).append(character);
        }
        return builder.toString();
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
            message = ColouredText(message);
        }
        p.sendMessage(message);
    }

    public static void console(String message, boolean colouredText, boolean isDebug) {
        if (isDebug && !getConfigCheck("settings.debug")) return;
        if (colouredText) {
            message = ColouredText(message);
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
