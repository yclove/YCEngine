package com.ycengine.yclibrary.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ycengine.yclibrary.Constants;
import com.ycengine.yclibrary.R;
import com.ycengine.yclibrary.util.CommonUtil;
import com.ycengine.yclibrary.util.DateUtil;
import com.ycengine.yclibrary.util.LogUtil;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class YCEngineDialog extends Dialog implements View.OnClickListener, NumberPicker.OnValueChangeListener {
    private Context mContext;
    private LinearLayout llSelectYearConfirmBtn;
    private ImageView ivNotifyAk01Circle, ivNotifyAk02Circle, ivNotifyAk03Circle, ivNotifyConfirm;
    private TextView tvNotifyAk01, tvNotifyAk02, tvNotifyAk03, tvNotifyConfirm;
    private LinearLayout btnNotifyConfirm;
    private int mTargetDay;
    private Button mDialogConfirmBtn, mDialogCancelBtn;

    public void setCancelabled(boolean cancleable) {
        setCancelable(cancleable);
    }

    public int getTargetDay() {
        return mTargetDay;
    }

    public void setTargetDay(int targetDay) {
        this.mTargetDay = targetDay;
    }

    public YCEngineDialog(Context context, int dialogType, View.OnClickListener onClickListener, Object... dialogMessage) {
        super(context, R.style.DialogTheme);
        this.mContext = context;
        setCancelable(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);

        View view;
        WindowManager.LayoutParams lp;

        switch (dialogType) {
            // 필수 업데이트 타입
            case Constants.DIALOG_TYPE_UPDATE_NEED:
                setCancelable(false);
                view = inflater.inflate(R.layout.dialog_update_need, null);
                setContentView(view);

                Button btnUpdateNeedConfirm = (Button)findViewById(R.id.btnUpdateNeedConfirm);

                btnUpdateNeedConfirm.setOnClickListener(onClickListener);
                break;
            // 권장 업데이트 타입
            case Constants.DIALOG_TYPE_UPDATE_SUPPORT:
                view = inflater.inflate(R.layout.dialog_update_support, null);
                setContentView(view);

                ImageView ivUpdateSupportUpdateBtn = (ImageView)findViewById(R.id.ivUpdateSupportUpdateBtn);
                Button btnUpdateSupportCancel = (Button)findViewById(R.id.btnUpdateSupportCancel);
                Button btnUpdateSupportSee = (Button)findViewById(R.id.btnUpdateSupportSee);

                ivUpdateSupportUpdateBtn.setOnClickListener(onClickListener);
                btnUpdateSupportCancel.setOnClickListener(this);
                btnUpdateSupportSee.setOnClickListener(onClickListener);
                break;
            // 태어난 해 선택 타입
            case Constants.DIALOG_TYPE_CHOICE_YEAR:
                view = inflater.inflate(R.layout.dialog_choice_year, null);
                setContentView(view);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;

                int currentYear = Integer.valueOf(DateUtil.getCurrentDate("yyyy"));
                int defaultYear = (CommonUtil.isNull((String)dialogMessage[0])) ? 2002 : Integer.valueOf((String)dialogMessage[0]);

                final NumberPicker numberPicker = (NumberPicker)findViewById(R.id.numberPicker);
                numberPickerTextColor( numberPicker, Color.BLACK );
                numberPicker.setMaxValue(currentYear);
                numberPicker.setMinValue(currentYear - 100);
                numberPicker.setValue(defaultYear);
                numberPicker.setWrapSelectorWheel(false);
                numberPicker.setOnValueChangedListener(this);

                setNumberPickerDividerColor(numberPicker);

                RelativeLayout rlSelectYearLayoutCloseBtn = (RelativeLayout)findViewById(R.id.rlSelectYearLayoutCloseBtn);
                rlSelectYearLayoutCloseBtn.setOnClickListener(this);

                llSelectYearConfirmBtn = (LinearLayout)findViewById(R.id.llSelectYearConfirmBtn);
                llSelectYearConfirmBtn.setTag(defaultYear);
                llSelectYearConfirmBtn.setOnClickListener(onClickListener);
                break;
            // 확인 타입
            case Constants.DIALOG_TYPE_CONFIRM:
                view = inflater.inflate(R.layout.dialog_confirm, null);
                setContentView(view);

                LinearLayout llConfrimTitle = (LinearLayout)findViewById(R.id.llConfrimTitle);
                TextView tvConfrimTitle = (TextView)findViewById(R.id.tvConfrimTitle);
                TextView tvConfrimMessage = (TextView)findViewById(R.id.tvConfrimMessage);
                Button btnConfirmCancel = (Button)findViewById(R.id.btnConfirmCancel);
                Button btnConfirmConfirm = (Button)findViewById(R.id.btnConfirmConfirm);

                tvConfrimMessage.setText((String)dialogMessage[0]);
                btnConfirmCancel.setOnClickListener(this);

                if (dialogMessage.length > 1 && dialogMessage[1] instanceof Bundle)
                    btnConfirmConfirm.setTag(R.id.tag_data, dialogMessage[1]);

                if (dialogMessage.length > 1 && dialogMessage[1] instanceof String) {
                    llConfrimTitle.setVisibility(View.VISIBLE);
                    tvConfrimTitle.setText((String)dialogMessage[1]);
                }

                btnConfirmConfirm.setOnClickListener(onClickListener);
                break;
            // 위치 확인 타입
            case Constants.DIALOG_TYPE_LOCATION_SERVICE:
                view = inflater.inflate(R.layout.dialog_location_service, null);
                setContentView(view);

                Button btnLocationServiceCancel = (Button)findViewById(R.id.btnLocationServiceCancel);
                Button btnLocationServiceSetting = (Button)findViewById(R.id.btnLocationServiceSetting);

                btnLocationServiceCancel.setOnClickListener(this);
                btnLocationServiceSetting.setOnClickListener(onClickListener);
                break;
            // 안내 타입
            case Constants.DIALOG_TYPE_INFO:
                view = inflater.inflate(R.layout.dialog_info, null);
                setContentView(view);

                LinearLayout llInfoTitle = (LinearLayout)findViewById(R.id.llInfoTitle);
                TextView tvInfoTitle = (TextView)findViewById(R.id.tvInfoTitle);
                TextView tvInfoMessage = (TextView)findViewById(R.id.tvInfoMessage);
                Button btnInfoConfirm = (Button)findViewById(R.id.btnInfoConfirm);

                tvInfoMessage.setText((String)dialogMessage[0]);

                if (dialogMessage.length > 1 && dialogMessage[1] instanceof String) {
                    llInfoTitle.setVisibility(View.VISIBLE);
                    tvInfoTitle.setText((String)dialogMessage[1]);
                }

                btnInfoConfirm.setOnClickListener(onClickListener);
                break;
            // RADIO 타이틀 선택
            case Constants.DIALOG_TYPE_RADIO_TITLE:
                view = inflater.inflate(R.layout.dialog_radio_title, null);
                setContentView(view);

                LinearLayout btnRadioTitleOnce = (LinearLayout)findViewById(R.id.btnRadioTitleOnce);
                LinearLayout btnRadioTitleCycle = (LinearLayout)findViewById(R.id.btnRadioTitleCycle);
                Button btnRadioTitleCancel = (Button)findViewById(R.id.btnRadioTitleCancel);

                btnRadioTitleOnce.setOnClickListener(onClickListener);
                btnRadioTitleCycle.setOnClickListener(onClickListener);
                btnRadioTitleCancel.setOnClickListener(this);
                break;
            // 통신사 선택
            case Constants.DIALOG_TYPE_NEWS_AGENCY:
                view = inflater.inflate(R.layout.dialog_news_agency, null);
                setContentView(view);

                LinearLayout llNewsAgencySkt = (LinearLayout)findViewById(R.id.llNewsAgencySkt);
                LinearLayout llNewsAgencyKt = (LinearLayout)findViewById(R.id.llNewsAgencyKt);
                LinearLayout llNewsAgencyLg = (LinearLayout)findViewById(R.id.llNewsAgencyLg);
                LinearLayout llNewsAgencySaving = (LinearLayout)findViewById(R.id.llNewsAgencySaving);

                llNewsAgencySkt.setOnClickListener(onClickListener);
                llNewsAgencyKt.setOnClickListener(onClickListener);
                llNewsAgencyLg.setOnClickListener(onClickListener);
                llNewsAgencySaving.setOnClickListener(onClickListener);
                break;
            // 우표 필요 타입
            case Constants.DIALOG_TYPE_STAMP_REQUIRED:
                view = inflater.inflate(R.layout.dialog_stamp_required, null);
                setContentView(view);

                // 자세히 보기
                Button btnDetailView = (Button)findViewById(R.id.btnDetailView);
                btnDetailView.setOnClickListener(onClickListener);

                // 확인
                Button btnStampRequiredConfirm = (Button)findViewById(R.id.btnStampRequiredConfirm);
                btnStampRequiredConfirm.setOnClickListener(this);
                break;
            // 신고하기 타입
            case Constants.DIALOG_TYPE_NOTIFY:
                view = inflater.inflate(R.layout.dialog_notify, null);
                setContentView(view);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;

                RelativeLayout btnNotifyCloseLayout = (RelativeLayout)findViewById(R.id.btnNotifyCloseLayout);
                LinearLayout btnNotifyAk01 = (LinearLayout)findViewById(R.id.btnNotifyAk01);
                LinearLayout btnNotifyAk02 = (LinearLayout)findViewById(R.id.btnNotifyAk02);
                LinearLayout btnNotifyAk03 = (LinearLayout)findViewById(R.id.btnNotifyAk03);

                btnNotifyConfirm = (LinearLayout)findViewById(R.id.btnNotifyConfirm);
                ivNotifyAk01Circle = (ImageView)findViewById(R.id.ivNotifyAk01Circle);
                ivNotifyAk02Circle = (ImageView)findViewById(R.id.ivNotifyAk02Circle);
                ivNotifyAk03Circle = (ImageView)findViewById(R.id.ivNotifyAk03Circle);
                ivNotifyConfirm = (ImageView)findViewById(R.id.ivNotifyConfirm);
                tvNotifyAk01 = (TextView)findViewById(R.id.tvNotifyAk01);
                tvNotifyAk02 = (TextView)findViewById(R.id.tvNotifyAk02);
                tvNotifyAk03 = (TextView)findViewById(R.id.tvNotifyAk03);
                tvNotifyConfirm = (TextView)findViewById(R.id.tvNotifyConfirm);

                btnNotifyConfirm.setTag(R.id.tag_dcre_plac_id, dialogMessage[0]);
                btnNotifyConfirm.setTag(R.id.tag_dcre_target_type, dialogMessage[1]);

                ivNotifyAk01Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk02Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk03Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));

                btnNotifyAk01.setOnClickListener(this);
                btnNotifyAk02.setOnClickListener(this);
                btnNotifyAk03.setOnClickListener(this);

                btnNotifyCloseLayout.setOnClickListener(this);
                btnNotifyConfirm.setOnClickListener(onClickListener);
                break;
            // 공유 타입
            case Constants.DIALOG_TYPE_SHARE:
                view = inflater.inflate(R.layout.dialog_share, null);
                setContentView(view);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;

                RelativeLayout rlShareLayoutCloseBtn = (RelativeLayout)findViewById(R.id.rlShareLayoutCloseBtn);
                rlShareLayoutCloseBtn.setOnClickListener(this);

                LinearLayout llShareImage = (LinearLayout)findViewById(R.id.llShareImage);
                LinearLayout llShareFacebook = (LinearLayout)findViewById(R.id.llShareFacebook);
                LinearLayout llShareTwitter = (LinearLayout)findViewById(R.id.llShareTwitter);
                LinearLayout llShareInstagram = (LinearLayout)findViewById(R.id.llShareInstagram);
                LinearLayout llShareLine = (LinearLayout)findViewById(R.id.llShareLine);
                LinearLayout llShareKakaoTalk = (LinearLayout)findViewById(R.id.llShareKakaoTalk);

                llShareImage.setOnClickListener(onClickListener);
                llShareFacebook.setOnClickListener(onClickListener);
                llShareTwitter.setOnClickListener(onClickListener);
                llShareInstagram.setOnClickListener(onClickListener);
                llShareLine.setOnClickListener(onClickListener);
                llShareKakaoTalk.setOnClickListener(onClickListener);
                break;
            // 공지 타입
            case Constants.DIALOG_TYPE_NOTICE:
                view = inflater.inflate(R.layout.dialog_notice, null);
                setContentView(view);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.DialogSlideUpAnimation;

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

                // Dialog 호출시 배경화면이 검정색으로 바뀌는 것 막기
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                TextView tvNoticeMsg = (TextView)findViewById(R.id.tvNoticeMsg);
                tvNoticeMsg.setText((String)dialogMessage[0]);

                RelativeLayout rlNoticeLayoutCloseBtn = (RelativeLayout)findViewById(R.id.rlNoticeLayoutCloseBtn);
                rlNoticeLayoutCloseBtn.setOnClickListener(this);
                break;
            // 달력 선택 타입
            case Constants.DIALOG_TYPE_CALENDAR:
                view = inflater.inflate(R.layout.dialog_calendar, null);
                setContentView(view);
                setCanceledOnTouchOutside(true);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;

                mTargetDay = ((Integer)dialogMessage[0] == -1) ? Integer.valueOf(DateUtil.getCurrentDate("yyyyMMdd")) : (Integer)dialogMessage[0];

                CalendarView mCalendarView = (CalendarView)findViewById(R.id.mCalendarView);
                mDialogCancelBtn = (Button)findViewById(R.id.mDialogCancelBtn);
                mDialogConfirmBtn = (Button)findViewById(R.id.mDialogConfirmBtn);

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

                try {
                    calendar.setTime(sdf.parse(DateUtil.getCurrentDate("yyyyMMdd")));
                } catch (ParseException e) {
                    e.printStackTrace();
                    dismiss();
                }
                long startOfMonth = calendar.getTimeInMillis();

                calendar = Calendar.getInstance();
                try {
                    calendar.setTime(sdf.parse(DateUtil.getCurrentDate("yyyyMMdd")));
                    Date date = DateUtil.addYears(calendar.getTime(), 1);
                    calendar.setTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                    dismiss();
                }
                long endOfMonth = calendar.getTimeInMillis();

                mCalendarView.setMaxDate(endOfMonth);
                mCalendarView.setMinDate(startOfMonth);

                calendar = Calendar.getInstance();
                try {
                    calendar.setTime(sdf.parse(String.valueOf(mTargetDay)));
                } catch (ParseException e) {
                    e.printStackTrace();
                    dismiss();
                }
                mCalendarView.setDate(calendar.getTimeInMillis());

                mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                        mTargetDay = Integer.valueOf(String.format("%04d", year) + String.format("%02d", month + 1) + String.format("%02d", dayOfMonth));
                        mDialogConfirmBtn.setTag(R.id.tag_data, mTargetDay);
                    }
                });

                mDialogCancelBtn.setOnClickListener(onClickListener);
                mDialogConfirmBtn.setTag(R.id.tag_data, mTargetDay);
                mDialogConfirmBtn.setOnClickListener(onClickListener);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 신고하기 타입 01 onClick
            /*case R.id.btnNotifyAk01:
                ivNotifyAk01Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFD73D66")));
                ivNotifyAk02Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk03Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                tvNotifyAk01.setTextColor(Color.parseColor("#FFD73D66"));
                tvNotifyAk02.setTextColor(Color.parseColor("#99000000"));
                tvNotifyAk03.setTextColor(Color.parseColor("#99000000"));
                btnNotifyConfirm.setTag(R.id.tag_dcre_resn_code, Constants.REQUEST_DCRE_RESN_CODE_AK01);
                updateNofityConfirmUI();
                break;
            // 신고하기 타입 02 onClick
            case R.id.btnNotifyAk02:
                ivNotifyAk01Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk02Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFD73D66")));
                ivNotifyAk03Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                tvNotifyAk01.setTextColor(Color.parseColor("#99000000"));
                tvNotifyAk02.setTextColor(Color.parseColor("#FFD73D66"));
                tvNotifyAk03.setTextColor(Color.parseColor("#99000000"));
                btnNotifyConfirm.setTag(R.id.tag_dcre_resn_code, Constants.REQUEST_DCRE_RESN_CODE_AK02);
                updateNofityConfirmUI();
                break;
            // 신고하기 타입 03 onClick
            case R.id.btnNotifyAk03:
                ivNotifyAk01Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk02Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk03Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFD73D66")));
                tvNotifyAk01.setTextColor(Color.parseColor("#99000000"));
                tvNotifyAk02.setTextColor(Color.parseColor("#99000000"));
                tvNotifyAk03.setTextColor(Color.parseColor("#FFD73D66"));
                btnNotifyConfirm.setTag(R.id.tag_dcre_resn_code, Constants.REQUEST_DCRE_RESN_CODE_AK03);
                updateNofityConfirmUI();
                break;*/
            default:
                dismiss();
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
        LogUtil.e("" + newVal);
        llSelectYearConfirmBtn.setTag(newVal);
    }

    private void setNumberPickerDividerColor(NumberPicker numberPicker){
        final int count = numberPicker.getChildCount();

        for (int i = 0; i < count; i++) {
            try{
                Field dividerField = numberPicker.getClass().getDeclaredField("mSelectionDivider");
                dividerField.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FFF2F2F2"));
                dividerField.set(numberPicker, colorDrawable);
                numberPicker.invalidate();
            } catch(NoSuchFieldException e){
                LogUtil.e(e.getMessage());
            } catch(IllegalAccessException e){
                LogUtil.e(e.getMessage());
            } catch(IllegalArgumentException e){
                LogUtil.e(e.getMessage());
            }
        }
    }

    void numberPickerTextColor(NumberPicker $v, int $c ){
        for(int i = 0, j = $v.getChildCount() ; i < j; i++){
            View t0 = $v.getChildAt(i);
            if( t0 instanceof EditText){
                try{
                    Field t1 = $v.getClass() .getDeclaredField( "mSelectorWheelPaint" );
                    t1.setAccessible(true);
                    ((Paint)t1.get($v)) .setColor($c);
                    ((Paint)t1.get($v)) .setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, mContext.getResources().getDisplayMetrics()));
                    ((EditText)t0) .setTextColor($c);
                    ((EditText)t0) .setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                    $v.invalidate();
                }catch(Exception e){}
            }
        }
    }

    private void updateNofityConfirmUI() {
        String notifyType = (String)btnNotifyConfirm.getTag(R.id.tag_dcre_resn_code);
        if (CommonUtil.isNull(notifyType)) {
            tvNotifyConfirm.setTextColor(Color.parseColor("#99000000"));
            ivNotifyConfirm.setImageResource(R.drawable.icon_chk_gray);
        } else {
            tvNotifyConfirm.setTextColor(Color.parseColor("#FF00AFD5"));
            ivNotifyConfirm.setImageResource(R.drawable.icon_chk_blue);
        }
    }

    // 신고하기 Layout 처리
    /*private void showChoiceNotifyLayout() {
        if(mChoiceNotifyLayout == null || isAnimationCheck == true) return;

        if (mChoiceNotifyLayout.getVisibility() == View.INVISIBLE) {
            mChoiceNotifyLayout.setVisibility(View.VISIBLE);
        }

        ObjectAnimator objectAnimator;
        if (!isChoiceNotifyVisible) {
            objectAnimator = ObjectAnimator.ofFloat(mChoiceNotifyLayout, "translationY", mChoiceNotifyLayout.getHeight(), 0.0f);
            mMainViewPager.setSwiping(false);
        }else{
            objectAnimator = ObjectAnimator.ofFloat(mChoiceNotifyLayout, "translationY", 0.0f, mChoiceNotifyLayout.getHeight());
            mMainViewPager.setSwiping(true);
        }

        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {isAnimationCheck = true;}

            @Override
            public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {isAnimationCheck = false;}

            @Override
            public void onAnimationCancel(Animator animation) {}
        });

        objectAnimator.setDuration(400);
        objectAnimator.start();
        objectAnimator = null;

        isChoiceNotifyVisible = !isChoiceNotifyVisible;
    }*/
}
