package com.ycengine.shoppinglist;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ycengine.shoppinglist.data.GoodsItem;
import com.ycengine.shoppinglist.library.dslv.DragSortListView;
import com.ycengine.yclibrary.util.DateUtil;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GoodsAdapterBackBeforeRecyclerView extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener {
    private Context mContext = null;
    private LayoutInflater mInflater;
    private ArrayList<GoodsItem> mItems = null;
    private WeakRefHandler mWeakRefHandler = null;

    public GoodsAdapterBackBeforeRecyclerView(Context context, ArrayList<GoodsItem> items, WeakRefHandler handler) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mItems = items;
        mWeakRefHandler = handler;
    }

    public ArrayList<GoodsItem> getAdapterData() {
        return mItems;
    }

    public void addAllItems(ArrayList<GoodsItem> data) {
        if (data != null) mItems = data;
        else mItems.clear();
        notifyDataSetChanged();
    }

    public void addItem(GoodsItem item) {
        if (item != null) {
            mItems.add(item);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public GoodsItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        CheckBox mCheckBox;
        TextView tvGoodsName, tvGoodsRegDate;
        Button mItemBtn;
        RelativeLayout mTrashBtn, drag_handle;

        public ViewHolder(View view) {
            mCheckBox = (CheckBox) view.findViewById(R.id.mCheckBox);
            tvGoodsName = (TextView) view.findViewById(R.id.tvGoodsName);
            tvGoodsRegDate = (TextView) view.findViewById(R.id.tvGoodsRegDate);

            mItemBtn = (Button) view.findViewById(R.id.mItemBtn);
            mTrashBtn = (RelativeLayout) view.findViewById(R.id.mTrashBtn);
            drag_handle = (RelativeLayout) view.findViewById(R.id.drag_handle);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GoodsAdapterBackBeforeRecyclerView.ViewHolder viewHolder;
        GoodsItem item = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_handle_right, parent, false);
            viewHolder = new GoodsAdapterBackBeforeRecyclerView.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GoodsAdapterBackBeforeRecyclerView.ViewHolder) convertView.getTag();
        }

        try {
            String goodsRegDate = item.getGoodsRegDate();
            DateFormat originalFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.ENGLISH);
            Date date = originalFormat.parse(goodsRegDate);
            goodsRegDate = targetFormat.format(date);

            goodsRegDate = DateUtil.getDateDisplay(date);

            viewHolder.tvGoodsName.setText(item.getGoodsName());
            viewHolder.tvGoodsRegDate.setText(mContext.getString(R.string.txt_goods_reg_date, goodsRegDate));

            if (Constants.FLAG_GOODS_EVENT_TYPE_AFTER.equals(item.getGoodsEventType())) {
                viewHolder.mCheckBox.setChecked(true);
                viewHolder.tvGoodsName.setTextColor(Color.parseColor("#888888"));
                viewHolder.tvGoodsRegDate.setTextColor(Color.parseColor("#888888"));
                viewHolder.tvGoodsName.setPaintFlags(viewHolder.tvGoodsName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                viewHolder.tvGoodsRegDate.setPaintFlags(viewHolder.tvGoodsRegDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                viewHolder.mCheckBox.setChecked(false);
                viewHolder.tvGoodsName.setTextColor(Color.parseColor("#000000"));
                viewHolder.tvGoodsRegDate.setTextColor(Color.parseColor("#000000"));
                viewHolder.tvGoodsName.setPaintFlags(0);
                viewHolder.tvGoodsRegDate.setPaintFlags(0);
            }

            if (parent instanceof DragSortListView) {
                viewHolder.drag_handle.setVisibility(View.VISIBLE);
                viewHolder.mTrashBtn.setVisibility(View.GONE);
            } else {
                viewHolder.drag_handle.setVisibility(View.GONE);
                viewHolder.mTrashBtn.setVisibility(View.VISIBLE);
            }

            viewHolder.mItemBtn.setTag(R.id.position, position);
            viewHolder.mItemBtn.setOnClickListener(this);
            viewHolder.mItemBtn.setOnLongClickListener(this);
            viewHolder.mTrashBtn.setTag(R.id.position, position);
            viewHolder.mTrashBtn.setOnClickListener(this);
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
            case R.id.mItemBtn:
                position = (int) v.getTag(R.id.position);

                data.putInt("position", position);
                msg.setData(data);
                msg.what = Constants.QUERY_UPDATE_GOODS;
                mWeakRefHandler.sendMessage(msg);
                break;
            case R.id.mTrashBtn:
                position = (int) v.getTag(R.id.position);

                data.putInt("position", position);
                msg.setData(data);
                msg.what = Constants.QUERY_DELETE_GOODS;
                mWeakRefHandler.sendMessage(msg);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();
        int position = -1;

        switch (v.getId()) {
            case R.id.mItemBtn:
                position = (int) v.getTag(R.id.position);

                data.putInt("position", position);
                msg.setData(data);
                msg.what = Constants.QUERY_RENAME_GOODS;
                mWeakRefHandler.sendMessage(msg);
                break;
        }
        return true; // 다음 이벤트 계속 진행 false, 이벤트 완료 true
    }
}
