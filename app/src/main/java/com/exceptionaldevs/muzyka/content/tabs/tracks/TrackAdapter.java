package com.exceptionaldevs.muzyka.content.tabs.tracks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.exceptional.musiccore.BinderCustomer;
import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptionaldevs.muzyka.MuzikServiceActivity;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.ui.widget.CircleImageView;
import com.exceptionaldevs.muzyka.ui.widget.QuickAnimationFactory;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by darken on 30.03.14.
 */
public class TrackAdapter extends ContentAdapter<Track, TrackAdapter.ViewHolder> {

    // we need to hold on to an activity ref for the shared element transitions :/
    private final MuzikServiceActivity mHost;

    public TrackAdapter(MuzikServiceActivity hostActivity) {
        super(hostActivity);
        this.mHost = hostActivity;
    }

    @Override
    public void onBindBasicItemView(ViewHolder holder, int position) {
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
        public final View mView;
        @BindView(R.id.item_image) public CircleImageView mImageView;
        @BindView(R.id.item_primary_text) public TextView primaryText;
        @BindView(R.id.item_secondary_text) public TextView secondaryText;
        @BindView(R.id.item_add_track) View queueTrack;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }

        void bind(final Track track, final MuzikServiceActivity serviceActivity) {
            primaryText.setText(track.getTitle());
            secondaryText.setText(track.getArtistName());

            Glide.with(getContext())
                    .load(CoverModelMetaVisitor.visitAcceptor(track, getContext()).getModel())
                    .apply(RequestOptions.circleCropTransform())
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
            return super.toString() + " '" + primaryText.getText();
        }
    }
}
