package org.cptgummiball.mcdealer2.config;

import org.cptgummiball.mcdealer2.MCDealer2;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MCDealer2 plugin;
    private FileConfiguration config;

    public ConfigManager(MCDealer2 plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public long getSyncInterval() {
        return config.getLong("general.sync-interval", 600);
    }
}
