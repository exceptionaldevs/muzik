package com.exceptional.musiccore;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.exceptional.musiccore.utils.Logy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darken on 16.11.2014.
 */
public abstract class MusicServiceActivity<B extends MusicBinder> extends AppCompatActivity implements BinderProvider<B> {
    private final static String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "MusicServiceActivity";
    private final List<BinderCustomer<B>> mWaitingBinderCustomers = new ArrayList<>();
    private B mBinder;
    private Intent serviceIntent;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logy.i(TAG, "MusicService is UNbound");
            mBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logy.i(TAG, "MusicService is bound");
            mBinder = (B) service;
            while (!mWaitingBinderCustomers.isEmpty()) {
                mWaitingBinderCustomers.remove(0).onBinderAvailable(mBinder);
            }
        }
    };

    @Override
    public void registerBinderCustomer(BinderCustomer<B> customer) {
        if (getBinder() != null) {
            customer.onBinderAvailable(getBinder());
        } else {
            if (!mWaitingBinderCustomers.contains(customer)) {
                mWaitingBinderCustomers.add(customer);
            }
        }
    }

    @Override
    public B getBinder() {
        return mBinder;
    }

    public abstract Class<? extends MusicService> supplyServiceClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceIntent = new Intent(this, supplyServiceClass());
        startService(serviceIntent);
    }

    @Override
    protected void onStart() {
        Logy.i(TAG, "Connecting to MusicService");
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        Logy.d(TAG, "onStop");
        if (getBinder() != null) {
            Logy.i(TAG, "Disconnecting from MusicService");
            unbindService(serviceConnection);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Logy.d(TAG, "onDestroy");
        if (!isFinishing())
            Logy.i(TAG, "System wants to free resources");
        super.onDestroy();
    }
}
