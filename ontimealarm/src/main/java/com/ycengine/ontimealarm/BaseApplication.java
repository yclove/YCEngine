package com.ycengine.ontimealarm;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;

import io.fabric.sdk.android.Fabric;

public class BaseApplication extends Application {
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Fabric.with(this, new Crashlytics());
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));
    }
}
