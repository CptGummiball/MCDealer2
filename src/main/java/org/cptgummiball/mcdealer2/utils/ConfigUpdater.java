package org.cptgummiball.mcdealer2.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.cptgummiball.mcdealer2.MCDealer2;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class ConfigUpdater {

    private final JavaPlugin plugin;
    private final String currentVersion = "2"; // Set your plugin version here
    private final Translator translator;

    public ConfigUpdater(MCDealer2 plugin) {
        this.plugin = plugin;
        this.translator = plugin.translator;
    }

    public void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Get the version from the current config file, defaulting to "0" if not found
        String configVersion = config.getString("config-version", "0");

        // Check if the current config version is outdated
        if (configVersion.equals("0") || configVersion.compareTo(currentVersion) < 0) {
            // Load default config from the resources folder
            YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml")))
            );

            // Copy old configuration values to the new config
            for (String key : config.getKeys(true)) {
                if (newConfig.contains(key)) {
                    newConfig.set(key, config.get(key));
                }
            }

            // Set the new version in the config
            newConfig.set("config-version", currentVersion);

            // Save the updated config
            try {
                newConfig.save(configFile);
                plugin.getLogger().info(translator.translate("configupdater.update") + currentVersion);
            } catch (IOException e) {
                plugin.getLogger().severe(translator.translate("configupdater.updatefail") + e.getMessage());
            }
        }
    }
}
