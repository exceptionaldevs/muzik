package com.exceptional.musiccore.glide.lfm;

import com.exceptional.musiccore.glide.persistence.ImageDB;
import com.exceptional.musiccore.lfm.LFMEndpoint;

/**
 * Created by sebnap on 21.12.15.
 */
public interface LFMRequest {
    String getImageUrl(LFMEndpoint lfmEndpoint, boolean cacheToDB, ImageDB imageDB);

    String getFilePathFromDB(ImageDB imageDB);

    String getImageUrlFromDB(ImageDB imageDB);

    boolean insertFilePathToDB(ImageDB imageDB, String absolutePath);
}
