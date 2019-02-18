package com.exceptional.musiccore.library;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.exceptional.musiccore.utils.Logy;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class LoaderArgs implements Parcelable {
    public static final Creator<LoaderArgs> CREATOR = new Creator<LoaderArgs>() {
        @Override
        public LoaderArgs createFromParcel(Parcel in) {
            return new LoaderArgs(in);
        }

        @Override
        public LoaderArgs[] newArray(int size) {
            return new LoaderArgs[size];
        }
    };
    private static final String ARG_KEY = "loaderargs";
    private Uri mUri;
    private int mResultsLimit = -1;
    private String mSelectionStatement;
    private String[] mSelectionArgs;

    private LoaderArgs() {
    }

    protected LoaderArgs(@NonNull Parcel in) {
        mUri = in.readParcelable(Uri.class.getClassLoader());
        mSelectionStatement = in.readString();
        if (in.readInt() == 1)
            in.readStringArray(mSelectionArgs);
        mResultsLimit = in.readInt();
    }

    public static LoaderArgs fromBundle(@NonNull Bundle bundle) {
        return bundle.getParcelable(ARG_KEY);
    }

    @NonNull
    public Uri getUri() {
        return mUri;
    }

    /**
     * >=0 for limit
     * -1 for unlimited
     */
    public int getResultsLimit() {
        return mResultsLimit;
    }

    @Nullable
    public String getSelectionStatement() {
        return mSelectionStatement;
    }

    @Nullable
    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public void intoBundle(@NonNull Bundle bundle) {
        bundle.putParcelable(ARG_KEY, this);
    }

    @NonNull
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        intoBundle(bundle);
        return bundle;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mUri, flags);
        dest.writeString(mSelectionStatement);
        dest.writeInt(mSelectionArgs != null ? 1 : 0);
        if (mSelectionArgs != null)
            dest.writeStringArray(mSelectionArgs);
        dest.writeInt(mResultsLimit);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static class Builder {
        private LoaderArgs mLoaderArgs;

        public Builder(LoaderArgs loaderArgs) {
            mLoaderArgs = loaderArgs;
        }

        public Builder() {
            mLoaderArgs = new LoaderArgs();
        }

        public Builder forUri(@NonNull Uri uri) {
            mLoaderArgs.mUri = uri;
            return this;
        }

        public Builder withLimit(int limit) {
            mLoaderArgs.mResultsLimit = limit;
            return this;
        }

        public Builder withSelection(@NonNull Selection selection) {
            mLoaderArgs.mSelectionStatement = selection.getStatement();
            mLoaderArgs.mSelectionArgs = selection.getArguments();
            return this;
        }

        public LoaderArgs build() {
            return mLoaderArgs;
        }

        public Bundle buildBundle() {
            Bundle bundle = new Bundle();
            mLoaderArgs.intoBundle(bundle);
            return bundle;
        }
    }

    public static class Selection {
        private LinkedHashMap<Operator, WhereBuilder> mSelections;

        public Selection(@NonNull Selection start) {
            mSelections = new LinkedHashMap<>(start.mSelections);
        }

        public Selection() {
            mSelections = new LinkedHashMap<>();
        }

        public Selection or(@NonNull WhereBuilder where) {
            mSelections.put(new Operator(Operator.OR), where);
            return this;
        }

        public Selection and(@NonNull WhereBuilder where) {
            mSelections.put(new Operator(Operator.AND), where);
            return this;
        }

        public Selection init(@NonNull WhereBuilder where) {
            mSelections = new LinkedHashMap<>();
            mSelections.put(new Operator(Operator.START), where);
            return this;
        }


        public String getStatement() {
            StringBuilder sb = new StringBuilder();
            Iterator<Map.Entry<Operator, WhereBuilder>> mapIterator = mSelections.entrySet().iterator();
            for (int i = 0; i < mSelections.size(); i++) {
                Map.Entry<Operator, WhereBuilder> map = mapIterator.next();
                if (i == 0) {
                    sb.append("(").append(map.getValue().toString()).append(")");
                } else {
                    sb.append(" ").append(map.getKey().toString()).append(" ")
                            .append("(").append(map.getValue().toString()).append(")");
                }
            }
            Logy.v("Selection", sb.toString());
            return sb.toString();
        }

        @Override
        public String toString() {
            return getStatement() + " " + getArguments().toString();
        }

        public String[] getArguments() {
            String[] result = new String[mSelections.size()]; // always one argument per where clause?
            Iterator<Map.Entry<Operator, WhereBuilder>> mapIterator = mSelections.entrySet().iterator();
            for (int i = 0; i < mSelections.size(); i++) {
                Map.Entry<Operator, WhereBuilder> map = mapIterator.next();
                result[i] = map.getValue().getArgument();
            }
            return result;
        }

        public static class Operator {
            public final static String START = "";
            public final static String OR = "OR";
            public final static String AND = "AND";
            private String op;

            public Operator(String op) {
                this.op = op;
            }

            @Override
            public String toString() {
                return op;
            }
        }

        public static class WhereBuilder {
            public final static String LIKE = "LIKE ?";
            public final static String MATCH = "MATCH ?";

            private final String mProjection;
            private final String mKeyword;
            private final String mArgument;

            public WhereBuilder(@NonNull String projection, @NonNull String keyword, @NonNull String argument) {
                mProjection = projection;
                mKeyword = keyword;
                mArgument = argument;
            }

            @Override
            public String toString() {
                return mProjection + " " + mKeyword;
            }

            public String getArgument() {
                return mArgument;
            }
        }

        public static class WhereLike extends WhereBuilder {

            public WhereLike(@NonNull String projection, @NonNull String argument) {
                super(projection, WhereBuilder.LIKE, argument);
            }
        }
    }

}
