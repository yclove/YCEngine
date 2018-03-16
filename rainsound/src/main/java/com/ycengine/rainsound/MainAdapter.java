package com.ycengine.rainsound;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.ycengine.rainsound.data.CoreItem;
import com.ycengine.rainsound.library.parallaxscroll.ParallaxImageView;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * YCNOTE - FragmentPagerAdapter와 FragmentStatePagerAdapter
 * Fragment를 처리하는 PagerAdapter는 두 가지 Class가 존재한다.
 * 하나는 FragmentPagerAdapter 이고 다른 하나는 FragmentStatePagerAdapter이다.
 * FragmentPagerAdapter의 경우, 사용자가 ViewPager에서 좌/우로 스크롤(플링)하여 화면 전환을 하여 다음 Fragment가 표시되면 이전 Fragment를 메모리 상에 저장해 만일 사용자가 화면을 반대로 이동하면 메모리 상에 저장되어있는 Fragment를 사용하게된다.
 * 2번째 FragmentStatePagerAdapter는 ViewPager의 페이지를 이동하여 다음 Fragment가 표시되면 이전 Fragment는 메모리 상에서 제거된다.
 * 사용자가 화면을 다시 반대로 전환하면 기존에 저장된 상태값(state)을 기반으로 재생성합니다.
 * 그러므로 페이지 수가 정해져 있고 그 수가 많지 않다면 FragmentPagerAdapter를 사용하는 편이 좋고 반대로 페이지 수를 알 수 없거나 많다면 FragmentStatePagerAdapter를 사용하는 것이 좋다.
 */

/**
 * YCNOTE - PagerAdapter notifyDataSetChanged
 * PagerAdapter 에 대한 notifyDataSetChanged()는 오직 ViewPager 에게 data set 이 변경되었다는 사실만을 알려준다.
 * ViewPager 는 view 의 등록과 삭제를 getItemPosition( Object ) 과 getCount() 를 통해 한다.
 * notifyDataSetChanged() 가 불리면 ViewPager 는 child view의 position 을 getItemPosition( Object ) 을 호출하여 알아본다.
 * 만약 이 child view 가 POSITION_NONE 을 던지면 ViewPager 는 view 가 삭제되었음을 안다.
 * 그리고 destroyItem( ViewGroup, int, Object )을 불러 이 view 를 제거한다.
 * ViewPager 가 View 를 업데이트하지 않는 현상이 나타나면 다음과 같이 억지로 update 시킬 수 있다.
 * <p/>
 * 1. PagerAdapter의 getItemPosition( Object object ) 를 override 하고 여기서 POSITION_NONE 값을 return 한다.
 * 저 값은 -2로, 저 값이 들어가면 ViewPager 는 notifyDataSetChanged() 가 불릴 때마다 모든 View 를 다시 그린다.
 * 따라서 효율성이 떨어지긴 하지만 어쨌든 해결은 된다. 권장할만한 방법은 아니다.
 * <p/>
 * 2. setTag() 를 통해 Fragment 에 tag 를 매겨놓고, PagerAdapter 의 instantiateItem( View, position ) 을 override 하여 tag 값 기준으로 필요한 view 만 다시 생성한다.
 * 이 방법을 이용하면 notifyDataSetChanged() 를 부르지 않고, ViewPager.findViewWithTag( Object ) 를 통해서 update 를 시도해야 한다.
 */
public class MainAdapter extends PagerAdapter implements IOnHandlerMessage, View.OnClickListener {
    Context mContext;
    LayoutInflater inflater;
    WeakRefHandler mHandler;
    RequestManager mGlideRequestManager;
    List<CoreItem> mItems = new ArrayList<>();
    boolean isLoop = true;
    int paddingHeight = 0;

    public boolean isLoop() {
        return isLoop;
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    public void updateLicensedUI(int height) {
        this.paddingHeight = height;
    }

    @Override
    public int getItemPosition(Object object) {
        // 전체 view를 다시 로드
        return POSITION_NONE;
    }

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();
        int position;

        switch (v.getId()) {
            /*case R.id.tvPostSubject:
                data.putString("ICI", (String) v.getTag(R.id.tag_color));
                data.putString("COLOR_HEX", (String) v.getTag(R.id.tag_color_hex));
                msg.setData(data);
                msg.what = Constants.QUERY_POST_DATA;
                mHandler.sendMessage(msg);
                break;*/
            default:
                break;
        }
    }

    public MainAdapter(Context context, LayoutInflater inflater, WeakRefHandler handler, RequestManager requestManager) {
        // 전달 받은 LayoutInflater를 멤버변수로 전달
        this.mContext = context;
        this.inflater = inflater;
        this.mHandler = handler;
        this.mGlideRequestManager = requestManager;
    }

    // PagerAdapter가 가지고 잇는 View의 개수를 리턴
    // 보통 보여줘야하는 이미지 배열 데이터의 길이를 리턴
    @Override
    public int getCount() {
        if (isLoop) {
            return Integer.MAX_VALUE;
        } else {
            return mItems.size();
        }
    }

    // ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
    // 쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
    // 첫번째 파라미터 : ViewPager
    // 두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (isLoop) {
            position = position % mItems.size();
        }

        View view = inflater.inflate(R.layout.viewpager_main, null);
        CoreItem item = mItems.get(position);

        LinearLayout contentLayout = (LinearLayout) view.findViewById(R.id.contentLayout);
        LinearLayout mLicensedLayout = (LinearLayout) view.findViewById(R.id.mLicensedLayout);
        ParallaxImageView ivRoot = (ParallaxImageView) view.findViewById(R.id.ivRoot);
        TextView tvPhotographer = (TextView) view.findViewById(R.id.tvPhotographer);
        TextView tvPhotoReferrer = (TextView) view.findViewById(R.id.tvPhotoReferrer);

        if (item.getCorePhoto() != -1) {
            mGlideRequestManager
                    .load(item.getCorePhoto())
                    .into(ivRoot);
        } else {
            ivRoot.setImageResource(android.R.color.transparent);
        }

        if ("".equals(item.getCorePhotographer())) {
            tvPhotographer.setText("");
        } else {
            tvPhotographer.setText("©" + item.getCorePhotographer());
        }

        if ("".equals(item.getCorePhotoReferrer())) {
            tvPhotoReferrer.setText("");
        } else {
            tvPhotographer.setText(tvPhotographer.getText().toString() + ":");
            tvPhotoReferrer.setText(item.getCorePhotoReferrer());
        }

        if (paddingHeight != 0) {
            mLicensedLayout.setPadding(0, 0, 0, paddingHeight);
            mLicensedLayout.setVisibility(View.VISIBLE);
        } else {
            mLicensedLayout.setVisibility(View.GONE);
        }

        container.addView(view);
        return view;
    }

    // 화면에 보이지 않은 View는파괴를 해서 메모리를 관리함.
    // 첫번째 파라미터 : ViewPager
    // 두번째 파라미터 : 파괴될 View의 인덱스(가장 처음부터 0,1,2,3...)
    // 세번째 파라미터 : 파괴될 객체(더 이상 보이지 않은 View 객체)
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // ViewPager에서 보이지 않는 View는 제거
        // 세번째 파라미터가 View 객체 이지만 데이터 타입이 Object여서 형변환 실시
        container.removeView((View) object);
    }

    // instantiateItem() 메소드에서 리턴된 Ojbect가 View가  맞는지 확인하는 메소드
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

    public void deleteAllItems() {
        mItems.clear();
        notifyDataSetChanged();
    }

    protected OnItemTouchListener mOnItemTouchListener = null;

    public interface OnItemTouchListener {
        boolean onTouch(View v, MotionEvent event);
    }

    public void setOnItemTouchListener(OnItemTouchListener listener) {
        mOnItemTouchListener = listener;
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mOnItemTouchListener.onTouch(v, event);
            return false;
        }
    };

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            default:
                break;
        }
    }
}
