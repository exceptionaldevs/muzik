package com.exceptional.musiccore.library.device;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.util.Log;

import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXObjectFactory;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.LibraryLoader;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.utils.Logy;
import com.exceptional.musiccore.utils.UriHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by darken on 26.03.14.
 */
public abstract class DeviceLoader extends LibraryLoader<List<LibraryItem>> {

    public static final String HIDDEN_PREFIX = "";
    protected static final int FILE_OBSERVER_MASK = FileObserver.CREATE
            | FileObserver.DELETE | FileObserver.DELETE_SELF
            | FileObserver.MOVED_FROM | FileObserver.MOVED_TO
            | FileObserver.MODIFY | FileObserver.MOVE_SELF;
    private static final Comparator<File> sDirFirstSorter = new Comparator<File>() {

        public int compare(File f1, File f2) {
            if (f1.isDirectory() == f2.isDirectory()) {
                return 0;
            } else if (f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            } else {
                return 1;
            }
        }
    };
    private static final Comparator<File> sNameSorter = new Comparator<File>() {

        public int compare(File f1, File f2) {
            return f1.compareTo(f2);
        }
    };
    private static final String TAG = "JX:DeviceBrowserLoader";

    private FileObserver mFileObserver;
    private List<LibraryItem> mData;

    public DeviceLoader(Context context, Bundle args) {
        super(context, LoaderArgs.fromBundle(args));
    }

    @Override
    public void deliverResult(List<LibraryItem> data) {
        if (isReset()) {
            releaseResources();
            return;
        }

        List<LibraryItem> oldData = this.mData;
        this.mData = data;

        if (isStarted())
            super.deliverResult(data);

        if (oldData != null && oldData != data)
            releaseResources();
    }

    @Override
    protected void onStartLoading() {
        if (mData != null)
            deliverResult(mData);

        if (mFileObserver == null) {
            mFileObserver = new FileObserver(getLoaderArgs().getUri().getPath(), FILE_OBSERVER_MASK) {
                @Override
                public void onEvent(int event, String path) {
                    onContentChanged();
                }
            };
        }
        mFileObserver.startWatching();

        if (takeContentChanged() || mData == null)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (mData != null) {
            releaseResources();
            mData = null;
        }
    }

    @Override
    public void onCanceled(List<LibraryItem> data) {
        super.onCanceled(data);
        releaseResources();
    }

    protected void releaseResources() {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
            mFileObserver = null;
        }
    }

    @DrawableRes
    public abstract int supplyDefaultFolderIcon();

    @DrawableRes
    public abstract int supplyDefaultFileIcon();

    // TODO performance is not as good as other loaders, it's a complex load, but maybe we can speed it up.
    @Override
    public List<LibraryItem> loadInBackground() {
        Logy.d(TAG, "loadInBackground start...");
        long dur = System.currentTimeMillis();
        File[] _currentListing = new File(getLoaderArgs().getUri().getPath()).listFiles();
        if (_currentListing == null)
            return new ArrayList<>();

        List<File> currentListing = Arrays.asList(_currentListing);

        Collections.sort(currentListing, sNameSorter);
        Collections.sort(currentListing, sDirFirstSorter);

        List<LibraryItem> directories = new ArrayList<LibraryItem>();

        List<String> filesToLookup = new ArrayList<>();
        for (File file : currentListing) {
            if (file.isDirectory()) {
                DirectoryItem directoryItem = new DirectoryItem(Uri.parse(file.getAbsolutePath()));

                directoryItem.setCoverSource(UriHelper.drawableToUri(getContext(), supplyDefaultFolderIcon()));

                directoryItem.setDirectoryName(file.getName());
                File[] dirContent = file.listFiles();
                directoryItem.setItemCount(dirContent != null ? dirContent.length : 0);
                directories.add(directoryItem);
            } else if (file.isFile()) {
                // we process them later
                filesToLookup.add(file.getAbsolutePath());
            } else {
                // Neither file, nor directory? WTF is it!
                // Might be a symbolic link?
                Log.w(TAG, "Wtf is:" + file.getAbsolutePath());
            }
        }

        List<LibraryItem> files = new ArrayList<>();
        if (!filesToLookup.isEmpty()) {
            // What information do we lose when limiting to 999
            // Matthias: Seeing only folder contents up to 999 items

            List<String> chunkList = new ArrayList<>();
            // Process files/folders in chunks of MAX 999
            while (!filesToLookup.isEmpty() || !chunkList.isEmpty()) {
                if (chunkList.size() < 999 && !filesToLookup.isEmpty()) {
                    chunkList.add(filesToLookup.remove(0));
                } else {
                    StringBuilder selectionBuilder = new StringBuilder();
                    selectionBuilder.append(MediaStore.Audio.Media.DATA + " IN (");

                    int size = chunkList.size();
                    for (int i = 0; i < size; i++) {
                        selectionBuilder.append("?");
                        if (i < size - 1)
                            selectionBuilder.append(",");
                    }
                    selectionBuilder.append(")");

                    String[] selectionArgs = new String[chunkList.size()];
                    for (int i = 0; i < size; i++)
                        selectionArgs[i] = chunkList.get(i);
                    chunkList.clear();

                    Uri queryTarget = MediaStore.Audio.Media.getContentUriForPath(getLoaderArgs().getUri().getPath());
                    Cursor cursor = getContext().getContentResolver().query(queryTarget, JXObjectFactory.getTrackProjection(), selectionBuilder.toString(), selectionArgs, null);

                    if (cursor != null) {
                        Logy.d(TAG, "cursorSize:" + cursor.getCount());
                        Map<Uri, JXObject> matchedJXObjects = JXObjectFactory.getJXObjectsFromCursor(cursor);

                        for (JXObject jxObject : matchedJXObjects.values())
                            files.add(new Track(jxObject));

                        for (String nonMediaFile : chunkList) {
                            Logy.d(TAG, "unknown-file:" + nonMediaFile);
                            FileItem fileItem = new FileItem(Uri.fromFile(new File(nonMediaFile)));
                            fileItem.setCoverSource(UriHelper.drawableToUri(getContext(), supplyDefaultFileIcon()));
                            files.add(fileItem);
                        }
                        cursor.close();
                    }
                }
            }

        }
        List<LibraryItem> returnData = new ArrayList<>();
        returnData.addAll(directories);
        returnData.addAll(files);
        Logy.d(TAG, "loadInBackground done:" + (System.currentTimeMillis() - dur));
        return returnData;
    }

    public static final String[] PROJECTION_PLAYLIST = new String[]{
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME,
            MediaStore.Audio.Playlists.DATA
    };

    public static Uri getUriForFile(ContentResolver resolver, File path) {
        Logy.d(TAG, "queryByFilePath(" + path.getAbsolutePath() + ")");
        Uri result = null;

        Uri queryTarget = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID
        };
        String selection = MediaStore.Audio.Media.DATA + "=?";
        String[] selectionArgs = new String[]{path.getAbsolutePath()};
        String sortOrder = null;

        Cursor cursor = null;
        try {
            cursor = resolver.query(queryTarget, projection, selection, selectionArgs, sortOrder);
            if (cursor != null && cursor.getCount() > 0) {
                // Move to last as we are interested in latest entry in database
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    result = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                    Logy.d(TAG, "success: " + result);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return result;
    }

    public static File getFileForUri(ContentResolver resolver, Uri uri) {
        Logy.d(TAG, "getFileForUri(" + uri.toString() + ")");
        File result = null;
        String[] projection = new String[]{
                MediaStore.Audio.Media.DATA
        };

        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, projection, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                // Move to last as we are interested in latest entry in database
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    result = new File((Uri.parse(data).getPath()));
                    Logy.d(TAG, "success: " + result.getAbsolutePath());
                }

            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        // TODO if this is an unknown track we should think about adding it to the provider
        return result;
    }
}