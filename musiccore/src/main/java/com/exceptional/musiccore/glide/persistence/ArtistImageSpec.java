package com.exceptional.musiccore.glide.persistence;

import com.yahoo.squidb.annotations.ColumnSpec;
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Created by sebnapi on 24.12.2015.
 */
@TableModelSpec(className = "ArtistImage", tableName = "artistImage")
public class ArtistImageSpec {

    @ColumnSpec(constraints = "NOT NULL")
    public String artistName;

    @ColumnSpec(defaultValue = "0")
    public long creationDate;

    @ColumnSpec(constraints = "NOT NULL")
    public String filePath;

    @ModelMethod
    public static void setNowAsCreationDate(ArtistImage instance) {
        instance.setCreationDate(System.currentTimeMillis());
    }
}
