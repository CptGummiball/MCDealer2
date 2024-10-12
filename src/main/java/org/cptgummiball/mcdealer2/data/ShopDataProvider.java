package org.cptgummiball.mcdealer2.data;

import org.json.JSONObject;
import org.json.JSONArray;

public class ShopDataProvider {

    // Template
    public String generateShopData() {
        JSONObject shop = new JSONObject();
        shop.put("name", "MC Dealer");

        JSONArray items = new JSONArray();
        JSONObject item1 = new JSONObject();
        item1.put("id", 1);
        item1.put("name", "Sword");
        item1.put("price", 10);

        JSONObject item2 = new JSONObject();
        item2.put("id", 2);
        item2.put("name", "Shield");
        item2.put("price", 15);

        items.put(item1);
        items.put(item2);

        shop.put("items", items);

        // jsonstring output
        return shop.toString();
    }
}
