package com.exceptionaldevs.muzyka.content;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.playlists.Playlist;
import com.exceptional.musiccore.library.playlists.PlaylistsLoader;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.ui.widget.FloatLabelLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by darken on 19.05.2015.
 */
public class AddToPlaylistDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<List<Playlist>>, AdapterView.OnItemClickListener, TextWatcher {
    private final List<Uri> mUrisToAdd = new ArrayList<>();
    private ListView mListView;
    private FloatLabelLayout mFloatLabelLayout;
    private EditText mNewPlaylistInput;

    public static AddToPlaylistDialog instantiateFromTracks(List<Track> tracks) {
        ArrayList<Uri> uriList = new ArrayList<>();
        for (Track track : tracks)
            uriList.add(track.getLibrarySource());
        return instantiate(uriList);
    }

    public static AddToPlaylistDialog instantiate(ArrayList<Uri> uris) {
        AddToPlaylistDialog dialog = new AddToPlaylistDialog();
        Bundle extras = new Bundle();
        extras.putParcelableArrayList("uris", uris);
        dialog.setArguments(extras);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<Uri> uris = getArguments().getParcelableArrayList("uris");
        mUrisToAdd.addAll(uris);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_addtoplaylist_layout, null);
        mListView = (ListView) layout.findViewById(R.id.playlists_list);
        mListView.setOnItemClickListener(this);
        mFloatLabelLayout = (FloatLabelLayout) layout.findViewById(R.id.fll_input);
        mFloatLabelLayout.setVisibility(View.GONE);
        mNewPlaylistInput = (EditText) layout.findViewById(R.id.et_input);
        mNewPlaylistInput.addTextChangedListener(this);

        final AlertDialog playlistDialog = new AlertDialog.Builder(getActivity(), R.style.DialogStyle)
                .setView(layout)
                .setPositiveButton(R.string.button_add, mPositiveClick)
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton(R.string.button_new, mNeutralClick)
                .create();
        return playlistDialog;
    }

    public AlertDialog getDialog() {
        return (AlertDialog) super.getDialog();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        getDialog().getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
        // Hack, because we don't wanted the dialog to be dismissed.
        getDialog().getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNeutralClick.onClick(getDialog(), DialogInterface.BUTTON_NEUTRAL);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LoaderArgs.Builder loaderArgsBuilder = new LoaderArgs.Builder().forUri(Playlist.getUriForAllPlaylists());
        getLoaderManager().restartLoader(1, loaderArgsBuilder.buildBundle(), this);
    }

    private final DialogInterface.OnClickListener mPositiveClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // We have to create a new playlist
            String playlistName = mNewPlaylistInput.getText().toString().trim();
            try {
                Playlist playlist = Playlist.createPlaylistWithName(getActivity(), playlistName);
                addToPlaylist(playlist);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void addToPlaylist(Playlist playlist) {
        playlist.addTracks(getContext(), mUrisToAdd);
        dismiss();
    }

    private final DialogInterface.OnClickListener mNeutralClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mListView.setVisibility(View.GONE);
            mFloatLabelLayout.setVisibility(View.VISIBLE);
            getDialog().getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.GONE);
            getDialog().getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        }
    };

    @Override
    public Loader<List<Playlist>> onCreateLoader(int id, Bundle args) {
        Loader<?> loader = new PlaylistsLoader(getActivity(), args) {
            @Override
            public int supplyDefaultPlaylistIcon() {
                return R.drawable.ic_playlist;
            }
        };
        return (Loader<List<Playlist>>) loader;
    }

    @Override
    public void onLoadFinished(Loader<List<Playlist>> loader, List<Playlist> data) {
        ATPAdapter adapter = new ATPAdapter();
        adapter.setData(data);
        mListView.setAdapter(adapter);
        if (adapter.isEmpty())
            getDialog().getButton(DialogInterface.BUTTON_NEUTRAL).callOnClick();
    }

    @Override
    public void onLoaderReset(Loader<List<Playlist>> loader) {
        mListView.setAdapter(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Playlist playlist = (Playlist) mListView.getItemAtPosition(position);
        addToPlaylist(playlist);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String input = s.toString().trim().toLowerCase(Locale.getDefault());
        getDialog().getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!Playlist.doesPlaylistExist(getActivity(), input) && !input.isEmpty());
        // TODO display a hint for the user if invalid input?
    }

    static class ATPAdapter extends BaseAdapter {
        private final List<Playlist> mPlaylistList = new ArrayList<>();

        public void setData(List<Playlist> data) {
            mPlaylistList.clear();
            if (data != null)
                mPlaylistList.addAll(data);
        }

        @Override
        public int getCount() {
            return mPlaylistList.size();
        }

        @Override
        public Playlist getItem(int position) {
            return mPlaylistList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mPlaylistList.get(position).hashCode();
        }


        static class ViewHolder {
            TextView mPlaylist;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_addtoplaylist_line, parent, false);
                holder = new ViewHolder();
                holder.mPlaylist = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Playlist item = getItem(position);
            holder.mPlaylist.setText(item.getName());
            return convertView;
        }
    }
}
