package com.ycengine.tourist;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ycengine.tourist.database.Databases;
import com.ycengine.tourist.model.CodeItem;
import com.ycengine.tourist.model.ResponseItem;
import com.ycengine.tourist.service.CodeService;
import com.ycengine.yclibrary.util.CommonUtil;
import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage;
import com.ycengine.yclibrary.util.handler.WeakRefHandler;
import com.ycengine.yclibrary.util.thread.RunnableThread;
import com.ycengine.yclibrary.util.thread.StopRunnable;
import com.ycengine.yclibrary.widget.MultiViewPager;

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
import java.util.List;
import java.util.Map;

public class FilterActivity extends BaseActivity implements IOnHandlerMessage, CodeService.AreaCodeListener {
    public static final int TYPE_AREA_CODE = 1;
    public static final int TYPE_SIGUNGU_CODE = 2;
    public static final int TYPE_CATEGORY_1_CODE = 3;
    public static final int TYPE_CATEGORY_2_CODE = 4;
    public static final int TYPE_CATEGORY_3_CODE = 5;

    private WeakRefHandler mHandler;
    private Databases mDatabases;

    private CodeService codeService;

    private List<CodeItem> mAreaCodeList = new ArrayList<>();
    private List<CodeItem> mSigunguCodeList = new ArrayList<>();
    private List<CodeItem> mCategory1CodeList = new ArrayList<>();
    private List<CodeItem> mCategory2CodeList = new ArrayList<>();
    private List<CodeItem> mCategory3CodeList = new ArrayList<>();
    private int areaCodePage = 1, sigunguCodePage = 1, category1CodePage = 1, category2CodePage = 1, category3CodePage = 1;
    private int areaCodeCount = 0, sigunguCodeCount = 0, category1CodeCount = 0, category2CodeCount = 0, category3CodeCount = 0;

    private HashMap<String, String> mCurrentSortData, mSortData;
    private RelativeLayout mSigunguLayout, mCategory2Layout, mCategory3Layout;
    private MultiViewPager mMotionViewPager, mAreaCodeViewPager, mSigunguViewPager, mCategory1ViewPager, mCategory2ViewPager, mCategory3ViewPager;
    private FilterAdapter mMotionAdapter, mAreaCodeAdapter, mSigunguAdapter, mCategory1Adapter, mCategory2Adapter, mCategory3Adapter;
    private int mAreaCodePosition, mSigunguPosition, mCategory1Position, mCategory2Position, mCategory3Position;
    private RelativeLayout mHeaderCheck, mInitFilter;
    private ImageView mInitFilterImage;

    @Override
    protected int layoutResId() {
        return R.layout.activity_filter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        codeService = ((BaseApplication) getApplicationContext()).getCodeService();
        codeService.addAreaCodeListener(this);

        mDatabases = new Databases(mContext);
        mDatabases.open();
        mAreaCodeList = mDatabases.getCodeList(Constants.TYPE_AREA);
        mCategory1CodeList = mDatabases.getCodeList(Constants.TYPE_CATEGORY);
        mCurrentSortData = mDatabases.getFilterData(Constants.FILTER_DATA);
        mSortData = mDatabases.getFilterData(Constants.FILTER_DATA);
        mDatabases.close();

        setDisplay();
    }

    @Override
    public void onAreaCodesLoaded(ResponseItem responseItem) {
        if(mProgressDialog != null && mProgressDialog.isShow()) { mProgressDialog.dissDialog(); }
        if ("0000".equals(responseItem.getHeader().getResultCode())) {
            try {
                if (mAreaCodeList.size() == 0)
                    mAreaCodeList.add(new CodeItem(Constants.TYPE_AREA, "", "전체", 0, true));

                for (CodeItem item : responseItem.getBody().getItems()) {
                    item.setType(Constants.TYPE_AREA);
                    mAreaCodeList.add(item);
                }

                int totalCount = Integer.valueOf(responseItem.getBody().getTotalCount());
                if (mAreaCodeList.size() > totalCount) {
                    mHandler.sendEmptyMessage(Constants.REQUEST_AREA_CODE_INFO);
                } else {
                    areaCodePage = Integer.valueOf(responseItem.getBody().getPageNo()) + 1;
                    getData(Constants.REQUEST_AREA_CODE_INFO);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.e("ERROR :::: " + responseItem.getHeader().getResultCode());
        }
    }

    @Override
    public void onAreaCodesLoadFailed(String message) {
        if(mProgressDialog != null && mProgressDialog.isShow()) { mProgressDialog.dissDialog(); }
        LogUtil.e("Failed :::: " + message);
    }

    @Override
    public void onDestroy() {
        codeService.removeAreaCodeListener(this);
        super.onDestroy();
    }

    private void setDisplay() {
        mHeaderCheck = (RelativeLayout) findViewById(R.id.mHeaderCheck);
        mInitFilter = (RelativeLayout) findViewById(R.id.mInitFilter);
        mInitFilterImage = (ImageView) findViewById(R.id.mInitFilterImage);

        // Footer Sort Image 활성 / 비활성 처리
        setInitSortImage();
        setHeaderCheckImage();

        mSigunguLayout = (RelativeLayout) findViewById(R.id.mSigunguLayout);
        mCategory2Layout = (RelativeLayout) findViewById(R.id.mCategory2Layout);
        mCategory3Layout = (RelativeLayout) findViewById(R.id.mCategory3Layout);

        // MultiViewPager
        mMotionViewPager = (MultiViewPager) findViewById(R.id.mMotionViewPager);
        mAreaCodeViewPager = (MultiViewPager) findViewById(R.id.mAreaCodeViewPager);
        mAreaCodeViewPager.setOnTouchListener(mTouchEvent);
        mSigunguViewPager = (MultiViewPager) findViewById(R.id.mSigunguViewPager);
        mSigunguViewPager.setOnTouchListener(mTouchEvent);
        mCategory1ViewPager = (MultiViewPager) findViewById(R.id.mCategory1ViewPager);
        mCategory1ViewPager.setOnTouchListener(mTouchEvent);
        mCategory2ViewPager = (MultiViewPager) findViewById(R.id.mCategory2ViewPager);
        mCategory2ViewPager.setOnTouchListener(mTouchEvent);
        mCategory3ViewPager = (MultiViewPager) findViewById(R.id.mCategory3ViewPager);
        mCategory3ViewPager.setOnTouchListener(mTouchEvent);

        // Adapter
        mMotionAdapter = new FilterAdapter(mContext, getLayoutInflater(), mHandler, "motion");
        mAreaCodeAdapter = new FilterAdapter(mContext, getLayoutInflater(), mHandler, "single");
        mSigunguAdapter = new FilterAdapter(mContext, getLayoutInflater(), mHandler, "single");
        mCategory1Adapter = new FilterAdapter(mContext, getLayoutInflater(), mHandler, "single");
        mCategory2Adapter = new FilterAdapter(mContext, getLayoutInflater(), mHandler, "single");
        mCategory3Adapter = new FilterAdapter(mContext, getLayoutInflater(), mHandler, "single");

        // MultiViewPager Set Adapter
        mMotionViewPager.setAdapter(mMotionAdapter);
        mAreaCodeViewPager.setAdapter(mAreaCodeAdapter);
        mSigunguViewPager.setAdapter(mSigunguAdapter);
        mCategory1ViewPager.setAdapter(mCategory1Adapter);
        mCategory2ViewPager.setAdapter(mCategory2Adapter);
        mCategory3ViewPager.setAdapter(mCategory3Adapter);

        // MutiViewPager Init Item
        mMotionViewPager.setCurrentItem(10000);

        // MultiViewPager Listener
        mAreaCodeViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int state) {}
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = mAreaCodeViewPager.getCurrentItem();

                    mSortData.put(Constants.FILTER_AREA_CODE, mAreaCodeList.get(position).getCode());
                    mAreaCodeAdapter.setItemSelected(position);
                    setInitSortImage();
                    setHeaderCheckImage();

                    if (position == 0) {
                        mSigunguLayout.setVisibility(View.GONE);
                    } else {
                        chkData(TYPE_SIGUNGU_CODE);
                    }
                }
            }
        });

        mSigunguViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int state) {}
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = mSigunguViewPager.getCurrentItem();

                    mSortData.put(Constants.FILTER_SIGUNGU_CODE, mSigunguCodeList.get(position).getCode());
                    mSigunguAdapter.setItemSelected(position);
                    setInitSortImage();
                    setHeaderCheckImage();
                }
            }
        });

        mCategory1ViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int state) {}
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = mCategory1ViewPager.getCurrentItem();

                    mSortData.put(Constants.FILTER_CATEGORY_1_CODE, mCategory1CodeList.get(position).getCode());
                    mCategory1Adapter.setItemSelected(position);
                    setInitSortImage();
                    setHeaderCheckImage();
                }
            }
        });

        chkData(TYPE_AREA_CODE);
    }

    private View.OnTouchListener mTouchEvent = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), -1 * event.getX(), event.getY(), event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
            mMotionViewPager.onTouchEvent(motionEvent);
            return false;
        }
    };

    private void chkData(int type) {
        switch (type) {
            case TYPE_AREA_CODE:
                if (mAreaCodeList.size() > 0) {
                    bindData(type);
                } else {
                    getData(Constants.REQUEST_AREA_CODE_INFO);
                }
                break;
            case TYPE_SIGUNGU_CODE:
                if (mDatabases == null) mDatabases = new Databases(mContext);
                mDatabases.open();
                mSigunguCodeList = mDatabases.getCodeList(mSortData.get(Constants.FILTER_AREA_CODE));
                mDatabases.close();

                if (mSigunguCodeList.size() > 0) {
                    bindData(type);
                } else {
                    getData(Constants.REQUEST_AREA_CODE_INFO);
                }
                break;
            case TYPE_CATEGORY_1_CODE:
                if (mCategory1CodeList.size() > 0) {
                    bindData(type);
                } else {
                    getData(Constants.REQUEST_CATEGORY_CODE_INFO);
                }
                break;
        }
    }

    private void setInitSortImage() {
        if ( CommonUtil.isNull(mSortData.get(Constants.FILTER_AREA_CODE))
                && CommonUtil.isNull(mSortData.get("sigunguCode"))
                && CommonUtil.isNull(mSortData.get("category1"))
                && CommonUtil.isNull(mSortData.get("category2"))
                && CommonUtil.isNull(mSortData.get("category3")) ) {
            mInitFilterImage.setBackgroundResource(R.drawable.btn_sort_disable);
            mInitFilter.setOnClickListener(null);
        } else {
            mInitFilterImage.setBackgroundResource(R.drawable.btn_sort_enable);
            mInitFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAreaCodeViewPager.setCurrentItem(0, true);
                    mSigunguViewPager.setCurrentItem(0, true);
                    mSigunguLayout.setVisibility(View.GONE);
                    mCategory1ViewPager.setCurrentItem(0, true);
                }
            });
        }
    }

    private void setHeaderCheckImage() {
       if (mCurrentSortData.equals(mSortData)) {
           mHeaderCheck.setAlpha(0.2f);
           mHeaderCheck.setOnClickListener(null);
       } else {
           mHeaderCheck.setAlpha(1.0f);
           mHeaderCheck.setOnClickListener(onClickListener);
       }
    }

    private void bindData(int type) {
        switch (type) {
            case TYPE_AREA_CODE:
                if (mAreaCodeList.size() > 0) {
                    mAreaCodeList.get(0).setSelected(true);
                    mAreaCodeAdapter.addAllItems(mAreaCodeList);
                    mAreaCodeViewPager.setCurrentItem(0, true);
                }
                // TODO
                // chkData(TYPE_CATEGORY_1_CODE);
                break;
            case TYPE_SIGUNGU_CODE:
                if (mSigunguCodeList.size() > 0) {
                    mSigunguCodeList.get(0).setSelected(true);
                    mSigunguLayout.setVisibility(View.VISIBLE);
                    mSigunguAdapter.addAllItems(mSigunguCodeList);
                    mSigunguViewPager.setCurrentItem(0, true);
                }
                break;
            case TYPE_CATEGORY_1_CODE:
                if (mCategory1CodeList.size() > 0) {
                    mCategory1CodeList.get(0).setSelected(true);
                    mCategory1Adapter.addAllItems(mCategory1CodeList);
                    mCategory1ViewPager.setCurrentItem(0, true);
                }
                break;
        }
    }

    private void getData(int queryType, Object... args) {
        if (!isFinishing()) {
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
        if (queryType == Constants.REQUEST_AREA_CODE_INFO) {
            String code = mSortData.get(Constants.FILTER_AREA_CODE);

            Map<String, String> query = new HashMap<>();
            query.put("ServiceKey", Constants.API_SERVICE_KEY);
            query.put("ServiceKey", Constants.API_SERVICE_KEY);
            query.put("numOfRows", "10");
            query.put("pageNo", String.valueOf(areaCodePage));
            query.put("MobileOS", "AND");
            query.put("MobileApp", "Tourist");

            codeService.getAreaCode(query);
        } else if (queryType == Constants.REQUEST_CATEGORY_CODE_INFO) {
            thread = getCategoryCodeThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getAreaCodeThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                try {
                    String code = mSortData.get(Constants.FILTER_AREA_CODE);

                    StringBuilder urlBuilder = new StringBuilder(Constants.API_AREA_CODE_URL);
                    urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + Constants.API_SERVICE_KEY);
                    urlBuilder.append("&" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + URLEncoder.encode(Constants.API_SERVICE_KEY, "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과수*/

                    if (CommonUtil.isNull(code)) {
                        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(String.valueOf(areaCodePage), "UTF-8")); /*현재 페이지 번호*/
                    } else {
                        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(String.valueOf(sigunguCodePage), "UTF-8")); /*현재 페이지 번호*/
                    }

                    urlBuilder.append("&" + URLEncoder.encode("MobileOS","UTF-8") + "=" + URLEncoder.encode("AND", "UTF-8")); /*IOS(아이폰), AND(안드로이드), WIN(원도우폰), ETC*/
                    urlBuilder.append("&" + URLEncoder.encode("MobileApp","UTF-8") + "=" + URLEncoder.encode("Tourist", "UTF-8")); /*서비스명=어플명*/

                    if (CommonUtil.isNotNull(code)) {
                        urlBuilder.append("&" + URLEncoder.encode("areaCode","UTF-8") + "=" + URLEncoder.encode(code, "UTF-8")); /*지역코드(areaCode 없을때), 시군구코드(areaCode=1)*/
                    }

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

                    if (CommonUtil.isNull(code)) {
                        mAreaCodeList.clear();
                        mAreaCodeList.add(new CodeItem(Constants.TYPE_AREA, "", "전체", 0, true));
                    } else {
                        mSigunguCodeList.clear();
                        mSigunguCodeList.add(new CodeItem(code, "", "전체", 0, true));
                    }
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                String startTag = parser.getName();
                                if (startTag.equals("item")) {
                                    codeItem = new CodeItem();
                                    if (CommonUtil.isNotNull(code)) {
                                        codeItem.setType(code);
                                    } else {
                                        codeItem.setType(Constants.TYPE_AREA);
                                    }
                                }
                                if (startTag.equals("code"))
                                    codeItem.setCode(parser.nextText());
                                if (startTag.equals("name"))
                                    codeItem.setName(parser.nextText());
                                if (startTag.equals("rnum"))
                                    codeItem.setRnum(Integer.valueOf(parser.nextText()));
                                if (startTag.equals("totalCount")) {
                                    if (CommonUtil.isNull(code)) {
                                        areaCodeCount = Integer.valueOf(parser.nextText()) + 1;
                                    } else {
                                        sigunguCodeCount = Integer.valueOf(parser.nextText()) + 1;
                                    }
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                String endTag = parser.getName();
                                if (endTag.equals("item")) {
                                    if (CommonUtil.isNull(code)) {
                                        mAreaCodeList.add(codeItem);
                                    } else {
                                        mSigunguCodeList.add(codeItem);
                                    }
                                    currentCount++;
                                }
                                break;
                        }
                        eventType = parser.next();
                    }

                    if (CommonUtil.isNull(code)) {
                        if (areaCodeCount != mAreaCodeList.size() && currentCount > 0) {
                            areaCodePage++;
                            getData(Constants.REQUEST_AREA_CODE_INFO);
                        } else {
                            mHandler.sendEmptyMessage(Constants.REQUEST_AREA_CODE_INFO);
                        }
                    } else {
                        if (sigunguCodeCount != mSigunguCodeList.size() && currentCount > 0) {
                            sigunguCodePage++;
                            getData(Constants.REQUEST_AREA_CODE_INFO);
                        } else {
                            mHandler.sendEmptyMessage(Constants.REQUEST_SIGUNGU_CODE_INFO);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.e("지역코드조회 ERROR : " + e.getMessage());
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getCategoryCodeThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                try {
                    StringBuilder urlBuilder = new StringBuilder(Constants.API_CATEGORY_CODE_URL);
                    urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + Constants.API_SERVICE_KEY);
                    urlBuilder.append("&" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + URLEncoder.encode(Constants.API_SERVICE_KEY, "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과수*/
                    urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(String.valueOf(category1CodePage), "UTF-8")); /*현재 페이지 번호*/
                    urlBuilder.append("&" + URLEncoder.encode("MobileOS","UTF-8") + "=" + URLEncoder.encode("AND", "UTF-8")); /*IOS(아이폰), AND(안드로이드), WIN(원도우폰), ETC*/
                    urlBuilder.append("&" + URLEncoder.encode("MobileApp","UTF-8") + "=" + URLEncoder.encode("Tourist", "UTF-8")); /*서비스명=어플명*/


                    /*urlBuilder.append("&" + URLEncoder.encode("contentTypeId","UTF-8") + "=" + URLEncoder.encode("12", "UTF-8")); *//*관광타입(관광지,숙박등) ID*//*
                    urlBuilder.append("&" + URLEncoder.encode("cat1","UTF-8") + "=" + URLEncoder.encode("A01", "UTF-8")); *//*대분류코드*//*
                    urlBuilder.append("&" + URLEncoder.encode("cat2","UTF-8") + "=" + URLEncoder.encode("A0101", "UTF-8")); *//*중분류코드(대분류코드 필수)*//*
                    urlBuilder.append("&" + URLEncoder.encode("cat3","UTF-8") + "=" + URLEncoder.encode("", "UTF-8")); *//*소분류코드(중분류,대분류코드 필수)*/


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

                    LogUtil.e("서비스분류코드조회 : " + sb.toString());

                    InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new InputStreamReader(is, "UTF-8"));
                    int eventType = parser.getEventType();

                    CodeItem codeItem = null;
                    int currentCount = 0;

                    mCategory1CodeList.clear();
                    mCategory1CodeList.add(new CodeItem(Constants.TYPE_CATEGORY, "", "전체", 0, true));

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                String startTag = parser.getName();
                                if (startTag.equals("item")) {
                                    codeItem = new CodeItem();
                                    codeItem.setType(Constants.TYPE_CATEGORY);
                                }
                                if (startTag.equals("code"))
                                    codeItem.setCode(parser.nextText());
                                if (startTag.equals("name"))
                                    codeItem.setName(parser.nextText());
                                if (startTag.equals("rnum"))
                                    codeItem.setRnum(Integer.valueOf(parser.nextText()));
                                if (startTag.equals("totalCount"))
                                    category1CodeCount = Integer.valueOf(parser.nextText()) + 1;
                                break;
                            case XmlPullParser.END_TAG:
                                String endTag = parser.getName();
                                if (endTag.equals("item")) {
                                    mCategory1CodeList.add(codeItem);
                                    currentCount++;
                                }
                                break;
                        }
                        eventType = parser.next();
                    }

                    if (category1CodeCount != mCategory1CodeList.size() && currentCount > 0) {
                        category1CodePage++;
                        getData(Constants.REQUEST_CATEGORY_CODE_INFO);
                    } else {
                        mHandler.sendEmptyMessage(Constants.REQUEST_CATEGORY_CODE_INFO);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.e("서비스분류코드조회 ERROR : " + e.getMessage());
                }
            }
        });
        thread.start();
        return thread;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.mHeaderBack:
                    finish();
                    break;
                // Header 확인 onClick
                case R.id.mHeaderCheck:
                    if (mDatabases == null) mDatabases = new Databases(mContext);
                    mDatabases.open();
                    mDatabases.setFilterData(Constants.FILTER_DATA, mSortData);
                    mDatabases.close();
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch (msg.what) {
            case Constants.REQUEST_AREA_CODE_INFO:
                if (mAreaCodeList.size() > 0) {
                    LogUtil.e("Database 저장 :::: REQUEST_AREA_CODE_INFO");
                    if (mDatabases == null) mDatabases = new Databases(mContext);
                    mDatabases.open();
                    mDatabases.setCodeList(mAreaCodeList);
                    mDatabases.close();

                    bindData(TYPE_AREA_CODE);
                }
                break;
            case Constants.REQUEST_SIGUNGU_CODE_INFO:
                if (mSigunguCodeList.size() > 0) {
                    LogUtil.e("Database 저장 :::: REQUEST_SIGUNGU_CODE_INFO");
                    if (mDatabases == null) mDatabases = new Databases(mContext);
                    mDatabases.open();
                    mDatabases.setCodeList(mSigunguCodeList);
                    mDatabases.close();

                    bindData(TYPE_SIGUNGU_CODE);
                }
                break;
            case Constants.REQUEST_CATEGORY_CODE_INFO:
                if (mAreaCodeList.size() > 0) {
                    LogUtil.e("Database 저장 :::: REQUEST_CATEGORY_CODE_INFO");
                    if (mDatabases == null) mDatabases = new Databases(mContext);
                    mDatabases.open();
                    mDatabases.setCodeList(mCategory1CodeList);
                    mDatabases.close();

                    bindData(TYPE_CATEGORY_1_CODE);
                }
                break;
        }
    }
}
