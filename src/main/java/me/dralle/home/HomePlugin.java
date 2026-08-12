package me.dralle.home;

import me.dralle.home.menu.PlayerMenuUtility;
import me.dralle.home.commands.homeCommands.DelHomeCommand;
import me.dralle.home.commands.homeCommands.HomeCommand;
import me.dralle.home.commands.homeCommands.ReloadCommand;
import me.dralle.home.commands.homeCommands.RenameHomeCommand;
import me.dralle.home.commands.homeCommands.SetHomeCommand;
import me.dralle.home.database.DatabaseUtil;
import me.dralle.home.listeners.MenuListener;
import me.dralle.home.listeners.TeleportListener;
import me.dralle.home.listeners.JoinLeaveListener;
import me.dralle.home.utils.EconomyUtils;
import me.dralle.home.utils.FileUtil;
import me.dralle.home.utils.UpdateChecker;
import me.dralle.home.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class HomePlugin extends JavaPlugin {

    private static HomePlugin instance;
    private static FileConfiguration homeConfig;
    private static FileConfiguration iconsConfig;
    private static FileConfiguration messagesConfig;
    private static FileConfiguration soundsConfig;
    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    private String latestVersion;
    private boolean updateAvailable = false;
    private ScheduledExecutorService executorService;

    /**
     * Enables plugin; loads configs; registers commands/listeners; sets up economy/database
     */
    @Override
    public void onEnable() {
        instance = this;

        // Load configurations
        new FileUtil(this);
        homeConfig = FileUtil.loadFile("config.yml", "config.yml");
        iconsConfig = FileUtil.loadFile("home-icons.yml", "home-icons.yml");
        messagesConfig = FileUtil.loadFile("messages.yml", "messages.yml");
        soundsConfig = FileUtil.loadFile("home-sounds.yml", "home-sounds.yml");

        // Economy setup
        if (!EconomyUtils.setupEconomy()) {
            getLogger().warning("Vault not found or no economy plugin found! Vault features will be disabled.");
        }

        // Database setup
        DatabaseUtil.initialize(this);

        // Register commands
        HomeCommand homeCommand = new HomeCommand(this);
        Objects.requireNonNull(getCommand("home")).setExecutor(homeCommand);
        Objects.requireNonNull(getCommand("home")).setTabCompleter(homeCommand);
        Objects.requireNonNull(getCommand("homes")).setExecutor(homeCommand);
        Objects.requireNonNull(getCommand("homes")).setTabCompleter(homeCommand);
        
        Objects.requireNonNull(getCommand("sethome")).setExecutor(new SetHomeCommand(this));
        
        DelHomeCommand delHomeCommand = new DelHomeCommand(this);
        Objects.requireNonNull(getCommand("delhome")).setExecutor(delHomeCommand);
        Objects.requireNonNull(getCommand("delhome")).setTabCompleter(delHomeCommand);
        
        RenameHomeCommand renameHomeCommand = new RenameHomeCommand(this);
        Objects.requireNonNull(getCommand("home-rename")).setExecutor(renameHomeCommand);
        Objects.requireNonNull(getCommand("home-rename")).setTabCompleter(renameHomeCommand);
        
        Objects.requireNonNull(getCommand("reload-home-config")).setExecutor(new ReloadCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(this), this);

        // Update checker
        if (Utils.getConfigCheck("settings.update-checker")) {
            new UpdateChecker(this, "genius-homes").getVersion(version -> {
                if (this.getDescription().getVersion().equals(version)) {
                    getLogger().info("There is not a new update available.");
                } else {
                    this.latestVersion = version;
                    this.updateAvailable = true;
                    getLogger().info("There is a new update available.");
                    getLogger().info("Current version: " + this.getDescription().getVersion());
                    getLogger().info("Latest version: " + version);
                    getLogger().info("Download it at: https://modrinth.com/plugin/genius-homes");
                }
            });
        }

        // bStats
        if (Utils.getConfigCheck("settings.bstats")) {
            int pluginId = 28727;
            Metrics metrics = new Metrics(this, pluginId);
            
            // Custom charts
            metrics.addCustomChart(new org.bstats.charts.SimplePie("database_type", () -> 
                Utils.getConfigCheck("settings.mysql.use") ? "MySQL" : "SQLite"));
                
            metrics.addCustomChart(new org.bstats.charts.SimplePie("teleport_cost_type", () -> 
                Utils.getConfigString("settings.homes.teleport.cost.type")));
                
            metrics.addCustomChart(new org.bstats.charts.SimplePie("teleport_cooldown_enabled", () -> 
                Utils.getConfigCheck("settings.homes.teleport.cooldown.enabled") ? "Enabled" : "Disabled"));
        }

        getLogger().info("Genius-Homes has been enabled!");
    }

    @Override
    public void onDisable() {
        DatabaseUtil.close();
        getLogger().info("Genius-Homes has been disabled!");
    }

    public void reloadConfigs() {
        homeConfig = FileUtil.loadFile("config.yml", "config.yml");
        iconsConfig = FileUtil.loadFile("home-icons.yml", "home-icons.yml");
        messagesConfig = FileUtil.loadFile("messages.yml", "messages.yml");
        soundsConfig = FileUtil.loadFile("home-sounds.yml", "home-sounds.yml");
    }

    public static HomePlugin getInstance() {
        return instance;
    }

    public static FileConfiguration getHomeConfig() {
        return homeConfig;
    }

    public static FileConfiguration getIconsConfig() {
        return iconsConfig;
    }

    public static FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public static FileConfiguration getSoundsConfig() {
        return soundsConfig;
    }

    public static PlayerMenuUtility getPlayerMenuUtility(Player p) {
        PlayerMenuUtility playerMenuUtility;
        if (!(playerMenuUtilityMap.containsKey(p))) {
            playerMenuUtility = new PlayerMenuUtility(instance, p);
            playerMenuUtilityMap.put(p, playerMenuUtility);
            return playerMenuUtility;
        } else {
            return playerMenuUtilityMap.get(p);
        }
    }

    public static void removePlayerMenuUtility(Player p) {
        playerMenuUtilityMap.remove(p);
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
