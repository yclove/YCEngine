package com.ycengine.wakeup;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.ads.MobileAds;
import com.crashlytics.android.Crashlytics;
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
