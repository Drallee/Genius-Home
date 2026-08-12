package me.dralle.home.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class FileUtil {

    private static JavaPlugin plugin;
    private static final String configVersion = "1.0.0";

    public FileUtil(JavaPlugin instance) {
        plugin = instance;
    }

    public static void copy(InputStream input, File target) throws IOException {
        if (target.exists()) {
            throw new IOException("File already exists!");
        }
        File parentDir = target.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed at creating directories!");
            }
        }
        if (!target.createNewFile()) {
            throw new IOException("Failed at creating new empty file!");
        }
        if (input == null) {
            throw new NullPointerException("Input is null!");
        }
        byte[] buffer = new byte[1024];
        OutputStream output = new FileOutputStream(target);
        int realLength;
        while ((realLength = input.read(buffer)) > 0) {
            output.write(buffer, 0, realLength);
        }
        output.flush();
        output.close();
    }

    public static InputStream getInputFromJar(String path) {
        if (path == null) {
            throw new IllegalArgumentException("The path can not be null");
        }
        return plugin.getResource(path);
    }

    public static boolean exists(String pathTo) {
        return new File(plugin.getDataFolder() + File.separator + pathTo).exists();
    }

    public static YamlConfiguration loadFile(String pathTo, String internalPath) {
        File conf = new File(plugin.getDataFolder() + File.separator + pathTo);
        if (!conf.exists()) {
            InputStream stream = getInputFromJar(internalPath);
            if (stream != null) {
                try {
                    copy(stream, conf);
                    plugin.getLogger().info("Creating " + pathTo + " for the first time..");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                plugin.getLogger().warning("Could not find internal resource: " + internalPath);
            }
        } else {
            // Update existing config with new keys
            YamlConfiguration externalConfig = YamlConfiguration.loadConfiguration(conf);
            InputStream internalStream = getInputFromJar(internalPath);
            if (internalStream != null) {
                YamlConfiguration internalConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(internalStream, StandardCharsets.UTF_8));
                boolean updated = false;
                for (String key : internalConfig.getKeys(true)) {
                    if (!externalConfig.contains(key)) {
                        externalConfig.set(key, internalConfig.get(key));
                        updated = true;
                    }
                }
                if (updated) {
                    try {
                        externalConfig.save(conf);
                        plugin.getLogger().info("Updated " + pathTo + " with missing configuration keys.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return YamlConfiguration.loadConfiguration(conf);
    }
}
