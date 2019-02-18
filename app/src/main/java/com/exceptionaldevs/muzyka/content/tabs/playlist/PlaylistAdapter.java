package com.exceptionaldevs.muzyka.content.tabs.playlist;

import android.app.Activity;
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
import com.exceptional.musiccore.library.playlists.Playlist;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.content.sheets.ContentSheet;
import com.exceptionaldevs.muzyka.content.sheets.playlist.PlaylistSheet;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;


/**
 * Created by darken on 30.03.14.
 */
public class PlaylistAdapter extends ContentAdapter<Playlist, PlaylistAdapter.ViewHolder> {

    // we need to hold on to an activity ref for the shared element transitions :/
    private final Activity mHost;

    public PlaylistAdapter(Activity hostActivity) {
        super(hostActivity);
        this.mHost = hostActivity;
    }

    @Override
    public void onBindBasicItemView(final ViewHolder holder, int position) {
        final Playlist libraryItem = getItem(position);

        holder.mBoundString = libraryItem.getName();
        holder.mTextView.setText(holder.mBoundString);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mImageView.setTransitionName(view.getResources().getString(R.string.transition_libitem));

                Intent intent = new Intent();
                intent.setClass(mHost, PlaylistSheet.class);
                intent.putExtra(ContentSheet.EXTRA_CONTENT_ITEM, libraryItem);

                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(mHost,
                            Pair.create((View) holder.mImageView, mHost.getString(R.string.transition_libitem)));
                mHost.startActivity(intent, options.toBundle());
            }
        });

        Glide.with(holder.mImageView.getContext())
                .load(CoverModelMetaVisitor.visitAcceptor(libraryItem, mContext).getModel())
                .thumbnail(Glide.with(holder.mImageView.getContext()).load(R.drawable.material_colors_stock))
                .apply(RequestOptions.circleCropTransform())
                .into(holder.mImageView);
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

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }
}
