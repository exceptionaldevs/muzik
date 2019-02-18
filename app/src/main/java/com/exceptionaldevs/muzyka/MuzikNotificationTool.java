package com.exceptionaldevs.muzyka;

import com.exceptional.musiccore.MusicService;
import com.exceptional.musiccore.NotificationHelper;

/**
 * Created by darken on 31.01.2016.
 */
public class MuzikNotificationTool extends NotificationHelper {
    public MuzikNotificationTool(MusicService<?> service) {
        super(service, MainActivity.class, new MuzikNotificationConfig());
    }

    static class MuzikNotificationConfig extends LayoutConfig {
        public MuzikNotificationConfig() {
            super.drawableFavour = R.drawable.ic_favorite_red_500_24dp;
            super.drawableUnfavour = R.drawable.ic_favorite_border_red_500_24dp;
            super.layoutSmall = R.layout.notification_small;
            super.layoutBig = R.layout.notification_fancy_big;
            super.smallIcon = R.mipmap.ic_launcher;
            super.drawablePlayDark = R.drawable.ic_play_arrow_black_36dp;
            super.drawablePauseDark = R.drawable.ic_pause_black_36dp;
            super.drawablePlayLight = R.drawable.ic_play_arrow_white_36dp;
            super.drawablePauseLight = R.drawable.ic_pause_white_36dp;
            super.textColorPrimaryDark = R.color.text_primary_dark;
            super.textColorSecondaryDark = R.color.text_secondary_dark;
            super.textColorPrimaryLight = R.color.text_primary_light;
            super.textColorSecondaryLight = R.color.text_secondary_light;
        }
    }
}
