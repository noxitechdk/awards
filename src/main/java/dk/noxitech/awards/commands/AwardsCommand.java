package dk.noxitech.awards.commands;

import dk.noxitech.awards.managers.AwardManager;
import dk.noxitech.awards.models.Award;
import dk.noxitech.awards.models.AwardProgress;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class AwardsCommand implements CommandExecutor {
    private final AwardManager awardManager;
    private final JavaPlugin plugin;
    private final String prefix;

    public AwardsCommand(JavaPlugin plugin, AwardManager awardManager) {
        this.awardManager = awardManager;
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
        Map<Integer, Award> allAwards = awardManager.getAwards();
        Map<Integer, AwardProgress> playerProgress = awardManager.getPlayerProgress(player.getUniqueId());

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            prefix + " &7=== &6Dine Awards &7==="));

        boolean hoverEnabled = plugin.getConfig().getBoolean("hover.enabled", true);
        boolean showItems = plugin.getConfig().getBoolean("hover.show_items", true);

        for (int awardId = 1; awardId <= allAwards.size(); awardId++) {
            Award award = allAwards.get(awardId);
            if (award == null) continue;
           
            AwardProgress progress = playerProgress.getOrDefault(awardId, new AwardProgress());

            String status;
            if (progress.isUnlocked()) {
                status = "&a✓ Unlocked";
            } else if (isPreviousAwardUnlocked(awardId, playerProgress)) {
                int timeLeft = progress.getTimeLeft();
                int hoursLeft = timeLeft / 60;
                int minutesLeft = timeLeft % 60;
                status = "&e" + hoursLeft + "t " + minutesLeft + "m tilbage";
            } else {
                status = "&8🔒 Låst (unlock award " + (awardId - 1) + " først)";
            }

            String rewardType;
            String type = award.getType().toLowerCase();
            if (type.equals("money")) {
                rewardType = "&a$" + (int)award.getMoney();
            } else if (type.equals("items")) {
                rewardType = "&bItems";
            } else if (type.equals("both")) {
                rewardType = "&a$" + (int)award.getMoney() + " &7+ &bItems";
            } else {
                rewardType = "&7Unknown";
            }

            String baseMessage = "&7Award " + awardId + ": " + status + " &7(" + rewardType + "&7)";

            if (hoverEnabled && showItems && (type.equals("items") || type.equals("both")) && 
                award.getItems() != null && !award.getItems().isEmpty()) {
                
                TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', baseMessage));
                
                ComponentBuilder hoverText = new ComponentBuilder("");
                hoverText.append(ChatColor.translateAlternateColorCodes('&', "&6Items i denne award:\n"));
                
                for (String itemString : award.getItems()) {
                    String[] parts = itemString.split(" ");
                    String materialName = parts[0];
                    int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                    
                    String displayName = getDisplayName(materialName);
                    hoverText.append(ChatColor.translateAlternateColorCodes('&', 
                        "&7• &b" + amount + "x " + displayName + "\n"));
                }
                
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.create()));
                player.spigot().sendMessage(message);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', baseMessage));
            }
        }

        return true;
    }

    private String getDisplayName(String materialName) {
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            return material.name().toLowerCase().replace("_", " ");
        } catch (IllegalArgumentException e) {
            return materialName.toLowerCase().replace("_", " ");
        }
    }

    private boolean isPreviousAwardUnlocked(int currentAwardId, Map<Integer, AwardProgress> playerProgress) {
        if (currentAwardId == 1) {
            return true;
        }
       
        AwardProgress previousProgress = playerProgress.get(currentAwardId - 1);
        return previousProgress != null && previousProgress.isUnlocked();
    }
}