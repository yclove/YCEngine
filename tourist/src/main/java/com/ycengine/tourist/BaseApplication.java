package com.ycengine.tourist;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.ycengine.tourist.service.CodeService;

import io.fabric.sdk.android.Fabric;

public class BaseApplication extends Application {
    private Context mContext;
    private CodeService codeService;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Fabric.with(this, new Crashlytics());
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));

        //Timber.plant(new Timber.DebugTree());

        codeService = new CodeService();
    }

    public CodeService getCodeService() {
        return codeService;
    }
}
