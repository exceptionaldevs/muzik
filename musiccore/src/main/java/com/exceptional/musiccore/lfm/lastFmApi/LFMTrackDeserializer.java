package com.exceptional.musiccore.lfm.lastFmApi;

import com.exceptional.musiccore.lfm.models.track.LFMRealBaseTrack;
import com.exceptional.musiccore.lfm.models.track.LFMRealTrackNoTags;
import com.exceptional.musiccore.lfm.models.track.LFMRealTrackWithOneTag;
import com.exceptional.musiccore.lfm.models.track.LFMRealTrackWithTags;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class LFMTrackDeserializer<T extends LFMRealBaseTrack> implements JsonDeserializer<LFMRealBaseTrack> {

    private static final String TAG = "TrackDeserializer";

    public LFMTrackDeserializer() {
    }

    @Override
    public LFMRealBaseTrack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String trackString = "track";
        String topTagsString = "toptags";
        if (jsonObject.toString().equals("{}")) {
            throw new JsonParseException("Object was {}");
        } else if (jsonObject.has("error")) {
            String message = jsonObject.get("message").getAsString();
            throw new JsonParseException(message);
        } else if (jsonObject.has(trackString)) {
            final JsonObject jsonElement = jsonObject.getAsJsonObject(trackString);
            if (jsonElement.get(topTagsString).isJsonPrimitive()) {
                return new Gson().fromJson(jsonElement, LFMRealTrackNoTags.class);
            } else if (jsonElement.get(topTagsString).getAsJsonObject().get("tag").isJsonObject()) {
                return new Gson().fromJson(jsonElement, LFMRealTrackWithOneTag.class);
            } else {
                return new Gson().fromJson(jsonElement, LFMRealTrackWithTags.class);
            }
        } else {
            if (jsonObject.get(topTagsString).isJsonPrimitive()) {
                return new Gson().fromJson(json, LFMRealTrackNoTags.class);
            } else if (jsonObject.get(topTagsString).getAsJsonObject().get("tag").isJsonObject()) {
                return new Gson().fromJson(json, LFMRealTrackWithOneTag.class);
            } else {
                return new Gson().fromJson(json, LFMRealTrackWithTags.class);
            }
        }
    }
}
