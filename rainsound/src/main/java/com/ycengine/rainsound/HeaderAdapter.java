package com.ycengine.rainsound;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ycengine.rainsound.data.CoreItem;
import com.ycengine.yclibrary.util.DeviceUtil;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;
import com.ycengine.yclibrary.widget.ColorCircleDrawable;

import java.util.ArrayList;
import java.util.List;

public class HeaderAdapter extends PagerAdapter implements View.OnClickListener {
    Context mContext;
    WeakRefHandler mHandler;
    LayoutInflater inflater;
    List<CoreItem> mItems = new ArrayList<>();
    String mColor;
    boolean isLoop = true;

    public boolean isLoop() {
        return isLoop;
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public HeaderAdapter(Context context, LayoutInflater inflater, WeakRefHandler handler) {
        this.mContext = context;
        this.inflater = inflater;
        this.mHandler = handler;
    }

    @Override
    public int getCount() {
        if (isLoop) {
            return Integer.MAX_VALUE;
        } else {
            return mItems.size();
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (isLoop) {
            position = position % mItems.size();
        }

        View view = inflater.inflate(R.layout.viewpager_today, null);
        CoreItem item = mItems.get(position);

        RelativeLayout vg_today = (RelativeLayout) view.findViewById(R.id.vg_today);
        TextView tvCoreTitle = (TextView) view.findViewById(R.id.tvCoreTitle);
        ImageView ivCoreCircle = (ImageView) view.findViewById(R.id.ivCoreCircle);

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DeviceUtil.getScreenWidthInDPs(mContext) / 2, mContext.getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 92, mContext.getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        vg_today.setLayoutParams(lp);

//        vg_today.setTag(R.id.tag_color, item.getCOLOR());
        vg_today.setOnClickListener(this);

        tvCoreTitle.setText(item.getCoreTitle());
        ivCoreCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFFFFF")));

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View v, Object obj) {
        return v == obj;
    }

    public void addAllItems(ArrayList<CoreItem> data) {
        if (data != null) {
            mItems = data;
        } else {
            mItems.clear();
        }
        notifyDataSetChanged();
    }

    public void setAdapterColor(String color) {
        this.mColor = color;
    }

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();

        switch (v.getId()) {
            case R.id.vg_today:
                data.putString("ICI", "");
                msg.setData(data);
                msg.what = 1000;
                mHandler.sendMessage(msg);
                break;
        }
    }
}
