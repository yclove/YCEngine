package com.ycengine.ontimealarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.ycengine.ontimealarm.data.OnTimeItem;
import com.ycengine.ontimealarm.dialogs.RingtonePickerDialog;
import com.ycengine.ontimealarm.dialogs.RingtonePickerDialogController;
import com.ycengine.ontimealarm.util.FragmentTagUtils;
import com.ycengine.yclibrary.util.CommonUtil;
import com.ycengine.yclibrary.util.DateUtil;
import com.ycengine.yclibrary.util.DeviceUtil;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.util.ArrayList;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class SettingActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage {
    private static final int TYPE_GAME = 1;

    private WeakRefHandler mWeakRefHandler;
    private InputMethodManager imm;

    private int mPosition;
    private OnTimeItem mOnTimeItem = null;
    private ArrayList<String> repeatDay = new ArrayList<>();
    private ArrayList<String> repeatHour = new ArrayList<>();

    private RelativeLayout mRootLayout;
    private ImageButton mTrashBtn;
    private RelativeLayout mMonBtn, mTueBtn, mWedBtn, mThuBtn, mFriBtn, mSatBtn, mSunBtn;
    private Switch mHour01Switch, mHour02Switch, mHour03Switch, mHour04Switch, mHour05Switch, mHour06Switch, mHour07Switch, mHour08Switch, mHour09Switch, mHour10Switch;
    private Switch mHour11Switch, mHour12Switch, mHour13Switch, mHour14Switch, mHour15Switch, mHour16Switch, mHour17Switch, mHour18Switch, mHour19Switch, mHour20Switch;
    private Switch mHour21Switch, mHour22Switch, mHour23Switch, mHour00Switch;
    private SeekBar mSeekBar;
    private Switch mSoundSwitch, mVibrateSwitch, mStatusBarSwitch;
    private boolean isSound = false, isVibrate = false;

    private EditText mEditText;
    private TextView mRingTone;

    private FragmentManager mFragmentManager;
    private RingtonePickerDialogController mRingtonePickerController;

    @Override
    protected int layoutResId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeakRefHandler = new WeakRefHandler(this);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(getAdRequest());
        mAdView.setAdListener(mAdListener);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mRootLayout = (RelativeLayout) findViewById(R.id.mRootLayout);
        mTrashBtn = (ImageButton) findViewById(R.id.mTrashBtn);

        mMonBtn = (RelativeLayout) findViewById(R.id.mMonBtn);
        mTueBtn = (RelativeLayout) findViewById(R.id.mTueBtn);
        mWedBtn = (RelativeLayout) findViewById(R.id.mWedBtn);
        mThuBtn = (RelativeLayout) findViewById(R.id.mThuBtn);
        mFriBtn = (RelativeLayout) findViewById(R.id.mFriBtn);
        mSatBtn = (RelativeLayout) findViewById(R.id.mSatBtn);
        mSunBtn = (RelativeLayout) findViewById(R.id.mSunBtn);

        mHour00Switch = (Switch) findViewById(R.id.mHour00Switch);
        mHour01Switch = (Switch) findViewById(R.id.mHour01Switch);
        mHour02Switch = (Switch) findViewById(R.id.mHour02Switch);
        mHour03Switch = (Switch) findViewById(R.id.mHour03Switch);
        mHour04Switch = (Switch) findViewById(R.id.mHour04Switch);
        mHour05Switch = (Switch) findViewById(R.id.mHour05Switch);
        mHour06Switch = (Switch) findViewById(R.id.mHour06Switch);
        mHour07Switch = (Switch) findViewById(R.id.mHour07Switch);
        mHour08Switch = (Switch) findViewById(R.id.mHour08Switch);
        mHour09Switch = (Switch) findViewById(R.id.mHour09Switch);
        mHour10Switch = (Switch) findViewById(R.id.mHour10Switch);
        mHour11Switch = (Switch) findViewById(R.id.mHour11Switch);
        mHour12Switch = (Switch) findViewById(R.id.mHour12Switch);
        mHour13Switch = (Switch) findViewById(R.id.mHour13Switch);
        mHour14Switch = (Switch) findViewById(R.id.mHour14Switch);
        mHour15Switch = (Switch) findViewById(R.id.mHour15Switch);
        mHour16Switch = (Switch) findViewById(R.id.mHour16Switch);
        mHour17Switch = (Switch) findViewById(R.id.mHour17Switch);
        mHour18Switch = (Switch) findViewById(R.id.mHour18Switch);
        mHour19Switch = (Switch) findViewById(R.id.mHour19Switch);
        mHour20Switch = (Switch) findViewById(R.id.mHour20Switch);
        mHour21Switch = (Switch) findViewById(R.id.mHour21Switch);
        mHour22Switch = (Switch) findViewById(R.id.mHour22Switch);
        mHour23Switch = (Switch) findViewById(R.id.mHour23Switch);

        mSeekBar = (SeekBar) findViewById(R.id.mSeekBar);

        mSoundSwitch = (Switch) findViewById(R.id.mSoundSwitch);
        mSoundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSound = isChecked;
                updateVolumeUI();
            }
        });

        mVibrateSwitch = (Switch) findViewById(R.id.mVibrateSwitch);
        mVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isVibrate = isChecked;
            }
        });

        mStatusBarSwitch = (Switch) findViewById(R.id.mStatusBarSwitch);

        mEditText = (EditText) findViewById(R.id.mEditText);
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.DB_ONTIME_NAME_MAX_LENGTH)});
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        // 포커스를 루트뷰로 이동
                        mRootLayout.requestFocus();
                        // 소프트키보드 가림
                        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                        return true;
                    default:
                        return false;
                }
            }
        });

        mRingTone = (TextView) findViewById(R.id.mRingTone);

        Intent intent = getIntent();
        mOnTimeItem = intent.getParcelableExtra("OnTimeItem");
        mPosition = intent.getIntExtra("position", -1);

        if (mOnTimeItem == null) {
            mOnTimeItem = new OnTimeItem();
            mTrashBtn.setVisibility(View.GONE);
        } else {
            mTrashBtn.setVisibility(View.VISIBLE);
        }

        /* 알람 이름 */
        mEditText.setText(mOnTimeItem.getOnTimeName());

        /* 요일반복 */
        String onTimeDay = mOnTimeItem.getOnTimeDay();
        if (CommonUtil.isNull(onTimeDay) || onTimeDay.length() != 7) {
            onTimeDay = "0000000";
        }

        for (int i = 0; i < onTimeDay.length(); i++) {
            repeatDay.add(onTimeDay.substring(i, i + 1));
        }

        updateRepeatDayUI();

        /* 알람시간 */
        String onTimeHour = mOnTimeItem.getOnTimeHour();
        if (CommonUtil.isNull(onTimeHour) || onTimeHour.length() != 24) {
            onTimeHour = "000000000000000000000000";
        }

        for (int i = 0; i < onTimeHour.length(); i++) {
            repeatHour.add(onTimeHour.substring(i, i + 1));
        }

        updateRepeatHourUI();

        /* 알람음 볼륨 */
        mSeekBar.setProgress(mOnTimeItem.getOnTimeVolume());

        /* 알람음 및 진동 */
        if (mOnTimeItem.getOnTimeType() == Constants.ONTIME_TYPE_VIBRATE_SOUND) {
            isSound = true;
            isVibrate = true;
        } else if (mOnTimeItem.getOnTimeType() == Constants.ONTIME_TYPE_SOUND) {
            isSound = true;
            isVibrate = false;
        } else if (mOnTimeItem.getOnTimeType() == Constants.ONTIME_TYPE_VIBRATE) {
            isSound = false;
            isVibrate = true;
        }

        mSoundSwitch.setChecked(isSound);
        mVibrateSwitch.setChecked(isVibrate);
        updateVolumeUI();

        /* 알람음 */
        String ringTone = RingtoneManager.getRingtone(mContext, getSelectedRingtoneUri()).getTitle(mContext);
        mRingTone.setText(ringTone);

        mFragmentManager = getSupportFragmentManager();
        mRingtonePickerController = new RingtonePickerDialogController(mFragmentManager,
                new RingtonePickerDialog.OnRingtoneSelectedListener() {
                    @Override
                    public void onRingtoneSelected(Uri ringtoneUri) {
                        LogUtil.e("Selected ringtone: " + ringtoneUri.toString());
                        mOnTimeItem.setOnTimeRingTone(ringtoneUri.toString());
                        String ringTone = RingtoneManager.getRingtone(mContext, getSelectedRingtoneUri()).getTitle(mContext);
                        mRingTone.setText(ringTone);
                    }
                }
        );

        /* 상태바에 표시 */
        mStatusBarSwitch.setChecked(mOnTimeItem.getOnTimeStatusBar() == Constants.ONTIME_STATUS_BAR_ENABLED);
    }

    private String makeTag(@IdRes int viewId) {
        return FragmentTagUtils.makeTag(SettingActivity.class, viewId, mOnTimeItem.getOnTimeIdx());
    }

    private Uri getSelectedRingtoneUri() {
        // If showing an item for "Default" (@see EXTRA_RINGTONE_SHOW_DEFAULT), this can be one
        // of DEFAULT_RINGTONE_URI, DEFAULT_NOTIFICATION_URI, or DEFAULT_ALARM_ALERT_URI to have the
        // "Default" item checked.
        //
        // Otherwise, use RingtoneManager.getActualDefaultRingtoneUri() to get the "actual sound URI".
        //
        // Do not use RingtoneManager.getDefaultUri(), because that just returns one of
        // DEFAULT_RINGTONE_URI, DEFAULT_NOTIFICATION_URI, or DEFAULT_ALARM_ALERT_URI
        // depending on the type requested (i.e. what the docs calls "symbolic URI
        // which will resolved to the actual sound when played").
        String ringtone = mOnTimeItem.getOnTimeRingTone();
        return ringtone.isEmpty() ? RingtoneManager.getActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(ringtone);
    }

    private void updateVolumeUI() {
        mSeekBar.setEnabled(isSound);
        if (isSound) {
            mSeekBar.setAlpha(1.0f);
        } else {
            mSeekBar.setAlpha(0.3f);
        }
    }

    private void updateRepeatDayUI() {
        for (int i = 0; i < repeatDay.size(); i++) {
            switch (i) {
                case 0:
                    if ("1".equals(repeatDay.get(i))) {
                        mSunBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                    } else
                        mSunBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 1:
                    if ("1".equals(repeatDay.get(i))) {
                        mMonBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                    } else
                        mMonBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 2:
                    if ("1".equals(repeatDay.get(i))) {
                        mTueBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                    } else
                        mTueBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 3:
                    if ("1".equals(repeatDay.get(i))) {
                        mWedBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                    } else
                        mWedBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 4:
                    if ("1".equals(repeatDay.get(i))) {
                        mThuBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                    } else
                        mThuBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 5:
                    if ("1".equals(repeatDay.get(i))) {
                        mFriBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                    } else
                        mFriBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 6:
                    if ("1".equals(repeatDay.get(i))) {
                        mSatBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                    } else
                        mSatBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
            }
        }
    }

    private void updateRepeatHourUI() {
        if (repeatHour.size() == 24) {
            mHour00Switch.setChecked("1".equals(repeatHour.get(0)));
            mHour01Switch.setChecked("1".equals(repeatHour.get(1)));
            mHour02Switch.setChecked("1".equals(repeatHour.get(2)));
            mHour03Switch.setChecked("1".equals(repeatHour.get(3)));
            mHour04Switch.setChecked("1".equals(repeatHour.get(4)));
            mHour05Switch.setChecked("1".equals(repeatHour.get(5)));
            mHour06Switch.setChecked("1".equals(repeatHour.get(6)));
            mHour07Switch.setChecked("1".equals(repeatHour.get(7)));
            mHour08Switch.setChecked("1".equals(repeatHour.get(8)));
            mHour09Switch.setChecked("1".equals(repeatHour.get(9)));
            mHour10Switch.setChecked("1".equals(repeatHour.get(10)));
            mHour11Switch.setChecked("1".equals(repeatHour.get(11)));
            mHour12Switch.setChecked("1".equals(repeatHour.get(12)));
            mHour13Switch.setChecked("1".equals(repeatHour.get(13)));
            mHour14Switch.setChecked("1".equals(repeatHour.get(14)));
            mHour15Switch.setChecked("1".equals(repeatHour.get(15)));
            mHour16Switch.setChecked("1".equals(repeatHour.get(16)));
            mHour17Switch.setChecked("1".equals(repeatHour.get(17)));
            mHour18Switch.setChecked("1".equals(repeatHour.get(18)));
            mHour19Switch.setChecked("1".equals(repeatHour.get(19)));
            mHour20Switch.setChecked("1".equals(repeatHour.get(20)));
            mHour21Switch.setChecked("1".equals(repeatHour.get(21)));
            mHour22Switch.setChecked("1".equals(repeatHour.get(22)));
            mHour23Switch.setChecked("1".equals(repeatHour.get(23)));
        }
    }

    private boolean isEnabledDay() {
        for (String item : repeatDay) {
            if (item.equals("1"))
                return true;
        }

        return false;
    }

    private boolean isEnabledHour() {
        if (mHour00Switch.isChecked()) return true;
        if (mHour01Switch.isChecked()) return true;
        if (mHour02Switch.isChecked()) return true;
        if (mHour03Switch.isChecked()) return true;
        if (mHour04Switch.isChecked()) return true;
        if (mHour05Switch.isChecked()) return true;
        if (mHour06Switch.isChecked()) return true;
        if (mHour07Switch.isChecked()) return true;
        if (mHour08Switch.isChecked()) return true;
        if (mHour09Switch.isChecked()) return true;
        if (mHour10Switch.isChecked()) return true;
        if (mHour11Switch.isChecked()) return true;
        if (mHour12Switch.isChecked()) return true;
        if (mHour13Switch.isChecked()) return true;
        if (mHour14Switch.isChecked()) return true;
        if (mHour15Switch.isChecked()) return true;
        if (mHour16Switch.isChecked()) return true;
        if (mHour17Switch.isChecked()) return true;
        if (mHour18Switch.isChecked()) return true;
        if (mHour19Switch.isChecked()) return true;
        if (mHour20Switch.isChecked()) return true;
        if (mHour21Switch.isChecked()) return true;
        if (mHour22Switch.isChecked()) return true;
        if (mHour23Switch.isChecked()) return true;
        return false;
    }

    private String getOnTimeHourString() {
        StringBuilder onTimeHour = new StringBuilder();
        onTimeHour.append((mHour00Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour01Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour02Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour03Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour04Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour05Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour06Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour07Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour08Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour09Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour10Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour11Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour12Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour13Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour14Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour15Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour16Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour17Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour18Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour19Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour20Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour21Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour22Switch.isChecked()) ? "1" : "0");
        onTimeHour.append((mHour23Switch.isChecked()) ? "1" : "0");

        return onTimeHour.toString();
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        Intent intent;

        switch (id) {
            case R.id.mCloseBtn:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.mTrashBtn:
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder
                        .setMessage(R.string.msg_delete_noti_confirm)
                        .setPositiveButton(R.string.dialog_alert_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = getIntent();
                                        intent.putExtra("position", mPosition);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.dialog_alert_no, new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int which) {
                                dialog.dismiss();
                            }
                        }).show();
                break;
            case R.id.mSunBtn:
            case R.id.mMonBtn:
            case R.id.mTueBtn:
            case R.id.mWedBtn:
            case R.id.mThuBtn:
            case R.id.mFriBtn:
            case R.id.mSatBtn:
                int position = Integer.valueOf((String) v.getTag());
                if ("0".equals(repeatDay.get(position))) {
                    repeatDay.set(position, "1");
                } else {
                    repeatDay.set(position, "0");
                }
                updateRepeatDayUI();
                break;
            case R.id.mRingToneBtn:
                mRingtonePickerController.setmVolume(mSeekBar.getProgress());
                mRingtonePickerController.show(getSelectedRingtoneUri(), makeTag(R.id.ringtone));
                break;
            case R.id.mSaveBtn:
                if (!isEnabledDay()) {
                    DeviceUtil.showToast(mContext, getString(R.string.msg_choice_noti_day));
                } else if (!isEnabledHour()) {
                    DeviceUtil.showToast(mContext, getString(R.string.msg_choice_noti_hour));
                } else if (!isSound && !isVibrate) {
                    DeviceUtil.showToast(mContext, getString(R.string.msg_choice_noti_type));
                } else {
                    StringBuilder onTimeDay = new StringBuilder();
                    for (String item : repeatDay) {
                        onTimeDay.append(item);
                    }

                    if (isSound && isVibrate) {
                        mOnTimeItem.setOnTimeType(Constants.ONTIME_TYPE_VIBRATE_SOUND);
                    } else if (isSound && !isVibrate) {
                        mOnTimeItem.setOnTimeType(Constants.ONTIME_TYPE_SOUND);
                    } else if (!isSound && isVibrate) {
                        mOnTimeItem.setOnTimeType(Constants.ONTIME_TYPE_VIBRATE);
                    }

                    mOnTimeItem.setOnTimeDay(onTimeDay.toString());
                    mOnTimeItem.setOnTimeHour(getOnTimeHourString());
                    mOnTimeItem.setOnTimeVolume(mSeekBar.getProgress());
                    mOnTimeItem.setOnTimeName(mEditText.getText().toString());
                    mOnTimeItem.setOnTimeStatusBar((mStatusBarSwitch.isChecked()) ? Constants.ONTIME_STATUS_BAR_ENABLED : Constants.ONTIME_STATUS_BAR_DISABLED);

                    if (mPosition == -1)
                        mOnTimeItem.setOnTimeRegDate(DateUtil.getCurrentDate("yyyyMMddHHmmssSSS"));

                    intent = getIntent();
                    intent.putExtra("item", mOnTimeItem);
                    intent.putExtra("position", mPosition);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();

        switch (msg.what) {
            case Constants.REQUEST_INIT_TIME:
                /*mHourWheelView.setCurrentItem(mNotifyItem.getNotiHour(), true);
                mMinuteWheelView.setCurrentItem(mNotifyItem.getNotiMinute(), true);*/
                break;
        }
    }
}
