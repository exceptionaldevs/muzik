package com.exceptional.musiccore;

/**
 * Created by darken on 16.11.2014.
 */
public interface BinderCustomer<B extends MusicBinder> {
    void onBinderAvailable(B binder);
}
