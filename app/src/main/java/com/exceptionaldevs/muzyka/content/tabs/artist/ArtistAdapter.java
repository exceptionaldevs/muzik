package com.exceptionaldevs.muzyka.content.tabs.artist;

import android.app.ActivityOptions;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptional.musiccore.library.artists.Artist;
import com.exceptionaldevs.muzyka.MuzikServiceActivity;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.content.sheets.ContentSheet;
import com.exceptionaldevs.muzyka.content.sheets.artist.ArtistSheet;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;


/**
 * Created by darken on 30.03.14.
 */
public class ArtistAdapter extends ContentAdapter<Artist, ArtistAdapter.ViewHolder> {

    // we need to hold on to an activity ref for the shared element transitions :/
    private final MuzikServiceActivity mHost;

    public ArtistAdapter(MuzikServiceActivity hostActivity) {
        super(hostActivity);
        this.mHost = hostActivity;
    }

    @Override
    public void onBindBasicItemView(final ViewHolder holder, int position) {
        holder.bind(getItem(position), mHost);
    }

    @Override
    public ViewHolder onCreateBasicItemViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grid_circle, parent, false);
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

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.item_image);
            mTextView = (TextView) view.findViewById(R.id.item_primary_text);
        }

        void bind(final Artist artist, final MuzikServiceActivity serviceActivity) {
            mBoundString = artist.getArtistName();
            mTextView.setText(mBoundString);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mImageView.setTransitionName(view.getResources().getString(R.string.transition_libitem));

                    Intent intent = new Intent();
                    intent.setClass(serviceActivity, ArtistSheet.class);
                    intent.putExtra(ContentSheet.EXTRA_CONTENT_ITEM, artist);
                    ActivityOptions options =
                            ActivityOptions.makeSceneTransitionAnimation(serviceActivity,
                                    Pair.create((View) mImageView, getString(R.string.transition_libitem)));
                    serviceActivity.startActivity(intent, options.toBundle());
                }
            });

            Glide.with(mImageView.getContext())
                    .load(CoverModelMetaVisitor.visitAcceptor(artist, getContext()).getModel())
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(Glide.with(mImageView.getContext()).load(R.drawable.material_colors_stock))
                    .into(mImageView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }
}
