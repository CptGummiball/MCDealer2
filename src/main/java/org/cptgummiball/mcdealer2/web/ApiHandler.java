package org.cptgummiball.mcdealer2.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ApiHandler extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String path = req.getPathInfo();
        if ("/shopdata".equals(path)) {
            String filePath = getServletContext().getRealPath("/web/data.json");
            File dataFile = new File(filePath);

            if (dataFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                StringBuilder jsonContent = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
                reader.close();

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().println(jsonContent.toString());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().println("{\"message\": \"Data file not found!\"}");
            }
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
        resp.getWriter().println("{\"message\": \"Post request received!\"}");
    }
}
