package com.exceptional.musiccore;

/**
 * Created by darken on 24.01.2016.
 */
public interface BinderProvider<BINDERTYPE extends MusicBinder> {
    void registerBinderCustomer(BinderCustomer<BINDERTYPE> customer);

    BINDERTYPE getBinder();
}
