package org.cptgummiball.mcdealer2.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.cptgummiball.mcdealer2.MCDealer2;
import org.cptgummiball.mcdealer2.commands.MCDealerCommand;
import org.cptgummiball.mcdealer2.utils.Translator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class ShopClickListener implements Listener {

    private final MCDealer2 plugin;
    private final Translator translator;

    public ShopClickListener(MCDealer2 plugin) {
        this.plugin = plugin;
        this.translator = plugin.translator;

        // Load hidden shops on startup
        loadHiddenShops();
    }

    @EventHandler
    public void onPlayerClickEntity(PlayerInteractEntityEvent event) {
        // Only handle main hand clicks
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // Check if player is trying to hide a shop
        if (MCDealerCommand.getHideshopPlayerList().contains(player.getName())) {
            UUID entityId = entity.getUniqueId();
            // Hide the shop by adding the entity UUID to the YAML file
            addHiddenShop(entityId);
            MCDealerCommand.removeHideshopPlayer(player.getName());
            player.sendMessage(translator.translate("shopcommands.added")); // Feedback message
        }

        // Check if player is trying to show a shop
        if (MCDealerCommand.getShowshopPlayerList().contains(player.getName())) {
            UUID entityId = entity.getUniqueId();
            // Show the shop by removing the entity UUID from the YAML file
            removeHiddenShop(entityId);
            MCDealerCommand.removeShowshopPlayer(player.getName());
            player.sendMessage(translator.translate("shopcommands.removed")); // Feedback message
        }
    }

    private void addHiddenShop(UUID entityId) {
        File file = new File(plugin.getDataFolder(), "hiddenshops.yml");
        List<UUID> hiddenShops = loadHiddenShopsFromFile(file);

        if (!hiddenShops.contains(entityId)) {
            hiddenShops.add(entityId);
            saveHiddenShopsToFile(hiddenShops, file);
        }
    }

    private void removeHiddenShop(UUID entityId) {
        File file = new File(plugin.getDataFolder(), "hiddenshops.yml");
        List<UUID> hiddenShops = loadHiddenShopsFromFile(file);

        if (hiddenShops.contains(entityId)) {
            hiddenShops.remove(entityId);
            saveHiddenShopsToFile(hiddenShops, file);
        }
    }

    private List<UUID> loadHiddenShopsFromFile(File file) {
        List<UUID> hiddenShops = new ArrayList<>();
        if (file.exists()) {
            // Load the existing hidden shops from the YAML file
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            try {
                List<String> hiddenShopStrings = config.getStringList("hiddenshops");
                for (String uuidString : hiddenShopStrings) {
                    hiddenShops.add(UUID.fromString(uuidString));
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe(translator.translate("shopcommands.loadhiddenshopsfail") + e.getMessage());
            }
        }
        return hiddenShops;
    }

    private void saveHiddenShopsToFile(List<UUID> hiddenShops, File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("hiddenshops", new ArrayList<>(hiddenShops.stream().map(UUID::toString).toList())); // Save UUIDs as Strings
        try {
            config.save(file); // Save to the hiddenshops.yml file
        } catch (IOException e) {
            Bukkit.getLogger().severe(translator.translate("shopcommands.savehiddenshopsfail") + e.getMessage());
        }
    }

    // Load hidden shops when starting the plugin
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadHiddenShops() {
        File file = new File(plugin.getDataFolder(), "hiddenshops.yml");
        if (!file.exists()) {
            try {
                file.createNewFile(); // Create file if it doesn't exist
            } catch (IOException e) {
                Bukkit.getLogger().severe(translator.translate("shopcommands.createhiddenshopsfail") + e.getMessage());
            }
        }
    }
}

