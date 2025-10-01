package dk.noxitech.awards.listeners;

import dk.noxitech.awards.database.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private final DatabaseManager database;

    public PlayerListener(DatabaseManager database) {
        this.database = database;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        database.initializePlayer(event.getPlayer().getUniqueId());
    }
}