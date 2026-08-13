package me.dralle.home.utils;

import me.dralle.home.HomePlugin;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DebugLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static HomePlugin plugin;
    private static File debugFile;

    private DebugLogger() {
    }

    public static void initialize(HomePlugin homePlugin) {
        plugin = homePlugin;
        debugFile = new File(plugin.getDataFolder(), "debug.log");

        log("INFO", "Debug log initialized");
        log("INFO", "Server version: " + plugin.getServer().getVersion());
        log("INFO", "Bukkit version: " + plugin.getServer().getBukkitVersion());
        log("INFO", "Plugin version: " + plugin.getDescription().getVersion());
    }

    public static void warning(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void error(String message, Throwable throwable) {
        log("ERROR", message);
        if (throwable != null) {
            log("ERROR", stackTraceToString(throwable));
        }
    }

    public static void logConsoleMessage(String message) {
        String stripped = ChatColor.stripColor(Utils.ColouredText(message));
        String level = inferLevel(stripped);
        if ("WARN".equals(level) || "ERROR".equals(level)) {
            log(level, stripped);
        }
    }

    private static String inferLevel(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("error") || lower.contains("exception") || lower.contains("failed") || lower.contains("invalid")) {
            return "ERROR";
        }
        if (lower.contains("warn") || lower.contains("missing") || lower.contains("unable") || lower.contains("not found")) {
            return "WARN";
        }
        return "INFO";
    }

    private static void log(String level, String message) {
        if (debugFile == null) {
            return;
        }

        File parent = debugFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(debugFile, true))) {
            writer.println("[" + FORMATTER.format(LocalDateTime.now()) + "] [" + level + "] " + message);
        } catch (IOException ignored) {
            if (plugin != null) {
                plugin.getLogger().warning("Unable to write to debug.log: " + ignored.getMessage());
            }
        }
    }

    private static String stackTraceToString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
