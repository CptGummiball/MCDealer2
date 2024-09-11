package org.cptgummiball.mcdealer2;

import org.cptgummiball.mcdealer2.config.ConfigManager;
import org.cptgummiball.mcdealer2.database.DatabaseManager;
import org.cptgummiball.mcdealer2.tasks.ShopSyncTask;
import org.bukkit.plugin.java.JavaPlugin;

public class MCDealer2 extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // Initialize config manager
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        // Initialize database manager
        this.databaseManager = new DatabaseManager(this);

        // Schedule the shop sync task
        long syncInterval = configManager.getSyncInterval();
        getServer().getScheduler().runTaskTimerAsynchronously(this, new ShopSyncTask(this), 0L, syncInterval);

        getLogger().info("MCDealer2 has been enabled.");
    }

    @Override
    public void onDisable() {
        databaseManager.closeDatabase();
        getLogger().info("MCDealer2 has been disabled.");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
