package me.dralle.home.utils;

import me.dralle.home.HomePlugin;
import me.dralle.home.menu.PlayerMenuUtility;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

import static me.dralle.home.database.HomeDBManager.*;
import static me.dralle.home.utils.Utils.*;

public class HomeUtils {

    private static String getChatPrefix() {
        return getConfigMessage("chat.prefix.home");
    }

    private static String getChatPrefixError() {
        return getConfigMessage("chat.prefix.error");
    }

    public static List<me.dralle.home.models.Home> getPlayerHomesList(OfflinePlayer target, Player player, String where) {
        console("&eCALLED: getPlayerHomesList -> " + where, true, true);
        return getAllHomesFromDB(target, player);
    }

    public static int getMaxHomes(OfflinePlayer target){
        int max = getConfigNumber("settings.homes.max-amount");

        if (!getConfigCheck("settings.homes.need-permission") || !target.isOnline()) {
            return max;
        }

        Player onlinePlayer = (Player) target;
        if (onlinePlayer.hasPermission("genius.homes.max.*")) {
            return max;
        }

        IntStream rangeStream;
        if (max == -1) {
            rangeStream = IntStream.iterate(1, i -> i + 1).limit(2800)
                    .boxed()
                    .sorted(Comparator.reverseOrder())
                    .mapToInt(Integer::intValue);
        } else {
            rangeStream = IntStream.rangeClosed(1, max)
                    .boxed()
                    .sorted(Comparator.reverseOrder())
                    .mapToInt(Integer::intValue);
        }

        int permissionMax = rangeStream
                .filter(i -> onlinePlayer.hasPermission("genius.homes.max." + i))
                .findFirst()
                .orElse(0);

        return (max == -1) ? permissionMax : Math.min(max, permissionMax);
    }

    public static int getCurrentHomes(OfflinePlayer target, Player player) {
        return getAllHomesFromDB(target, player).size();
    }

    public static ArrayList<String> getHomeIconsPlayerHeads() {
        if (HomePlugin.getIconsConfig().isSet("icons.player_heads")) {
            return new ArrayList<>(HomePlugin.getIconsConfig().getStringList("icons.player_heads"));
        }
        return new ArrayList<>();
    }

    public static ArrayList<String> getHomeIcons() {
        if (HomePlugin.getIconsConfig().isSet("icons.material")) {
            List<Map<?, ?>> icons = HomePlugin.getIconsConfig().getMapList("icons.material");
            ArrayList<String> iconNames = new ArrayList<>();
            for (Map<?, ?> iconMap : icons) {
                iconNames.add((String) iconMap.get("name"));
            }
            return iconNames;
        }
        return new ArrayList<>();
    }

    public static String getIconDisplayName(String iconName) {
        if (iconName == null || iconName.equalsIgnoreCase("none")) {
            return "None";
        }
        List<Map<?, ?>> icons = HomePlugin.getIconsConfig().getMapList("icons.material");
        for (Map<?, ?> iconMap : icons) {
            if (((String) iconMap.get("name")).equalsIgnoreCase(iconName)) {
                return (String) iconMap.get("display_name");
            }
        }
        return iconName;
    }

    public static void refreshOpenMenus(OfflinePlayer target, Player exclude) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (exclude != null && onlinePlayer.getUniqueId().equals(exclude.getUniqueId())) continue;

            if (onlinePlayer.getOpenInventory().getTopInventory().getHolder() instanceof me.dralle.home.menu.Menu menu) {
                PlayerMenuUtility pmu = menu.getPlayerMenuUtility();
                if (pmu.getTarget().getUniqueId().equals(target.getUniqueId())) {
                    // Update homes list in PMU
                    Bukkit.getScheduler().runTaskAsynchronously(HomePlugin.getInstance(), () -> {
                        List<me.dralle.home.models.Home> homes = getPlayerHomesList(target, onlinePlayer, "refreshOpenMenus");
                        Bukkit.getScheduler().runTask(HomePlugin.getInstance(), () -> {
                            // Check if the player still has the same menu open before refreshing
                            if (onlinePlayer.getOpenInventory().getTopInventory().getHolder() == menu) {
                                pmu.setPlayerHomes(homes);
                                menu.open(); // Re-open (refresh) the menu
                            }
                        });
                    });
                }
            }
        }
    }

    public static String getSoundDisplayName(String soundName) {
        if (soundName == null || soundName.equalsIgnoreCase("none")) {
            return "None";
        }
        List<Map<?, ?>> sounds = HomePlugin.getSoundsConfig().getMapList("sounds.list");
        for (Map<?, ?> soundMap : sounds) {
            if (((String) soundMap.get("name")).equalsIgnoreCase(soundName)) {
                return (String) soundMap.get("display_name");
            }
        }
        return soundName;
    }

    public static void refreshOpenMenus(OfflinePlayer target) {
        refreshOpenMenus(target, null);
    }

    public static void changeHomeName(Player player, OfflinePlayer target, String oldHomeName, String newHomeName, Runnable callback) {
        String name = target.getName();
        UUID targetUUID = target.getUniqueId();
        HomePlugin plugin = HomePlugin.getInstance();

        console("&eRenaming home for &a" + name + "&e: '&f" + oldHomeName + "&e' -> '&f" + newHomeName + "&e'", true, true);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (oldHomeName.equalsIgnoreCase(newHomeName)) {
                    console("&eOld name matches new name. Skipping DB update.", true, true);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (callback != null) callback.run();
                    });
                    return;
                }

                // Get home to check world
                me.dralle.home.models.Home home = getHomeFromDB(targetUUID, oldHomeName);

                if (home == null) {
                    console("&cHome '&f" + oldHomeName + "&c' not found in DB for " + name, true, true);
                    String errorMessageHomeNotFound = rep(getConfigMessage("chat.message.error.player.home.not-found"),
                            "%player%", name,
                            "%timestamp%", updateTimestamp(),
                            "%chat_prefix%", getConfigMessage("chat.prefix.error"),
                            "%home%", oldHomeName);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        msp(player, errorMessageHomeNotFound, true);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                    });
                    return;
                }
                if (checkHomeExistInDB(targetUUID, newHomeName)) {
                    console("&cHome '&f" + newHomeName + "&c' already exists in DB for " + name, true, true);
                    String errorMessageHomeAlreadyExist = rep(getConfigMessage("chat.message.error.player.home.already-exists"),
                            "%home%", newHomeName,
                            "%chat_prefix%", getConfigMessage("chat.prefix.error"));
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        msp(player, errorMessageHomeAlreadyExist, true);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                    });
                    return;
                }
                
                String worldName = home.getLocation().getWorld().getName();
                updateHomeNameToDB(targetUUID, oldHomeName, newHomeName, worldName);
                console("&aSuccessfully updated home name in DB for world " + worldName, true, true);
                
                String homeChangedNameMessage = rep(getConfigMessage("chat.message.player.home.name-changed"),
                        "%old%", oldHomeName,
                        "%new%", newHomeName,
                        "%chat_prefix%", getChatPrefix());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    msp(player, homeChangedNameMessage, true);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                    refreshOpenMenus(target, player);
                    if (callback != null) callback.run();
                });
            } catch (SQLException e) {
                console("&cError trying to change home name", true);
                console("&4" + e, true);
            }
        });
    }

    public static void changeHomeName(Player player, OfflinePlayer target, String oldHomeName, String newHomeName) {
        changeHomeName(player, target, oldHomeName, newHomeName, null);
    }

    public static void changeHomeName(Player player, String oldHomeName, String newHomeName, Runnable callback) {
        changeHomeName(player, player, oldHomeName, newHomeName, callback);
    }

    public static void changeHomeName(Player player, String oldHomeName, String newHomeName) {
        changeHomeName(player, player, oldHomeName, newHomeName, null);
    }

    public static void changeHomeLocation(Player player, me.dralle.home.models.Home home, Location location, Runnable callback) {
        UUID targetUUID = home.getTargetUUID();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        HomePlugin plugin = HomePlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                updateHomeLocationToDB(targetUUID, home, location);
                String message = rep(getConfigMessage("chat.message.player.home.location-updated"),
                        "%chat_prefix%", getChatPrefix());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    msp(player, message, true);
                    refreshOpenMenus(target, player);
                    if (callback != null) callback.run();
                });
            } catch (SQLException e) {
                console("&cError trying to update home location", true);
                console("&4" + e, true);
            }
        });
    }

    public static void changeHomeLocation(Player player, me.dralle.home.models.Home home, Location location) {
        changeHomeLocation(player, home, location, null);
    }

    public static void setHome(Player player, OfflinePlayer target, String homeName, String icon, String skullMeta, Runnable callback) {
        String playerName = target.getName();
        UUID targetUUID = target.getUniqueId();
        Location location = player.getLocation();
        HomePlugin plugin = HomePlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (checkHomeExistInDB(targetUUID, homeName)) {
                    String errorMessage = rep(getConfigMessage("chat.message.error.player.home.already-exists"),
                            "%home%", homeName,
                            "%chat_prefix%", getConfigMessage("chat.prefix.error"));
                    Bukkit.getScheduler().runTask(plugin, () -> msp(player, errorMessage, true));
                    return;
                }
                insertHomeToDB(targetUUID, homeName, icon, skullMeta, "none", location);

                String homeSetMessage = rep(getConfigMessage("chat.message.player.home.set"),
                        "%home%", homeName,
                        "%x%", df.format(location.getX()),
                        "%y%", df.format(location.getY()),
                        "%z%", df.format(location.getZ()),
                        "%chat_prefix%", getChatPrefix());

                int max_homes = getMaxHomes(target);
                int current_homes = getCurrentHomes(target, player);
                String homeAmountMessage = rep(getConfigMessage("chat.message.player.home.amount"),
                        "%player%", playerName,
                        "%timestamp%", updateTimestamp(),
                        "%chat_prefix%", getChatPrefix(),
                        "%current%", current_homes,
                        "%max%", max_homes);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    msp(player, homeAmountMessage, true);
                    msp(player, homeSetMessage, true);
                    refreshOpenMenus(target, player);
                    if (callback != null) callback.run();
                });
            } catch (SQLException e) {
                console("&cError trying to set home", true);
                console("&4" + e, true);
            }
        });
    }

    public static void setHome(Player player, OfflinePlayer target, String homeName, String icon, String skullMeta) {
        setHome(player, target, homeName, icon, skullMeta, null);
    }

    public static void setHome(Player player, String homeName, String icon, String skullMeta, Runnable callback) {
        setHome(player, player, homeName, icon, skullMeta, callback);
    }

    public static void setHome(Player player, String homeName, String icon, String skullMeta) {
        setHome(player, player, homeName, icon, skullMeta, null);
    }

    public static void setHomeIcon(Player player, me.dralle.home.models.Home home, String icon, String skullMeta, Runnable callback) {
        UUID targetUUID = home.getTargetUUID();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        HomePlugin plugin = HomePlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                updateHomeIconToDB(targetUUID, home, icon, skullMeta);
                String homeChangedIconMessageMessage = rep(getConfigMessage("chat.message.player.home.icon-changed"),
                        "%home%", home.getHomeName(),
                        "%chat_prefix%", getChatPrefix());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    msp(player, homeChangedIconMessageMessage, true);
                    refreshOpenMenus(target, player);
                    if (callback != null) callback.run();
                });
            } catch (SQLException e) {
                console("&cError trying to update home icon", true);
                console("&4" + e, true);
            }
        });
    }

    public static void setHomeIcon(Player player, me.dralle.home.models.Home home, String icon, String skullMeta) {
        setHomeIcon(player, home, icon, skullMeta, null);
    }

    public static void setHomeSound(Player player, me.dralle.home.models.Home home, String sound, Runnable callback) {
        UUID targetUUID = home.getTargetUUID();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        HomePlugin plugin = HomePlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                updateHomeSoundToDB(targetUUID, home, sound);
                String homeChangedSoundMessage = rep(getConfigMessage("chat.message.player.home.sound-changed"),
                        "%home%", home.getHomeName(),
                        "%chat_prefix%", getChatPrefix());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    msp(player, homeChangedSoundMessage, true);
                    refreshOpenMenus(target, player);
                    if (callback != null) callback.run();
                });
            } catch (SQLException e) {
                console("&cError trying to update home sound", true);
                console("&4" + e, true);
            }
        });
    }

    public static void setHomeSound(Player player, me.dralle.home.models.Home home, String sound) {
        setHomeSound(player, home, sound, null);
    }

    public static void deleteHome(Player player, me.dralle.home.models.Home home, Runnable callback) {
        UUID targetUUID = home.getTargetUUID();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        HomePlugin plugin = HomePlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                deleteHomeFromDB(targetUUID, home);
                String homeDeleteMessage = rep(getConfigMessage("chat.message.player.home.deleted"),
                        "%home%", home.getHomeName(),
                        "%chat_prefix%", getChatPrefix());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    msp(player, homeDeleteMessage, true);
                    refreshOpenMenus(target, player);
                    if (callback != null) callback.run();
                });
            } catch (SQLException e) {
                console("&cError trying to delete home", true);
                console("&4" + e, true);
            }
        });
    }

    public static void deleteHome(Player player, me.dralle.home.models.Home home) {
        deleteHome(player, home, null);
    }

    private static final Map<UUID, Integer> teleportTasks = new HashMap<>();

    public static boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);

        // Check if feet and head are non-solid
        if (!feet.getType().isAir() && feet.getType() != Material.WATER && !isPassable(feet)) return false;
        if (!head.getType().isAir() && head.getType() != Material.WATER && !isPassable(head)) return false;

        // Check if ground is solid and not dangerous
        if (ground.getType().isAir() || isDangerous(ground.getType())) return false;
        
        return ground.isSolid();
    }

    private static boolean isDangerous(Material type) {
        String name = type.name();
        return type == Material.LAVA || type == Material.MAGMA_BLOCK || type == Material.FIRE || type == Material.SOUL_FIRE || 
               type == Material.CAMPFIRE || type == Material.SOUL_CAMPFIRE || type == Material.CACTUS || type == Material.WITHER_ROSE || 
               type == Material.SWEET_BERRY_BUSH || type == Material.COBWEB || type == Material.POWDER_SNOW ||
               name.contains("DRIPSTONE") || name.contains("VOID") || name.contains("BERRY") || type == Material.LILY_PAD;
    }

    private static boolean isPassable(Block block) {
        // Simple check for common non-solid blocks that aren't air/water
        return block.isPassable() || block.getType().name().endsWith("_SAPLING") || block.getType().name().endsWith("_FLOWER") || block.getType().name().endsWith("_CORAL") || block.getType().name().endsWith("_FAN");
    }

    public static int getCooldownTime(Player player) {
        if (!getConfigCheck("settings.homes.teleport.cooldown.enabled")) {
            return 0;
        }

        if (player.hasPermission("genius.homes.bypass.cooldown")) {
            return 0;
        }

        int configCooldown = getConfigNumber("settings.homes.teleport.cooldown.time");
        
        // Find the lowest custom cooldown from permissions
        int lowestCooldown = configCooldown;
        boolean foundCustom = false;

        for (org.bukkit.permissions.PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            String permission = perm.getPermission().toLowerCase();
            if (permission.startsWith("genius.homes.bypass.cooldown.")) {
                try {
                    String timeStr = permission.substring("genius.homes.bypass.cooldown.".length());
                    int customTime = Integer.parseInt(timeStr);
                    if (!foundCustom || customTime < lowestCooldown) {
                        lowestCooldown = customTime;
                        foundCustom = true;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return lowestCooldown;
    }

    public static void teleportToHome(Player player, OfflinePlayer target, me.dralle.home.models.Home home) {
        Location location = home.getLocation();
        HomePlugin plugin = HomePlugin.getInstance();
        if (location == null) {
            String locationNotFound = rep(getConfigMessage("chat.message.error.player.teleport.location-not-found"),
                    "%chat_prefix%", getChatPrefixError());
            msp(player, locationNotFound, true);
            return;
        }

        if (!getConfigCheck("settings.homes.teleport.unsafe") && !isSafeLocation(location)) {
            String unsafeLocation = rep(getConfigMessage("chat.message.error.player.teleport.unsafe-location"),
                    "%chat_prefix%", getChatPrefixError());
            msp(player, unsafeLocation, true);
            return;
        }

        int time = getCooldownTime(player);
        if (time > 0) {
            cancelTeleport(player);

            String cooldownMessage = rep(getConfigMessage("chat.message.error.player.teleport.cooldown"),
                    "%time%", time,
                    "%chat_prefix%", getChatPrefix());
            msp(player, cooldownMessage, true);

            int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                teleportTasks.remove(player.getUniqueId());
                performTeleport(player, target, home);
            }, time * 20L);

            teleportTasks.put(player.getUniqueId(), taskId);
        } else {
            performTeleport(player, target, home);
        }
    }

    private static void performTeleport(Player player, OfflinePlayer target, me.dralle.home.models.Home home) {
        Location location = home.getLocation();
        String targetName = target.getName();
        String homeName = home.getHomeName();
        String homeSound = home.getSound();

        // Handle cost
        if (getConfigCheck("settings.homes.teleport.cost.enabled")) {
            String costType = getConfigString("settings.homes.teleport.cost.type");
            int amount = getConfigNumber("settings.homes.teleport.cost.amount");

            if (costType.equalsIgnoreCase("VAULT")) {
                if (!EconomyUtils.hasEnough(player, amount)) {
                    String noMoney = rep(getConfigMessage("chat.message.error.player.teleport.insufficient-funds"),
                            "%amount%", amount,
                            "%currency%", EconomyUtils.getCurrencyName(),
                            "%chat_prefix%", getChatPrefixError());
                    msp(player, noMoney, true);
                    return;
                }
                EconomyUtils.withdraw(player, amount);
                String paidMessage = rep(getConfigMessage("chat.message.error.player.teleport.paid"),
                        "%amount%", amount,
                        "%currency%", EconomyUtils.getCurrencyName(),
                        "%chat_prefix%", getChatPrefix());
                msp(player, paidMessage, true);
            } else if (costType.equalsIgnoreCase("XP")) {
                if (getTotalExperience(player) < amount) {
                    String noXP = rep(getConfigMessage("chat.message.error.player.teleport.insufficient-xp"),
                            "%amount%", amount,
                            "%chat_prefix%", getChatPrefixError());
                    msp(player, noXP, true);
                    return;
                }
                setTotalExperience(player, getTotalExperience(player) - amount);
                String paidXPMessage = rep(getConfigMessage("chat.message.error.player.teleport.paid-xp"),
                        "%amount%", amount,
                        "%chat_prefix%", getChatPrefix());
                msp(player, paidXPMessage, true);
            } else if (costType.equalsIgnoreCase("LEVEL")) {
                if (player.getLevel() < amount) {
                    String noLevel = rep(getConfigMessage("chat.message.error.player.teleport.insufficient-levels"),
                            "%amount%", amount,
                            "%chat_prefix%", getChatPrefixError());
                    msp(player, noLevel, true);
                    return;
                }
                player.setLevel(player.getLevel() - amount);
                String paidLevelMessage = rep(getConfigMessage("chat.message.error.player.teleport.paid-levels"),
                        "%amount%", amount,
                        "%chat_prefix%", getChatPrefix());
                msp(player, paidLevelMessage, true);
            } else if (costType.equalsIgnoreCase("ITEM")) {
                String itemString = getConfigString("settings.homes.teleport.cost.item");
                Material material = Material.getMaterial(itemString.toUpperCase());
                if (material == null) {
                    console("&cInvalid material for teleport cost: " + itemString, true);
                    return;
                }

                if (!player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
                    String noItem = rep(getConfigMessage("chat.message.error.player.teleport.insufficient-items"),
                            "%amount%", amount,
                            "%item%", material.name(),
                            "%chat_prefix%", getChatPrefixError());
                    msp(player, noItem, true);
                    return;
                }

                player.getInventory().removeItem(new ItemStack(material, amount));
                String paidItemMessage = rep(getConfigMessage("chat.message.error.player.teleport.paid-item"),
                        "%amount%", amount,
                        "%item%", material.name(),
                        "%chat_prefix%", getChatPrefix());
                msp(player, paidItemMessage, true);
            }
        }

        String homeTeleportMessage;
        if (player.getUniqueId().equals(target.getUniqueId())) {
            homeTeleportMessage = rep(getConfigMessage("chat.message.player.home.teleporting"),
                    "%home%", homeName,
                    "%chat_prefix%", getChatPrefix());
        } else {
            homeTeleportMessage = rep(getConfigMessage("chat.message.player.home.teleporting-other"),
                    "%target%", targetName,
                    "%home%", homeName,
                    "%chat_prefix%", getChatPrefix());
        }

        String soundStr = home.getSound();
        Sound sound = Sound.ENTITY_ENDERMAN_TELEPORT;
        if (soundStr != null && !soundStr.equalsIgnoreCase("none")) {
            try {
                sound = Sound.valueOf(soundStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        playSoundAtLocation(player.getLocation(), sound, 1f, 0.5f);
        player.teleport(location);
        msp(player, homeTeleportMessage, true);
        playSoundAtLocation(location, sound, 1f, 0.9f);
    }

    public static void cancelTeleport(Player player) {
        if (teleportTasks.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(teleportTasks.get(player.getUniqueId()));
            teleportTasks.remove(player.getUniqueId());
            String cancelledMessage = rep(getConfigMessage("chat.message.error.player.teleport.cancelled"),
                    "%chat_prefix%", getChatPrefixError());
            msp(player, cancelledMessage, true);
        }
    }

    public static void playSoundAtLocation(Location location, Sound sound, float volume, float pitch) {
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }

    public static long calculatePagesCount(long pageSize, long totalCount) {
        return totalCount < pageSize ? 1 : (long) Math.ceil((double) totalCount / (double) pageSize);
    }

    // Helper methods to correctly manage player XP
    public static void setTotalExperience(Player player, int exp) {
        if (exp < 0) exp = 0;
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        int amount = exp;
        while (amount > 0) {
            int xpToNextLevel = getExpAtLevel(player.getLevel());
            if (amount >= xpToNextLevel) {
                amount -= xpToNextLevel;
                player.setLevel(player.getLevel() + 1);
            } else {
                float progress = (float) amount / (float) xpToNextLevel;
                player.setExp(progress);
                amount = 0;
            }
        }
        player.setTotalExperience(exp);
    }

    private static int getExpAtLevel(int level) {
        if (level <= 15) return (2 * level) + 7;
        if (level <= 30) return (5 * level) - 38;
        return (9 * level) - 158;
    }

    public static int getTotalExperience(Player player) {
        int exp = Math.round(getExpAtLevel(player.getLevel()) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) exp = 0;
        return exp;
    }

    public static void clearCache() {
        console(getConfigMessage("database.connection.cache-cleared"), true, true);
    }
}
