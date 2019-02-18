package com.exceptional.musiccore.glide.persistence;

import android.content.Context;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.utils.Logy;
import com.yahoo.squidb.data.SquidDatabase;
import com.yahoo.squidb.data.adapter.SQLiteDatabaseWrapper;
import com.yahoo.squidb.sql.Table;

/**
 * Created by sebnapi on 24.12.2015.
 */

public class ImageDB extends SquidDatabase {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "ImageDB";
    private volatile static ImageDB sInstance;

    public static ImageDB getInstance(Context c) {
        if (sInstance == null) {
            synchronized (ImageDB.class) {
                if (sInstance == null)
                    sInstance = new ImageDB(c.getApplicationContext());
            }
        }
        return sInstance;
    }

    private static final int VERSION = 1;

    private ImageDB(Context context) {
        super(context);
        Logy.i(TAG, "ImageDB created.");
    }

    @Override
    public String getName() {
        return "image.db";
    }


    @Override
    protected int getVersion() {
        return VERSION;
    }

    @Override
    protected Table[] getTables() {
        return new Table[]{
                ArtistImage.TABLE,
                LFMArtistResponse.TABLE
        };
    }

    @Override
    protected boolean onUpgrade(SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
        return false;
    }

}
