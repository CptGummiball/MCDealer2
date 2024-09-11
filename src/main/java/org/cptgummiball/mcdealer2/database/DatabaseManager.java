package org.cptgummiball.mcdealer2.database;

import org.cptgummiball.mcdealer2.MCDealer2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DatabaseManager {

    private final MCDealer2 plugin;
    private File databaseFile;

    public DatabaseManager(MCDealer2 plugin) {
        this.plugin = plugin;
        createDatabase();
    }

    private void createDatabase() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        databaseFile = new File(dataFolder, "shops.db");

        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create the database file!");
            }
        }
    }

    public void writeShopData(String shopId, String shopData) {
        // Flat file database logic to write data
        try (RandomAccessFile raf = new RandomAccessFile(databaseFile, "rw")) {
            // Write shop data to the file
            raf.seek(raf.length());  // Move to end of file
            raf.writeBytes(shopId + ": " + shopData + "\n");
        } catch (IOException e) {
            plugin.getLogger().severe("Error writing to the database: " + e.getMessage());
        }
    }

    public void closeDatabase() {
        // Clean up resources if needed
    }
}
