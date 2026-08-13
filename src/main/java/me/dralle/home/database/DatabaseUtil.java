package me.dralle.home.database;

import java.io.File;
import java.sql.*;
import me.dralle.home.HomePlugin;
import static me.dralle.home.utils.Utils.*;

public class DatabaseUtil {
    private static Connection connection;

    private static HomePlugin plugin;

    public static void initialize(HomePlugin homePlugin) {
        plugin = homePlugin;
        connect();
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    public static void connect() {
        try {
            if (!getConfigCheck("settings.mysql.use")) {
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                String url = "jdbc:sqlite:" + dataFolder + File.separator + "homes.db";
                connection = DriverManager.getConnection(url);
                console(getConfigMessage("database.connection.sqlite"), true, true);
            } else {
                String host = getConfigString("settings.mysql.host");
                int port = getConfigNumber("settings.mysql.port");
                String username = getConfigString("settings.mysql.username");
                String password = getConfigString("settings.mysql.password");
                String database = getConfigString("settings.mysql.database");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
                connection = DriverManager.getConnection(url, username, password);
                console(getConfigMessage("database.connection.mysql"), true, true);
            }

            String columns;
            if (!getConfigCheck("settings.mysql.use")) {
                columns = "id INTEGER PRIMARY KEY AUTOINCREMENT, player_uuid TEXT DEFAULT 'none', home_name TEXT DEFAULT 'no_name', world TEXT DEFAULT 'world', x REAL DEFAULT 0.0, y REAL DEFAULT 0.0, z REAL DEFAULT 0.0, pitch REAL DEFAULT 0.0, yaw REAL DEFAULT 0.0, icon_type TEXT DEFAULT 'RED_BED', skull_meta TEXT DEFAULT 'none', sound TEXT DEFAULT 'none'";
            } else {
                columns = "id INT PRIMARY KEY AUTO_INCREMENT, player_uuid VARCHAR(36) DEFAULT 'none', home_name VARCHAR(255) DEFAULT 'no_name', world VARCHAR(255) DEFAULT 'world', x DOUBLE DEFAULT 0.0, y DOUBLE DEFAULT 0.0, z DOUBLE DEFAULT 0.0, pitch FLOAT DEFAULT 0.0, yaw DOUBLE DEFAULT 0.0, icon_type VARCHAR(255) DEFAULT 'RED_BED', skull_meta VARCHAR(255) DEFAULT 'none', sound VARCHAR(255) DEFAULT 'none'";
            }
            String tableName = getConfigString("settings.mysql.table-prefix");
            if (tableName.isEmpty()) tableName = "homes";
            createTable(tableName, columns);
        } catch (Exception e) {
            console(getConfigMessage("database.connection.error"), true);
            console("&4" + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean tableExists(String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(null, null, tableName, null)) {
            return tables.next();
        }
    }

    public static void createTable(String tableName, String columns) throws SQLException {
        if (!tableExists(tableName)) {
            String sql = "CREATE TABLE " + tableName + " (" + columns + ")";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
            console(rep(getConfigMessage("database.connection.table-created"), "%table%", tableName), true, true);
        } else {
            console(rep(getConfigMessage("database.connection.table-exists"), "%table%", tableName), true, true);
            updateTableSchema(tableName);
        }
    }

    private static void updateTableSchema(String tableName) {
        try {
            // Check for missing columns and add them if necessary
            checkAndAddColumn(tableName, "icon_type", getConfigCheck("settings.mysql.use") ? "VARCHAR(255) DEFAULT 'RED_BED'" : "TEXT DEFAULT 'RED_BED'");
            checkAndAddColumn(tableName, "skull_meta", getConfigCheck("settings.mysql.use") ? "VARCHAR(255) DEFAULT 'none'" : "TEXT DEFAULT 'none'");
            checkAndAddColumn(tableName, "sound", getConfigCheck("settings.mysql.use") ? "VARCHAR(255) DEFAULT 'none'" : "TEXT DEFAULT 'none'");
        } catch (SQLException e) {
            console("&4Error updating database schema: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private static void checkAndAddColumn(String tableName, String columnName, String columnDef) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            if (!rs.next()) {
                String sql = "ALTER TABLE `" + tableName + "` ADD COLUMN " + columnName + " " + columnDef;
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(sql);
                    console("&eAdded missing column '&a" + columnName + "&e' to table '&a" + tableName + "&e'", true, true);
                }
            }
        }
    }
}
