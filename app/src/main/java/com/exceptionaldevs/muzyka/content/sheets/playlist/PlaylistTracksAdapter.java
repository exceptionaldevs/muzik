package com.exceptionaldevs.muzyka.content.sheets.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.exceptional.musiccore.BinderCustomer;
import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptionaldevs.muzyka.MuzikServiceActivity;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.ui.widget.QuickAnimationFactory;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;
import com.exceptionaldevs.muzyka.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by darken on 30.03.14.
 */
public class PlaylistTracksAdapter extends ContentAdapter<Track, PlaylistTracksAdapter.ViewHolder> {

    // we need to hold on to an activity ref for the shared element transitions :/
    // Shh bby is ok
    private final MuzikServiceActivity mHost;

    public PlaylistTracksAdapter(MuzikServiceActivity hostActivity) {
        super(hostActivity);
        this.mHost = hostActivity;
    }

    @Override
    public void onBindBasicItemView(final ViewHolder holder, int position) {
        holder.bind(getItem(position), mHost);
    }

    @Override
    public ViewHolder onCreateBasicItemViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_line_default_lists, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public static class ViewHolder extends SDMViewHolder {
        public String mBoundString;

        public final View mView;
        public final ImageView mImageView;
        public final TextView mTextView;
        @BindView(R.id.item_add_track) View queueTrack;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.item_image);
            mTextView = (TextView) view.findViewById(R.id.item_primary_text);
            ButterKnife.bind(this, view);
        }

        void bind(final Track track, final MuzikServiceActivity serviceActivity) {
            mBoundString = track.getTitle();
            mTextView.setText(mBoundString);

            ViewUtils.setOvalOutlineProvider(mImageView);
            Glide.with(mImageView.getContext())
                    .load(CoverModelMetaVisitor.visitAcceptor(track, getContext()).getModel())
                    .thumbnail(Glide.with(mImageView.getContext()).load(R.drawable.material_colors_stock))
                    .into(mImageView);

            queueTrack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    serviceActivity.registerBinderCustomer(new BinderCustomer<MusicBinder>() {
                        @Override
                        public void onBinderAvailable(MusicBinder binder) {
                            binder.getQueueHandler().add(track.getJXObject());
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    QuickAnimationFactory.elasticPop(v);
                                }
                            });
                        }
                    });
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }
}
