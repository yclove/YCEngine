package com.ycengine.morseflash;

import android.app.Dialog;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ycengine.yclibrary.util.SPUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.io.IOException;
import java.util.List;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class MainActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage, SurfaceHolder.Callback {
    private WeakRefHandler mWeakRefHandler;
    private FirebaseAnalytics mFirebaseAnalytics;

    private FinishConfirmDialog mFinishConfirmDialog;

    private static Camera camera;
    private static Parameters param;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private Switch mSwitch, mRepeatSwitch;
    private boolean mAutoFlashFlag = false, mAutoMorseRepeatFlag = false;

    private String mMosMessage = "";

    private TextView tvMenuLight, tvMenuFlicker, tvMenuMorse;
    private ImageButton lightButton, flickerButton, morseButton;
    private SeekBar mSeekBar;
    private LinearLayout mMoreHeader;
    private EditText morseText;

    private FlickerThread flickerThread = null;
    private MorseThread morseThread = null;

    private static boolean flashLightOn = false;
    private List<String> list;
    private boolean flickerOn = false;
    private int speed = 250;
    private String morseStr;
    private boolean morseOn = false;
    private Handler handler;

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
        mWeakRefHandler = new WeakRefHandler(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(getAdRequest());
        mAdView.setAdListener(mAdListener);

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

        mAutoFlashFlag = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_AUTO_FLASH_FLAG, true);
        mSwitch = (Switch) findViewById(R.id.mSwitch);
        mSwitch.setChecked(mAutoFlashFlag);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtil.setSharedPreference(mContext, Constants.SP_AUTO_FLASH_FLAG, isChecked);
                mAutoFlashFlag = isChecked;
            }
        });

        mAutoMorseRepeatFlag = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_AUTO_MORSE_FLASH_REPEAT_FLAG, true);
        mRepeatSwitch = (Switch) findViewById(R.id.mRepeatSwitch);
        mRepeatSwitch.setChecked(mAutoMorseRepeatFlag);
        mRepeatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtil.setSharedPreference(mContext, Constants.SP_AUTO_MORSE_FLASH_REPEAT_FLAG, isChecked);
                mAutoMorseRepeatFlag = isChecked;
            }
        });

        tvMenuLight = (TextView) findViewById(R.id.tvMenuLight);
        tvMenuFlicker = (TextView) findViewById(R.id.tvMenuFlicker);
        tvMenuMorse = (TextView) findViewById(R.id.tvMenuMorse);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        camera = Camera.open();
        param = camera.getParameters();
        camera.startPreview();

        list = param.getSupportedFlashModes();

        if (!list.contains(Parameters.FLASH_MODE_TORCH)) {
            Toast.makeText(MainActivity.this, "Not supported device.",
                    Toast.LENGTH_SHORT).show();
        }

        lightButton = (ImageButton) findViewById(R.id.lightButton);
        lightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flickerThread != null) {
                    stopFlicker();
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (morseThread != null) {
                    stopMorse();
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (flashLightOn == false) {
                    flashLightOn();
                } else {
                    flashLightOff();
                }

            }
        });

        int flickerLevel = SPUtil.getIntSharedPreference(mContext, Constants.SP_FLICKER_LEVEL, 5);
        speed = (10 - flickerLevel) * 50;
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setMax(9);
        mSeekBar.setProgress(flickerLevel);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SPUtil.setSharedPreference(mContext, Constants.SP_FLICKER_LEVEL, progress);

                speed = (10 - progress) * 50;
                if (flickerThread != null) {
                    flickerThread.setFlickerSpeed(speed);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        flickerButton = (ImageButton) findViewById(R.id.flickerButton);
        flickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (morseThread != null) {
                    stopMorse();
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (flickerOn == false) {
                    flashLightOff();
                    startFlicker();
                } else {
                    stopFlicker();
                }
            }
        });

        handler = new MorseHandler();

        mMosMessage = SPUtil.getSharedPreference(mContext, Constants.SP_MOS_MESSAGE, "SOS");
        mMoreHeader = (LinearLayout) findViewById(R.id.mMoreHeader);
        morseText = (EditText) findViewById(R.id.morseText);
        morseText.setText(mMosMessage);
        morseText.setFilters(new InputFilter[] {
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence cs, int start, int end, Spanned spanned, int dStart, int dEnd) {
                        if(cs.equals("")){
                            return cs;
                        }
                        if(cs.toString().matches("^[a-zA-Z0-9]+$")){
                            return cs;
                        }
                        return "";
                    }
                }
        });

        morseButton = (ImageButton) findViewById(R.id.morseButton);
        morseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flickerThread != null) {
                    stopFlicker();
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (morseOn == false) {
                    flashLightOff();
                    startMorse();
                } else {
                    stopMorse();
                }
            }
        });

        int tabMenu = SPUtil.getIntSharedPreference(mContext, Constants.SP_TAB_MENU, Constants.TAB_MENU_LIGHT);
        updateUI(tabMenu);
        if (mAutoFlashFlag) {
            if (tabMenu == Constants.TAB_MENU_LIGHT) {
                lightButton.performClick();
            } else if (tabMenu == Constants.TAB_MENU_FLICKER) {
                flickerButton.performClick();
            } else if (tabMenu == Constants.TAB_MENU_MORSE) {
                morseButton.performClick();
            }
        }
    }

    public static void turnOn() {
        if (param.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
            param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(param);
        }
    }

    public static void turnOff() {
        if (param.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(param);
        }
    }

    private void flashLightOn() {
        turnOn();
        flashLightOn = true;
        lightButton.setImageResource(R.drawable.btn_flash_on);
    }

    private void flashLightOff() {
        turnOff();
        flashLightOn = false;
        lightButton.setImageResource(R.drawable.btn_flash_off);
    }

    public class MorseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 0) {
                if (mAutoMorseRepeatFlag) {
                    if (handler != null) {
                        Message revMsg = new Message();
                        revMsg.what = 2;
                        handler.sendMessageDelayed(revMsg, 1500);
                    }
                } else {
                    stopMorse();
                }
            } else if (msg.what == 1) {
                Toast.makeText(getBaseContext(), R.string.morsecodeinfo, Toast.LENGTH_LONG).show();
            } else if (msg.what == 2) {
                if (morseThread != null) {
                    new Thread(morseThread).start();
                }
            }
        }

    }

    private void startMorse() {
        morseStr = morseText.getText().toString();
        SPUtil.setSharedPreference(mContext, Constants.SP_MOS_MESSAGE, morseStr);
        morseThread = new MorseThread(morseStr, handler, this);
        morseThread.setMorseRun();
        morseButton.setImageResource(R.drawable.btn_flash_on);
        new Thread(morseThread).start();
        flashLightOn = true;
        morseOn = true;
    }

    private void stopMorse() {
        if (morseThread != null) {
            morseThread.setMorseStop();

            morseButton.setImageResource(R.drawable.btn_flash_off);
            flashLightOn = false;
            morseOn = false;
            morseThread = null;
        }
    }

    private void startFlicker() {
        flickerThread = new FlickerThread();
        flickerThread.setFlickerRun();
        flickerThread.setFlickerSpeed(speed);
        flickerButton.setImageResource(R.drawable.btn_flash_on);
        new Thread(flickerThread).start();
        flashLightOn = true;
        flickerOn = true;
    }

    private void stopFlicker() {
        if (flickerThread != null) {
            flickerThread.setFlickerStop();

            flickerButton.setImageResource(R.drawable.btn_flash_off);
            flashLightOn = false;
            flickerOn = false;
            flickerThread = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO
        // finish();
    }

    @Override
    protected void onDestroy() {
        if (morseThread != null) {
            morseThread.setMorseStop();
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (flickerThread != null) {
            flickerThread.setFlickerStop();
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        camera.stopPreview();
        camera.release();
        super.onDestroy();
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        Intent intent;

        switch (id) {
            case R.id.tabMenuLight:
                SPUtil.setSharedPreference(mContext, Constants.SP_TAB_MENU, Constants.TAB_MENU_LIGHT);
                updateUI(Constants.TAB_MENU_LIGHT);
                break;
            case R.id.tabMenuFlicker:
                SPUtil.setSharedPreference(mContext, Constants.SP_TAB_MENU, Constants.TAB_MENU_FLICKER);
                updateUI(Constants.TAB_MENU_FLICKER);
                break;
            case R.id.tabMenuMorse:
                SPUtil.setSharedPreference(mContext, Constants.SP_TAB_MENU, Constants.TAB_MENU_MORSE);
                updateUI(Constants.TAB_MENU_MORSE);
                break;
        }
    }

    private void updateUI(int menu) {
        switch (menu) {
            case Constants.TAB_MENU_LIGHT:
                tvMenuLight.setBackgroundResource(R.drawable.bg_round_red);
                tvMenuFlicker.setBackgroundResource(R.drawable.bg_round_blue);
                tvMenuMorse.setBackgroundResource(R.drawable.bg_round_blue);

                lightButton.setVisibility(View.VISIBLE);
                flickerButton.setVisibility(View.GONE);
                morseButton.setVisibility(View.GONE);

                mSeekBar.setVisibility(View.GONE);
                mMoreHeader.setVisibility(View.GONE);
                break;
            case Constants.TAB_MENU_FLICKER:
                tvMenuLight.setBackgroundResource(R.drawable.bg_round_blue);
                tvMenuFlicker.setBackgroundResource(R.drawable.bg_round_red);
                tvMenuMorse.setBackgroundResource(R.drawable.bg_round_blue);

                lightButton.setVisibility(View.GONE);
                flickerButton.setVisibility(View.VISIBLE);
                morseButton.setVisibility(View.GONE);

                mSeekBar.setVisibility(View.VISIBLE);
                mMoreHeader.setVisibility(View.GONE);
                break;
            case Constants.TAB_MENU_MORSE:
                tvMenuLight.setBackgroundResource(R.drawable.bg_round_blue);
                tvMenuFlicker.setBackgroundResource(R.drawable.bg_round_blue);
                tvMenuMorse.setBackgroundResource(R.drawable.bg_round_red);

                lightButton.setVisibility(View.GONE);
                flickerButton.setVisibility(View.GONE);
                morseButton.setVisibility(View.VISIBLE);

                mSeekBar.setVisibility(View.GONE);
                mMoreHeader.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1000:
                break;
        }
    }
}
