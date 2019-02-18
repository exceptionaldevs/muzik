package com.exceptional.musiccore.glide.persistence;

import com.yahoo.squidb.annotations.ColumnSpec;
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Created by sebnapi on 24.12.2015.
 */
@TableModelSpec(className = "LFMArtistResponse", tableName = "LFMArtistResponse")
public class LFMArtistResponseSpec {

    @ColumnSpec(constraints = "NOT NULL")
    public String artistName;

    @ColumnSpec(defaultValue = "0")
    public long creationDate;

    @ColumnSpec(constraints = "NOT NULL")
    public String defaultImageUrl;

    @ColumnSpec(constraints = "NOT NULL")
    public String defaultImageSize;

    public int preloadColor;

    @ModelMethod
    public static void setNowAsCreationDate(LFMArtistResponse instance) {
        instance.setCreationDate(System.currentTimeMillis());
    }
}
