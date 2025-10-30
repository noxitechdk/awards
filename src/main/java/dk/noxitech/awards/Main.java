package dk.noxitech.awards;

import dev.respark.licensegate.LicenseGate;
import dk.noxitech.awards.commands.AwardsCommand;
import dk.noxitech.awards.commands.AwardsSettingsCommand;
import dk.noxitech.awards.commands.NotificationsCommand;
import dk.noxitech.awards.commands.PlaytimeCommand;
import dk.noxitech.awards.commands.ResetPlaytimeCommand;
import dk.noxitech.awards.database.DatabaseManager;
import dk.noxitech.awards.listeners.PlayerListener;
import dk.noxitech.awards.managers.AwardManager;
import dk.noxitech.awards.tasks.PlaytimeTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;

public final class Main extends JavaPlugin {
    private DatabaseManager database;
    private AwardManager awardManager;
    private Economy economy;
    private PlaytimeTask playtimeTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!verifyLicense()) {
            getLogger().severe("Licens verificering fejlede! Plugin deaktiveres.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Initialiserer Awards System...");
       
        if (!setupEconomy()) {
            getLogger().severe("Vault dependency ikke fundet! Plugin deaktiveres.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        database = new DatabaseManager(this);
        database.connect();

        awardManager = new AwardManager(this, database, economy);

        getServer().getPluginManager().registerEvents(new PlayerListener(database), this);

        getCommand("playtime").setExecutor(new PlaytimeCommand(this, database));
        getCommand("resetplaytime").setExecutor(new ResetPlaytimeCommand(this, awardManager));
        getCommand("notifications").setExecutor(new NotificationsCommand(this, database));
        getCommand("awards").setExecutor(new AwardsCommand(this, awardManager));
        getCommand("awardssettings").setExecutor(new AwardsSettingsCommand(this));

        playtimeTask = new PlaytimeTask(this, awardManager);
        playtimeTask.runTaskTimer(this, 1200L, 1200L);

        getLogger().info("Awards System aktiveret succesfuldt!");

        validateConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Deaktiverer Awards System...");

        if (playtimeTask != null) {
            playtimeTask.cancel();
        }
       
        if (database != null) {
            database.disconnect();
        }
       
        getLogger().info("Awards System deaktiveret!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        getLogger().info("Vault økonomi integration aktiveret!");
        return economy != null;
    }

    private void validateConfig() {
        if (!getConfig().contains("awards") ||
            getConfig().getConfigurationSection("awards").getKeys(false).isEmpty()) {
            getLogger().warning("Ingen awards konfigureret! Tilføj awards under 'awards' i config.yml");
        }

        String licenseKey = getConfig().getString("Core.license", "");
        if (licenseKey.isEmpty()) {
            getLogger().warning("Ingen licens nøgle fundet i config.yml! Tilføj under 'Core.license'");
        }
    }

    private boolean verifyLicense() {
        try {
            LicenseGate licenseGate = new LicenseGate("a20a4");

            String licenseKey = getConfig().getString("Core.license", "");

            if (licenseKey.isEmpty()) {
                getLogger().severe("Ingen licens nøgle fundet i config.yml!");
                getLogger().severe("Tilføj din licens nøgle under 'Core.license' i config.yml");
                return false;
            }

            String serverIP = getServerIP();
            String verificationString = serverIP + ",AwardsPlugin";

            getLogger().info("Verificerer licens for server: " + serverIP);

            LicenseGate.ValidationType result = licenseGate.verify(licenseKey, verificationString);

            if (result == LicenseGate.ValidationType.VALID) {
                getLogger().info("Licens verificeret succesfuldt!");
                return true;
            } else if (result == LicenseGate.ValidationType.EXPIRED) {
                getLogger().severe("Licens er udløbet! Kontakt NoXiTech for fornyelse.");
                return false;
            } else {
                getLogger().severe("Ugyldig licens! Kontakt NoXiTech for support. Status: " + result);
                return false;
            }
        } catch (Exception e) {
            getLogger().severe("Fejl ved licens verificering: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getServerIP() {
        try {
            URL url = new URL("https://api.ipify.org");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = reader.readLine();
            reader.close();

            if (ip != null && !ip.isEmpty()) {
                return ip;
            }
        } catch (Exception e) {
            getLogger().warning("Kunne ikke hente ekstern IP adresse: " + e.getMessage());
        }

        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();
            getLogger().warning("Bruger lokal IP adresse: " + localIP);
            return localIP;
        } catch (Exception e) {
            getLogger().warning("Kunne ikke hente lokal IP adresse: " + e.getMessage());
            return "localhost";
        }
    }
}
