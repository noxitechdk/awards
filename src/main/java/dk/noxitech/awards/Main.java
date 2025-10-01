package dk.noxitech.awards;

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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private DatabaseManager database;
    private AwardManager awardManager;
    private Economy economy;
    private PlaytimeTask playtimeTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
       
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

        getLogger().info("Awards plugin aktiveret succesfuldt!");
    }

    @Override
    public void onDisable() {
        if (playtimeTask != null) {
            playtimeTask.cancel();
        }
       
        if (database != null) {
            database.disconnect();
        }
       
        getLogger().info("Awards plugin deaktiveret.");
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
        return economy != null;
    }
}
