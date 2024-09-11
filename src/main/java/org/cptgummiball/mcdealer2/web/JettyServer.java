package org.cptgummiball.mcdealer2.web;

import org.cptgummiball.mcdealer2.MCDealer2;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.File;

public class JettyServer {

    private final MCDealer2 plugin;
    private Server server;

    public JettyServer(MCDealer2 plugin) {
        this.plugin = plugin;
    }

    public void startServer() {
        int port = plugin.getConfig().getInt("webserver-port", 8090);
        server = new Server(port);

        // Setting up the context handler to serve the web files
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Setting the base directory for serving static files
        File webDir = new File(plugin.getDataFolder(), "web");
        if (!webDir.exists()) {
            webDir.mkdirs();
            plugin.getLogger().info("Web directory created.");
        }

        context.addServlet(new ServletHolder(new RequestHandler(webDir)), "/*");
        server.setHandler(context);

        try {
            server.start();
            plugin.getLogger().info("Web server started on port " + port);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start web server: " + e.getMessage());
        }
    }

    public void stopServer() {
        if (server != null && server.isRunning()) {
            try {
                server.stop();
                plugin.getLogger().info("Web server stopped.");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to stop web server: " + e.getMessage());
            }
        }
    }
}
