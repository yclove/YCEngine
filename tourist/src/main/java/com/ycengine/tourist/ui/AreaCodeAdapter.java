package com.ycengine.tourist.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ycengine.tourist.R;
import com.ycengine.tourist.model.CodeItem;

import java.util.List;


public class AreaCodeAdapter extends RecyclerView.Adapter<AreaCodeAdapter.ViewHolder>{
    private Context mContext;
    private List<CodeItem> mItems = null;

    public AreaCodeAdapter(Context context, List<CodeItem> items){
        mContext = context;
        mItems = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mAreaName;

        public ViewHolder(View v){
            super(v);
            mAreaName = (TextView) v.findViewById(R.id.mAreaName);
        }
    }

    @Override
    public AreaCodeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Create a new View
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_area_code,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.mAreaName.setText(mItems.get(position).getName());
    }

    @Override
    public int getItemCount(){
        return mItems.size();
    }

    public void addAllItems(List<CodeItem> data) {
        if (data != null) mItems = data;
        else mItems.clear();
        notifyDataSetChanged();
    }
}