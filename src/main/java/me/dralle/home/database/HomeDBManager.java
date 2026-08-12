package me.dralle.home.database;

import me.dralle.home.models.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.dralle.home.database.DatabaseUtil.*;
import static me.dralle.home.utils.Utils.*;

public class HomeDBManager {

    public static List<Home> getAllHomesFromDB(OfflinePlayer target, Player player){
        console("&eCALLED: getAllHomesFromDB", true, true);
        UUID targetUUID = target.getUniqueId();
        String playerWorld = player.getWorld().getName();

        List<Home> homes = new ArrayList<>();
        String sql;
        String databaseTable = getConfigString("settings.mysql.table-prefix");
        if (databaseTable.isEmpty()) databaseTable = "homes";

        if(getConfigCheck("settings.homes.per-world")){
            playerWorld = playerWorld.replace("_the_end", "").replace("_nether", "");
            sql = "SELECT * FROM " + databaseTable + " WHERE player_uuid = ? AND (world = ? OR world = ? OR world = ?) ORDER BY home_name ASC, world ASC";
        } else {
            sql = "SELECT * FROM `" + databaseTable + "` WHERE player_uuid = ?";
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, targetUUID.toString());
            if (getConfigCheck("settings.homes.per-world")) {
                statement.setString(2, playerWorld);
                statement.setString(3, playerWorld + "_nether");
                statement.setString(4, playerWorld + "_the_end");
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String homeName = resultSet.getString("home_name");
                    String icon = resultSet.getString("icon_type");
                    String skullMeta = resultSet.getString("skull_meta");
                    String sound = resultSet.getString("sound");
                    String worldName = resultSet.getString("world");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        Location location = new Location(world, resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"), resultSet.getFloat("yaw"), resultSet.getFloat("pitch"));
                        Home home = new Home(targetUUID, homeName, icon, skullMeta, sound, location);
                        homes.add(home);
                    }
                }
            }
        } catch (SQLException e) {
            console("&cError trying to get all homes", true);
            console("&4" + e, true);
        }
        return homes;
    }

    public static Location getHomeLocationFromDB(UUID playerUUID, String homeName) throws SQLException {
        console("&eCALLED: getHomeLocationFromDB", true, true);
        Player player = Bukkit.getPlayer(playerUUID);
        String worldNameByPlayer = player != null ? player.getWorld().getName() : null;
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";
        
        String sql;
        if(getConfigCheck("settings.homes.per-world") && worldNameByPlayer != null){
            worldNameByPlayer = worldNameByPlayer.replace("_the_end", "").replace("_nether", "");
            sql = "SELECT x, y, z, world FROM " + tableName + " WHERE player_uuid = ? AND home_name = ? AND (world = ? OR world = ? OR world = ?)";
        } else {
            sql = "SELECT x, y, z, world FROM " + tableName + " WHERE player_uuid = ? AND home_name = ?";
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, playerUUID.toString());
            statement.setString(2, homeName);
            if (getConfigCheck("settings.homes.per-world") && worldNameByPlayer != null) {
                statement.setString(3, worldNameByPlayer);
                statement.setString(4, worldNameByPlayer + "_nether");
                statement.setString(5, worldNameByPlayer + "_the_end");
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");
                    String worldName = resultSet.getString("world");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        return new Location(world, x, y, z);
                    }
                }
            }
        }
        return null;
    }

    public static Home getHomeFromDB(UUID playerUUID, String homeName) throws SQLException {
        console("&eCALLED: getHomeFromDB", true, true);
        Player player = Bukkit.getPlayer(playerUUID);
        String worldNameByPlayer = player != null ? player.getWorld().getName() : null;
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";
        
        String sql;
        if(getConfigCheck("settings.homes.per-world") && worldNameByPlayer != null){
            worldNameByPlayer = worldNameByPlayer.replace("_the_end", "").replace("_nether", "");
            sql = "SELECT * FROM " + tableName + " WHERE player_uuid = ? AND home_name = ? AND (world = ? OR world = ? OR world = ?)";
        } else {
            sql = "SELECT * FROM " + tableName + " WHERE player_uuid = ? AND home_name = ?";
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, playerUUID.toString());
            statement.setString(2, homeName);
            if (getConfigCheck("settings.homes.per-world") && worldNameByPlayer != null) {
                statement.setString(3, worldNameByPlayer);
                statement.setString(4, worldNameByPlayer + "_nether");
                statement.setString(5, worldNameByPlayer + "_the_end");
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String homeHomeName = resultSet.getString("home_name");
                    String iconType = resultSet.getString("icon_type");
                    String skullMeta = resultSet.getString("skull_meta");
                    String sound = resultSet.getString("sound");
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");
                    float pitch = resultSet.getFloat("pitch");
                    float yaw = resultSet.getFloat("yaw");
                    String worldName = resultSet.getString("world");
                    World world = Bukkit.getWorld(worldName);
                    Location location = new Location(world, x, y, z, yaw, pitch);
                    return new Home(playerUUID, homeHomeName, iconType, skullMeta, sound, location);
                }
            }
        }
        return null;
    }

    public static boolean checkHomeExistInDB(UUID playerUUID, String homeName) throws SQLException {
        console("&eCALLED: checkHomeExistInDB", true, true);
        Player player = Bukkit.getPlayer(playerUUID);
        String worldNameByPlayer = player != null ? player.getWorld().getName() : null;
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";
        
        String sql;
        if(getConfigCheck("settings.homes.per-world") && worldNameByPlayer != null){
            worldNameByPlayer = worldNameByPlayer.replace("_the_end", "").replace("_nether", "");
            sql = "SELECT home_name FROM " + tableName + " WHERE player_uuid = ? AND home_name = ? AND (world = ? OR world = ? OR world = ?)";
        } else {
            sql = "SELECT home_name FROM `" + tableName + "` WHERE player_uuid = ? AND home_name = ?";
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, playerUUID.toString());
            statement.setString(2, homeName);
            if (getConfigCheck("settings.homes.per-world") && worldNameByPlayer != null) {
                statement.setString(3, worldNameByPlayer);
                statement.setString(4, worldNameByPlayer + "_nether");
                statement.setString(5, worldNameByPlayer + "_the_end");
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("home_name").equalsIgnoreCase(homeName);
                }
            }
        } catch (SQLException e) {
            console("&cError checking home existence", true);
            console("&4" + e, true);
        }

        return false;
    }

    public static void insertHomeToDB(UUID playerUUID, String homeName, String iconType, String skullMeta, String sound, Location location) throws SQLException {
        console("&eCALLED: insertHomeToDB", true, true);
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";

        String sql = "INSERT INTO `" + tableName + "` (player_uuid, home_name, icon_type, skull_meta, sound, x, y, z, pitch, yaw, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, playerUUID.toString());
            statement.setString(2, homeName);
            statement.setString(3, iconType);
            statement.setString(4, skullMeta);
            statement.setString(5, sound);
            statement.setDouble(6, location.getX());
            statement.setDouble(7, location.getY());
            statement.setDouble(8, location.getZ());
            statement.setFloat(9, location.getPitch());
            statement.setFloat(10, location.getYaw());
            statement.setString(11, location.getWorld().getName());

            statement.executeUpdate();
        }
    }

    public static void updateHomeNameToDB(UUID playerUUID, String oldHomeName, String newHomeName, String worldName) throws SQLException {
        console("&eCALLED: updateHomeNameToDB", true, true);
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";
        
        String sql = "UPDATE `" + tableName + "` SET home_name = ? WHERE player_uuid = ? AND home_name = ? AND world = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, newHomeName);
            statement.setString(2, playerUUID.toString());
            statement.setString(3, oldHomeName);
            statement.setString(4, worldName);

            int rowsAffected = statement.executeUpdate();
            console("&eUpdate home name affected " + rowsAffected + " rows.", true, true);
        }
    }

    public static void updateHomeNameToDB(UUID playerUUID, String oldHomeName, String newHomeName) throws SQLException {
        // Legacy/fallback version
        console("&eCALLED: updateHomeNameToDB (Legacy)", true, true);
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";
        
        String sql = "UPDATE `" + tableName + "` SET home_name = ? WHERE player_uuid = ? AND home_name = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, newHomeName);
            statement.setString(2, playerUUID.toString());
            statement.setString(3, oldHomeName);

            int rowsAffected = statement.executeUpdate();
            console("&eUpdate home name (Legacy) affected " + rowsAffected + " rows.", true, true);
        }
    }

    public static void updateHomeLocationToDB(UUID playerUUID, Home home, Location location) throws SQLException {
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";
        
        String sql;
        if (getConfigCheck("settings.homes.per-world")) {
            String homeWorld = location.getWorld().getName().replace("_the_end", "").replace("_nether", "");
            sql = "UPDATE `" + tableName + "` SET x = ?, y = ?, z = ?, pitch = ?, yaw = ?, world = ? WHERE player_uuid = ? AND home_name = ? AND (world = ? OR world = ? OR world = ?)";
        } else {
            sql = "UPDATE `" + tableName + "` SET x = ?, y = ?, z = ?, pitch = ?, yaw = ?, world = ? WHERE player_uuid = ? AND home_name = ?";
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setDouble(1, location.getX());
            statement.setDouble(2, location.getY());
            statement.setDouble(3, location.getZ());
            statement.setFloat(4, location.getPitch());
            statement.setFloat(5, location.getYaw());
            statement.setString(6, location.getWorld().getName());
            statement.setString(7, playerUUID.toString());
            statement.setString(8, home.getHomeName());
            
            if (getConfigCheck("settings.homes.per-world")) {
                String homeWorld = location.getWorld().getName().replace("_the_end", "").replace("_nether", "");
                statement.setString(9, homeWorld);
                statement.setString(10, homeWorld + "_nether");
                statement.setString(11, homeWorld + "_the_end");
            }

            statement.executeUpdate();
        }
    }

    public static void updateHomeIconToDB(UUID playerUUID, Home home, String iconType, String skullMeta) throws SQLException {
        console("&eCALLED: updateHomeIconToDB", true, true);
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";
        
        String sql = "UPDATE `" + tableName + "` SET icon_type = ?, skull_meta = ? WHERE player_uuid = ? AND home_name = ? AND world = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, iconType);
            statement.setString(2, skullMeta);
            statement.setString(3, playerUUID.toString());
            statement.setString(4, home.getHomeName());
            statement.setString(5, home.getLocation().getWorld().getName());

            statement.executeUpdate();
        }
    }

    public static void updateHomeSoundToDB(UUID playerUUID, Home home, String sound) throws SQLException {
        console("&eCALLED: updateHomeSoundToDB", true, true);
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";

        String sql = "UPDATE `" + tableName + "` SET sound = ? WHERE player_uuid = ? AND home_name = ? AND world = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, sound);
            statement.setString(2, playerUUID.toString());
            statement.setString(3, home.getHomeName());
            statement.setString(4, home.getLocation().getWorld().getName());

            statement.executeUpdate();
        }
    }

    public static void deleteHomeFromDB(UUID playerUUID, Home home) throws SQLException {
        console("&eCALLED: deleteHomeFromDB", true, true);
        String tableName = getConfigString("settings.mysql.table-prefix");
        if (tableName.isEmpty()) tableName = "homes";
        
        String sql = "DELETE FROM `" + tableName + "` WHERE player_uuid = ? AND home_name = ? AND world = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, playerUUID.toString());
            statement.setString(2, home.getHomeName());
            statement.setString(3, home.getLocation().getWorld().getName());

            statement.executeUpdate();
        }
    }
}
