package com.exceptional.musiccore.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.exceptional.musiccore.MusicCoreApplication;

public class GenerateCoverModelLoader implements ModelLoader<GenerateCover, GenerateCover> {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "GenerateCoverModelLoader";

    @Nullable
    @Override
    public LoadData<GenerateCover> buildLoadData(GenerateCover generateCover, int width, int height, Options options) {
        return new LoadData<>(generateCover, new PassThroughFakeFetcher(generateCover));
    }

    @Override
    public boolean handles(GenerateCover generateCover) {
        return true;
    }


    //TODO: refactor as this fetcher does more or less noting only callbacking the callback
    private static class PassThroughFakeFetcher implements DataFetcher<GenerateCover> {

        private final GenerateCover mGenerateCover;

        public PassThroughFakeFetcher(GenerateCover generateCover) {
            mGenerateCover = generateCover;
        }

        @Override
        public void loadData(Priority priority, DataCallback<? super GenerateCover> callback) {
            callback.onDataReady(mGenerateCover);
        }

        @Override
        public void cleanup() {

        }

        @Override
        public void cancel() {

        }

        @Override
        public Class<GenerateCover> getDataClass() {
            return GenerateCover.class;
        }

        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    public static class Factory implements ModelLoaderFactory<GenerateCover, GenerateCover> {

        @NonNull
        @Override
        public ModelLoader<GenerateCover, GenerateCover> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new GenerateCoverModelLoader();
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }


}
