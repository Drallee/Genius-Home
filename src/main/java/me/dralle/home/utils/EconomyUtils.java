package me.dralle.home.utils;

import me.dralle.home.HomePlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyUtils {

    private static Economy econ = null;

    public static boolean setupEconomy() {
        if (HomePlugin.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = HomePlugin.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static boolean hasEnough(Player player, double amount) {
        if (econ == null) return true;
        return econ.has(player, amount);
    }

    public static void withdraw(Player player, double amount) {
        if (econ == null) return;
        econ.withdrawPlayer(player, amount);
    }

    public static String getCurrencyName() {
        if (econ == null) return "money";
        return econ.currencyNamePlural();
    }
}
