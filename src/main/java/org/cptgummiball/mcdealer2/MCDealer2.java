package org.cptgummiball.mcdealer2;

import org.bukkit.Bukkit;
import org.cptgummiball.mcdealer2.commands.MCDealerCommand;
import org.cptgummiball.mcdealer2.listeners.ShopClickListener;
import org.cptgummiball.mcdealer2.utils.ConfigUpdater;
import org.bukkit.plugin.java.JavaPlugin;
import org.cptgummiball.mcdealer2.utils.Translator;
import org.cptgummiball.mcdealer2.web.WebServer;
import org.cptgummiball.mcdealer2.data.SchedulerTask;

public class MCDealer2 extends JavaPlugin {

    String language = getConfig().getString("language","en-US");
    public Translator translator = new Translator(this, language);
    private WebServer webServer;

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {

        // Update and-or Load the Config
        saveDefaultConfig();
        ConfigUpdater configUpdater = new ConfigUpdater(this);
        configUpdater.updateConfig();
        // Copy Resources
        saveResource("hiddenshops.yml", false);
        saveResource("web/data.json", false);
        // Initialize and start processes
        getLogger().info(translator.translate("plugin.startprocess"));
        // Start the web server
        webServer = new WebServer(this);
        webServer.start();
        // Register commands
        this.getCommand("mcdealer").setExecutor(new MCDealerCommand(this));
        // Register Listeners
        Bukkit.getPluginManager().registerEvents(new ShopClickListener(this), this);

        // Scheduler starten
        String intervalConfig = getConfig().getString("data.interval", "10m");
        int intervalSeconds = SchedulerTask.parseInterval(intervalConfig);
        SchedulerTask schedulerTask = new SchedulerTask(this, intervalSeconds);
        schedulerTask.start();

        // Starting Message
        getLogger().info(translator.translate("plugin.enablemessage"));
    }

    @Override
    public void onDisable() {
        // Stop the web server
        if (webServer != null) {
            webServer.stop();
        }
        // Closing Message
        getLogger().info(translator.translate("plugin.disablemessage"));
    }

}
