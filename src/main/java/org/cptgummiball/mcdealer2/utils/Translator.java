package org.cptgummiball.mcdealer2.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Translator {
    private final Map<String, String> translations = new HashMap<>();
    private final String langFileName;
    private final JavaPlugin plugin;

    public Translator(JavaPlugin plugin, String langFileName) {
        this.plugin = plugin;
        this.langFileName = langFileName;
        loadTranslations(plugin);
    }

    @SuppressWarnings("unchecked")
    private void loadTranslations(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "lang/" + langFileName + ".yml");
        if (!file.exists()) {
            plugin.getLogger().warning("Could not find the translation file: " + file.getPath());
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(new InputStreamReader(fileInputStream));

            if (data == null) {
                plugin.getLogger().warning("The file " + file.getPath() + " contains no data or could not be loaded.");
                return;
            }

            // Rekursiv flache Map erzeugen
            flattenMap("", data, translations);

            // Ausgabe der geladenen Übersetzungen
            for (Map.Entry<String, String> entry : translations.entrySet()) {
                plugin.getLogger().info("Loaded translation: " + entry.getKey() + " -> " + entry.getValue());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Error loading translations: " + e.getMessage());
        }
    }

    private void flattenMap(String prefix, Map<String, Object> map, Map<String, String> flatMap) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flattenMap(key, (Map<String, Object>) value, flatMap);
            } else {
                flatMap.put(key, value.toString());
            }
        }
    }

    public String translate(String key) {
        return translations.getOrDefault(key, key);
    }
}
