package com.ycengine.tourist.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ycengine.tourist.BaseFragment;
import com.ycengine.tourist.BaseRecyclerViewAdapter;
import com.ycengine.tourist.Constants;
import com.ycengine.tourist.MainActivity;
import com.ycengine.tourist.R;
import com.ycengine.tourist.database.Databases;
import com.ycengine.tourist.databinding.FragmentAreaBasedListBinding;
import com.ycengine.tourist.model.AreaBasedList;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;
import com.ycengine.yclibrary.util.thread.RunnableThread;
import com.ycengine.yclibrary.util.thread.StopRunnable;

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
import java.util.HashMap;
import java.util.Map;

public class AreaBasedListFragment extends BaseFragment<FragmentAreaBasedListBinding> implements View.OnClickListener, IOnHandlerMessage {
    private Context mContext;
    private WeakRefHandler mWeakRefHandler;
    private Databases mDatabases;
    private HashMap<String, String> mSortData;

    private ArrayList<AreaBasedList> mAreaBasedList = new ArrayList<>();
    private int currentPage = 1;
    private int totalCount = 0;

    private AreaBasedListAdapter mAreaBasedListAdapter;

    @Override
    protected int contentLayout() {
        return R.layout.fragment_area_based_list;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mContext = getContext();
        mWeakRefHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        mDatabases = new Databases(mContext);
        mDatabases.open();
        mSortData = mDatabases.getFilterData(Constants.FILTER_DATA);
        mDatabases.close();

        mBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Initialize a new instance of RecyclerView Adapter instance
        mAreaBasedListAdapter = new AreaBasedListAdapter(mContext);

        // Set the adapter for RecyclerView
        mBinding.mRecyclerView.setAdapter(mAreaBasedListAdapter);

        mAreaBasedListAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                LogUtil.e("click :::::::::::::: " + position);
            }
        });

        mAreaBasedListAdapter.setOnItemLongClickListener(new BaseRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                LogUtil.e("long click :::::::::::::: " + position);
            }
        });

        /*
        mBinding.mRecyclerView.addOnItemTouchListener(new RecyclerViewOnItemClickListener(getActivity(), mBinding.mRecyclerView,
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
        */

        RelativeLayout mFilterBtn = (RelativeLayout) rootView.findViewById(R.id.mFilterBtn);
        mFilterBtn.setOnClickListener(this);

        getData(Constants.REQUEST_AREA_BASED_LIST);

        return rootView;
    }

    private void getData(int queryType, Object... args) {
        if (!getActivity().isFinishing()) {
            if(mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.REQUEST_AREA_BASED_LIST) {
            thread = getAreaBasedListThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getAreaBasedListThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                try {
                    StringBuilder urlBuilder = new StringBuilder(Constants.API_AREA_BASED_LIST_URL);
                    urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + Constants.API_SERVICE_KEY);
                    urlBuilder.append("&" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + URLEncoder.encode(Constants.API_SERVICE_KEY, "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(String.valueOf(currentPage), "UTF-8")); // 현재 페이지 번호
                    urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("20", "UTF-8")); // 한 페이지 결과수
                    urlBuilder.append("&" + URLEncoder.encode("MobileApp","UTF-8") + "=" + URLEncoder.encode("Tourist", "UTF-8")); // 서비스명=어플명
                    urlBuilder.append("&" + URLEncoder.encode("MobileOS","UTF-8") + "=" + URLEncoder.encode("AND", "UTF-8")); // IOS(아이폰), AND(안드로이드), WIN(원도우폰), ETC
                    urlBuilder.append("&" + URLEncoder.encode("arrange","UTF-8") + "=" + URLEncoder.encode("A", "UTF-8")); // (A=제목순, B=조회순, C=수정일순, D=생성일순) , 대표이미지 정렬 추가(D=제목순, P=조회순, Q=수정일순, R=생성일순)
                    urlBuilder.append("&" + URLEncoder.encode("listYN","UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8")); // 목록구분

                    if (!"".equals(mSortData.get(Constants.FILTER_AREA_CODE)))
                        urlBuilder.append("&" + URLEncoder.encode("areaCode","UTF-8") + "=" + URLEncoder.encode(mSortData.get(Constants.FILTER_AREA_CODE), "UTF-8")); // 지역코드

                    if (!"".equals(mSortData.get(Constants.FILTER_SIGUNGU_CODE)))
                        urlBuilder.append("&" + URLEncoder.encode("sigunguCode","UTF-8") + "=" + URLEncoder.encode(mSortData.get(Constants.FILTER_SIGUNGU_CODE), "UTF-8")); // 시군구코드

                    if (!"".equals(mSortData.get(Constants.FILTER_CATEGORY_1_CODE)))
                        urlBuilder.append("&" + URLEncoder.encode("cat1","UTF-8") + "=" + URLEncoder.encode(mSortData.get(Constants.FILTER_CATEGORY_1_CODE), "UTF-8")); // 대분류

                    if (!"".equals(mSortData.get(Constants.FILTER_CATEGORY_2_CODE)))
                        urlBuilder.append("&" + URLEncoder.encode("cat2","UTF-8") + "=" + URLEncoder.encode(mSortData.get(Constants.FILTER_CATEGORY_2_CODE), "UTF-8")); // 중분류

                    if (!"".equals(mSortData.get(Constants.FILTER_CATEGORY_2_CODE)))
                        urlBuilder.append("&" + URLEncoder.encode("cat3","UTF-8") + "=" + URLEncoder.encode(mSortData.get(Constants.FILTER_CATEGORY_3_CODE), "UTF-8")); // 소분류
/*
                    urlBuilder.append("&" + URLEncoder.encode("contentTypeId","UTF-8") + "=" + URLEncoder.encode("15", "UTF-8")); // 관광타입(관광지, 숙박등) ID
*/
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

                    LogUtil.e("지역기반 관광정보조회 : " + sb.toString());

                    InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new InputStreamReader(is, "UTF-8"));
                    int eventType = parser.getEventType();

                    AreaBasedList areaBasedList = null;
                    int currentCount = 0;

                    mAreaBasedList.clear();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                String startTag = parser.getName();
                                if (startTag.equals("item")) areaBasedList = new AreaBasedList();
                                if (startTag.equals("addr1")) areaBasedList.setAddr1(parser.nextText());
                                if (startTag.equals("addr2")) areaBasedList.setAddr2(parser.nextText());
                                if (startTag.equals("areacode")) areaBasedList.setAreacode(parser.nextText());
                                if (startTag.equals("cat1")) areaBasedList.setCat1(parser.nextText());
                                if (startTag.equals("cat2")) areaBasedList.setCat2(parser.nextText());
                                if (startTag.equals("cat3")) areaBasedList.setCat3(parser.nextText());
                                if (startTag.equals("contentid")) areaBasedList.setContentid(parser.nextText());
                                if (startTag.equals("contenttypeid")) areaBasedList.setContenttypeid(parser.nextText());
                                if (startTag.equals("createdtime")) areaBasedList.setCreatedtime(parser.nextText());
                                if (startTag.equals("firstimage")) areaBasedList.setFirstimage(parser.nextText());
                                if (startTag.equals("firstimage2")) areaBasedList.setFirstimage2(parser.nextText());
                                if (startTag.equals("mapx")) areaBasedList.setMapx(parser.nextText());
                                if (startTag.equals("mapy")) areaBasedList.setMapy(parser.nextText());
                                if (startTag.equals("mlevel")) areaBasedList.setMlevel(parser.nextText());
                                if (startTag.equals("modifiedtime")) areaBasedList.setModifiedtime(parser.nextText());
                                if (startTag.equals("readcount")) areaBasedList.setReadcount(parser.nextText());
                                if (startTag.equals("sigungucode")) areaBasedList.setSigungucode(parser.nextText());
                                if (startTag.equals("tel")) areaBasedList.setTel(parser.nextText());
                                if (startTag.equals("title")) areaBasedList.setTitle(parser.nextText());
                                if (startTag.equals("zipcode")) areaBasedList.setZipcode(parser.nextText());
                                if (startTag.equals("booktour")) areaBasedList.setBooktour(parser.nextText());
                                if (startTag.equals("totalCount")) totalCount = Integer.valueOf(parser.nextText());
                                break;
                            case XmlPullParser.END_TAG:
                                String endTag = parser.getName();
                                if (endTag.equals("item")) {
                                    mAreaBasedList.add(areaBasedList);
                                    currentCount++;
                                }
                                break;
                        }
                        eventType = parser.next();
                    }

                    mWeakRefHandler.sendEmptyMessage(Constants.REQUEST_AREA_BASED_LIST);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.e("지역기반 관광정보조회 ERROR : " + e.getMessage());
                }
            }
        });
        thread.start();
        return thread;
    }

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
            case R.id.mFilterBtn:
                ((MainActivity)getActivity()).showFilterLayout();
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }
        Bundle data = msg.getData();
        int position;

        switch (msg.what) {
            case Constants.REQUEST_AREA_BASED_LIST:
                if (mAreaBasedList.size() > 0) {
                    mAreaBasedListAdapter.addItems(mAreaBasedList);
                }
                break;
        }
    }
}
