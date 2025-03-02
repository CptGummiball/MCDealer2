package org.cptgummiball.mcdealer2.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.cptgummiball.mcdealer2.MCDealer2;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

import static org.cptgummiball.mcdealer2.data.helper.*;

public class ShopDataProvider {

    static final JavaPlugin plugin = MCDealer2.getPlugin(MCDealer2.class);
    static final boolean debugMode = plugin.getConfig().getBoolean("debug-mode", false);


    public static void process() {
        File ShopFolder = new File("plugins/VillagerMarket/Shops");
        List<Map<String, Object>> shopsList = new ArrayList<>();
        Map<String, Object> yamlMap = processFolder(ShopFolder);
        shopsList.add(yamlMap);
        bestpricecheck(shopsList);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(shopsList);
        writeToGsonFile(json);
    }

    // process the folder
    private static Map<String, Object> processFolder(File folder) {
        try {
            Set<String> hiddenShops;
            File hiddenShopsFile = new File(folder.getParent(), "hiddenshops.yml");
            hiddenShops = loadHiddenShops(hiddenShopsFile);

            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    String fileNameWithoutExtension = getFileNameWithoutExtension(file.getName());

                    assert hiddenShops != null;
                    if (!hiddenShops.contains(fileNameWithoutExtension)) {
                        if (file.isFile()) {
                            return processYamlNew(file);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Error while processing villagermarket shop folder: " + e.getMessage());
            }
        }
        return null;
    }

    // process the yaml
    private static Map<String, Object> processYamlNew(File file) {
        try {
            Map<String, Object> yamlMap = new HashMap<>(Objects.requireNonNull(readShopData(file)));
            yamlMap.put("items", readItems(file));
            return yamlMap;
        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Error while processing villagermarket shop file: " + e.getMessage());
            }
            return null;
        }
    }

    // Read Shop Data
    private static Map<String, Object> readShopData(File file) {
        Map<String, Object> data = new HashMap<>();
        try {
            String shopId = file.getName().replace(".yml", "");
            String shopType = (String) data.getOrDefault("type", "UNKNOWN");
            String shopName = cleanString(getNestedString(data, "entity", "name"));
            String shopProfession = getNestedString(data, "entity", "profession");
            Map<String, Object> entityData = (Map<String, Object>) data.get("entity");
            String world = getNestedString(entityData, "location", "world");
            double x = Math.round(getNestedDouble(entityData, "x"));
            double y = Math.round(getNestedDouble(entityData, "y"));
            double z = Math.round(getNestedDouble(entityData, "z"));
            String owner = shopType.equals("ADMIN") ? "ADMIN" : (String) data.getOrDefault("ownerName", "UNKNOWN");
            String ownerUUID = shopType.equals("ADMIN") ? "ADMIN" : (String) data.getOrDefault("ownerUUID", "UNKNOWN");
            boolean requirePermission = (boolean) data.getOrDefault("require_permission", false);

            // Missing data handle
            shopName = shopName.isEmpty() ? "Unnamed Shop" : shopName;
            shopProfession = shopProfession.isEmpty() ? "NONE" : shopProfession;
            world = world.isEmpty() ? "UNKNOWN" : world;
            owner = owner.isEmpty() ? "UNKNOWN" : owner;
            ownerUUID = ownerUUID.isEmpty() ? "UNKNOWN" : ownerUUID;

            // Add cleaned Shop Data
            Map<String, Object> cleanedShopData = new HashMap<>();
            cleanedShopData.put("shopId", shopId);
            cleanedShopData.put("shopType", shopType);
            cleanedShopData.put("shopName", shopName);
            cleanedShopData.put("shopProfession", shopProfession);
            cleanedShopData.put("shopLocation", world + ", " + x + ", " + y + ", " + z);
            cleanedShopData.put("owner", owner);
            cleanedShopData.put("ownerUUID", ownerUUID);
            cleanedShopData.put("requirePermission", requirePermission);

            return cleanedShopData;
        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Error while processing villagermarket shop data: " + e.getMessage());
            }
        }
        return null;
    }

    // Erstellen Sie die Item-Sektion
    private static List<Map<String, Object>> readItems(File file) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(new FileReader(file));
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items_for_sale");
        List<Map<String, Object>> storage = (List<Map<String, Object>>) data.get("storage");
        return (List<Map<String, Object>>) processItems(items, storage);
    }

    private static Map<String, List<Map<String, Object>>> processItems(List<Map<String, Object>> items, List<Map<String, Object>> storage) {
        Map<String, List<Map<String, Object>>> processedItems = new HashMap<>();
        processedItems.put("SELL", new ArrayList<>());
        processedItems.put("BUY", new ArrayList<>());
        try {
            for (Map<String, Object> item : items) {
                String uuid = UUID.randomUUID().toString();
                String type = getNestedString(item, "item", "type");
                boolean bestPrice = false;
                int storageAmount = getStorageAmount(type, item, storage);
                int amount = (int) item.get("amount");
                double price = (double) item.get("price");
                String mode = (String) item.get("mode");
                Map<String, Object> metadata = getMetadata(item);

                    Map<String, Object> processedItem = new HashMap<>();
                    processedItem.put("uuid", uuid);
                    processedItem.put("type", type);
                    processedItem.put("bestPrice", bestPrice);
                    processedItem.put("storageAmount", storageAmount);
                    processedItem.put("amount", amount);
                    processedItem.put("price", price);
                    processedItem.put("priceperunit", price / amount);
                    processedItem.put("mode", mode);
                    processedItem.put("metadata", metadata);

                if (Objects.equals(mode, "SELL")) {
                    processedItem.put("storageAmount", storageAmount);
                    processedItems.get("SELL").add(processedItem);
                } else if (Objects.equals(mode, "BUY")) {
                    processedItems.get("BUY").add(processedItem);
                }
            }
            return processedItems;

        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Error while processing items: " + e.getMessage());
            }
            return new HashMap<>() {
                {
                    put("SELL", new ArrayList<>());
                    put("BUY", new ArrayList<>());
                }
            };
        }
    }

    private static int getStorageAmount(String type, Map<String, Object> item, List<Map<String, Object>> storage) {
        try {
            for (Map<String, Object> storageItem : storage) {
                if (storageItem.get("type").equals(type) && Objects.equals(getNestedMap(storageItem, "meta"), getTripleNestedMap(item, "item", "meta", ""))) {
                    return (int) storageItem.get("amount");
                }
            }
            return 0;
        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Error while getting storage amount: " + e.getMessage());
            }
            return 0;
        }
    }

    private static Map<String, Object> getMetadata(Map<String, Object> item) {
        Map<String, Object> metadata = new HashMap<>();
        if (getNestedMap(item, "item") != null && getNestedMap(Objects.requireNonNull(getNestedMap(item, "item")), "meta") != null) {
            metadata.put("enchants", getTripleNestedList(item, "item", "meta", "enchants") != null ? "enchants" : new ArrayList<>());
            metadata.put("trims", getTripleNestedMap(item, "item", "meta", "trims") != null ? "trims" : new HashMap<>());
            metadata.put("display-name", extractDisplayName(getTripleNestedString(item, "item", "meta", "display-name") != null ? "display-name" : ""));
            metadata.put("custommodeldata", getTripleNestedInteger(item, "item", "meta", "custommodeldata") != null ? "custommodel" : 0);
            metadata.put("lore", getTripleNestedList(item, "item", "meta", "lore") != null ? "lore" : new ArrayList<>());
        }else{
            metadata.put("enchants", new ArrayList<>());
            metadata.put("trims", new HashMap<>());
            metadata.put("display-name", "");
            metadata.put("custommodel", 0);
            metadata.put("lore", new ArrayList<>());
        }
        if (debugMode) {
            plugin.getLogger().info("Gernerated Metadata:");
            plugin.getLogger().info("Enchants: " + metadata.get("enchants"));
            plugin.getLogger().info("Trims: " + metadata.get("trims"));
            plugin.getLogger().info("Display Name: " + metadata.get("display-name"));
            plugin.getLogger().info("Custom Model Data: " + metadata.get("custommodeldata"));
            plugin.getLogger().info("Lore: " + metadata.get("lore"));
        }
        return metadata;
    }

    // write to JSON file
    private static void writeToGsonFile(String dataList) {
        try {
            File outputFile = new File("plugins/MCDealer2/web/data.json");
            outputFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(outputFile)) {
                if (debugMode) {
                    plugin.getLogger().info("Writing to JSON file: " + outputFile.getAbsolutePath());
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonString = gson.toJson(dataList);
                writer.write(jsonString);
            }
        } catch (IOException e) {
            if (debugMode) {
                plugin.getLogger().info("Error while writing to JSON file: " + e.getMessage());
            }
        }
    }
}