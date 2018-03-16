package com.ycengine.tourist;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.ycengine.tourist.ui.AreaBasedListFragment;
import com.ycengine.tourist.ui.AreaCodeFragment;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.Utils;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class MainActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage {
    public static final int PAGE_AREA_BASED_LIST = 0;
    public static final int PAGE_AREA_CODE = 1;

    private WeakRefHandler mWeakRefHandler;
    private FinishConfirmDialog mFinishConfirmDialog;
    private InterstitialAd mInterstitialAd;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private AdView mAdView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    public void onBackPressed() {
        if (mFinishConfirmDialog != null && !mFinishConfirmDialog.isShowing()) {
            mFinishConfirmDialog.show();
        } else {
            finish();
        }
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        // http://stackoverflow.com/a/24035591/5055032, http://stackoverflow.com/a/3948036/5055032
        // 레이아웃의 뷰가 그리기 시작했습니다. (The views in our layout have begun drawing.)
        // 레이아웃이 끝나는 시점을 알려주는 라이프 사이클 콜백은 없습니다. (There is no lifecycle callback that tells us when our layout finishes drawing.)
        // 내 자신의 테스트에서 여전히 드로잉은 onResume ()에 의해 완료되지 않았습니다. (in my own test, drawing still isn't finished by onResume.)
        // 드로잉이 완료된 후에 UI 이벤트 큐에 메시지를 게시하여 크기를 얻을 수 있도록 게시하십시오. (Post a message in the UI events queue to be executed after drawing is complete, so that we may get their dimensions.)
        rootView.post(new Runnable() {
            @Override
            public void run() {
            }
        });

        // 새로운 Activity가 생성되고 Activty에 Fragment가 다시 연결될 때 안드로이드 OS는 Activity에 추가된 fragment를 기억합니다. onCreate메소드에서 savedInstanceState가 null이면, activity를 위해 fragment가 처음 생성되어 추가된 것입니다.
        if (savedInstanceState == null) {
            LogUtil.e("savedInstanceState is Null :::: " + (savedInstanceState == null));
        }

        mWeakRefHandler = new WeakRefHandler(this);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_fullscreen_id));
        mInterstitialAd.loadAd(getAdRequest());

        mAdView = (AdView) findViewById(R.id.mAdView);
        mAdView.loadAd(getAdRequest());

        mFinishConfirmDialog = new FinishConfirmDialog(this);
        //mFinishConfirmDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        mFinishConfirmDialog.setListener(new FinishConfirmDialog.OnDialogEventListener() {
            @Override
            public void onClickDialog(Dialog dialog, int index) {
                if (index == FinishConfirmDialog.dialog_result_cancel) {
                    if (mFinishConfirmDialog != null && mFinishConfirmDialog.isShowing()) {
                        mFinishConfirmDialog.dismiss();
                    }
                } else if (index == FinishConfirmDialog.dialog_result_confirm) {
                    mFinishConfirmDialog.hide();
                    mFinishConfirmDialog.dismiss();
                    finish();
                }
            }
        });

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                LogUtil.e("onPageSelected");
                Fragment f = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
                // NOTE: This callback is fired after a rotation, right after onStart().
                // Unfortunately, the FragmentManager handling the rotation has yet to
                // tell our adapter to re-instantiate the Fragments, so our collection
                // of fragments is empty. You MUST keep this check so we don't cause a NPE.
                if (f instanceof BaseFragment) {
                    ((BaseFragment) f).onPageSelected();
                }
            }
        });

        mTabLayout = (TabLayout) findViewById(R.id.mTabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        ColorStateList tabIconColor = ContextCompat.getColorStateList(this, R.color.tab_icon_color);
        setTabIcon(PAGE_AREA_BASED_LIST, R.drawable.icon_twt, tabIconColor);
        //setTabIcon(PAGE_AREA_CODE, R.drawable.icon_fb, tabIconColor);
    }

    private void setTabIcon(int index, @DrawableRes int iconRes, @NonNull final ColorStateList color) {
        TabLayout.Tab tab = mTabLayout.getTabAt(index);
        Drawable icon = Utils.getTintedDrawable(this, iconRes, color);
        DrawableCompat.setTintList(icon, color);
        tab.setIcon(icon);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();

        switch (id) {
            default:
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        int position;

        switch (msg.what) {
            case Constants.REQUEST_AREA_CODE_INFO:

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUEST_FILTER_INFO:
                Fragment frg = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.detach(frg);
                ft.attach(frg);
                ft.commit();
                break;
        }
    }

    private static class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final SparseArray<Fragment> mFragments = new SparseArray<>(getCount());

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PAGE_AREA_BASED_LIST:
                    return new AreaBasedListFragment();
                case PAGE_AREA_CODE:
                    return new AreaCodeFragment();
                default:
                    throw new IllegalStateException("No fragment can be instantiated for position " + position);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return 1;
        }

        public Fragment getFragment(int position) {
            return mFragments.get(position);
        }
    }
}
