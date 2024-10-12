package org.cptgummiball.mcdealer2.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebHandler extends HttpServlet {
    private final JavaPlugin plugin;

    public WebHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String webPath = plugin.getDataFolder() + "/web"; // Your web folder in the plugin
        Path file = Paths.get(webPath, req.getPathInfo());

        if (Files.exists(file)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            Files.copy(file, resp.getOutputStream());
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println("404 - Not Found");
        }
    }
}
