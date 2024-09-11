package org.cptgummiball.mcdealer2.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class Item {

    private Material itemType;
    private int inStock;
    private double valuePerSell;
    private double price;
    private Map<String, Object> metaData;

    public Item(Material itemType, int inStock, double valuePerSell, double price, Map<String, Object> metaData) {
        this.itemType = itemType;
        this.inStock = inStock;
        this.valuePerSell = valuePerSell;
        this.price = price;
        this.metaData = metaData;
    }

    public static Item fromYaml(ConfigurationSection config) {
        // Parse itemType
        Material itemType = Material.valueOf(config.getString("itemType"));

        // Parse inStock
        int inStock = config.getInt("inStock", 0);

        // Parse valuePerSell
        double valuePerSell = config.getDouble("valuePerSell", 0.0);

        // Parse price
        double price = config.getDouble("price", 0.0);

        // Parse metaData (if available)
        ConfigurationSection metaSection = config.getConfigurationSection("metaData");
        Map<String, Object> metaData = null;
        if (metaSection != null) {
            metaData = metaSection.getValues(false);
        }

        return new Item(itemType, inStock, valuePerSell, price, metaData);
    }

    public Material getItemType() {
        return itemType;
    }

    public int getInStock() {
        return inStock;
    }

    public double getValuePerSell() {
        return valuePerSell;
    }

    public double getPrice() {
        return price;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemType=" + itemType +
                ", inStock=" + inStock +
                ", valuePerSell=" + valuePerSell +
                ", price=" + price +
                ", metaData=" + metaData +
                '}';
    }
}
