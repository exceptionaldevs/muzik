package com.exceptional.musiccore.glide.lfm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.glide.persistence.ImageDB;
import com.exceptional.musiccore.lfm.LFMEndpoint;
import com.exceptional.musiccore.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sebnap on 21.12.15.
 */
public class LFMImageLoader extends BaseGlideUrlLoader<LFMRequest> {

    private final ModelLoader<File, InputStream> mfilePathLoader;
    private final LFMEndpoint mLFMEndpoint;
    private final ImageDB mImageDB;
    private final SafeKeyGenerator mSafeKeyGenerator;
    private final File mCacheDir;


    public LFMImageLoader(ModelLoader<GlideUrl, InputStream> concreteLoader,
                          ModelLoader<File, InputStream> filePathLoader,
                          LFMEndpoint lfmEndpoint,
                          ImageDB imageDB,
                          SafeKeyGenerator skg,
                          File cacheDir) {
        this(concreteLoader, filePathLoader, null, lfmEndpoint, imageDB, skg, cacheDir);
    }

    public LFMImageLoader(ModelLoader<GlideUrl, InputStream> concreteLoader,
                          ModelLoader<File, InputStream> filePathLoader,
                          @Nullable ModelCache<LFMRequest, GlideUrl> modelCache,
                          LFMEndpoint lFMEndpoint,
                          ImageDB imageDB,
                          SafeKeyGenerator skg,
                          File cacheDir) {
        super(concreteLoader, modelCache);
        mfilePathLoader = filePathLoader;
        mLFMEndpoint = lFMEndpoint;
        mImageDB = imageDB;
        mSafeKeyGenerator = skg;
        mCacheDir = cacheDir;
    }

    private LoadData<InputStream> wrapFetcher(LoadData<InputStream> loadData, LFMRequest lfmRequest) {
        return new LoadData<>(
                loadData.sourceKey, loadData.alternateKeys,
                new PersistenceFetcherWrapper(
                        loadData.fetcher,
                        loadData.sourceKey,
                        mImageDB,
                        lfmRequest,
                        mSafeKeyGenerator,
                        mCacheDir));
    }

    /*
        We wrap the fetcher, such that we can wrap the DataCallback<> in loadData.
        We intercept the given InputStream, save it in the cache dir and callback
        the original callback with a FileInputStream from the newly created file instead!
        I consider this hacky but it works
     */
    public static class PersistenceFetcherWrapper implements DataFetcher<InputStream> {
        private static final String LFM_PREFIX = "lfm-";
        private final DataFetcher<InputStream> mFetcher;
        private final LFMRequest mLfmRequest;
        private final Key mSourceKey;
        private final SafeKeyGenerator mSafeKeyGenerator;
        private final ImageDB mImageDB;
        private final File mCacheDir;

        public class WrappedCallback implements DataCallback<InputStream> {
            final DataCallback<InputStream> mWrapee;

            public WrappedCallback(DataCallback<InputStream> cal) {
                mWrapee = cal;
            }

            @Override
            public void onDataReady(@Nullable InputStream data) {
                if (data != null) {
                    String filename = LFM_PREFIX + mSafeKeyGenerator.getSafeKey(mSourceKey);
                    File outputFile = new File(mCacheDir, filename);
                    try {
                        Utils.copyInputStreamToFile(data, outputFile);
                        data = new FileInputStream(outputFile);
                        // if everything went fine, lets save it to the database
                        mLfmRequest.insertFilePathToDB(mImageDB, outputFile.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        // What happens with an IOException in the middle of the file?
                        // How to deal with it? And how probable is that?
                        mWrapee.onDataReady(data);
                    }
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                mWrapee.onLoadFailed(e);
            }
        }

        public PersistenceFetcherWrapper(DataFetcher<InputStream> fetcher, Key sourceKey, ImageDB imageDB,
                                         LFMRequest lfmRequest, SafeKeyGenerator skg, File cacheDir) {
            this.mFetcher = fetcher;
            this.mSourceKey = sourceKey;
            this.mImageDB = imageDB;
            this.mLfmRequest = lfmRequest;
            this.mSafeKeyGenerator = skg;
            this.mCacheDir = cacheDir;
        }


        @Override
        public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
            mFetcher.loadData(priority, new WrappedCallback((DataCallback<InputStream>) callback));
        }

        @Override
        public void cleanup() {
            mFetcher.cleanup();
        }

        @Override
        public void cancel() {
            mFetcher.cancel();
        }

        @Override
        public Class<InputStream> getDataClass() {
            return mFetcher.getDataClass();
        }

        @Override
        public DataSource getDataSource() {
            return mFetcher.getDataSource();
        }
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(LFMRequest lfmRequest, int width, int height, Options options) {
        //TODO: snyc only other WIFI?

        //check the persistence layer
        String result = lfmRequest.getFilePathFromDB(mImageDB);
        if (result != null) {
            //we have a FilePath in our DB
            File file = new File(result);

            if (file.isFile() && file.canRead()) {
                LoadData<InputStream> concreteLoaderData = mfilePathLoader.buildLoadData(file, width, height, options);
                return new LoadData<>(concreteLoaderData.sourceKey, concreteLoaderData.fetcher);
            }
        }
        // download file, wrap such that file gets saved
        return wrapFetcher(super.buildLoadData(lfmRequest, width, height, options), lfmRequest);
    }

    //this only gets called if the tuple (LFMRequest, GlideUrl) is not in the ModelCache
    @Override
    protected String getUrl(LFMRequest lfmRequest, int width, int height, Options options) {
        //check persistence to get image url
        String result = lfmRequest.getImageUrlFromDB(mImageDB);
        if (result != null) {
            return result; //we have an URL in our DB
        }

        // or access LFM API
        String url = lfmRequest.getImageUrl(mLFMEndpoint, true, mImageDB); //this will trigger a synchronized network op
        if (url != null) {
            return url;
        }

        //TODO: What happens if url == null?
        return null;
    }

    @Override
    public boolean handles(LFMRequest lfmRequest) {
        return true;
    }


    public static class Factory implements ModelLoaderFactory<LFMRequest, InputStream> {
        private final ModelCache<LFMRequest, GlideUrl> modelCache = new ModelCache<>(500);
        private final SafeKeyGenerator mSafeKeyGenerator = new SafeKeyGenerator();
        private Context mContext;

        public Factory(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public ModelLoader<LFMRequest, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new LFMImageLoader(
                    multiFactory.build(GlideUrl.class, InputStream.class),
                    multiFactory.build(File.class, InputStream.class),
                    modelCache,
                    ((MusicCoreApplication) mContext.getApplicationContext()).getLFMServiceEndpoint(),
                    ImageDB.getInstance(mContext.getApplicationContext()),
                    mSafeKeyGenerator,
                    mContext.getCacheDir()
            );
        }

        @Override
        public void teardown() {
        }
    }
}
