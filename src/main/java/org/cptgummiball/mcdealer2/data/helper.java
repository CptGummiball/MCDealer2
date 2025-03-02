package org.cptgummiball.mcdealer2.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

import static org.cptgummiball.mcdealer2.data.ShopDataProvider.debugMode;
import static org.cptgummiball.mcdealer2.data.ShopDataProvider.plugin;

public class helper {

    // clean String
    static String cleanString(String value) {
        try {
            return value.replaceAll("§.", "");
        } catch (Exception e) {
            if (debugMode) {
                plugin.getLogger().info("Error while cleaning string: " + e.getMessage());
            }
            return value;
        }
    }
    // clean DisplayName
    static String cleanDisplayName(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        return input.replaceAll("[^\\x20-\\x7E]", "");  // Entfernt alle nicht-druckbaren Zeichen
    }

    // extract DisplayName from JSON
    static String extractDisplayName(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray extraArray = jsonObject.getJSONArray("extra");
            JSONObject extraObject = extraArray.getJSONObject(0);
            return extraObject.getString("text");
        } catch (JSONException e) {
            if (debugMode) {
                plugin.getLogger().info("Error while cleaning DisplayName: " + e.getMessage());
            }
            return "";
        }
    }

    // get file name without extension
    static String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
    }

    // get nested String
    static String getNestedString(Map<String, Object> data, String key1, String key2) {
        Map<String, Object> nested = (Map<String, Object>) data.get(key1);
        return nested != null && nested.get(key2) != null ? nested.get(key2).toString() : "";
    }

    // get nested String (triple nested)
    static String getTripleNestedString(Map<String, Object> data, String key1, String key2, String key3) {
        Map<String, Object> nested1 = (Map<String, Object>) data.get(key1);
        if (nested1 == null) return "";
        Map<String, Object> nested2 = (Map<String, Object>) nested1.get(key2);
        if (nested2 == null) return "";
        return nested2.get(key3) != null ? nested2.get(key3).toString() : "";
    }

    // get nested Double
    static double getNestedDouble(Map<String, Object> data, String key2) {
        Map<String, Object> nested = (Map<String, Object>) data.get("location");
        if (nested == null || nested.get(key2) == null) return 0.0;
        try {
            return ((Number) nested.get(key2)).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    // get nested Double (triple nested)
    static double getTripleNestedDouble(Map<String, Object> data, String key1, String key2, String key3) {
        Map<String, Object> nested1 = (Map<String, Object>) data.get(key1);
        if (nested1 == null) return 0.0;
        Map<String, Object> nested2 = (Map<String, Object>) nested1.get(key2);
        if (nested2 == null) return 0.0;
        Object value = nested2.get(key3);
        if (value == null) return 0.0;
        try {
            return ((Number) value).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    // get nested Integer
    static Integer getNestedInteger(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        try {
            return (Integer) value;
        } catch (Exception e) {
            return null;
        }
    }

    // get triple nested Integer
    static Integer getTripleNestedInteger(Map<String, Object> data, String key1, String key2, String key3) {
        Map<String, Object> nested1 = (Map<String, Object>) data.get(key1);
        if (nested1 == null) return null;
        Map<String, Object> nested2 = (Map<String, Object>) nested1.get(key2);
        if (nested2 == null) return null;
        Object value = nested2.get(key3);
        if (value == null) return null;
        try {
            return (Integer) value;
        } catch (Exception e) {
            return null;
        }
    }

    // get nested Map
    static Map<String, Object> getNestedMap(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        try {
            return (Map<String, Object>) value;
        } catch (Exception e) {
            return null;
        }
    }

    // get triple nested Map
    static Map<String, Object> getTripleNestedMap(Map<String, Object> data, String key1, String key2, String key3) {
        Map<String, Object> nested1 = (Map<String, Object>) data.get(key1);
        if (nested1 == null) return null;
        Map<String, Object> nested2 = (Map<String, Object>) nested1.get(key2);
        if (nested2 == null) return null;
        Object value = nested2.get(key3);
        if (value == null) return null;
        try {
            return (Map<String, Object>) value;
        } catch (Exception e) {
            return null;
        }
    }

    // get nested List
    static List<Object> getNestedList(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        try {
            return (List<Object>) value;
        } catch (Exception e) {
            return null;
        }
    }

    // get triple nested List
    static List<Object> getTripleNestedList(Map<String, Object> data, String key1, String key2, String key3) {
        Map<String, Object> nested1 = (Map<String, Object>) data.get(key1);
        if (nested1 == null) return null;
        Map<String, Object> nested2 = (Map<String, Object>) nested1.get(key2);
        if (nested2 == null) return null;
        Object value = nested2.get(key3);
        if (value == null) return null;
        try {
            return (List<Object>) value;
        } catch (Exception e) {
            return null;
        }
    }

    // load hidden shops
    static Set<String> loadHiddenShops(File hiddenShopsFile) {
        Set<String> hiddenShops = new HashSet<>();
        try {
            if (hiddenShopsFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(hiddenShopsFile);
                List<String> shopsList = config.getStringList("hiddenShops");
                hiddenShops.addAll(shopsList);
            }
            return hiddenShops;
        } catch (Exception e) {
        if (debugMode) {
            plugin.getLogger().info("Error while loading hidden shops: " + e.getMessage());
        }
        return null;
        }
    }

    static void bestpricecheck(List<Map<String, Object>> ShopsList) {
        Map<String, Double> bestprice = new HashMap<>();
        for (Map<String, Object> yamlMap : ShopsList) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) yamlMap.get("items");
            for (Map<String, Object> item : items) {
                String itemType = (String) item.get("type");
                Double priceperitem = (Double) item.get("price");
                if (!bestprice.containsKey(itemType) || priceperitem < bestprice.get(itemType)) {
                    bestprice.put(itemType, priceperitem);
                }
            }
        }
        // Setzen Sie den Wert auf true, wenn der Preis des Items der günstigste ist
        for (Map<String, Object> yamlMap : ShopsList) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) yamlMap.get("items");
            for (Map<String, Object> item : items) {
                String itemType = (String) item.get("type");
                Double priceperitem = (Double) item.get("priceperitem");
                if (priceperitem.equals(bestprice.get(itemType)) && item.get("meta") == null) {
                    item.put("bestprice", true);
                }
            }
        }
    }
}
