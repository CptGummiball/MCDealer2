package org.cptgummiball.mcdealer2.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

public class ShopDataProvider {

    // Template
    public static void main(String[] args) {
        generateOutputJson();
    }

    // generate output
    private static void generateOutputJson() {
        try {
            File ShopFolder = new File("plugins/villagermarket/shops");

            List<Map<String, Object>> result = new ArrayList<>();

            processFolder(ShopFolder, result);

            writeToJsonFile(result, "plugins/mcdealer/web/data.json");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while generating data.json" + e.getMessage());
        }
    }

    // Verarbeite den Ordner
    private static void processFolder(File folder, List<Map<String, Object>> result) {
        try {
            // Lade die 'hiddenshops.yml'-Datei
            File hiddenShopsFile = new File(folder.getParent(), "hiddenshops.yml");  // Hauptordner des Plugins
            Set<String> hiddenShops = loadHiddenShops(hiddenShopsFile); // Set von Dateinamen ohne Erweiterung

            // Filtere alle .yml-Dateien im angegebenen Ordner
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    String fileNameWithoutExtension = getFileNameWithoutExtension(file.getName());

                    // Wenn der Dateiname in der hiddenshops.yml steht, überspringen
                    if (!hiddenShops.contains(fileNameWithoutExtension)) {
                        if (file.isFile()) {
                            processYaml(file, result); // Verarbeite die Datei
                        }
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while processing villagermarket shop folder: " + e.getMessage());
        }
    }

    // Lädt die 'hiddenshops.yml' und gibt ein Set der Dateinamen zurück
    private static Set<String> loadHiddenShops(File hiddenShopsFile) throws IOException {
        Set<String> hiddenShops = new HashSet<>();
        if (hiddenShopsFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(hiddenShopsFile);
            // Angenommen, die YAML-Datei hat eine Liste von Shop-Namen unter dem Schlüssel 'hiddenShops'
            List<String> shopsList = config.getStringList("hiddenShops");
            hiddenShops.addAll(shopsList);
        }
        return hiddenShops;
    }

    // Hilfsmethode, um den Dateinamen ohne Erweiterung zu extrahieren
    private static String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
    }


    // start processing
    private static void processYaml(File file, List<Map<String, Object>> result) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(FileReader(file));

            // Extract shop UUID from the filename (remove .yaml extension)
            String shopId = file.getName().replace(".yaml", "");

            // Bereinige Strings
            cleanAndProcessData(yamlData, shopId);

            // Füge die bereinigten Daten zur Ergebnisliste hinzu
            result.add(yamlData);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while processing yaml file" + e.getMessage());
        }
    }

    // read file
    private static FileReader FileReader(File file) throws IOException {
        return new FileReader(file);
    }

    // process the YAML data
    private static void cleanAndProcessData(Map<String, Object> data, String shopId) {
        try {
            // Shop-Daten extrahieren
            String shopType = (String) data.get("type");
            String shopName = cleanString(getNestedString(data, "entity", "name"));
            String shopProfession = getNestedString(data, "entity", "profession");
            String world = getNestedString(data, "location", "world");
            double x = getNestedDouble(data, "location", "x");
            double y = getNestedDouble(data, "location", "y");
            double z = getNestedDouble(data, "location", "z");
            String owner = shopType.equals("ADMIN") ? "ADMIN" : (String) data.get("ownerName");

            // Shop-Objekt aufbereiten
            data.put("shopId", shopId);
            data.put("shopType", shopType);
            data.put("shopName", shopName);
            data.put("shopProfession", shopProfession);
            data.put("shopLocation", world + ", " + x + ", " + y + ", " + z);
            data.put("owner", owner);

            // Items verarbeiten
            List<Map<String, Object>> processedItems = new ArrayList<>();
            Map<String, Object> itemsForSale = (Map<String, Object>) data.get("items_for_sale");
            List<Map<String, Object>> storage = (List<Map<String, Object>>) data.get("storage");

            if (itemsForSale != null) {
                for (Map.Entry<String, Object> entry : itemsForSale.entrySet()) {
                    Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                    Map<String, Object> item = (Map<String, Object>) itemData.get("item");

                    Map<String, Object> processedItem = new HashMap<>();
                    processedItem.put("type", item.get("type"));
                    processedItem.put("meta", item.getOrDefault("meta", new HashMap<>()));
                    processedItem.put("amount", itemData.get("amount"));

                    double price = getDouble(itemData, "price");
                    int amount = getInt(itemData, "amount");
                    double pricePerItem = amount > 0 ? price / amount : price;

                    processedItem.put("price", price);
                    processedItem.put("pricePerItem", pricePerItem);
                    processedItem.put("discount.amount", getDouble(itemData, "discount", "amount"));
                    processedItem.put("mode", itemData.get("mode"));

                    // Lagerbestand setzen, falls der Shop kein Admin-Shop ist
                    if (!shopType.equals("ADMIN")) {
                        processedItem.put("stock", getStockAmount(storage, item));
                    } else {
                        processedItem.put("stock", "∞");
                    }

                    processedItems.add(processedItem);
                }
            }

            data.put("Items", processedItems);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while processing data: " + e.getMessage());
        }
    }

    // Hilfsfunktionen zum sicheren Extrahieren von Daten
    private static String getNestedString(Map<String, Object> data, String key1, String key2) {
        Map<String, Object> nested = (Map<String, Object>) data.get(key1);
        return nested != null ? (String) nested.getOrDefault(key2, "") : "";
    }

    private static double getNestedDouble(Map<String, Object> data, String key1, String key2) {
        Map<String, Object> nested = (Map<String, Object>) data.get(key1);
        return nested != null ? ((Number) nested.getOrDefault(key2, 0.0)).doubleValue() : 0.0;
    }

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

    private static int getInt(Map<String, Object> data, String key) {
        return data.get(key) instanceof Number ? ((Number) data.get(key)).intValue() : 0;
    }

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

    // clean String und gib den bereinigten Wert zurück
    private static String cleanString(String value) {
        try {
            // Entferne Farbcodes (das "§" gefolgt von einem beliebigen Zeichen)
            return value.replaceAll("§.", ""); // Entfernt Farbcodes
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while cleaning string: " + e.getMessage());
            return value; // Gib den Originalwert zurück, falls ein Fehler auftritt
        }
    }

    // clean String Lists
    private static void cleanStringList(Map<String, Object> data, String key) {
        try {
            Bukkit.getLogger().severe("Cleaning " + key + " list for " + data.get("hopperName"));
            if (data.containsKey(key)) {
                Object value = data.get(key);
                if (value instanceof List<?>) {
                    List<String> stringList = (List<String>) value;
                    List<String> cleanedList = new ArrayList<>();
                    for (String str : stringList) {
                        // Bereinige jeden String in der Liste
                        String cleanedValue = str.replaceAll("[^a-zA-Z0-9_]", "");
                        cleanedList.add(cleanedValue);
                    }
                    data.put(key, cleanedList);
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while cleaning string list" + e.getMessage());
        }
    }

    // generate UUID
    private static void createUUID(Map<String, Object> data, String key) {
        try {
            Bukkit.getLogger().severe("Creating UUID for " + data.get("hopperName"));
            if (!data.containsKey(key)) {
                // Erstelle eine neue UUID, da sie nicht in der YAML-Datei vorhanden ist
                UUID uuid = UUID.randomUUID();
                data.put(key, uuid.toString());
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error while creating UUID" + e.getMessage());
        }
    }

    // write to json file
    private static void writeToJsonFile(List<Map<String, Object>> dataList, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath)) {
            Bukkit.getLogger().severe("Writing to JSON file: " + outputPath);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(dataList);
            writer.write(jsonString);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error while writing to JSON file: " + e.getMessage());
        }
    }
}
