package com.exceptional.musiccore.lfm;

import com.exceptional.musiccore.lfm.models.LFMTrackHistoryPage;
import com.exceptional.musiccore.lfm.models.LFMUser;
import com.exceptional.musiccore.lfm.models.artist.LFMRealBaseArtist;
import com.exceptional.musiccore.lfm.models.track.LFMRealBaseTrack;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by sebnap on 01.12.15.
 */
@SuppressWarnings("unused")
public interface LFMEndpoint {
    String BASE_URL = "http://ws.audioscrobbler.com/2.0/";
    String APIKEY = "beeed7fd80ac8f164f1068bace7a029e";

    @GET("?method=user.getinfo")
    Call<LFMUser> getUserInfo(@Query("user") String user);

    @GET("?method=user.getrecenttracks&limit=200")
    Call<LFMTrackHistoryPage> getRecentTracks(
            @Query("user") String user,
            @Query("page") int page
    );

    @GET("?method=track.getinfo")
    Call<LFMRealBaseTrack> getTrackInfo(
            @Query("track") String track,
            @Query("artist") String artist
    );

    @GET("?method=track.getinfo")
    Call<LFMRealBaseTrack> getTrackInfo(@Query("mbid") String mbid);

    @GET("?method=artist.getInfo")
    Call<LFMRealBaseArtist> getArtistInfoByMbid(@Query("mbid") String mbid);

    @GET("?method=artist.getInfo&autocorrect=1")
    Call<LFMRealBaseArtist> getArtistInfoByName(@Query("artist") String artist);

    class LFMInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            if (chain.request().httpUrl().toString().startsWith(LFMEndpoint.BASE_URL)) {
                // if the start of the urls matches we intercept it adding the api key and format=json
                HttpUrl url = chain.request().httpUrl().newBuilder()
                        .addQueryParameter("api_key", LFMEndpoint.APIKEY)
                        .addQueryParameter("format", "json")
                        .build();
                Request request = chain.request().newBuilder().url(url).build();
                return chain.proceed(request);
            }
            //if not thats not an lfm request
            return chain.proceed(chain.request());
        }
    }

}