package com.exceptionaldevs.muzyka.utils;

import android.content.Context;
import android.net.Uri;
import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXObjectFactory;
import com.exceptional.musiccore.engine.queue.Queue;
import com.exceptional.musiccore.engine.queuetasks.JXSnackTask;
import com.exceptional.musiccore.engine.queuetasks.JXTask;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.playlists.Playlist;
import com.exceptional.musiccore.library.tracks.TrackLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebnap on 26.01.16.
 */
public class QueueHelper {

    public static String addToQueue(Context context, MusicBinder binder, JXTask.JXTaskCallback callback,
                                    final Uri uri, String message, String action) {
        List<Uri> tempList = new ArrayList<>();
        tempList.add(uri);
        return addToQueue(context, binder, callback, tempList, message, action);
    }

    public static String replaceQueue(final Context context, final MusicBinder binder, JXTask.JXTaskCallback callback,
                                    final List<Uri> uris, String message, String action) {
        JXTask task = new JXSnackTask(message, action) {
            @Override
            public void onDo() {
                List<Uri> resolvedUris = new ArrayList<>();
                for (Uri uri : uris)
                    resolvedUris.addAll(TrackLoader.resolveToTrackUris(context, new LoaderArgs.Builder().forUri(uri).build()));
                JXObjectFactory jxObjectFactory = new JXObjectFactory(context);
                final List<JXObject> toAdd = jxObjectFactory.make(resolvedUris);
                final Queue queue = binder.getQueueHandler();
                final List<JXObject> toSave =  queue.getTracks();
                queue.clear();
                queue.add(toAdd);
                this.setUndoTask(new JXTask() {
                    @Override
                    public void onDo() {
                        queue.removeLastOccurrenceOf(toAdd);
                        queue.add(toSave);
                    }
                });
            }
        };
        return execute(binder, task, callback);
    }

    public static String addToQueue(final Context context, final MusicBinder binder, JXTask.JXTaskCallback callback,
                                    final List<Uri> uris, String message, String action) {
        JXTask task = new JXSnackTask(message, action) {
            @Override
            public void onDo() {
                List<Uri> resolvedUris = new ArrayList<>();
                for (Uri uri : uris)
                    resolvedUris.addAll(TrackLoader.resolveToTrackUris(context, new LoaderArgs.Builder().forUri(uri).build()));
                JXObjectFactory jxObjectFactory = new JXObjectFactory(context);
                final List<JXObject> toAdd = jxObjectFactory.make(resolvedUris);
                final Queue queue = binder.getQueueHandler();
                queue.add(toAdd);
                this.setUndoTask(new JXTask() {
                    @Override
                    public void onDo() {
                        queue.removeLastOccurrenceOf(toAdd);
                    }
                });
            }
        };
        return execute(binder, task, callback);
    }

    public static String addToPlaylist(final Context context, MusicBinder binder, JXTask.JXTaskCallback callback,
                                       final List<Playlist> playlists, final List<Uri> uris, String message, String action) {
        JXTask task = new JXSnackTask(message, action) {
            @Override
            public void onDo() {
                final List<Uri> resolvedUris = new ArrayList<>();
                for (Uri uri : uris)
                    resolvedUris.addAll(TrackLoader.resolveToTrackUris(context, new LoaderArgs.Builder().forUri(uri).build()));
                for (Playlist playlist : playlists)
                    playlist.addTracks(context, resolvedUris);
                this.setUndoTask(new JXTask() {
                    @Override
                    public void onDo() {
                        for (Playlist playlist : playlists)
                            playlist.removeTracks(context, resolvedUris);
                    }
                });
            }
        };
        return execute(binder, task, callback);
    }

    /**
     * returns tag for created task
     *
     * @param binder
     * @param task
     * @param callback
     * @return
     */
    public static String execute(MusicBinder binder, JXTask task, JXTask.JXTaskCallback callback) {
        return binder.getTasker().execute(task, callback);
    }

    public static String clearQueue(final MusicBinder binder, JXTask.JXTaskCallback callback, String message, String action) {
        JXTask task = new JXSnackTask(message, action) {
            @Override
            public void onDo() {
                final Queue queue = binder.getQueueHandler();
                final List<JXObject> toSave =  queue.getTracks();
                queue.clear();
                this.setUndoTask(new JXTask() {
                    @Override
                    public void onDo() {
                        queue.add(toSave);
                    }
                });
            }
        };
        return execute(binder, task, callback);
    }

}
