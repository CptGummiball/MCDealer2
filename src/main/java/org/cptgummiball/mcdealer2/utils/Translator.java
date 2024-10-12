package org.cptgummiball.mcdealer2.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Translator {
    private final Map<String, String> translations = new HashMap<>();
    private final String langFileName;

    public Translator(JavaPlugin plugin, String langFileName) {
        this.langFileName = langFileName;
        loadTranslations(plugin);
    }

    private void loadTranslations(JavaPlugin plugin) {
        try (InputStream inputStream = plugin.getResource("lang/" + langFileName + ".yaml")) {
            if (inputStream == null) {
                plugin.getLogger().warning("Could not find translation file: " + langFileName);
                return;
            }

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(new InputStreamReader(inputStream));

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                translations.put(entry.getKey(), entry.getValue().toString());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load translations: " + e.getMessage());
        }
    }

    public String translate(String key) {
        return translations.getOrDefault(key, key);
    }
}
