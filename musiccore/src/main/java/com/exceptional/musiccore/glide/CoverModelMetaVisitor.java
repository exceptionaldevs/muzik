package com.exceptional.musiccore.glide;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.exceptional.musiccore.engine.metadata.JXMetaAcceptor;
import com.exceptional.musiccore.engine.metadata.JXMetaFile;
import com.exceptional.musiccore.engine.metadata.JXMetaVisitor;
import com.exceptional.musiccore.glide.lfm.LFMArtistRequest;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.albums.Album;
import com.exceptional.musiccore.library.artists.Artist;
import com.exceptional.musiccore.library.device.DirectoryItem;
import com.exceptional.musiccore.library.device.FileItem;
import com.exceptional.musiccore.library.playlists.Playlist;
import com.exceptional.musiccore.library.tracks.Track;


public class CoverModelMetaVisitor implements JXMetaVisitor<CoverModelMetaVisitor.GlideModelWrapper> {
    Context mContext;

    public CoverModelMetaVisitor(Context context) {
        mContext = context;
    }

    public static GlideModelWrapper visitAcceptor(JXMetaAcceptor acceptor, Context context) {
        return acceptor.accept(new CoverModelMetaVisitor(context));
    }

    public static class GlideModelWrapper<T> {
        final T model;

        public GlideModelWrapper(T model) {
            this.model = model;
        }

        public T getModel() {
            return model;
        }
    }

    // GlideWrapper erstellen der Backup strategien fahren kann also erst lfm anfragen, file, dann generieren
    // API new GlideWrapper(Glide.with(context).load().bla, JXMetaFile)
    // consider Glide.with().load(JXMetafile) triggerd loader der das macht was new GlideWrapper(Glide.with(context).load().bla, JXMetaFile) machen soll
    // Glide.load(...) kann mit zweiten .load(...) überschrieben werden genauso wie .as das auch macht

    @Override
    public GlideModelWrapper visit(JXMetaFile jxMetaFile) {
        boolean standardAvailabe = useSameProcedureAsEveryYear(jxMetaFile.getCoverSource());
        if (standardAvailabe) {
            return new GlideModelWrapper<Uri>(jxMetaFile.getCoverSource());
        } else {
            String l = jxMetaFile.getTrackName();
            if (l == null) l = "";
            return new GlideModelWrapper<>(new GenerateCover(l, GenerateCover.colorFromHash(l.hashCode())));
        }
    }

    @Override
    public GlideModelWrapper visit(Artist libraryItem) {
        if (libraryItem.getArtistName() != null) {
            return new GlideModelWrapper<>(new LFMArtistRequest(libraryItem.getArtistName()));
        } else {
            String fallback = String.valueOf(libraryItem.getArtistId());
            return new GlideModelWrapper<>(new GenerateCover(fallback, GenerateCover.colorFromHash(fallback.hashCode())));
        }
    }

    @Override
    public GlideModelWrapper visit(Album libraryItem) {
        boolean standardAvailabe = useSameProcedureAsEveryYear(libraryItem.getCoverSource());

        if (standardAvailabe) {
            return new GlideModelWrapper<Uri>(libraryItem.getCoverSource());
        } else {
            String l = libraryItem.getAlbumName();
            if (l == null) l = "";
            return new GlideModelWrapper<>(new GenerateCover(l, GenerateCover.colorFromHash(l.hashCode())));
        }
    }

    @Override
    public GlideModelWrapper visit(Playlist libraryItem) {
        boolean standardAvailabe = useSameProcedureAsEveryYear(libraryItem.getCoverSource());
        if (standardAvailabe) {
            return new GlideModelWrapper<Uri>(libraryItem.getCoverSource());
        } else {
            String l = libraryItem.getName();
            if (l == null) l = "";
            return new GlideModelWrapper<>(new GenerateCover(l, GenerateCover.colorFromHash(l.hashCode())));
        }

    }

    @Override
    public GlideModelWrapper visit(Track libraryItem) {
        boolean standardAvailabe = useSameProcedureAsEveryYear(libraryItem.getCoverSource());
        if (standardAvailabe) {
            return new GlideModelWrapper<Uri>(libraryItem.getCoverSource());
        } else {
            String l = libraryItem.getTitle();
            if (l == null) l = "";
            return new GlideModelWrapper<>(new GenerateCover(l, GenerateCover.colorFromHash(l.hashCode())));
        }

    }

    @Override
    public GlideModelWrapper visit(LibraryItem libraryItem) {
        boolean standardAvailabe = useSameProcedureAsEveryYear(libraryItem.getCoverSource());
        if (standardAvailabe) {
            return new GlideModelWrapper<Uri>(libraryItem.getCoverSource());
        } else {
            String l = libraryItem.getLibrarySource().getLastPathSegment();
            if (l == null) l = "";
            return new GlideModelWrapper<>(new GenerateCover(l, GenerateCover.colorFromHash(l.hashCode())));
        }
    }

    @Override
    public GlideModelWrapper visit(FileItem libraryItem) {
        boolean standardAvailabe = useSameProcedureAsEveryYear(libraryItem.getCoverSource());
        if (standardAvailabe) {
            return new GlideModelWrapper<Uri>(libraryItem.getCoverSource());
        } else {
            String l = libraryItem.getFile().getName();
            if (l == null) l = "";
            return new GlideModelWrapper<>(new GenerateCover(l, GenerateCover.colorFromHash(l.hashCode())));
        }
    }

    @Override
    public GlideModelWrapper visit(DirectoryItem libraryItem) {
        boolean standardAvailabe = useSameProcedureAsEveryYear(libraryItem.getCoverSource());
        if (standardAvailabe) {
            return new GlideModelWrapper<Uri>(libraryItem.getCoverSource());
        } else {
            String l = libraryItem.getDirectoryName();
            if (l == null) l = "";
            return new GlideModelWrapper<>(new GenerateCover(l, GenerateCover.colorFromHash(l.hashCode())));
        }
    }

    /* Media store has cover */
    private boolean fromMediaStore(Uri coverSource) {
        return coverSource.getScheme().equals(ContentResolver.SCHEME_CONTENT)
                && CoverUtil.quickCheckCoverExistenceInProvider(mContext, coverSource);
    }

    /* Android resource, i.e. drawable */
    private boolean fromAndroidResource(Uri coverSource) {
        return coverSource.getScheme().equals(ContentResolver.SCHEME_ANDROID_RESOURCE);
    }

    /* Cover source is a file, let's hope it's an image */
    private boolean fromFile(Uri coverSource) {
        return coverSource.getScheme().equals(ContentResolver.SCHEME_FILE);
    }

    private boolean useSameProcedureAsEveryYear(Uri coverSource) {
        return fromMediaStore(coverSource) || fromAndroidResource(coverSource) || fromFile(coverSource);
    }
}