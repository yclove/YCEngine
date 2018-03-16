package com.ycengine.morseflash;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.ycengine.yclibrary.MarketVersionChecker;
import com.ycengine.yclibrary.util.DeviceUtil;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SplashActivity extends BaseActivity implements IOnHandlerMessage {
    private WeakRefHandler mWeakRefHandler;
    private TextView tvAppVersion;

    @Override
    protected int layoutResId() {
        return R.layout.activity_splash;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeakRefHandler = new WeakRefHandler(this);

        tvAppVersion = (TextView)findViewById(R.id.tvAppVersion);
        tvAppVersion.setText(getString(R.string.app_version, DeviceUtil.getAppVersion(mContext)));

        final MarketVersionChecker mChecker = new MarketVersionChecker();
        Thread versionCheckThread = new Thread() {
            public void run() {
                boolean isMarketVersionOk = false;
                boolean isAppVersionOk = false;

                final String marketVersion = mChecker.getMarketVersion(getPackageName());

                if (marketVersion != null) {
                    if (!marketVersion.isEmpty()) {
                        isMarketVersionOk = true;
                    }
                }

                if (isMarketVersionOk) {
                    final String appVersion = DeviceUtil.getAppVersion(mContext);

                    if (appVersion != null) {
                        if (!appVersion.isEmpty()) {
                            isAppVersionOk = true;
                        }
                    }

                    if (isAppVersionOk) {
                        try {
                            versionCheck(marketVersion, appVersion);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    LogUtil.e("marketVersion=" + marketVersion + ", appVersion=" + appVersion);
                }
            }

            private void versionCheck(String marketVersion, String appVersion) {

                List<String> marketVersionList = new ArrayList<>(Arrays.asList(marketVersion.split("\\.")));
                if (marketVersionList == null || marketVersionList.isEmpty()) {
                    return;
                }

                int length = marketVersionList.size();

                if (length == 1) {
                    marketVersionList.add("0");
                    marketVersionList.add("0");
                } else if (length == 2) {
                    marketVersionList.add("0");
                }

                String marketMajor = marketVersionList.get(0).toString();
                if (!isIntegerString(marketMajor)) {
                    return;
                }

                String marketMinor = marketVersionList.get(1).toString();
                if (!isIntegerString(marketMinor)) {
                    return;
                }

                String marketPatch = marketVersionList.get(2).toString();
                if (!isIntegerString(marketPatch)) {
                    return;
                }

                List<String> appVersionList = new ArrayList<>(Arrays.asList(appVersion.split("\\.")));
                if (appVersionList == null || appVersionList.isEmpty()) {
                    return;
                }

                int appLength = appVersionList.size();

                if (appLength == 1) {
                    appVersionList.add("0");
                    appVersionList.add("0");
                } else if (appLength == 2) {
                    appVersionList.add("0");
                }

                String appMajor = appVersionList.get(0).toString();
                if (!isIntegerString(appMajor)) {
                    return;
                }

                String appMinor = appVersionList.get(1).toString();
                if (!isIntegerString(appMinor)) {
                    return;
                }

                String appPatch = appVersionList.get(2).toString();
                if (!isIntegerString(appPatch)) {
                    return;
                }

                boolean majorUpdate = Integer.parseInt(marketMajor) > Integer.parseInt(appMajor) ? true : false;
                boolean minorUpdate = Integer.parseInt(marketMinor) > Integer.parseInt(appMinor) ? true : false;
                boolean patchUpdate = Integer.parseInt(marketPatch) > Integer.parseInt(appPatch) ? true : false;

                if (majorUpdate) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            onUpdate(true);
                        }
                    });
                } else {
                    if (marketMajor.equals(appMajor)) {
                        if (minorUpdate) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onUpdate(false);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkInitPermission();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkInitPermission();
                            }
                        });
                    }
                }

                LogUtil.e("Force Update=" + majorUpdate + ", minorUpdate=" + minorUpdate + ", patchUpdate=" + patchUpdate);
            }

            private void onUpdate(final boolean isForceUpdate) {
                String message = null;
                String positiveMessage = null;
                String negativeMessage = null;

                if (isForceUpdate) {
                    message = mContext.getString(R.string.force_update_msg);
                    positiveMessage = mContext.getString(R.string.force_positive_msg);
                    negativeMessage = mContext.getString(R.string.force_negative_msg);
                } else {
                    message = mContext.getString(R.string.choice_update_msg);
                    positiveMessage = mContext.getString(R.string.force_positive_msg);
                    negativeMessage = mContext.getString(R.string.non_force_negative_msg);
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder
                        .setTitle(mContext.getString(R.string.app_name))
                        .setMessage(message)
                        .setPositiveButton(positiveMessage, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName())));
                                finish();
                            }
                        }).setNegativeButton(negativeMessage, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (isForceUpdate) {
                            finish();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkInitPermission();
                                }
                            });
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                    }
                });

                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
                        return true;
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }

            private boolean isIntegerString(String str) {
                if (str == null || str.isEmpty()) {
                    return false;
                }

                if (Pattern.matches("^[0-9]+$", str)) {
                    return true;
                }

                return false;
            }
        };

        versionCheckThread.start();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1000:
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void checkInitPermission() {
        new TedPermission(mContext)
                .setPermissionListener(initPermissionListener)
                .setRationaleMessage(getString(R.string.permission_rationale_message_camera))
                .setDeniedMessage(getString(R.string.permission_denied_message))
                .setPermissions(Manifest.permission.CAMERA)
                .check();
    }

    PermissionListener initPermissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            mWeakRefHandler.sendEmptyMessageDelayed(1000, 1000);
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            finish();
        }
    };
}
