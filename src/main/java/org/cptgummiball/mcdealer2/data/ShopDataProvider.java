package org.cptgummiball.mcdealer2.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

public class ShopDataProvider {

    private static final Map<String, Map<String, Object>> bestPriceItems = new HashMap<>();

    public static void main(String[] args) {
        generateOutputJson();
    }

    static void generateOutputJson() {
        try {
            File ShopFolder = new File("plugins/VillagerMarket/shops");

            List<Map<String, Object>> shopsList = new ArrayList<>();
            processFolder(ShopFolder, shopsList);

            // JSON-Structure with "shops" and "bestprice"
            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("shops", shopsList);
            jsonData.put("bestprice", new ArrayList<>(bestPriceItems.values()));

            // Save to data.json
            List<Map<String, Object>> finalList = new ArrayList<>();
            finalList.add(jsonData);

            writeToJsonFile(finalList, "plugins/mcdealer/web/data.json");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while generating data.json: " + e.getMessage());
        }
    }

    private static void processFolder(File folder, List<Map<String, Object>> result) {
        try {
            File hiddenShopsFile = new File(folder.getParent(), "hiddenshops.yml");  // Hauptordner des Plugins
            Set<String> hiddenShops = loadHiddenShops(hiddenShopsFile); // Set von Dateinamen ohne Erweiterung

            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    String fileNameWithoutExtension = getFileNameWithoutExtension(file.getName());

                    if (!hiddenShops.contains(fileNameWithoutExtension)) {
                        if (file.isFile()) {
                            processYaml(file, result);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while processing villagermarket shop folder: " + e.getMessage());
        }
    }


    // start processing
    private static void processYaml(File file, List<Map<String, Object>> result) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(FileReader(file));

            // Extract shop UUID from the filename (remove .yaml extension)
            String shopId = file.getName().replace(".yaml", "");

            // Process the YAML data
            cleanAndProcessData(yamlData, shopId);

            // Add the processed data to the result
            result.add(yamlData);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while processing yaml file" + e.getMessage());
        }
    }


    // process the YAML data
    private static void cleanAndProcessData(Map<String, Object> data, String shopId) {
        try {
            // Shop-Daten abrufen
            String shopType = (String) data.getOrDefault("type", "UNKNOWN");
            String shopName = cleanString(getNestedString(data, "entity", "name"));
            String shopProfession = getNestedString(data, "entity", "profession");
            String world = getNestedString(data, "location", "world");
            double x = getNestedDouble(data, "location", "x");
            double y = getNestedDouble(data, "location", "y");
            double z = getNestedDouble(data, "location", "z");
            String owner = shopType.equals("ADMIN") ? "ADMIN" : (String) data.getOrDefault("ownerName", "UNBEKANNT");
            String ownerUUID = shopType.equals("ADMIN") ? "ADMIN" : (String) data.getOrDefault("ownerUUID", "UNBEKANNT");
            boolean requirePermission = (boolean) data.getOrDefault("require_permission", false);

            // Fehlende oder leere Werte durch Standardwerte ersetzen
            shopName = shopName.isEmpty() ? "Unbenannter Shop" : shopName;
            shopProfession = shopProfession.isEmpty() ? "NONE" : shopProfession;
            world = world.isEmpty() ? "UNKNOWN" : world;

            // Shop-Informationen in das JSON-Datenobjekt speichern
            data.put("shopId", shopId);
            data.put("shopType", shopType);
            data.put("shopName", shopName);
            data.put("shopProfession", shopProfession);
            data.put("shopLocation", world + ", " + x + ", " + y + ", " + z);
            data.put("owner", owner);
            data.put("ownerUUID", ownerUUID);
            data.put("requirePermission", requirePermission);

            // Artikel verarbeiten
            List<Map<String, Object>> processedItems = processItemsAndStorage(data, shopId);
            data.put("Items", processedItems);

        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while processing data: " + e.getMessage());
        }
    }


    // Helper Functions

    // read file
    private static FileReader FileReader(File file) throws IOException {
        return new FileReader(file);
    }

    // check and update best price
    private static void checkAndUpdateBestPrice(Map<String, Object> item) {
        String itemType = (String) item.get("type");
        Map<String, Object> meta = (Map<String, Object>) item.get("meta");
        double pricePerItem = (double) item.get("pricePerItem");
        String itemUUID = (String) item.get("itemuuid");

        // Unique Key for each item
        String itemKey = itemType + meta.toString();

        // If the item is not in the map or the price is lower
        if (!bestPriceItems.containsKey(itemKey) || pricePerItem < (double) bestPriceItems.get(itemKey).get("pricePerItem")) {
            Map<String, Object> bestItem = new HashMap<>();
            bestItem.put("itemuuid", itemUUID);
            bestItem.put("type", itemType);
            bestItem.put("meta", meta);
            bestItem.put("pricePerItem", pricePerItem);

            bestPriceItems.put(itemKey, bestItem);
        }
    }


    // get nested String
    private static String getNestedString(Map<String, Object> data, String key1, String key2) {
        Map<String, Object> nested = (Map<String, Object>) data.get(key1);
        return nested != null && nested.get(key2) != null ? nested.get(key2).toString() : "";
    }

    // get nested Double
    private static double getNestedDouble(Map<String, Object> data, String key1, String key2) {
        Map<String, Object> nested = (Map<String, Object>) data.get(key1);
        if (nested == null || nested.get(key2) == null) return 0.0;
        try {
            return ((Number) nested.get(key2)).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }


    // get Double
    private static double getDouble(Map<String, Object> data, String... keys) {
        Map<String, Object> nested = data;
        for (int i = 0; i < keys.length - 1; i++) {
            nested = (Map<String, Object>) nested.get(keys[i]);
            if (nested == null) return 0.0;
        }
        return nested.get(keys[keys.length - 1]) instanceof Number
                ? ((Number) nested.get(keys[keys.length - 1])).doubleValue()
                : 0.0;
    }

    // get Int
    private static int getInt(Map<String, Object> data, String key) {
        return data.get(key) instanceof Number ? ((Number) data.get(key)).intValue() : 0;
    }

    // get Stock
    private static int getStockAmount(List<Map<String, Object>> storage, Map<String, Object> item) {
        if (storage == null) return 0;
        for (Map<String, Object> storedItem : storage) {
            if (itemsMatch(storedItem, item)) {
                return getInt(storedItem, "amount");
            }
        }
        return 0;
    }

    private static boolean itemsMatch(Map<String, Object> item1, Map<String, Object> item2) {
        return item1.get("type").equals(item2.get("type")) &&
                Objects.equals(item1.get("meta"), item2.get("meta"));
    }

    // clean String
    private static String cleanString(String value) {
        try {
            return value.replaceAll("§.", "");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while cleaning string: " + e.getMessage());
            return value;
        }
    }

    // generate UUID
    private static String createUUID(String item, String shopid) {
        try {
            Bukkit.getLogger().severe("Creating UUID for item " + item + " in Shop " + shopid);
            return UUID.randomUUID().toString();
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while creating UUID: " + e.getMessage());
            return null;
        }
    }

    // load hidden shops
    private static Set<String> loadHiddenShops(File hiddenShopsFile) throws IOException {
        Set<String> hiddenShops = new HashSet<>();
        if (hiddenShopsFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(hiddenShopsFile);
            List<String> shopsList = config.getStringList("hiddenShops");
            hiddenShops.addAll(shopsList);
        }
        return hiddenShops;
    }

    private static List<Map<String, Object>> processItemsAndStorage(Map<String, Object> yamlData, String shopId) {
        List<Map<String, Object>> processedItems = new ArrayList<>();
        List<Map<String, Object>> storage = extractStorage(yamlData);

        Map<String, Map<String, Object>> storageMap = new HashMap<>();
        for (Map<String, Object> storedItem : storage) {
            String key = generateItemKey(storedItem);
            storageMap.put(key, storedItem);
        }

        Map<String, Object> itemsForSale = (Map<String, Object>) yamlData.get("items_for_sale");
        if (itemsForSale != null) {
            for (Map.Entry<String, Object> entry : itemsForSale.entrySet()) {
                Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                Map<String, Object> itemDetails = extractItemDetails(itemData, shopId);
                String itemKey = generateItemKey(itemDetails);

                int stock = storageMap.containsKey(itemKey) ? (int) storageMap.get(itemKey).get("amount") : 0;
                itemDetails.put("stock", stock);

                checkAndUpdateBestPrice(itemDetails);
                processedItems.add(itemDetails);
            }
        }
        return processedItems;
    }

    private static List<Map<String, Object>> extractStorage(Map<String, Object> yamlData) {
        List<Map<String, Object>> storageList = new ArrayList<>();
        List<Map<String, Object>> storage = (List<Map<String, Object>>) yamlData.get("storage");

        if (storage != null) {
            for (Map<String, Object> item : storage) {
                storageList.add(extractItemMeta(item));
            }
        }
        return storageList;
    }

    private static Map<String, Object> extractItemDetails(Map<String, Object> itemData, String shopId) {
        Map<String, Object> extractedItem = new HashMap<>();
        Map<String, Object> item = (Map<String, Object>) itemData.get("item");

        extractedItem.put("itemuuid", createUUID(item.getOrDefault("type", "UNKNOWN").toString(), shopId));
        extractedItem.put("type", item.getOrDefault("type", "UNKNOWN"));
        extractedItem.put("mode", itemData.getOrDefault("mode", "UNKNOWN"));
        extractedItem.put("amount", itemData.getOrDefault("amount", 1));

        double price = itemData.get("price") instanceof Number ? ((Number) itemData.get("price")).doubleValue() : 0.0;
        extractedItem.put("price", price);
        extractedItem.put("pricePerItem", price / Math.max(1, (int) extractedItem.get("amount")));

        extractedItem.putAll(extractItemMeta(item));
        return extractedItem;
    }


    private static Map<String, Object> extractItemMeta(Map<String, Object> item) {
        Map<String, Object> metaData = new HashMap<>();

        if (item.containsKey("meta")) {
            Map<String, Object> meta = (Map<String, Object>) item.get("meta");

            metaData.put("display-name", meta.getOrDefault("display-name", ""));
            metaData.put("enchants", meta.getOrDefault("enchants", Collections.emptyMap()));
            metaData.put("trim", meta.getOrDefault("trim", null));
        } else {
            metaData.put("display-name", "");
            metaData.put("enchants", Collections.emptyMap());
            metaData.put("trim", null);
        }
        return metaData;
    }


    // generate item key
    private static String generateItemKey(Map<String, Object> item) {
        return item.get("type") + "|" + item.getOrDefault("display-name", "") + "|" + item.getOrDefault("enchants", "") + "|" + item.getOrDefault("trim", "");
    }


    // get file name without extension
    private static String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
    }

    // final write to json
    private static void writeToJsonFile(List<Map<String, Object>> dataList, String outputPath) {
        try {
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(outputFile)) {
                Bukkit.getLogger().severe("Writing to JSON file: " + outputPath);
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(dataList);
                writer.write(jsonString);
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error while writing to JSON file: " + e.getMessage());
        }
    }
}