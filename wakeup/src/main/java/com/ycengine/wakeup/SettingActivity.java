package com.ycengine.wakeup;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.ycengine.wakeup.data.NotifyItem;
import com.ycengine.wakeup.dialogs.RingtonePickerDialog;
import com.ycengine.wakeup.dialogs.RingtonePickerDialogController;
import com.ycengine.wakeup.game.CalculationActivity;
import com.ycengine.wakeup.game.CalculationSimpleActivity;
import com.ycengine.wakeup.game.NonActivity;
import com.ycengine.wakeup.game.PuzzleActivity;
import com.ycengine.wakeup.library.wheel.OnWheelScrollListener;
import com.ycengine.wakeup.library.wheel.WheelView;
import com.ycengine.wakeup.util.FragmentTagUtils;
import com.ycengine.yclibrary.util.CastUtil;
import com.ycengine.yclibrary.util.CommonUtil;
import com.ycengine.yclibrary.util.DateUtil;
import com.ycengine.yclibrary.util.DeviceUtil;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;
import com.ycengine.yclibrary.widget.YCEngineDialog;

import java.util.ArrayList;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class SettingActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage {
    private static final int TYPE_GAME = 1;

    private WeakRefHandler mWeakRefHandler;
    private InputMethodManager imm;

    private int mPosition;
    private NotifyItem mNotifyItem = null;
    private ArrayList<String> repeatDay = new ArrayList<>();

    private RelativeLayout mRootLayout;
    private ImageButton mTrashBtn;
    private LinearLayout mGameNon, mGameCalcuration, mGameSimpleCalcuration, mGamePuzzle;
    private WheelView mHourWheelView, mMinuteWheelView;
    private PlayerTimerAdapter mHourAdapter, mMinuteAdapter;

    private RelativeLayout mMonBtn, mTueBtn, mWedBtn, mThuBtn, mFriBtn, mSatBtn, mSunBtn;
    private SeekBar mSeekBar;
    private Switch mSoundSwitch, mVibrateSwitch;
    private boolean isSound = false, isVibrate = false;

    private TextView mNotiTargetDay, mRingTone;
    private EditText mEditText;

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

        mGameNon = (LinearLayout) findViewById(R.id.mGameNon);
        mGameCalcuration = (LinearLayout) findViewById(R.id.mGameCalcuration);
        mGameSimpleCalcuration = (LinearLayout) findViewById(R.id.mGameSimpleCalcuration);
        mGamePuzzle = (LinearLayout) findViewById(R.id.mGamePuzzle);

        mHourAdapter = new PlayerTimerAdapter(mContext, "HOUR");
        mHourWheelView = (WheelView) findViewById(R.id.mHourWheelView);
        mHourWheelView.setViewAdapter(mHourAdapter);
        mHourWheelView.setDrawShadows(false);
        mHourWheelView.setCyclic(true);
        mHourWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                mHourAdapter.setCurrentItem("HOUR", wheel.getCurrentItem());
            }
        });

        mMinuteAdapter = new PlayerTimerAdapter(mContext, "MINUTE");
        mMinuteWheelView = (WheelView) findViewById(R.id.mMinuteWheelView);
        mMinuteWheelView.setViewAdapter(mMinuteAdapter);
        mMinuteWheelView.setDrawShadows(false);
        mMinuteWheelView.setCyclic(true);
        mMinuteWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                mMinuteAdapter.setCurrentItem("MINUTE", wheel.getCurrentItem());
            }
        });

        mMonBtn = (RelativeLayout) findViewById(R.id.mMonBtn);
        mTueBtn = (RelativeLayout) findViewById(R.id.mTueBtn);
        mWedBtn = (RelativeLayout) findViewById(R.id.mWedBtn);
        mThuBtn = (RelativeLayout) findViewById(R.id.mThuBtn);
        mFriBtn = (RelativeLayout) findViewById(R.id.mFriBtn);
        mSatBtn = (RelativeLayout) findViewById(R.id.mSatBtn);
        mSunBtn = (RelativeLayout) findViewById(R.id.mSunBtn);

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

        mNotiTargetDay = (TextView) findViewById(R.id.mNotiTargetDay);

        mEditText = (EditText) findViewById(R.id.mEditText);
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.DB_NOTIFY_NAME_MAX_LENGTH)});
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
        mNotifyItem = intent.getParcelableExtra("NotifyItem");
        mPosition = intent.getIntExtra("position", -1);

        if (mNotifyItem == null) {
            mNotifyItem = new NotifyItem();
            mTrashBtn.setVisibility(View.GONE);
        } else {
            mTrashBtn.setVisibility(View.VISIBLE);
        }

        /* 알람 해지 미션 */
        if (mNotifyItem.getNotiGame() == Constants.NOTI_GAME_NON)
            mGameNon.setBackgroundResource(R.drawable.bg_round_setting_item_on);
        else if (mNotifyItem.getNotiGame() == Constants.NOTI_GAME_CALCULATION)
            mGameCalcuration.setBackgroundResource(R.drawable.bg_round_setting_item_on);
        else if (mNotifyItem.getNotiGame() == Constants.NOTI_GAME_CALCULATION_SIMPLE)
            mGameSimpleCalcuration.setBackgroundResource(R.drawable.bg_round_setting_item_on);
        else if (mNotifyItem.getNotiGame() == Constants.NOTI_GAME_PUZZLE)
            mGamePuzzle.setBackgroundResource(R.drawable.bg_round_setting_item_on);

        /* 알람 시간 */
        mWeakRefHandler.sendEmptyMessageDelayed(Constants.REQUEST_INIT_TIME, 500);

        /* 알람 이름 */
        mEditText.setText(mNotifyItem.getNotiName());

        /* 요일반복 */
        String notiDay = mNotifyItem.getNotiDay();
        if (CommonUtil.isNull(notiDay) || notiDay.length() != 7) {
            notiDay = "0000000";
        }

        for (int i = 0; i < notiDay.length(); i++) {
            repeatDay.add(notiDay.substring(i, i + 1));
        }

        updateRepeatDayUI();

        /* 알람음 볼륨 */
        mSeekBar.setProgress(mNotifyItem.getNotiVolume());

        /* 알람음 및 진동 */
        if (mNotifyItem.getNotiType() == Constants.NOTI_TYPE_VIBRATE_SOUND) {
            isSound = true;
            isVibrate = true;
        } else if (mNotifyItem.getNotiType() == Constants.NOTI_TYPE_SOUND) {
            isSound = true;
            isVibrate = false;
        } else if (mNotifyItem.getNotiType() == Constants.NOTI_TYPE_VIBRATE) {
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
                        mNotifyItem.setNotiRingTone(ringtoneUri.toString());
                        String ringTone = RingtoneManager.getRingtone(mContext, getSelectedRingtoneUri()).getTitle(mContext);
                        mRingTone.setText(ringTone);
                        /*final Alarm oldAlarm = getAlarm();
                        Alarm newAlarm = oldAlarm.toBuilder()
                                .ringtone(ringtoneUri.toString())
                                .build();
                        oldAlarm.copyMutableFieldsTo(newAlarm);
                        persistUpdatedAlarm(newAlarm, false);*/
                    }
                }
        );
    }

    private String makeTag(@IdRes int viewId) {
        return FragmentTagUtils.makeTag(SettingActivity.class, viewId, mNotifyItem.getNotiIdx());
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
        String ringtone = mNotifyItem.getNotiRingTone();
        return ringtone.isEmpty() ? RingtoneManager.getActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_ALARM) : Uri.parse(ringtone);
    }

    private void clearEffect(int type) {
        if (type == TYPE_GAME) {
            mGameNon.setBackgroundResource(R.drawable.bg_round_setting_item_off);
            mGameCalcuration.setBackgroundResource(R.drawable.bg_round_setting_item_off);
            mGameSimpleCalcuration.setBackgroundResource(R.drawable.bg_round_setting_item_off);
            mGamePuzzle.setBackgroundResource(R.drawable.bg_round_setting_item_off);
        }
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
        StringBuilder notiDay = new StringBuilder();
        String suffixTag = ", ";
        boolean isRepeatDayEnabled = false;
        for (int i = 0; i < repeatDay.size(); i++) {
            switch (i) {
                case 0:
                    if ("1".equals(repeatDay.get(i))) {
                        mSunBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                        notiDay.append(getString(R.string.sunday));
                        isRepeatDayEnabled = true;
                    } else
                        mSunBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 1:
                    if ("1".equals(repeatDay.get(i))) {
                        mMonBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                        if (CommonUtil.isNotNull(notiDay.toString())) notiDay.append(suffixTag);
                        notiDay.append(getString(R.string.monday));
                        isRepeatDayEnabled = true;
                    } else
                        mMonBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 2:
                    if ("1".equals(repeatDay.get(i))) {
                        mTueBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                        if (CommonUtil.isNotNull(notiDay.toString())) notiDay.append(suffixTag);
                        notiDay.append(getString(R.string.tuesday));
                        isRepeatDayEnabled = true;
                    } else
                        mTueBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 3:
                    if ("1".equals(repeatDay.get(i))) {
                        mWedBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                        if (CommonUtil.isNotNull(notiDay.toString())) notiDay.append(suffixTag);
                        notiDay.append(getString(R.string.wednesday));
                        isRepeatDayEnabled = true;
                    } else
                        mWedBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 4:
                    if ("1".equals(repeatDay.get(i))) {
                        mThuBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                        if (CommonUtil.isNotNull(notiDay.toString())) notiDay.append(suffixTag);
                        notiDay.append(getString(R.string.thursday));
                        isRepeatDayEnabled = true;
                    } else
                        mThuBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 5:
                    if ("1".equals(repeatDay.get(i))) {
                        mFriBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                        if (CommonUtil.isNotNull(notiDay.toString())) notiDay.append(suffixTag);
                        notiDay.append(getString(R.string.friday));
                        isRepeatDayEnabled = true;
                    } else
                        mFriBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
                case 6:
                    if ("1".equals(repeatDay.get(i))) {
                        mSatBtn.setBackgroundResource(R.drawable.bg_round_item_on);
                        if (CommonUtil.isNotNull(notiDay.toString())) notiDay.append(suffixTag);
                        notiDay.append(getString(R.string.saturday));
                        isRepeatDayEnabled = true;
                    } else
                        mSatBtn.setBackgroundResource(R.drawable.bg_round_item_off);
                    break;
            }
        }

        if (isRepeatDayEnabled) {
            mNotiTargetDay.setText(notiDay.toString());
            mNotifyItem.setNotiSpecialDay(-1);
        } else {
            if (mNotifyItem.getNotiSpecialDay() == -1)
                mNotifyItem.setNotiSpecialDay(Integer.valueOf(DateUtil.getCurrentAddDayDate("yyyyMMdd", 1)));
            mNotiTargetDay.setText(DateUtil.getChangeDateFormat(String.valueOf(mNotifyItem.getNotiSpecialDay()), "yyyyMMdd", "yyyy.MM.dd (EEE)"));
        }
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
            case R.id.mPreviewBtn:
                if (mNotifyItem.getNotiGame() == Constants.NOTI_GAME_NON) {
                    intent = new Intent(mContext, NonActivity.class);
                    intent.putExtra("preview", true);
                    startActivity(intent);
                } else if (mNotifyItem.getNotiGame() == Constants.NOTI_GAME_CALCULATION) {
                    intent = new Intent(mContext, CalculationActivity.class);
                    intent.putExtra("preview", true);
                    startActivity(intent);
                } else if (mNotifyItem.getNotiGame() == Constants.NOTI_GAME_CALCULATION_SIMPLE) {
                    intent = new Intent(mContext, CalculationSimpleActivity.class);
                    intent.putExtra("preview", true);
                    startActivity(intent);
                } else if (mNotifyItem.getNotiGame() == Constants.NOTI_GAME_PUZZLE) {
                    intent = new Intent(mContext, PuzzleActivity.class);
                    intent.putExtra("preview", true);
                    startActivity(intent);
                }
                break;
            case R.id.mGameNon:
                mNotifyItem.setNotiGame(Constants.NOTI_GAME_NON);
                clearEffect(TYPE_GAME);
                v.setBackgroundResource(R.drawable.bg_round_setting_item_on);
                break;
            case R.id.mGameCalcuration:
                mNotifyItem.setNotiGame(Constants.NOTI_GAME_CALCULATION);
                clearEffect(TYPE_GAME);
                v.setBackgroundResource(R.drawable.bg_round_setting_item_on);
                break;
            case R.id.mGameSimpleCalcuration:
                mNotifyItem.setNotiGame(Constants.NOTI_GAME_CALCULATION_SIMPLE);
                clearEffect(TYPE_GAME);
                v.setBackgroundResource(R.drawable.bg_round_setting_item_on);
                break;
            case R.id.mGamePuzzle:
                mNotifyItem.setNotiGame(Constants.NOTI_GAME_PUZZLE);
                clearEffect(TYPE_GAME);
                v.setBackgroundResource(R.drawable.bg_round_setting_item_on);
                break;
            case R.id.mCalendarBtn:
                mYCEngineDialog = new YCEngineDialog(mContext, com.ycengine.yclibrary.Constants.DIALOG_TYPE_CALENDAR, this, mNotifyItem.getNotiSpecialDay());
                mYCEngineDialog.show();
                break;
            case R.id.mDialogCancelBtn:
                if (mYCEngineDialog != null && mYCEngineDialog.isShowing())
                    mYCEngineDialog.dismiss();
                break;
            case R.id.mDialogConfirmBtn:
                int targetDay = (Integer) v.getTag(R.id.tag_data);
                mNotifyItem.setNotiSpecialDay(targetDay);

                for (int i = 0; i < repeatDay.size(); i++)
                    repeatDay.set(i, "0");

                updateRepeatDayUI();

                if (mYCEngineDialog != null && mYCEngineDialog.isShowing())
                    mYCEngineDialog.dismiss();
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
                if (!isSound && !isVibrate) {
                    DeviceUtil.showToast(mContext, getString(R.string.msg_choice_noti_type));
                } else if (isPastTime()) {
                    DeviceUtil.showToast(mContext, getString(R.string.msg_already_pass_time));
                } else {
                    StringBuilder notiDay = new StringBuilder();
                    for (String item : repeatDay) {
                        notiDay.append(item);
                    }

                    if (isSound && isVibrate) {
                        mNotifyItem.setNotiType(Constants.NOTI_TYPE_VIBRATE_SOUND);
                    } else if (isSound && !isVibrate) {
                        mNotifyItem.setNotiType(Constants.NOTI_TYPE_SOUND);
                    } else if (!isSound && isVibrate) {
                        mNotifyItem.setNotiType(Constants.NOTI_TYPE_VIBRATE);
                    }

                    mNotifyItem.setNotiHour(mHourWheelView.getCurrentItem());
                    mNotifyItem.setNotiMinute(mMinuteWheelView.getCurrentItem());
                    mNotifyItem.setNotiDay(notiDay.toString());
                    mNotifyItem.setNotiVolume(mSeekBar.getProgress());
                    mNotifyItem.setNotiName(mEditText.getText().toString());

                    if (mPosition == -1)
                        mNotifyItem.setNotiRegDate(DateUtil.getCurrentDate("yyyyMMddHHmmssSSS"));

                    intent = getIntent();
                    intent.putExtra("item", mNotifyItem);
                    intent.putExtra("position", mPosition);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }

    private boolean isPastTime() {
        if (mNotifyItem.getNotiSpecialDay() == -1)
            return false;

        String selectDateTime = String.valueOf(mNotifyItem.getNotiSpecialDay()) + CastUtil.getFillString(mHourWheelView.getCurrentItem(), 2) + CastUtil.getFillString(mMinuteWheelView.getCurrentItem(), 2);
        String currentDateTime = DateUtil.getCurrentDate("yyyyMMddHHmm");

        if (Long.valueOf(selectDateTime) > Long.valueOf(currentDateTime))
            return false;

        return true;
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();

        switch (msg.what) {
            case Constants.REQUEST_INIT_TIME:
                mHourWheelView.setCurrentItem(mNotifyItem.getNotiHour(), true);
                mMinuteWheelView.setCurrentItem(mNotifyItem.getNotiMinute(), true);
                break;
        }
    }
}
