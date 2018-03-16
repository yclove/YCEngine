package com.ycengine.incheonairport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ycengine.incheonairport.protocol.data.AirportPassengerInfoItem;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AirportInfoAdapter extends BaseAdapter {
    private Context mContext = null;
    private LayoutInflater mInflater;
    private ArrayList<AirportPassengerInfoItem> mItems = null;

    public AirportInfoAdapter(Context context, ArrayList<AirportPassengerInfoItem> items) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mItems = items;
    }

    public void addAllItems(ArrayList<AirportPassengerInfoItem> data) {
        if (data != null) mItems = data;
        else mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public AirportPassengerInfoItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        TextView tvAirportPassengerTime, tvAirportPassengerGate5, tvAirportPassengerGate4, tvAirportPassengerGate3, tvAirportPassengerGate2;

        public ViewHolder(View view) {
            tvAirportPassengerTime = (TextView) view.findViewById(R.id.tvAirportPassengerTime);
            tvAirportPassengerGate5 = (TextView) view.findViewById(R.id.tvAirportPassengerGate5);
            tvAirportPassengerGate4 = (TextView) view.findViewById(R.id.tvAirportPassengerGate4);
            tvAirportPassengerGate3 = (TextView) view.findViewById(R.id.tvAirportPassengerGate3);
            tvAirportPassengerGate2 = (TextView) view.findViewById(R.id.tvAirportPassengerGate2);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AirportInfoAdapter.ViewHolder viewHolder;
        AirportPassengerInfoItem item = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_airport_passenger_info, parent, false);
            viewHolder = new AirportInfoAdapter.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AirportInfoAdapter.ViewHolder) convertView.getTag();
        }

        try {
            if (item.getAdate().length() == 8 && item.getAtime().length() == 5) {
                viewHolder.tvAirportPassengerTime.setText(mContext.getString(R.string.airport_info_at_time, item.getAdate().substring(4, 6), item.getAdate().substring(6, 8), item.getAtime().substring(0, 2)));
            } else {
                viewHolder.tvAirportPassengerTime.setText(item.getAtime());
            }

            if (position == 0) {
                viewHolder.tvAirportPassengerGate5.setText(item.getSum8());
                viewHolder.tvAirportPassengerGate4.setText(item.getSum7());
                viewHolder.tvAirportPassengerGate3.setText(item.getSum6());
                viewHolder.tvAirportPassengerGate2.setText(item.getSum5());
            } else {
                viewHolder.tvAirportPassengerGate5.setText(new DecimalFormat("#,###").format(Integer.valueOf(item.getSum8())));
                viewHolder.tvAirportPassengerGate4.setText(new DecimalFormat("#,###").format(Integer.valueOf(item.getSum7())));
                viewHolder.tvAirportPassengerGate3.setText(new DecimalFormat("#,###").format(Integer.valueOf(item.getSum6())));
                viewHolder.tvAirportPassengerGate2.setText(new DecimalFormat("#,###").format(Integer.valueOf(item.getSum5())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
