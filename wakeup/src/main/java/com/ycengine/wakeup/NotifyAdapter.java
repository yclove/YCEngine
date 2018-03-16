package com.ycengine.wakeup;

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

import com.ycengine.wakeup.data.NotifyItem;
import com.ycengine.yclibrary.util.CastUtil;
import com.ycengine.yclibrary.util.CommonUtil;
import com.ycengine.yclibrary.util.DateUtil;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.util.ArrayList;

public class NotifyAdapter extends BaseAdapter implements View.OnClickListener {
    private Context mContext = null;
    private LayoutInflater mInflater;
    private ArrayList<NotifyItem> mItems = null;
    private WeakRefHandler mWeakRefHandler = null;

    public NotifyAdapter(Context context, ArrayList<NotifyItem> items, WeakRefHandler handler) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mItems = items;
        mWeakRefHandler = handler;
    }

    public ArrayList<NotifyItem> getAdapterData() {
        return mItems;
    }

    public void addAllItems(ArrayList<NotifyItem> data) {
        if (data != null) mItems = data;
        else mItems.clear();
        notifyDataSetChanged();
    }

    public void addItem(NotifyItem item) {
        if (item != null) {
            mItems.add(item);
            notifyDataSetChanged();
        }
    }

    public void setItem(int position, NotifyItem item) {
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
    public NotifyItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        LinearLayout mRootLayout;
        TextView tvNotifyTime, tvNotifyName, tvNotifyDay;
        SeekBar mSeekBar;
        Switch mEnabledSwitch;

        public ViewHolder(View view) {
            mRootLayout = (LinearLayout) view.findViewById(R.id.mRootLayout);
            tvNotifyTime = (TextView) view.findViewById(R.id.tvNotifyTime);
            tvNotifyName = (TextView) view.findViewById(R.id.tvNotifyName);
            tvNotifyDay = (TextView) view.findViewById(R.id.tvNotifyDay);
            mSeekBar = (SeekBar) view.findViewById(R.id.mSeekBar);
            mEnabledSwitch = (Switch) view.findViewById(R.id.mEnabledSwitch);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        NotifyAdapter.ViewHolder viewHolder;
        NotifyItem item = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_notify, parent, false);
            viewHolder = new NotifyAdapter.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (NotifyAdapter.ViewHolder) convertView.getTag();
        }

        try {
            viewHolder.mRootLayout.setTag(R.id.position, position);
            viewHolder.mRootLayout.setOnClickListener(this);

            viewHolder.tvNotifyTime.setText(CastUtil.getFillString(item.getNotiHour(), 2) + ":" + CastUtil.getFillString(item.getNotiMinute(), 2));

            if (CommonUtil.isNotNull(item.getNotiName())) {
                viewHolder.tvNotifyName.setText(item.getNotiName());
                viewHolder.tvNotifyName.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvNotifyName.setVisibility(View.GONE);
            }

            viewHolder.mSeekBar.setProgress(item.getNotiVolume());
            if (item.getNotiType() == Constants.NOTI_TYPE_VIBRATE) {
                viewHolder.mSeekBar.setAlpha(0.3f);
            } else {
                viewHolder.mSeekBar.setAlpha(1.0f);
            }

            ArrayList<String> repeatDay = new ArrayList<>();
            String notiDay = item.getNotiDay();
            for (int i = 0; i < notiDay.length(); i++) {
                repeatDay.add(notiDay.substring(i, i + 1));
            }

            StringBuilder displayNotiDay = new StringBuilder();
            String suffixTag = " ";
            boolean isRepeatDayEnabled = false;
            for (int i = 0; i < repeatDay.size(); i++) {
                switch (i) {
                    case 0:
                        if ("1".equals(repeatDay.get(i))) {
                            displayNotiDay.append("<font color='#e53433'>" + mContext.getString(R.string.sunday) + "</font>");
                            isRepeatDayEnabled = true;
                        }
                        break;
                    case 1:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.monday));
                            isRepeatDayEnabled = true;
                        }
                        break;
                    case 2:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.tuesday));
                            isRepeatDayEnabled = true;
                        }
                        break;
                    case 3:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.wednesday));
                            isRepeatDayEnabled = true;
                        }
                        break;
                    case 4:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.thursday));
                            isRepeatDayEnabled = true;
                        }
                        break;
                    case 5:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append(mContext.getString(R.string.friday));
                            isRepeatDayEnabled = true;
                        }
                        break;
                    case 6:
                        if ("1".equals(repeatDay.get(i))) {
                            if (CommonUtil.isNotNull(displayNotiDay.toString())) displayNotiDay.append(suffixTag);
                            displayNotiDay.append("<font color='#005a9e'>" + mContext.getString(R.string.saturday) + "</font>");
                            isRepeatDayEnabled = true;
                        }
                        break;
                }
            }

            if (isRepeatDayEnabled) {
                viewHolder.tvNotifyDay.setText(Html.fromHtml(displayNotiDay.toString()));
            } else {
                viewHolder.tvNotifyDay.setText(DateUtil.getChangeDateFormat(String.valueOf(item.getNotiSpecialDay()), "yyyyMMdd", "yyyy.MM.dd (EEE)"));
            }

            viewHolder.mEnabledSwitch.setChecked(item.getNotiEnabled() == Constants.NOTI_ENABLED);
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

            if (item.getNotiEnabled() == Constants.NOTI_ENABLED) {
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
