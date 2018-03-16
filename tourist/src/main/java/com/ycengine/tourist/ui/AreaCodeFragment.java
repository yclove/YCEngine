package com.ycengine.tourist.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ycengine.tourist.BaseFragment;
import com.ycengine.tourist.Constants;
import com.ycengine.tourist.R;
import com.ycengine.tourist.database.Databases;
import com.ycengine.tourist.model.CodeItem;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class AreaCodeFragment extends BaseFragment implements View.OnClickListener, IOnHandlerMessage {
    public static final int AREA_CODE_SPAN_COUNT = 2;

    private Context mContext;
    private WeakRefHandler mWeakRefHandler;
    private Databases mDatabases;

    private List<CodeItem> mAreaCodeList = new ArrayList<>();
    private int currentPage = 1;
    private int totalCount = 0;

    private AreaCodeAdapter mAreaCodeAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView mRecyclerView;

    @Override
    protected int contentLayout() {
        return R.layout.fragment_area_code;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mContext = getContext();
        mWeakRefHandler = new WeakRefHandler(this);

        if (savedInstanceState != null) {
            // Restore the value of the last expanded position here.
            // We cannot tell the adapter to expand this item until onLoadFinished()
            // is called.
        }

        mDatabases = new Databases(getContext());
        mDatabases.open();
        mAreaCodeList = mDatabases.getCodeList(Constants.TYPE_AREA);
        mDatabases.close();

        mLayoutManager = new GridLayoutManager(mContext, AREA_CODE_SPAN_COUNT);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Initialize a new instance of RecyclerView Adapter instance
        mAreaCodeAdapter = new AreaCodeAdapter(mContext, mAreaCodeList);

        // Set the adapter for RecyclerView
        mRecyclerView.setAdapter(mAreaCodeAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerViewOnItemClickListener(getActivity(), mRecyclerView,
                new RecyclerViewOnItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        LogUtil.e("click :::: " + position);
                    }

                    @Override
                    public void onItemLongClick(View v, int position) {
                        LogUtil.e("long click :::: " + position);
                    }
                }));

        if (mAreaCodeList.size() > 0) {
            bindAreaCode();
        } else {
            new Thread(getAreaCodeThread).start();
        }

        return view;
    }

    private void bindAreaCode() {
        mAreaCodeAdapter.addAllItems(mAreaCodeList);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.e("onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtil.e("onStop()");
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.e("onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.e("onResume()");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private Runnable getAreaCodeThread = new Runnable() {
        public void run() {
            try {
                StringBuilder urlBuilder = new StringBuilder(Constants.API_AREA_CODE_URL);
                urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + Constants.API_SERVICE_KEY);
                urlBuilder.append("&" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + URLEncoder.encode(Constants.API_SERVICE_KEY, "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("20", "UTF-8")); /*한 페이지 결과수*/
                urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(String.valueOf(currentPage), "UTF-8")); /*현재 페이지 번호*/
                urlBuilder.append("&" + URLEncoder.encode("MobileOS","UTF-8") + "=" + URLEncoder.encode("AND", "UTF-8")); /*IOS(아이폰), AND(안드로이드), WIN(원도우폰), ETC*/
                urlBuilder.append("&" + URLEncoder.encode("MobileApp","UTF-8") + "=" + URLEncoder.encode("Tourist", "UTF-8")); /*서비스명=어플명*/
                //urlBuilder.append("&" + URLEncoder.encode("areaCode","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*지역코드(areaCode 없을때), 시군구코드(areaCode=1)*/
                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(Constants.TIMEOUT_HTTP_CONNECTION);
                conn.setRequestProperty("Content-type", "application/json");

                BufferedReader rd;
                if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                conn.disconnect();

                LogUtil.e("지역코드조회 : " + sb.toString());

                InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(is, "UTF-8"));
                int eventType = parser.getEventType();

                CodeItem codeItem = null;
                int currentCount = 0;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            String startTag = parser.getName();
                            if (startTag.equals("item")) {
                                codeItem = new CodeItem();
                                codeItem.setType(Constants.TYPE_AREA);
                            }
                            if (startTag.equals("code"))
                                codeItem.setCode(parser.nextText());
                            if (startTag.equals("name"))
                                codeItem.setName(parser.nextText());
                            if (startTag.equals("rnum"))
                                codeItem.setRnum(Integer.valueOf(parser.nextText()));
                            if (startTag.equals("totalCount"))
                                totalCount = Integer.valueOf(parser.nextText());
                            break;
                        case XmlPullParser.END_TAG:
                            String endTag = parser.getName();
                            if (endTag.equals("item")) {
                                mAreaCodeList.add(codeItem);
                                currentCount++;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                if (totalCount != mAreaCodeList.size() && currentCount > 0) {
                    currentPage++;
                    new Thread(getAreaCodeThread).start();
                } else {
                    mWeakRefHandler.sendEmptyMessage(Constants.REQUEST_AREA_CODE_INFO);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e("지역코드조회 ERROR : " + e.getMessage());
            }
        }
    };

    public static class RecyclerViewOnItemClickListener extends RecyclerView.SimpleOnItemTouchListener {
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        public RecyclerViewOnItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
            this.mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if(childView != null && mListener != null){
                        mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(child, rv.getChildAdapterPosition(child));
                return true;
            }
            return false;
        }

        public interface OnItemClickListener {
            void onItemClick(View v, int position);
            void onItemLongClick(View v, int position);
        }
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();

        switch (id) {
            default:
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        int position;

        switch (msg.what) {
            case Constants.REQUEST_AREA_CODE_INFO:
                if (mAreaCodeList.size() > 0) {
                    if (mDatabases == null) mDatabases = new Databases(getContext());
                    mDatabases.open();
                    mDatabases.setCodeList(mAreaCodeList);
                    mDatabases.close();

                    bindAreaCode();
                }
                break;
        }
    }
}
