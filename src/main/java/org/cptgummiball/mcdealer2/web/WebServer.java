package org.cptgummiball.mcdealer2.web;

import org.bukkit.plugin.java.JavaPlugin;
import org.cptgummiball.mcdealer2.MCDealer2;
import org.cptgummiball.mcdealer2.utils.Translator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebServer {
    private final JavaPlugin plugin;
    private final Translator translator;
    private Server server;

    public WebServer(MCDealer2 plugin) {
        this.plugin = plugin;
        this.translator = plugin.translator;
    }

    public void start() {
        int port = plugin.getConfig().getInt("web.port");
        boolean useInternalWebsite = plugin.getConfig().getBoolean("web.use-internal-website");

        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // API always active
        context.addServlet(new ServletHolder(new ApiHandler()), "/api/*");

        // Add internal website if configured
        if (useInternalWebsite) {
            context.addServlet(new ServletHolder(new WebHandler(plugin)), "/*");
        }

        server.setHandler(context);

        try {
            server.start();
            plugin.getLogger().info(translator.translate("webserver.startport") + port);
        } catch (Exception e) {
            plugin.getLogger().severe(translator.translate("webserver.startfail") + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            try {
                server.stop();
                server.join();
                plugin.getLogger().info(translator.translate("webserver.stopped"));
            } catch (Exception e) {
                plugin.getLogger().severe(translator.translate("webserver.stopfail") + e.getMessage());
            }
        }
    }
}
