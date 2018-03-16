/*
 * Copyright (C) 2016 Nishant Srivastava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ycengine.shoppinglist;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ycengine.shoppinglist.data.GoodsItem;
import com.ycengine.yclibrary.library.helper.ItemTouchHelperAdapter;
import com.ycengine.yclibrary.library.helper.ItemTouchHelperViewHolder;
import com.ycengine.yclibrary.library.helper.OnStartDragListener;
import com.ycengine.yclibrary.util.DateUtil;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class GoodsAdapter extends RecyclerView.Adapter<GoodsAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {
    private Context mContext = null;
    private ArrayList<GoodsItem> mItems = null;
    private WeakRefHandler mWeakRefHandler = null;
    private boolean isChangeMode = false;
    private final OnStartDragListener mDragStartListener;
    private int mZoomLevel = 0;

    public GoodsAdapter(Context context, ArrayList<GoodsItem> items, WeakRefHandler handler, OnStartDragListener dragStartListener, int zoomLevel) {
        this.mContext = context;
        this.mItems = items;
        this.mWeakRefHandler = handler;
        this.mDragStartListener = dragStartListener;
        this.mZoomLevel = zoomLevel;
    }

    /**
     * YCNOTE - onCreateViewHolder는
     * 뷰 홀더를 생성하고 뷰를 붙여주는 부분입니다.
     * 리스트 뷰가 사용했던 getView( ) 메소드는 매번 호출되면서 null 처리를 해줘야했다면, onCreateViewHolder는 새롭게 생성될 때만 불립니다.
     */
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goods, parent, false);
        return new ItemViewHolder(rootView);
    }

    /**
     * YCNOTE - onBindViewHolder
     * 재활용 되는 뷰가 호출하여 실행되는 메소드, 뷰 홀더를 전달하고 어댑터는 position 의 데이터를 결합시킵니다.
     */
    @Override
    public void onBindViewHolder(final ItemViewHolder viewHolder, int position) {

        try {
            GoodsItem item = mItems.get(position);
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
                viewHolder.tvGoodsName.setPaintFlags(viewHolder.tvGoodsName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                viewHolder.tvGoodsRegDate.setPaintFlags(viewHolder.tvGoodsRegDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            } else {
                viewHolder.mCheckBox.setChecked(false);
                viewHolder.tvGoodsName.setTextColor(Color.parseColor("#000000"));
                viewHolder.tvGoodsRegDate.setTextColor(Color.parseColor("#000000"));
                viewHolder.tvGoodsName.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
                viewHolder.tvGoodsRegDate.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
            }

            viewHolder.tvGoodsName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16 + mZoomLevel);
            viewHolder.tvGoodsRegDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14 + mZoomLevel);

            if (isChangeMode) {
                viewHolder.mDragHandle.setVisibility(View.VISIBLE);
                viewHolder.mTrashBtn.setVisibility(View.GONE);
            } else {
                viewHolder.mDragHandle.setVisibility(View.GONE);
                viewHolder.mTrashBtn.setVisibility(View.VISIBLE);
            }

            // Start a drag whenever the handle view it touched
            viewHolder.mDragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(viewHolder);
                    }
                    return false;
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setZoomLevel(int zoomLevel) {
        if (zoomLevel <= 10 && zoomLevel >= -5) {
            this.mZoomLevel = zoomLevel;
            notifyDataSetChanged();
        }
    }

    public void setChangeMode(boolean changeMode) {
        this.isChangeMode = changeMode;
        notifyDataSetChanged();
    }

    /*
    * 데이터의 개수 반환
    * */
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);

        /*
            notifyDataSetChanged : 데이터가 전체 바뀌었을 때 호출. 즉, 처음 부터 끝까지 전부 바뀌었을 경우
            notifyItemChanged : 특정 Position의 위치만 바뀌었을 경우. position 4 번 위치만 데이터가 바뀌었을 경우 사용 하면 된다.
            notifyItemRangeChanged : 특정 영역을 데이터가 바뀌었을 경우. position 3~10번까지의 데이터만 바뀌었을 경우 사용 하면 된다.
            notifyItemInserted : 특정 Position에 데이터 하나를 추가 하였을 경우. position 3번과 4번 사이에 넣고자 할경우 4를 넣으면 되겠죠
            notifyItemRangeInserted : 특정 영역에 데이터를 추가할 경우. position 3~10번 자리에 7개의 새로운 데이터를 넣을 경우
            notifyItemRemoved : 특정 Position에 데이터를 하나 제거할 경우.
            notifyItemRangeRemoved : 특정 영역의 데이터를 제거할 경우
            notifyItemMoved : 특정 위치를 교환할 경우
        */
        notifyItemRemoved(position);
    }

    public void addItem(GoodsItem item) {
        if (item != null) {
            mItems.add(item);
            notifyDataSetChanged();
        }
    }

    /**
     * YCNOTE - RecyclerView.ViewHolder
     * 리스트뷰에서는 뷰홀더 패턴을 권장했습니다. UI 를 수정할 때 마다 부르는 findViewById( ) 를 뷰홀더 패턴을 이용해 한번만 함으로서 리스트 뷰의 지연을 초래하는 무거운 연산을 줄여주었습니다.
     * 이 문제를 리사이클러 뷰에서는 뷰 홀더 패턴을 꼭 사용하도록 함으로서 해결했습니다.
     * 하지만 뷰홀더 패턴을 기적이라고 생각하면 오산입니다. 실제로 당신의 앱의 퍼포먼스를 향상시켜주지만 오래된 기계일 수록 더 향상시켜 준다고 보시면 됩니다.
     * 다른 말로 최신의 파워풀한 디바이스는 뷰 홀더 패턴을 사용하지 않은 리스트 뷰나 리사이클러 뷰의 성능 차이는 미세하기 때문입니다.
     * 간과하기 쉬운 중요한 점은 뷰홀더 패턴을 사용한 리스트뷰와 리사이클러뷰의 성능은 같습니다. 단지 차이점은 리사이클링뷰는 뷰홀더 패턴이 강제되는 것일 뿐입니다.
     * 이전의 리스트 뷰는 선택적이었지만 성능 차이가 너무 컸기 때문에 변화된 것으로 생각됩니다.
     * <p>
     * 리스트 뷰는 수직 스크롤만 가능합니다. 리스트뷰를 수평으로 사용할 수는 없었죠. 그것을 구현하기 위한 방법이 몇가지 있다는 것을 알고는 있지만 리스트 뷰는 그렇게 동작하도록 디자인 되지 않았습니다.
     * 그러나 이제 리사이클러뷰에서는 수평 스크롤 또한 지원합니다. 뿐만 아니라 더 다양한 타입의 리스트들을 지원하고, 커스텀 할 수 있도록 해줍니다. 많은 타입의 리스트를 사용하기 위해선 RecyclerView.LayoutManager 클래스를 사용하면 됩니다.
     * 리사이클러 뷰는 아래와 같은 3가지의 미리 정의된 Layout Managers를 제공합니다.
     * <p>
     * LinearLayoutManager : 리사이클러 뷰에서 가장 많이 쓰이는 레이아웃으로 수평, 수직 스크롤을 제공하는 리스트를 만들 수 있습니다.
     * StaggeredGridLayouManager : 이 레이아웃을 통해 뷰마다 크기가 다른 레이아웃을 만들 수 있습니다. 마치 Pinterest 처럼요.
     * GridLayoutManager : 여러분의 사진첩 같은 격자형 리스트를 만들 수 있습니다.
     * <p>
     * Item Decoration
     * 리스트 뷰에서는 아래의 파라미터를 XML에 추가함으로서 쉽게 divide할 수 있었습니다.리사이클러 뷰에서는 RecyclerView.ItemDecoration 클래스를 통해 divider를 원하는 아이템에 추가해주게 되었습니다. 조금 복잡해졌지만 동적인 데코레이팅이 가능해졌습니다.
     * <p>
     * Item Animator을 이용해 멋진 앱을 만드세요
     * Metarial Design에 대해 조명된 이후로 리스트에서의 애니메이션은 무궁무진한 가능성을 가지게 되었습니다. 리스트 뷰에서는 아이템의 삽입이나 삭제에 특별한 애니메이션이 없었습니다.
     * 하지만 리사이클러 뷰에서는 RecyclerView.ItemAnimator 클래스를 통해 애니메이션을 핸들링 할 수 있게되었죠.
     * 이 클래스를 통해서 아이템 삽입, 삭제, 이동에 대한 커스터마이징이 가능하고, 또한 DefaultItemAnimator가 제공되므로 커스터마이즈가 필요 없이 사용할 수도 있습니다.
     * notifyItemChanged(int position), notifyItemInserted(int position), notifyItemRemoved(int position)을 ItemAnimator을 통해 특정 아이템에 대한 애니메이션을 발생시킬 수 있습니다.
     * <p>
     * 클릭으로 인한 중첩을 OnItemTouchListener 로 피하세요
     * 리스트뷰는 쉽게 클릭을 감지할 수 있었습니다. AdapterView.OnItemClickListener 를 이용해서요. 하지만 RecyclerView.OnItemTouchListener은 리사이클러 뷰의 터치 이벤트를 감지합니다.
     * 좀 복잡하지만 개발자에게 터치 이벤트를 인터셉트하는 제어권한을 주게되었죠. 안드로이드 공식 문서에서는 터치 이벤트를 인터셉트함으로서 리사이클러 뷰에게 전달하기 전에 조작함으로서 유용하게 사용될 수 있다고 합니다.
     * 터치 이벤트를 통해 사용자가 아이템을 클릭 했는지 롱클릭 했는지를 직접 처리해주거나, ViewHolder에서 OnClickListener를 직접 달아주어 처리해야합니다.
     * <p>
     * 결론
     * 리사이클러 뷰는 리스트 뷰에 비해 상당히 기능적 커스터마이징에 중점을 두고 있습니다.
     * 리스트 뷰에 비해 조금 어려울 수 있지만, 사용자에게 앱에 대한 연결성을 유지시켜주는 머터리얼 디자인에 대한 요구를 충족시키는 복잡한 그리드나 리스트에 아주 유용하게 쓰일 수 있겠죠.
     */
    public class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, View.OnClickListener, View.OnLongClickListener {
        final LinearLayout mContainer;
        final CheckBox mCheckBox;
        final TextView tvGoodsName, tvGoodsRegDate;
        final RelativeLayout mTrashBtn;
        final RelativeLayout mDragHandle;
        final Button mItemBtn;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mContainer = (LinearLayout)itemView.findViewById(R.id.mContainer);
            mCheckBox = (CheckBox)itemView.findViewById(R.id.mCheckBox);
            tvGoodsName = (TextView)itemView.findViewById(R.id.tvGoodsName);
            tvGoodsRegDate = (TextView)itemView.findViewById(R.id.tvGoodsRegDate);
            mTrashBtn = (RelativeLayout)itemView.findViewById(R.id.mTrashBtn);
            mDragHandle = (RelativeLayout)itemView.findViewById(R.id.mDragHandle);
            mItemBtn = (Button)itemView.findViewById(R.id.mItemBtn);

            mItemBtn.setOnClickListener(this);
            mItemBtn.setOnLongClickListener(this);
            mTrashBtn.setOnClickListener(this);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
            itemView.setAlpha(0.3f);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
            itemView.setAlpha(1.0f);
            mWeakRefHandler.sendEmptyMessage(Constants.QUERY_DRAG_SORT);
        }

        @Override
        public void onClick(View v) {
            Bundle data = new Bundle();
            Message msg = new Message();
            int position = getAdapterPosition();
            LogUtil.e("Position :::: " + position);

            switch (v.getId()) {
                case R.id.mItemBtn:
                    data.putInt("position", position);
                    msg.setData(data);
                    msg.what = Constants.QUERY_UPDATE_GOODS;
                    mWeakRefHandler.sendMessage(msg);
                    break;
                case R.id.mTrashBtn:
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
            int position = getAdapterPosition();
            LogUtil.e("Position :::: " + position);

            switch (v.getId()) {
                case R.id.mItemBtn:
                    data.putInt("position", position);
                    msg.setData(data);
                    msg.what = Constants.QUERY_RENAME_GOODS;
                    mWeakRefHandler.sendMessage(msg);
                    break;
            }
            return true; // 다음 이벤트 계속 진행 false, 이벤트 완료 true
        }
    }
}
