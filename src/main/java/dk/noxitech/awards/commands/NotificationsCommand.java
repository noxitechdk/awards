package dk.noxitech.awards.commands;

import dk.noxitech.awards.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NotificationsCommand implements CommandExecutor {
    private final DatabaseManager database;
    private final String prefix;

    public NotificationsCommand(JavaPlugin plugin, DatabaseManager database) {
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
            boolean enabled = database.hasNotifications(player.getUniqueId());
            String status = enabled ? "&aaktiveret" : "&cdeaktiveret";
            String message = ChatColor.translateAlternateColorCodes('&',
                prefix + " &7Dine notifikationer er " + status);
            player.sendMessage(message);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                prefix + " &7Brug &e/notifications toggle &7for at ændre"));
            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            boolean currentState = database.hasNotifications(player.getUniqueId());
            boolean newState = !currentState;
           
            database.setNotifications(player.getUniqueId(), newState);
           
            String status = newState ? "&aaktiveret" : "&cdeaktiveret";
            String message = ChatColor.translateAlternateColorCodes('&',
                prefix + " &7Notifikationer er nu " + status);
            player.sendMessage(message);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                prefix + " &cBrug: /notifications [toggle]"));
        }

        return true;
    }
}