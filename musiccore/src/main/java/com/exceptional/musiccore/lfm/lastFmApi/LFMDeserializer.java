package com.exceptional.musiccore.lfm.lastFmApi;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class LFMDeserializer<T> implements JsonDeserializer<T> {
    @SuppressWarnings("unused")
    private static final String TAG = "LastFmDeserializer";

    String field;

    public LFMDeserializer(String field) {
        this.field = field;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.toString().equals("{}")) {
            throw new JsonParseException("Object was {}");
        } else if (jsonObject.has("error")) {
            String message = jsonObject.get("message").getAsString();
            throw new JsonParseException(message);
        } else if (jsonObject.has(field)) {
            final JsonElement jsonElement = jsonObject.get(field);
            return new Gson().fromJson(jsonElement, typeOfT);
        } else {
            return new Gson().fromJson(json, typeOfT);
        }
    }
}
