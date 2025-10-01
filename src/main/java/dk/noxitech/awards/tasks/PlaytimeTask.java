package dk.noxitech.awards.tasks;

import dk.noxitech.awards.Main;
import dk.noxitech.awards.managers.AwardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlaytimeTask extends BukkitRunnable {
    private final Main plugin;
    private final AwardManager awardManager;

    public PlaytimeTask(Main plugin, AwardManager awardManager) {
        this.plugin = plugin;
        this.awardManager = awardManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            awardManager.addPlaytime(player);
        }
    }
}