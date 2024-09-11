package org.cptgummiball.mcdealer2.tasks;

import org.cptgummiball.mcdealer2.MCDealer2;
import org.cptgummiball.mcdealer2.utils.YamlShopLoader;
import org.cptgummiball.mcdealer2.model.Shop;

import java.util.List;

public class ShopSyncTask implements Runnable {

    private final MCDealer2 plugin;

    public ShopSyncTask(MCDealer2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<Shop> shops = YamlShopLoader.loadShops(plugin);

        for (Shop shop : shops) {
            // Convert shop data to string (or a structured format)
            String shopData = shop.toString();  // Simplified for now
            plugin.getDatabaseManager().writeShopData(shop.getUuid().toString(), shopData);
        }
    }
}
