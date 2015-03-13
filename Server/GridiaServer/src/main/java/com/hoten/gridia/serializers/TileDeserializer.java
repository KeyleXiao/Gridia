package com.hoten.gridia.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hoten.gridia.Creature;
import com.hoten.gridia.content.ItemInstance;
import com.hoten.gridia.map.Tile;
import com.hoten.gridia.serving.ServingGridia;
import java.lang.reflect.Type;

public class TileDeserializer implements JsonDeserializer<Tile> {

    private final ServingGridia _servingGridia;

    // :( make creature factory
    public TileDeserializer(ServingGridia servingGridia) {
        _servingGridia = servingGridia;
    }

    @Override
    public Tile deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Tile tile = new Tile();
        tile.floor = jsonObject.get("floor").getAsInt();
        tile.item = context.deserialize(jsonObject.get("item"), ItemInstance.class);
        if (jsonObject.has("cre")) {
            Creature cre = context.deserialize(jsonObject.get("cre"), Creature.class);
            tile.cre = _servingGridia.createCreatureQuietly(cre.image, cre.name, cre.location, false, cre.isFriendly);
            tile.cre.friendlyMessage = cre.friendlyMessage;
            tile.cre.setAttribute("life", (Integer) cre.getAttribute("life"));
        }
        return tile;
    }
}
