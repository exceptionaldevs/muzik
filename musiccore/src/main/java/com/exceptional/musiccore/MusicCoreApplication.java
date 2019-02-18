package com.exceptional.musiccore;

import android.app.Application;

import com.exceptional.musiccore.lfm.LFMEndpoint;
import com.exceptional.musiccore.lfm.lastFmApi.LFMArtistDeserializer;
import com.exceptional.musiccore.lfm.lastFmApi.LFMDeserializer;
import com.exceptional.musiccore.lfm.lastFmApi.LFMTrackDeserializer;
import com.exceptional.musiccore.lfm.models.LFMTrackHistoryPage;
import com.exceptional.musiccore.lfm.models.LFMUser;
import com.exceptional.musiccore.lfm.models.artist.LFMRealBaseArtist;
import com.exceptional.musiccore.lfm.models.track.LFMRealBaseTrack;
import com.exceptional.musiccore.lfm.models.track.LFMTrackListTrack;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by darken on 18.11.2015.
 */
public class MusicCoreApplication extends Application {
    public static final String MUSICCORE_TAG_PREFIX = "MC:";
    public OkHttpClient mHttpClient;
    public LFMEndpoint mLFMServiceEndpoint;

    @Override
    public void onCreate() {
        super.onCreate();

        mHttpClient = new OkHttpClient();
        mHttpClient.interceptors().add(new LFMEndpoint.LFMInterceptor());
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        mHttpClient.interceptors().add(interceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .client(mHttpClient)
                .baseUrl(LFMEndpoint.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .registerTypeAdapter(LFMUser.class, new LFMDeserializer<LFMUser>("user"))
                        .registerTypeAdapter(LFMTrackListTrack.class, new LFMDeserializer<LFMTrackListTrack>("track"))
                        .registerTypeAdapter(LFMTrackHistoryPage.class, new LFMDeserializer<LFMTrackHistoryPage>("recenttracks"))
                        .registerTypeAdapter(LFMRealBaseTrack.class, new LFMTrackDeserializer<>())
                        .registerTypeAdapter(LFMRealBaseArtist.class, new LFMArtistDeserializer<>())
                        .create()))
                .build();

        mLFMServiceEndpoint = retrofit.create(LFMEndpoint.class);
    }

    public OkHttpClient getHttpClient() {
        return mHttpClient;
    }

    public LFMEndpoint getLFMServiceEndpoint() {
        return mLFMServiceEndpoint;
    }
}
