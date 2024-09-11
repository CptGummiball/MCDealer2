package org.cptgummiball.mcdealer2;

import org.cptgummiball.mcdealer2.config.ConfigManager;
import org.cptgummiball.mcdealer2.database.DatabaseManager;
import org.cptgummiball.mcdealer2.tasks.ShopSyncTask;
import org.cptgummiball.mcdealer2.web.JettyServer;
import org.cptgummiball.mcdealer2.commands.ReloadCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class MCDealer2 extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private JettyServer jettyServer;
    private int shopSyncTaskId = -1;

    @Override
    public void onEnable() {
        // Initialize and start processes
        startProcesses();

        // Register commands
        getCommand("mcdealer2reload").setExecutor(new ReloadCommand(this));

        getLogger().info("MCDealer2 has been enabled.");
    }

    @Override
    public void onDisable() {
        // Stop all processes when the plugin is disabled
        stopProcesses();
        getLogger().info("MCDealer2 has been disabled.");
    }

    /**
     * Starts all processes including the web server and scheduled tasks.
     */
    public void startProcesses() {
        // Initialize config manager
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        // Initialize database manager
        this.databaseManager = new DatabaseManager(this);

        // Initialize and start the web server
        this.jettyServer = new JettyServer(this);
        this.jettyServer.startServer();

        // Schedule the shop sync task
        long syncInterval = configManager.getSyncInterval();
        if (shopSyncTaskId != -1) {
            getServer().getScheduler().cancelTask(shopSyncTaskId);
        }
        shopSyncTaskId = getServer().getScheduler().runTaskTimerAsynchronously(this, new ShopSyncTask(this), 0L, syncInterval).getTaskId();
    }

    /**
     * Stops all running processes including the web server and scheduled tasks.
     */
    public void stopProcesses() {
        // Stop the web server
        if (jettyServer != null) {
            jettyServer.stopServer();
        }

        // Stop the scheduled shop sync task
        if (shopSyncTaskId != -1) {
            getServer().getScheduler().cancelTask(shopSyncTaskId);
            shopSyncTaskId = -1;
        }

        // Close the database connection
        if (databaseManager != null) {
            databaseManager.closeDatabase();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
