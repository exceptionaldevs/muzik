package com.exceptionaldevs.muzyka;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.exceptional.musiccore.BinderCustomer;
import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.NotificationHelper;
import com.exceptional.musiccore.engine.queue.Queue;
import com.exceptionaldevs.muzyka.content.tabs.SectionsPagerAdapter;
import com.exceptionaldevs.muzyka.player.FortuneWheelActivity;
import com.exceptionaldevs.muzyka.player.PlayerActivity;
import com.exceptionaldevs.muzyka.settings.SettingsActivity;
import com.exceptionaldevs.muzyka.ui.widget.Miniplayer;
import com.exceptionaldevs.muzyka.ui.widget.QuickAnimationFactory;
import com.exceptionaldevs.muzyka.utils.AccentColorFactory;
import com.exceptionaldevs.muzyka.utils.Logy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends MuzikServiceActivity implements QueueRotationCall {
    private static final String TAG = MuzikApplication.TAG_PREFIX + "MainActivity";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public static int REQUEST_FORTUNEWHEEL = 1337;
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    @BindView(R.id.player) Miniplayer mMiniplayer;
    @BindView(R.id.showPlaylistBtn) ImageButton mShowPlaylist;
    private PlayerViewBinder mPlayerViewBinder;
    public boolean mStoppedRotation;
    private Runnable mQueuedRotationCall;
    private Queue.JXQueueListener mQueueListener;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String task = intent.getStringExtra("task");
        if (NotificationHelper.INTENT_ACTION_COVERCLICK.equals(task)) {
            Logy.d(TAG, "COVERCLICK");
        } else if (NotificationHelper.INTENT_ACTION_NOTIFICATIONCLICK.equals(task)) {
            Logy.d(TAG, "NOTIFICATION CLICK");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        mShowPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                float rotation = mMiniplayer.getHeartView().getCoverView().getRotation();
                intent.putExtra(PlayerActivity.EXTRA_COVER_ROTATION, rotation);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,
                        Pair.create(findViewById(R.id.player), getResources().getString(R.string.transition_player))).toBundle());
            }
        });
        registerBinderCustomer(new BinderCustomer<MusicBinder>() {
            @Override
            public void onBinderAvailable(MusicBinder binder) {
                mQueueListener = new Queue.JXQueueListener() {
                    @Override
                    public void onPlaylistDataChanged(final Queue queue) {
                        mShowPlaylist.post(new Runnable() {
                            @Override
                            public void run() {
                                QuickAnimationFactory.wiggle(mShowPlaylist);
                            }
                        });
                    }
                };
                binder.getQueueHandler().addListener(mQueueListener);
            }
        });

        AccentColorFactory.initAccentColorFactory(this);

        mPlayerViewBinder = new PlayerViewBinder(mMiniplayer, this);
        mPlayerViewBinder.setOnFortuneClick(new OnFortuneClickListener() {
            @Override
            public void onClickFortune(View v) {
                mMiniplayer.getHeartView().getPlayPauseFab()
                        .setTransitionName(getResources().getString(R.string.transition_fortunewheel_fab));
                mMiniplayer.getHeartView().getCoverView()
                        .setTransitionName(getResources().getString(R.string.transition_fortunewheel_wheel));
                Pair<View, String> p1 = Pair.create((View) mMiniplayer.getHeartView().getCoverView(),
                        getResources().getString(R.string.transition_fortunewheel_wheel));
                Pair<View, String> p2 = Pair.create((View) mMiniplayer.getHeartView().getPlayPauseFab(),
                        getResources().getString(R.string.transition_fortunewheel_fab));

                // return transition screws up if we dont stop and start the rotation manually
                mStoppedRotation = true;
                mMiniplayer.getHeartView().setPlaying(false);
                startActivityForResult(new Intent(MainActivity.this, FortuneWheelActivity.class), REQUEST_FORTUNEWHEEL,
                        ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, p1, p2).toBundle());
            }

            @Override
            public boolean onTouchFortune(View v, MotionEvent event) {
                return false;
            }
        });
        registerBinderCustomer(mPlayerViewBinder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayerViewBinder != null) {
            boolean result = mPlayerViewBinder.bindMiniplayerToCurrentState();
            if (result == false) {
                registerBinderCustomer(new BinderCustomer<MusicBinder>() {
                    @Override
                    public void onBinderAvailable(MusicBinder binder) {
                        mPlayerViewBinder.bindMiniplayerToCurrentState();
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        if (getBinder() != null) {
            if (mQueueListener != null) {
                getBinder().getQueueHandler().removeListener(mQueueListener);
                mQueueListener = null;
            }
        }
        mPlayerViewBinder.destroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FORTUNEWHEEL) {
            // cosmetic only, return transition screws up if we dont stop and start the rotation manually
            // we dont need to save this in the instance state bundle, as this should at most just look bad
            mStoppedRotation = false;
            if (mQueuedRotationCall != null) {
                mQueuedRotationCall.run();
            }
            mQueuedRotationCall = null;
        }
    }


    @Override
    public boolean shouldQueueRotationCall() {
        return mStoppedRotation;
    }

    @Override
    public void queueRotationCall(Runnable run) {
        mQueuedRotationCall = run;
    }

    @OnClick(R.id.showSettingsBtn)
    void onClickSettings(View v) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }
}
