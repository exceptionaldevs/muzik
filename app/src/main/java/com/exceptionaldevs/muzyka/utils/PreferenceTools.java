package com.exceptionaldevs.muzyka.utils;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class PreferenceTools {
    /**
     * @param prefs  The SharedPreferences object to use for saving
     * @param key    The key under which the array should be saved
     * @param values The string array to be saved
     */
    public static void setStringArrayPref(@NonNull SharedPreferences prefs, @NonNull String key, List<String> values) {
        SharedPreferences.Editor editor = prefs.edit();
        if (values == null) {
            editor.remove(key).apply();
            return;
        }
        JSONArray a = new JSONArray();
        for (String v : values)
            a.put(v);

        editor.putString(key, a.toString());

        editor.apply();
    }

    /**
     * @param prefs The SharedPreferences object to use for saving
     * @param key   Which key to look under
     * @return Returns an ArrayList of Strings retreived, might be empty if nothing could be retreived
     */
    public static List<String> getStringArrayPref(@NonNull SharedPreferences prefs, @NonNull String key) {
        String json = prefs.getString(key, null);
        if (json == null)
            return null;
        List<String> values = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(json);
            for (int i = 0; i < a.length(); i++)
                values.add(a.optString(i));

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return values;
    }

}
