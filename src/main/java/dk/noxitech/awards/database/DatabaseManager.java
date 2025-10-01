package dk.noxitech.awards.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.noxitech.awards.models.AwardProgress;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;
    private final JavaPlugin plugin;
    private final Gson gson;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
    }

    public void connect() {
        try {
            String databaseType = plugin.getConfig().getString("database.type", "sqlite");
           
            if (databaseType.equalsIgnoreCase("mysql")) {
                connectMySQL();
            } else {
                connectSQLite();
            }
           
            createTables();
            plugin.getLogger().info("Database tilsluttet succesfuldt (" + databaseType + ")!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Kunne ikke oprette forbindelse til database: " + e.getMessage());
        }
    }

    private void connectSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File databaseFile = new File(dataFolder, "awards.db");
        String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);
    }

    private void connectMySQL() throws SQLException {
        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "awards");
        String username = plugin.getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "password");
        boolean useSSL = plugin.getConfig().getBoolean("database.mysql.useSSL", false);

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL + "&autoReconnect=true";
        connection = DriverManager.getConnection(url, username, password);
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database forbindelse lukket.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved lukning af database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String createAwardsTable = """
            CREATE TABLE IF NOT EXISTS awards (
                uuid VARCHAR(36) PRIMARY KEY,
                playtime INTEGER DEFAULT 0,
                awards_data TEXT DEFAULT '{}',
                notifications BOOLEAN DEFAULT 1
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createAwardsTable);
        }
    }

    public void initializePlayer(UUID uuid) {
        String sql = "INSERT IGNORE INTO awards (uuid) VALUES (?)";
        String databaseType = plugin.getConfig().getString("database.type", "sqlite");
       
        if (databaseType.equalsIgnoreCase("sqlite")) {
            sql = "INSERT OR IGNORE INTO awards (uuid) VALUES (?)";
        }
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved initialisering af spiller: " + e.getMessage());
        }
    }

    public int getPlaytime(UUID uuid) {
        String sql = "SELECT playtime FROM awards WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("playtime");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved hentning af spilletid: " + e.getMessage());
        }
        return 0;
    }

    public void setPlaytime(UUID uuid, int playtime) {
        String sql = "UPDATE awards SET playtime = ? WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playtime);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved opdatering af spilletid: " + e.getMessage());
        }
    }

    public void addPlaytime(UUID uuid, int minutes) {
        String sql = "UPDATE awards SET playtime = playtime + ? WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, minutes);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved tilføjelse af spilletid: " + e.getMessage());
        }
    }

    public Map<Integer, AwardProgress> getAwardsData(UUID uuid) {
        String sql = "SELECT awards_data FROM awards WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String jsonData = rs.getString("awards_data");
                if (jsonData != null && !jsonData.equals("{}")) {
                    Type type = new TypeToken<Map<Integer, AwardProgress>>(){}.getType();
                    return gson.fromJson(jsonData, type);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved hentning af awards data: " + e.getMessage());
        }
        return new HashMap<>();
    }

    public void updateAwardsData(UUID uuid, Map<Integer, AwardProgress> awardsData) {
        String sql = "UPDATE awards SET awards_data = ? WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String jsonData = gson.toJson(awardsData);
            pstmt.setString(1, jsonData);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved opdatering af awards data: " + e.getMessage());
        }
    }

    public void resetPlayerData(UUID uuid) {
        String sql = "UPDATE awards SET playtime = 0, awards_data = '{}' WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved nulstilling af spillerdata: " + e.getMessage());
        }
    }

    public boolean hasNotifications(UUID uuid) {
        String sql = "SELECT notifications FROM awards WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("notifications");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved tjek af notifikationer: " + e.getMessage());
        }
        return true;
    }

    public void setNotifications(UUID uuid, boolean enabled) {
        String sql = "UPDATE awards SET notifications = ? WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, enabled);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fejl ved opdatering af notifikationer: " + e.getMessage());
        }
    }
}