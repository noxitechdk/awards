package dk.noxitech.awards.commands;

import dk.noxitech.awards.managers.AwardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ResetPlaytimeCommand implements CommandExecutor {
    private final AwardManager awardManager;
    private final String prefix;

    public ResetPlaytimeCommand(JavaPlugin plugin, AwardManager awardManager) {
        this.awardManager = awardManager;
        this.prefix = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("Core.prefix", "&8[&6Awards&8]"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("awards.reset")) {
            sender.sendMessage(ChatColor.RED + "Du har ikke tilladelse til at bruge denne kommando!");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Du skal specificere en spiller når du bruger konsollen!");
                return true;
            }

            Player player = (Player) sender;
            awardManager.resetPlayerAwards(player.getUniqueId());
           
            String message = ChatColor.translateAlternateColorCodes('&',
                prefix + " &7Din spilletid og awards er blevet nulstillet!");
            player.sendMessage(message);
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    prefix + " &cSpilleren " + args[0] + " findes ikke!"));
                return true;
            }

            awardManager.resetPlayerAwards(target.getUniqueId());
           
            String message = ChatColor.translateAlternateColorCodes('&',
                prefix + " &7" + target.getName() + "'s spilletid og awards er blevet nulstillet!");
            sender.sendMessage(message);
        }

        return true;
    }
}