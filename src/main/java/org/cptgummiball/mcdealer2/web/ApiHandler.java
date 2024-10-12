package org.cptgummiball.mcdealer2.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.cptgummiball.mcdealer2.data.ShopDataProvider;

public class ApiHandler extends HttpServlet {

    private final ShopDataProvider shopDataProvider;

    public ApiHandler() {
        this.shopDataProvider = new ShopDataProvider(); // Initialize ShopDataProvider
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String path = req.getPathInfo();
        if ("/shopdata".equals(path)) {
            // Get Shop Data from ShopDataProvider
            String shopData = shopDataProvider.generateShopData();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println(shopData);
        } else {
            // Default-GET-Answer
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("{\"message\": \"API is working!\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        // Handle post request data here
        resp.getWriter().println("{\"message\": \"Post request received!\"}");
    }
}
