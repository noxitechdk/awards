package dk.noxitech.awards.managers;

import dk.noxitech.awards.Main;
import dk.noxitech.awards.database.DatabaseManager;
import dk.noxitech.awards.models.Award;
import dk.noxitech.awards.models.AwardProgress;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AwardManager {
    private final Main plugin;
    private final DatabaseManager database;
    private final Economy economy;
    private final Map<Integer, Award> awards;
    private final String prefix;

    public AwardManager(Main plugin, DatabaseManager database, Economy economy) {
        this.plugin = plugin;
        this.database = database;
        this.economy = economy;
        this.awards = new HashMap<>();
        this.prefix = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("prefix", "&8[&6Awards&8]"));
        loadAwards();
    }

    private void loadAwards() {
        var awardsSection = plugin.getConfig().getConfigurationSection("awards");
        if (awardsSection != null) {
            for (String key : awardsSection.getKeys(false)) {
                try {
                    int awardId = Integer.parseInt(key);
                    String path = "awards." + key + ".";
                   
                    int time = plugin.getConfig().getInt(path + "time");
                    String type = plugin.getConfig().getString(path + "type", "money");
                    double money = plugin.getConfig().getDouble(path + "money");
                    List<String> items = plugin.getConfig().getStringList(path + "items");
                    String message = plugin.getConfig().getString(path + "message");
                   
                    awards.put(awardId, new Award(time, type, money, items, message));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Ugyldig award ID: " + key);
                }
            }
        }
        plugin.getLogger().info("Indlæst " + awards.size() + " awards fra config!");
    }

    public void checkAwards(Player player) {
        UUID uuid = player.getUniqueId();
        int playtime = database.getPlaytime(uuid);
        Map<Integer, AwardProgress> playerAwards = database.getAwardsData(uuid);
       
        boolean dataUpdated = false;
       
        for (int awardId = 1; awardId <= awards.size(); awardId++) {
            Award award = awards.get(awardId);
            if (award == null) continue;
           
            AwardProgress progress = playerAwards.getOrDefault(awardId, new AwardProgress());
           
            if (!progress.isUnlocked() && playtime >= award.getTime()) {
                if (isPreviousAwardUnlocked(awardId, playerAwards)) {
                    giveAward(player, award);
                    progress.setUnlocked(true);
                    progress.setTimeLeft(0);
                    playerAwards.put(awardId, progress);
                    dataUpdated = true;
                } else {
                    progress.updateTimeLeft(playtime, award.getTime());
                    playerAwards.put(awardId, progress);
                    dataUpdated = true;
                }
            } else if (!progress.isUnlocked()) {
                progress.updateTimeLeft(playtime, award.getTime());
                playerAwards.put(awardId, progress);
                dataUpdated = true;
            }
        }
       
        if (dataUpdated) {
            database.updateAwardsData(uuid, playerAwards);
        }
    }

    private boolean isPreviousAwardUnlocked(int currentAwardId, Map<Integer, AwardProgress> playerAwards) {
        if (currentAwardId == 1) {
            return true;
        }
       
        AwardProgress previousProgress = playerAwards.get(currentAwardId - 1);
        return previousProgress != null && previousProgress.isUnlocked();
    }

    private void giveAward(Player player, Award award) {
        String type = award.getType().toLowerCase();
        
        if (type.equals("money") || type.equals("both")) {
            if (economy != null && award.getMoney() > 0) {
                economy.depositPlayer(player, award.getMoney());
            }
        }
        
        if (type.equals("items") || type.equals("both")) {
            if (award.getItems() != null && !award.getItems().isEmpty()) {
                giveItems(player, award.getItems());
            }
        }
       
        String formattedMessage = ChatColor.translateAlternateColorCodes('&',
            prefix + " &7" + award.getMessage());
        player.sendMessage(formattedMessage);
    }

    private void giveItems(Player player, List<String> items) {
        for (String itemString : items) {
            try {
                String[] parts = itemString.split(" ");
                Material material = Material.valueOf(parts[0].toUpperCase());
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
               
                ItemStack item = new ItemStack(material, amount);
               
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                } else {
                    player.getInventory().addItem(item);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Kunne ikke parse item: " + itemString);
            }
        }
    }

    public void addPlaytime(Player player) {
        UUID uuid = player.getUniqueId();
        database.addPlaytime(uuid, 1);
       
        if (database.hasNotifications(uuid)) {
            String message = ChatColor.translateAlternateColorCodes('&',
                prefix + " &7Der blev tilføjet ét minut til din spilletid");
            player.sendMessage(message);
        }
       
        checkAwards(player);
    }

    public Map<Integer, Award> getAwards() {
        return awards;
    }

    public Map<Integer, AwardProgress> getPlayerProgress(UUID uuid) {
        return database.getAwardsData(uuid);
    }

    public void resetPlayerAwards(UUID uuid) {
        database.resetPlayerData(uuid);
    }
}