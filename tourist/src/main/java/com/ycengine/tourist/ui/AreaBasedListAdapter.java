package com.ycengine.tourist.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ycengine.tourist.BaseRecyclerViewAdapter;
import com.ycengine.tourist.R;
import com.ycengine.tourist.databinding.ItemAreaBasedListBinding;
import com.ycengine.tourist.model.AreaBasedList;


public class AreaBasedListAdapter extends BaseRecyclerViewAdapter<AreaBasedList, AreaBasedListAdapter.ViewHolder> {
    public AreaBasedListAdapter(Context context) {
        super(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ItemAreaBasedListBinding binding;

        public ViewHolder(View v){
            super(v);
            binding = DataBindingUtil.bind(v);
        }
    }

    @Override
    public void onBindView(ViewHolder holder, int position) {
        AreaBasedList item = getItem(position);
        holder.binding.setArea(item);
    }

    @Override
    public AreaBasedListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_area_based_list, parent, false);
        return new ViewHolder(rootView);
    }
}