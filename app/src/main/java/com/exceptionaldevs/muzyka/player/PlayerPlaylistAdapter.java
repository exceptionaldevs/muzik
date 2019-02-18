package com.exceptionaldevs.muzyka.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXPlayer;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.utils.DateUtil;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.settings.MainSettingsFragment;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.MultiItemSelector;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMRecyclerView;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by darken on 19.03.14.
 */
public class PlayerPlaylistAdapter extends ContentAdapter<Track, PlayerPlaylistAdapter.TrackItemViewHolder> implements JXPlayer.PlayerProgressListener {
    private MultiItemSelector mMultiItemSelector;
    private final HashSet<JXPlayer.PlayerProgressListener> mSubProgressListeners = new HashSet<>();
    private SharedPreferences mSharedPreferences;

    public PlayerPlaylistAdapter(Context context, MultiItemSelector multiItemSelector) {
        super(context);
        mMultiItemSelector = multiItemSelector;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public TrackItemViewHolder onCreateBasicItemViewHolder(LayoutInflater inflater, ViewGroup viewGroup, int viewType) {
        View view = inflater.inflate(R.layout.adapter_line_queue, viewGroup, false);
        return new TrackItemViewHolder(this, view);
    }


    @Override
    public void onBindBasicItemView(final TrackItemViewHolder holder, int position) {
        Track track = getItem(position);
        if (mSharedPreferences.getBoolean(MainSettingsFragment.PREF_KEY_DRAGGABLE, true)) {
            holder.dragAnchor.setVisibility(View.VISIBLE);
            holder.dragAnchor.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ((SDMRecyclerView) holder.itemView.getParent()).startDrag(holder);
                    return false;
                }
            });
        } else {
            holder.dragAnchor.setVisibility(View.GONE);
        }
        holder.setTrack(track);
        holder.primaryText.setText(track.getPrimaryInfo(getContext()));
        holder.secondaryText.setText(track.getSecondaryInfo(getContext()));
        holder.tertiaryText.setText(track.getTertiaryInfo(getContext()));

//        if (mMultiItemSelector.isItemChecked(position)) {
//            QuickAnimationFactory.makeAnimatedListTick(getContext(), mStylePack, holder.icon);
//        } else {
        Glide.with(holder.getContext())
                .load(CoverModelMetaVisitor.visitAcceptor(track, mContext).getModel())
                .apply(RequestOptions.circleCropTransform())
                .thumbnail(Glide.with(holder.icon.getContext()).load(R.drawable.material_colors_stock))
                .into(holder.icon);
//        }
    }

    @Override
    public void onViewAttachedToWindow(SDMViewHolder holder) {
        synchronized (mSubProgressListeners) {
            if (holder instanceof JXPlayer.PlayerProgressListener)
                mSubProgressListeners.add((JXPlayer.PlayerProgressListener) holder);
        }
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(SDMViewHolder holder) {
        synchronized (mSubProgressListeners) {
            if (holder instanceof JXPlayer.PlayerProgressListener)
                mSubProgressListeners.remove(holder);
        }
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onPlayerProgressChanged(JXObject jxObject, int playlistPosition, long playbackPosition, long bufferPosition, long maxDuration, boolean isBusy) {
        List<JXPlayer.PlayerProgressListener> sppl = new ArrayList<>();
        synchronized (mSubProgressListeners) {
            // The player progress call does not necessarily come from the main thread.
            sppl.addAll(mSubProgressListeners);
        }
        for (JXPlayer.PlayerProgressListener listener : sppl) {
            listener.onPlayerProgressChanged(jxObject, playlistPosition, playbackPosition, bufferPosition, maxDuration, isBusy);
        }
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public static class TrackItemViewHolder extends SDMViewHolder implements JXPlayer.PlayerProgressListener {
        @BindView(R.id.item_image) public ImageView icon;
        @BindView(R.id.item_primary_text) public TextView primaryText;
        @BindView(R.id.item_secondary_text) public TextView secondaryText;
        @BindView(R.id.item_time_text) public TextView tertiaryText;
        @BindView(R.id.drag_anchor) public View dragAnchor;

        private Track mTrack;
        private final PlayerPlaylistAdapter mTrackAdapter;

        public TrackItemViewHolder(PlayerPlaylistAdapter adapter, View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mTrackAdapter = adapter;
        }

        public void setTrack(Track track) {
            mTrack = track;
        }

        @Override
        public void onPlayerProgressChanged(JXObject jxObject, int playlistPosition, final long playbackPosition, final long bufferPosition, final long maxDuration, boolean isBusy) {
            if (getAdapterPosition() == -1 || !itemView.isAttachedToWindow())
                return;
            Runnable progressUpdate;
            if (jxObject != null
                    && jxObject.getLibrarySource().equals(mTrack.getLibrarySource())
                    && playbackPosition != -1
                    && playlistPosition == getAdapterPosition()) {
                progressUpdate = new Runnable() {
                    @Override
                    public void run() {
                        primaryText.setTypeface(null, Typeface.BOLD);
                        tertiaryText.setText(DateUtil.formatDuration(playbackPosition) + "/" + DateUtil.formatDuration(maxDuration));
                    }
                };
            } else {
                progressUpdate = new Runnable() {
                    @Override
                    public void run() {
                        primaryText.setTypeface(null, Typeface.NORMAL);
                        tertiaryText.setText(DateUtil.formatDuration(mTrack.getDuration()));
                    }
                };
            }
            itemView.post(progressUpdate);
        }

    }
}
