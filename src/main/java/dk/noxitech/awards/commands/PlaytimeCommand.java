package dk.noxitech.awards.commands;

import dk.noxitech.awards.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlaytimeCommand implements CommandExecutor {
    private final DatabaseManager database;
    private final String prefix;

    public PlaytimeCommand(JavaPlugin plugin, DatabaseManager database) {
        this.database = database;
        this.prefix = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("Core.prefix", "&8[&6Awards&8]"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Denne kommando kan kun bruges af spillere!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            int totalMinutes = database.getPlaytime(player.getUniqueId());
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
           
            String message = ChatColor.translateAlternateColorCodes('&',
                prefix + " &7Du har spillet i &e" + hours + " timer &7og &e" + minutes + " minutter");
            player.sendMessage(message);
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    prefix + " &cSpilleren " + args[0] + " findes ikke!"));
                return true;
            }

            int totalMinutes = database.getPlaytime(target.getUniqueId());
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
           
            String message = ChatColor.translateAlternateColorCodes('&',
                prefix + " &7" + target.getName() + " har spillet i &e" + hours + " timer &7og &e" + minutes + " minutter");
            player.sendMessage(message);
        }

        return true;
    }
}