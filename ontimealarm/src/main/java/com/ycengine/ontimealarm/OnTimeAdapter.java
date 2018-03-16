package com.ycengine.ontimealarm;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.ycengine.ontimealarm.data.OnTimeItem;
import com.ycengine.yclibrary.util.CommonUtil;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.util.ArrayList;

public class OnTimeAdapter extends BaseAdapter implements View.OnClickListener {
    private Context mContext = null;
    private LayoutInflater mInflater;
    private ArrayList<OnTimeItem> mItems = null;
    private WeakRefHandler mWeakRefHandler = null;

    public OnTimeAdapter(Context context, ArrayList<OnTimeItem> items, WeakRefHandler handler) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mItems = items;
        mWeakRefHandler = handler;
    }

    public ArrayList<OnTimeItem> getAdapterData() {
        return mItems;
    }

    public void addAllItems(ArrayList<OnTimeItem> data) {
        if (data != null) mItems = data;
        else mItems.clear();
        notifyDataSetChanged();
    }

    public void addItem(OnTimeItem item) {
        if (item != null) {
            mItems.add(item);
            notifyDataSetChanged();
        }
    }

    public void setItem(int position, OnTimeItem item) {
        if (item != null) {
            mItems.set(position, item);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public OnTimeItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        LinearLayout mRootLayout;
        TextView tvNotifyName, tvNotifyDay;
        SeekBar mSeekBar;
        Switch mEnabledSwitch;

        public ViewHolder(View view) {
            mRootLayout = (LinearLayout) view.findViewById(R.id.mRootLayout);
            tvNotifyName = (TextView) view.findViewById(R.id.tvNotifyName);
            tvNotifyDay = (TextView) view.findViewById(R.id.tvNotifyDay);
            mSeekBar = (SeekBar) view.findViewById(R.id.mSeekBar);
            mEnabledSwitch = (Switch) view.findViewById(R.id.mEnabledSwitch);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        OnTimeAdapter.ViewHolder viewHolder;
        OnTimeItem item = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_on_time, parent, false);
            viewHolder = new OnTimeAdapter.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (OnTimeAdapter.ViewHolder) convertView.getTag();
        }

        try {
            viewHolder.mRootLayout.setTag(R.id.position, position);
            viewHolder.mRootLayout.setOnClickListener(this);

            /*ArrayList<String> repeatHour = new ArrayList<>();
            String onTimeHour = item.getOnTimeHour();
            for (int i = 0; i < onTimeHour.length(); i++) {
                repeatHour.add(onTimeHour.substring(i, i + 1));
            }

            String suffixTag = ",";
            if (repeatHour.size() == 24) {
                StringBuilder displayOnTimeHour = new StringBuilder();
                for (int i = 0; i < repeatHour.size(); i++) {
                    switch (i) {
                        case 0: if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_00)); break;
                        case 1: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_01)); break;
                        case 2: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_02)); break;
                        case 3: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_03)); break;
                        case 4: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_04)); break;
                        case 5: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_05)); break;
                        case 6: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_06)); break;
                        case 7: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_07)); break;
                        case 8: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_08)); break;
                        case 9: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_09)); break;
                        case 10: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_10)); break;
                        case 11: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_11)); break;
                        case 12: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_12)); break;
                        case 13: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_13)); break;
                        case 14: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_14)); break;
                        case 15: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_15)); break;
                        case 16: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_16)); break;
                        case 17: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_17)); break;
                        case 18: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_18)); break;
                        case 19: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_19)); break;
                        case 20: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_20)); break;
                        case 21: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_21)); break;
                        case 22: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_22)); break;
                        case 23: if (CommonUtil.isNotNull(displayOnTimeHour.toString())) displayOnTimeHour.append(suffixTag); if ("1".equals(repeatHour.get(i))) displayOnTimeHour.append(mContext.getString(R.string.hour_23)); break;
                    }
                }

                StringBuilder displayOnTimeHours = new StringBuilder();
                for (int i = 0; i < repeatHour.size(); i++) {
                    if ("1".equals(repeatHour.get(i))) {
                        if (CommonUtil.isNotNull(displayOnTimeHours.toString())) displayOnTimeHours.append(suffixTag);
                        displayOnTimeHours.append(String.valueOf(i));
                    }
                }
                viewHolder.tvNotifyTime.setText(displayOnTimeHours.toString());
            } else {
                viewHolder.tvNotifyTime.setText("ERROR");
            }*/

            if (CommonUtil.isNotNull(item.getOnTimeName())) {
                viewHolder.tvNotifyName.setText(item.getOnTimeName());
                viewHolder.tvNotifyName.setAlpha(1.0f);
            } else {
                viewHolder.tvNotifyName.setText(R.string.app_name);
                viewHolder.tvNotifyName.setAlpha(0.5f);
            }

            viewHolder.mSeekBar.setProgress(item.getOnTimeVolume());
            if (item.getOnTimeType() == Constants.ONTIME_TYPE_VIBRATE) {
                viewHolder.mSeekBar.setAlpha(0.3f);
            } else {
                viewHolder.mSeekBar.setAlpha(1.0f);
            }

            ArrayList<String> repeatDay = new ArrayList<>();
            String notiDay = item.getOnTimeDay();
            for (int i = 0; i < notiDay.length(); i++) {
                repeatDay.add(notiDay.substring(i, i + 1));
            }

            String suffixTag = " ";
            StringBuilder displayNotiDay = new StringBuilder();
            for (int i = 0; i < repeatDay.size(); i++) {
                switch (i) {
                    case 0:
                        if ("1".equals(repeatDay.get(i))) {
                            displayNotiDay.append("<font color='#e53433'>" + mContext.getString(R.string.sunday) + "</font>");
                        }
                        break;
                    case 1:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.monday));
                        }
                        break;
                    case 2:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.tuesday));
                        }
                        break;
                    case 3:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.wednesday));
                        }
                        break;
                    case 4:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.thursday));
                        }
                        break;
                    case 5:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.friday));
                        }
                        break;
                    case 6:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append("<font color='#005a9e'>" + mContext.getString(R.string.saturday) + "</font>");
                        }
                        break;
                }
            }

            viewHolder.tvNotifyDay.setText(Html.fromHtml(displayNotiDay.toString()));

            viewHolder.mEnabledSwitch.setChecked(item.getOnTimeEnabled() == Constants.ONTIME_ENABLED);
            viewHolder.mEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Bundle data = new Bundle();
                    Message msg = new Message();
                    data.putInt("position", position);
                    data.putBoolean("enabled", isChecked);
                    msg.setData(data);
                    msg.what = Constants.REQUEST_NOTIFY_CHANGE_ENABLED;
                    mWeakRefHandler.sendMessage(msg);
                }
            });

            if (item.getOnTimeEnabled() == Constants.ONTIME_ENABLED) {
                viewHolder.mRootLayout.setAlpha(1.0f);
            } else {
                viewHolder.mRootLayout.setAlpha(0.5f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();
        int position = -1;

        switch (v.getId()) {
            case R.id.mRootLayout:
                position = (int) v.getTag(R.id.position);

                data.putInt("position", position);
                msg.setData(data);
                msg.what = Constants.REQUEST_NOTIFY_CHANGE_DATA;
                mWeakRefHandler.sendMessage(msg);
                break;
        }
    }
}
