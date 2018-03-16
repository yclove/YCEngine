package com.ycengine.incheonairport;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.NativeExpressAdView;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class FinishConfirmDialog extends Dialog {
    Context context;
    private NativeExpressAdView mAdView;

    public static int dialog_result_cancel = 0;
    public static int dialog_result_confirm = 1;

    private boolean mEnableBackKey = false;    // back key로 팝업 닫기 허용
    private boolean mEnableCancel = true;        // 외부 터치로 닫기 허용.

    private OnDialogEventListener 	listener	= null;

    @Override
    public void show() {
        super.show();
    }

    public FinishConfirmDialog(Context context) {
        super(context);

        this.context = context;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_finish_confirm);

        setCancelable(mEnableBackKey);
        setCanceledOnTouchOutside(mEnableCancel);

        mAdView = (NativeExpressAdView)findViewById(R.id.adView);
        mAdView.loadAd(getAdRequest());

        RelativeLayout mCancelBtn = (RelativeLayout) findViewById(R.id.mCancelBtn);
        RelativeLayout mConfirmBtn = (RelativeLayout) findViewById(R.id.mConfirmBtn);

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onClickDialog(FinishConfirmDialog.this, dialog_result_cancel);
                }
                cancel();
            }
        });

        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onClickDialog(FinishConfirmDialog.this, dialog_result_confirm);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setListener(OnDialogEventListener listener) {
        this.listener = listener;
    }

    public interface OnDialogEventListener {
        public void onClickDialog(Dialog dialog, int index);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mEnableBackKey) {
            if(listener != null) {
                listener.onClickDialog(FinishConfirmDialog.this, dialog_result_confirm);
            }
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }
}
