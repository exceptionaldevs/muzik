package com.exceptional.musiccore.lfm.lastFmApi;

import com.exceptional.musiccore.lfm.models.artist.LFMRealArtistWithNoTags;
import com.exceptional.musiccore.lfm.models.artist.LFMRealArtistWithOneTag;
import com.exceptional.musiccore.lfm.models.artist.LFMRealArtistWithTags;
import com.exceptional.musiccore.lfm.models.artist.LFMRealBaseArtist;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class LFMArtistDeserializer<T extends LFMRealBaseArtist> implements
        JsonDeserializer<LFMRealBaseArtist> {

    private static final String TAG = "ArtistDeserializer";

    public LFMArtistDeserializer() {
    }

    @Override
    public LFMRealBaseArtist deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext
            context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String trackString = "artist";
        String tagsString = "tags";
        if (jsonObject.toString().equals("{}")) {
            throw new JsonParseException("Object was {}");
        } else if (jsonObject.has("error")) {
            String message = jsonObject.get("message").getAsString();
            throw new JsonParseException(message);
        } else {
            final JsonObject jsonElement = jsonObject.getAsJsonObject(trackString);
            if (jsonElement.get(tagsString).isJsonPrimitive()) {
                return new Gson().fromJson(jsonElement, LFMRealArtistWithNoTags.class);
            } else if (jsonElement.get(tagsString).getAsJsonObject().get("tag").isJsonObject()) {
                return new Gson().fromJson(jsonElement, LFMRealArtistWithOneTag.class);
            } else {
                return new Gson().fromJson(jsonElement, LFMRealArtistWithTags.class);
            }
        }
    }
}
