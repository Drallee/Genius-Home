package me.dralle.home.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public class PlayerHeadUtils {

    public static ItemStack changePlayerHeadSkinByString(String playerName, ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) {
            return item;
        }
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        if (skullMeta != null) {
            PlayerProfile profile = Bukkit.createProfile(playerName);
            skullMeta.setPlayerProfile(profile);
            skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            skullMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(skullMeta);
        }
        return item;
    }

    public static ItemStack changePlayerHeadSkinByPlayer(@NotNull Player player, @NotNull ItemStack item){
        if (item.getType() != Material.PLAYER_HEAD) {
            return item;
        }
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setPlayerProfile(player.getPlayerProfile());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }
}
