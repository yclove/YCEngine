package com.ycengine.ebook;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.ycengine.yclibrary.widget.YCEngineDialog;

public abstract class BaseActivity extends AppCompatActivity {
    public Context mContext;
    public YCEngineDialog mYCEngineDialog;
    public ProgressDialog mProgressDialog;

    // @StringRes, @LayoutRes, @ColorRes, @MenuRes : 해당 Resource 값만 받아서 처리해야할 경우 이를 코딩상에서 미리 방지하기위한 Annotations
    @LayoutRes
    protected abstract int layoutResId();

    // @CallSuper : 애너테이션은 이 애너테이션이 붙은 메서드를 하위 클래스에서 오버라이드할 때는 반드시 상위 클래스의 메서드를 호출하도록 강제한다. 액티비티의 라이프사이클 메서드에도 사용된다.
    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResId());
        mContext = this;
    }

    public AdListener mAdListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            // Code to be executed when an ad finishes loading.
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            // Code to be executed when an ad request fails.
        }

        @Override
        public void onAdOpened() {
            // Code to be executed when the ad is displayed.
        }

        @Override
        public void onAdLeftApplication() {
            // Code to be executed when the user has left the app.
        }

        @Override
        public void onAdClosed() {
            // Code to be executed when when the interstitial ad is closed.
        }
    };
}
