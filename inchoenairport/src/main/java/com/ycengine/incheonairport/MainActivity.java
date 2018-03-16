package com.ycengine.incheonairport;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ycengine.yclibrary.util.DeviceUtil;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private TextView tvAppVersion;
    private FinishConfirmDialog mFinishConfirmDialog;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvAppVersion = (TextView)findViewById(R.id.tvAppVersion);
        tvAppVersion.setText(getString(R.string.app_version, DeviceUtil.getAppVersion(mContext)));

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
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        Intent intent;

        switch (id) {
            case R.id.mAirportInfoBtn:
                intent = new Intent(mContext, AirportInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.mParkingLotInfoBtn:
                intent = new Intent(mContext, ParkingLotInfoActivity.class);
                startActivity(intent);
                break;
        }
    }
}
