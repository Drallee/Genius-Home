package me.dralle.home.utils;

import me.dralle.home.HomePlugin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    private final HomePlugin plugin;
    private final String projectSlug;

    public UpdateChecker(HomePlugin plugin, String projectSlug) {
        this.plugin = plugin;
        this.projectSlug = projectSlug;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.modrinth.com/v2/project/" + this.projectSlug + "/version").openStream(); Scanner scanner = new Scanner(inputStream)) {
                StringBuilder builder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    builder.append(scanner.nextLine());
                }
                String response = builder.toString();
                // Modrinth returns a JSON array of versions. The first one is the latest.
                // We'll use a simple regex to find the first "version_number":"..."
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");
                java.util.regex.Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    consumer.accept(matcher.group(1));
                }
            } catch (IOException exception) {
                plugin.getLogger().info("Unable to check for updates: " + exception.getMessage());
            }
        });
    }
}
