package dk.noxitech.awards.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AwardsSettingsCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final String prefix;

    public AwardsSettingsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.prefix = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("prefix", "&8[&6Awards&8]"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Denne kommando kan kun bruges af spillere!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            boolean hoverEnabled = plugin.getConfig().getBoolean("hover.enabled", true);
            boolean showItems = plugin.getConfig().getBoolean("hover.show_items", true);
            
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                prefix + " &7=== &6Awards Indstillinger &7==="));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                "&7Hover aktiveret: " + (hoverEnabled ? "&aJa" : "&cNej")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                "&7Vis items i hover: " + (showItems ? "&aJa" : "&cNej")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                "&7Brug &e/awardssettings toggle hover &7eller &e/awardssettings toggle items"));
            return true;
        }

        if (!player.hasPermission("awards.admin")) {
            player.sendMessage(ChatColor.RED + "Du har ikke tilladelse til at ændre indstillinger!");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            if (args[1].equalsIgnoreCase("hover")) {
                boolean current = plugin.getConfig().getBoolean("hover.enabled", true);
                plugin.getConfig().set("hover.enabled", !current);
                plugin.saveConfig();
                
                String status = !current ? "&aaktiveret" : "&cdeaktiveret";
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    prefix + " &7Hover er nu " + status));
            } else if (args[1].equalsIgnoreCase("items")) {
                boolean current = plugin.getConfig().getBoolean("hover.show_items", true);
                plugin.getConfig().set("hover.show_items", !current);
                plugin.saveConfig();
                
                String status = !current ? "&aaktiveret" : "&cdeaktiveret";
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    prefix + " &7Vis items i hover er nu " + status));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    prefix + " &cBrug: /awardssettings toggle [hover|items]"));
            }
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                prefix + " &cBrug: /awardssettings [toggle hover|toggle items]"));
        }

        return true;
    }
}