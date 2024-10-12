package org.cptgummiball.mcdealer2;

import org.cptgummiball.mcdealer2.utils.ConfigUpdater;
import org.bukkit.plugin.java.JavaPlugin;
import org.cptgummiball.mcdealer2.utils.Translator;
import org.cptgummiball.mcdealer2.web.WebServer;

public class MCDealer2 extends JavaPlugin {

    String language = getConfig().getString("language","en-US");
    public Translator translator = new Translator(this, language);
    private WebServer webServer;

    @Override
    public void onEnable() {

        // Update or Load the Config
        saveDefaultConfig();
        ConfigUpdater configUpdater = new ConfigUpdater(this);
        configUpdater.updateConfig();
        // Copy Resources
        saveResource("hiddenshops.yml", false);
        // Initialize and start processes
        getLogger().info(translator.translate("plugin.startprocess"));
        // Start the web server
        webServer = new WebServer(this);
        webServer.start();
        // Register commands

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
