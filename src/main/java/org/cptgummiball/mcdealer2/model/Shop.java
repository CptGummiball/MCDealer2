package org.cptgummiball.mcdealer2.model;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Shop {

    private UUID uuid;
    private String owner;
    private String type;
    private Location location;
    private List<Item> offers;
    private List<Item> demands;

    public Shop(UUID uuid, String owner, String type, Location location, List<Item> offers, List<Item> demands) {
        this.uuid = uuid;
        this.owner = owner;
        this.type = type;
        this.location = location;
        this.offers = offers;
        this.demands = demands;
    }

    public static Shop fromYaml(YamlConfiguration config) {
        // UUID of the shop (from file name or YAML)
        UUID shopUUID = UUID.fromString(config.getString("uuid"));

        // Shop owner, defaults to "Server" if not set
        String shopOwner = config.getString("owner", "Server");

        // Shop type (e.g., BUY/SELL)
        String shopType = config.getString("type");

        // Shop location
        Location location = config.getLocation("location");

        // Load offers
        List<Item> offers = loadItemsFromYaml(config, "offers");

        // Load demands
        List<Item> demands = loadItemsFromYaml(config, "demands");

        return new Shop(shopUUID, shopOwner, shopType, location, offers, demands);
    }

    private static List<Item> loadItemsFromYaml(YamlConfiguration config, String path) {
        List<Item> items = new ArrayList<>();

        if (config.isConfigurationSection(path)) {
            for (String key : config.getConfigurationSection(path).getKeys(false)) {
                String fullPath = path + "." + key;
                Item item = Item.fromYaml(config.getConfigurationSection(fullPath));
                items.add(item);
            }
        }
        return items;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getOwner() {
        return owner;
    }

    public String getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public List<Item> getOffers() {
        return offers;
    }

    public List<Item> getDemands() {
        return demands;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "uuid=" + uuid +
                ", owner='" + owner + '\'' +
                ", type='" + type + '\'' +
                ", location=" + location +
                ", offers=" + offers +
                ", demands=" + demands +
                '}';
    }
}
