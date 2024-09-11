package org.cptgummiball.mcdealer2.utils;

import org.cptgummiball.mcdealer2.MCDealer2;
import org.cptgummiball.mcdealer2.model.Shop;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class YamlShopLoader {

    public static List<Shop> loadShops(MCDealer2 plugin) {
        List<Shop> shops = new ArrayList<>();
        File shopFolder = new File(plugin.getDataFolder().getParentFile(), "OtherPlugin/Shops");

        if (shopFolder.exists() && shopFolder.isDirectory()) {
            for (File shopFile : shopFolder.listFiles()) {
                if (shopFile.getName().endsWith(".yml")) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(shopFile);
                    Shop shop = Shop.fromYaml(config);
                    shops.add(shop);
                }
            }
        }

        return shops;
    }
}
