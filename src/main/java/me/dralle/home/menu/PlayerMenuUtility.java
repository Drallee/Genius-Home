package me.dralle.home.menu;

import me.dralle.home.HomePlugin;
import me.dralle.home.models.Home;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerMenuUtility {
    private final HomePlugin plugin;
    private Player owner;
    private Player player;
    private OfflinePlayer target;
    private Home home;
    private List<Home> homes = new ArrayList<>();
    private List<Home> targetHomes = new ArrayList<>();

    public PlayerMenuUtility(HomePlugin plugin, Player p) {
        this.plugin = plugin;
        this.owner = p;
    }

    public HomePlugin getPlugin() {
        return plugin;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setTarget(OfflinePlayer player) {
        this.target = player;
    }

    public void setHomeToChange(Home home) {
        this.home = home;
    }

    public void setPlayerHomes(List<Home> homes) {
        this.homes = homes;
    }

    public void setTargetHomesHomes(ArrayList<Home> targetHomes) {
        this.targetHomes = targetHomes;
    }

    public Player getOwner() {
        return owner;
    }

    public Player getPlayer() {
        return player;
    }

    public OfflinePlayer getTarget() {
        return target;
    }

    public Home getPlayerHome() {
        return home;
    }

    public Home getHomeByName(String homeName) {
        for (Home home : homes) {
            if (home.getHomeName().equalsIgnoreCase(homeName)) {
                return home;
            }
        }
        return null;
    }

    public List<Home> getPlayerHomes() {
        return homes;
    }

    public List<Home> getTargetHomes() {
        return targetHomes;
    }

    public void updateHomeIconByName(String homeName, String icon, String skullMeta) {
        for (Home home : homes) {
            if (home.getHomeName().equalsIgnoreCase(homeName)) {
                home.setIconType(icon);
                home.setSkullMeta(skullMeta);
                break;
            }
        }
    }
}
