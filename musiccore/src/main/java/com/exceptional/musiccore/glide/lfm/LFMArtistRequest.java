package com.exceptional.musiccore.glide.lfm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Key;
import com.exceptional.musiccore.glide.persistence.ArtistImage;
import com.exceptional.musiccore.glide.persistence.ImageDB;
import com.exceptional.musiccore.glide.persistence.LFMArtistResponse;
import com.exceptional.musiccore.lfm.LFMEndpoint;
import com.exceptional.musiccore.lfm.models.LFMImage;
import com.exceptional.musiccore.lfm.models.artist.LFMRealBaseArtist;
import com.yahoo.squidb.sql.Query;

import java.io.IOException;
import java.security.MessageDigest;

import retrofit.Response;

/**
 * Created by sebnap on 21.12.15.
 */
public class LFMArtistRequest implements Key, LFMRequest {
    private final String mArtistName;

    public LFMArtistRequest(@NonNull String artistName) {
        mArtistName = artistName;
    }

    @Override
    public String toString() {
        return "LFMArtistRequest{"
                + "mArtistName=" + mArtistName
                + '}';
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(toString().getBytes(CHARSET));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LFMArtistRequest))
            return false;

        LFMArtistRequest foo = (LFMArtistRequest) o;
        if (!mArtistName.equals(foo.mArtistName))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + mArtistName.hashCode();
        return hash;
    }

    @Override
    @Nullable
    public String getImageUrl(LFMEndpoint lfmEndpoint, boolean cacheToDB, ImageDB imageDB) {
        Response<LFMRealBaseArtist> response = null;
        try {
            response = lfmEndpoint.getArtistInfoByName(mArtistName).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response != null && response.isSuccess()) {
            LFMImage image = response.body().getBiggestImage();
            if (image != null) {
                String size = image.getSize();
                String url = image.getUrl();

                if (cacheToDB && imageDB != null) {
                    Query query = Query.select().from(LFMArtistResponse.TABLE)
                            .where(LFMArtistResponse.ARTIST_NAME.eq(mArtistName));
                    LFMArtistResponse row = imageDB.fetchByQuery(LFMArtistResponse.class, query);
                    if (row == null) {
                        row = new LFMArtistResponse();
                    }
                    row.setArtistName(mArtistName);
                    row.setDefaultImageSize(size);
                    row.setDefaultImageUrl(url);
                    row.setNowAsCreationDate();
                    imageDB.persist(row);
                }

                return url;
            }
        }

        return null;
    }

    @Override
    @Nullable
    public String getFilePathFromDB(ImageDB imageDB) {
        String result = null;
        Query query = Query.select(ArtistImage.FILE_PATH).from(ArtistImage.TABLE)
                .where(ArtistImage.ARTIST_NAME.eq(mArtistName));
        ArtistImage response = imageDB.fetchByQuery(ArtistImage.class, query);
        if (response != null) {
            return response.getFilePath();
        }
        return result;
    }

    @Override
    @Nullable
    public String getImageUrlFromDB(ImageDB imageDB) {
        String result = null;
        Query query = Query.select(LFMArtistResponse.DEFAULT_IMAGE_URL).from(LFMArtistResponse.TABLE)
                .where(LFMArtistResponse.ARTIST_NAME.eq(mArtistName));
        LFMArtistResponse response = imageDB.fetchByQuery(LFMArtistResponse.class, query);
        if (response != null) {
            return response.getDefaultImageUrl();
        }
        return result;
    }

    @Override
    public boolean insertFilePathToDB(ImageDB imageDB, String absolutePath) {
        Query query = Query.select().from(ArtistImage.TABLE)
                .where(ArtistImage.ARTIST_NAME.eq(mArtistName));
        ArtistImage row = imageDB.fetchByQuery(ArtistImage.class, query);
        if (row == null) {
            row = new ArtistImage();
        }
        row.setArtistName(mArtistName);
        row.setFilePath(absolutePath);
        row.setNowAsCreationDate();
        return imageDB.persist(row);
    }

}
