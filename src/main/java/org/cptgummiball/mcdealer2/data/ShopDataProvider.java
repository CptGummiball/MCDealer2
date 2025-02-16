package org.cptgummiball.mcdealer2.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.cptgummiball.mcdealer2.MCDealer2;
import org.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

public class ShopDataProvider {

    private static final Map<String, Map<String, Object>> bestPriceItems = new HashMap<>();
    private static final JavaPlugin plugin = MCDealer2.getPlugin(MCDealer2.class);
    private static final boolean debugMode = plugin.getConfig().getBoolean("debug-mode", false);

    public static void main(String[] args) {
        generateOutputJson();
    }

    static void generateOutputJson() {
        try {
            File ShopFolder = new File("plugins/VillagerMarket/Shops");

            List<Map<String, Object>> shopsList = new ArrayList<>();
            processFolder(ShopFolder, shopsList);

            // JSON-Structure with "shops" and "bestprice"
            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("shops", shopsList);
            jsonData.put("bestprice", new ArrayList<>(bestPriceItems.values()));

            // Save to data.json
            List<Map<String, Object>> finalList = new ArrayList<>();
            finalList.add(jsonData);

            writeToJsonFile(finalList, "plugins/MCDealer2/web/data.json");
        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Error while generating output JSON: " + e.getMessage());
            }
        }
    }

    private static void processFolder(File folder, List<Map<String, Object>> result) {
        try {
            File hiddenShopsFile = new File(folder.getParent(), "hiddenshops.yml");
            Set<String> hiddenShops = loadHiddenShops(hiddenShopsFile);

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
            if (debugMode) {
                plugin.getLogger().info("Error while processing villagermarket shop folder: " + e.getMessage());
            }
        }
    }


    // start processing
    private static void processYaml(File file, List<Map<String, Object>> result) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(new FileReader(file));

            String shopId = file.getName().replace(".yml", "");

            Map<String, Object> cleanedData = new HashMap<>();
            cleanAndProcessData(yamlData, shopId);

            result.add(cleanedData);
        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Fehler beim Verarbeiten von " + file.getName() + ": " + e.getMessage());
            }
        }
    }


    // process the YAML data
    private static void cleanAndProcessData(Map<String, Object> data, String shopId) {
        try {
            // Get Shop Data
            String shopType = (String) data.getOrDefault("type", "UNKNOWN");
            String shopName = cleanString(getNestedString(data, "entity", "name"));
            String shopProfession = getNestedString(data, "entity", "profession");
            String world = getNestedString(data, "location", "world");
            double x = getNestedDouble(data, "location", "x");
            double y = getNestedDouble(data, "location", "y");
            double z = getNestedDouble(data, "location", "z");
            String owner = shopType.equals("ADMIN") ? "ADMIN" : (String) data.getOrDefault("ownerName", "UNKNOWN");
            String ownerUUID = shopType.equals("ADMIN") ? "ADMIN" : (String) data.getOrDefault("ownerUUID", "UNKNOWN");
            boolean requirePermission = (boolean) data.getOrDefault("require_permission", false);

            // Missing data handle
            shopName = shopName.isEmpty() ? "Unnamed Shop" : shopName;
            shopProfession = shopProfession.isEmpty() ? "NONE" : shopProfession;
            world = world.isEmpty() ? "UNKNOWN" : world;

            // Add Shop Data
            data.put("shopId", shopId);
            data.put("shopType", shopType);
            data.put("shopName", shopName);
            data.put("shopProfession", shopProfession);
            data.put("shopLocation", world + ", " + x + ", " + y + ", " + z);
            data.put("owner", owner);
            data.put("ownerUUID", ownerUUID);
            data.put("requirePermission", requirePermission);

            // Process Items and Storage
            List<Map<String, Object>> processedItems = processItemsAndStorage(data, shopId);
            data.put("Items", processedItems);

        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Error while processing " + shopId + ": " + e.getMessage());
            }
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

        // Eindeutiger Schlüssel basierend auf Item-Typ und Metadaten
        String itemKey = itemType + (meta != null ? meta.toString() : "");

        // Wenn das Item noch nicht existiert oder der Preis besser ist, aktualisieren
        if (!bestPriceItems.containsKey(itemKey) || pricePerItem < (double) bestPriceItems.get(itemKey).get("pricePerItem")) {
            Map<String, Object> bestItem = new HashMap<>();
            bestItem.put("itemuuid", itemUUID);
            bestItem.put("type", itemType);
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
            if (debugMode) {
                plugin.getLogger().info("Error while cleaning string: " + e.getMessage());
            }
            return value;
        }
    }

    // generate UUID
    private static String createUUID(String item, String shopid) {
        try {
            if (debugMode) {
                plugin.getLogger().info("Creating UUID for " + item + " in " + shopid);
            }
            return UUID.randomUUID().toString();
        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Error while creating UUID: " + e.getMessage());
            }
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

    // process Items and Storage
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

                // Stock check
                String itemKey = generateItemKey(itemDetails);
                int stock = storageMap.containsKey(itemKey) ? (int) storageMap.get(itemKey).get("amount") : 0;
                itemDetails.put("stock", stock);

                // Best Price check
                checkAndUpdateBestPrice(itemDetails);

                // Final Item Data
                Map<String, Object> finalItemData = new HashMap<>();
                finalItemData.put("itemuuid", UUID.randomUUID().toString());
                finalItemData.put("type", itemDetails.get("type"));
                finalItemData.put("mode", itemDetails.get("mode"));
                finalItemData.put("amount", itemDetails.get("amount"));
                finalItemData.put("price", itemDetails.get("price"));
                double price = itemDetails.containsKey("price") ? (double) itemDetails.get("price") : 0.0;
                int amount = itemDetails.containsKey("amount") ? (int) itemDetails.get("amount") : 1;
                double pricePerItem = (amount > 0) ? price / amount : price;
                itemDetails.put("pricePerItem", pricePerItem);

                finalItemData.put("stock", itemDetails.get("stock"));

                processedItems.add(finalItemData);
            }
        }
        return processedItems;
    }


    // extract storage
    private static List<Map<String, Object>> extractStorage(Map<String, Object> yamlData) {
        List<Map<String, Object>> storageList = new ArrayList<>();
        List<Map<String, Object>> storage = (List<Map<String, Object>>) yamlData.get("storage");

        if (storage != null) {
            for (Map<String, Object> itemData : storage) {
                ItemStack itemStack = ItemStack.deserialize(itemData);

                // Extracted Item
                Map<String, Object> extractedItem = new HashMap<>();
                extractedItem.put("type", itemStack.getType().toString());
                extractedItem.put("amount", itemStack.getAmount());

                extractedItem.putAll(extractItemMeta(itemStack));

                storageList.add(extractedItem);
            }
        }
        return storageList;
    }

    // extract item details
    private static Map<String, Object> extractItemDetails(Map<String, Object> itemData, String shopId) {
        Map<String, Object> extractedItem = new HashMap<>();
        Map<String, Object> item = (Map<String, Object>) itemData.get("item");

        ItemStack itemStack = ItemStack.deserialize(item);

        // Extracted Item
        extractedItem.put("itemuuid", createUUID(itemStack.getType().toString(), shopId));
        extractedItem.put("type", itemStack.getType().toString());
        extractedItem.put("mode", itemData.getOrDefault("mode", "UNKNOWN"));
        extractedItem.put("amount", itemStack.getAmount());

        // Get Price and Price Per Item
        double price = itemData.get("price") instanceof Number ? ((Number) itemData.get("price")).doubleValue() : 0.0;
        extractedItem.put("price", price);
        extractedItem.put("pricePerItem", price / Math.max(1, itemStack.getAmount()));


        extractedItem.putAll(extractItemMeta(itemStack));

        return extractedItem;
    }


    // extract item meta
    private static Map<String, Object> extractItemMeta(ItemStack itemStack) {
        Map<String, Object> metaData = new HashMap<>();

        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();

            // Extracted Meta
            metaData.put("lore", meta.hasLore() ? meta.getLore() : Collections.emptyList());
            metaData.put("display-name", meta.hasDisplayName() ? meta.getDisplayName() : "");
            metaData.put("enchants", meta.hasEnchants() ? meta.getEnchants() : Collections.emptyMap());

            if (meta instanceof ArmorMeta) {
                metaData.put("trim", ((ArmorMeta) meta).getTrim());
            } else {
                metaData.put("trim", null);
            }
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
                if (debugMode) {
                    plugin.getLogger().info("Writing to JSON file: " + outputFile.getAbsolutePath());
                }
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(dataList);
                writer.write(jsonString);
            }
        } catch (IOException e) {
            if (debugMode) {
                plugin.getLogger().info("Error while writing to JSON file: " + e.getMessage());
            }
        }
    }
}