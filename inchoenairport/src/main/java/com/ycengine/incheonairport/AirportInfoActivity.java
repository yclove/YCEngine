package com.ycengine.incheonairport;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yalantis.phoenix.PullToRefreshView;
import com.ycengine.incheonairport.protocol.data.AirportGateInfoItem;
import com.ycengine.incheonairport.protocol.data.AirportPassengerInfoItem;
import com.ycengine.incheonairport.widget.WaveHelper;
import com.ycengine.incheonairport.widget.WaveView;
import com.ycengine.yclibrary.util.DateUtil;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class AirportInfoActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage {
    private WeakRefHandler mWeakRefHandler;
    private FirebaseAnalytics mFirebaseAnalytics;

    private PullToRefreshView mPullToRefreshView;

    private TextView tvLatestDateTime, tvAirportGateInfo5, tvAirportGateInfo4, tvAirportGateInfo3, tvAirportGateInfo2;
    private TextView tvAirportGateWaitLevel3, tvAirportGateWaitLevel2, tvAirportGateWaitLevel1, tvAirportGateWaitLevel0, tvAirportGateWaitLevel9;
    private ListView lvAirportPassengerInfo;
    private View mAirportInfoHeader;
    private LinearLayout mAirportGateInfoAllDayBtn;
    private TextView tvAirportGateInfoPassengerPerTime, tvAirportGateInfoAllDay;
    private AirportInfoAdapter mAirportInfoAdapter;

    private WaveView mWaveGate5, mWaveGate4, mWaveGate3, mWaveGate2;
    private WaveHelper mWaveHelper5, mWaveHelper4, mWaveHelper3, mWaveHelper2;
    private int mBorderColor = Color.parseColor("#44FFFFFF");
    private int mBorderWidth = 0;

    @Override
    protected void onPause() {
        super.onPause();
        mWaveHelper5.cancel();
        mWaveHelper4.cancel();
        mWaveHelper3.cancel();
        mWaveHelper2.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWaveHelper5.start();
        mWaveHelper4.start();
        mWaveHelper3.start();
        mWaveHelper2.start();
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_airport_info;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeakRefHandler = new WeakRefHandler(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(getAdRequest());
        mAdView.setAdListener(mAdListener);

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.api_calling_msg));

        mAirportInfoHeader = getLayoutInflater().inflate(R.layout.inc_airport_info_header, null, false);
        tvLatestDateTime = (TextView) mAirportInfoHeader.findViewById(R.id.tvLatestDateTime);
        tvAirportGateInfo5 = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateInfo5);
        tvAirportGateInfo4 = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateInfo4);
        tvAirportGateInfo3 = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateInfo3);
        tvAirportGateInfo2 = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateInfo2);
        tvAirportGateWaitLevel3 = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateWaitLevel3);
        tvAirportGateWaitLevel2 = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateWaitLevel2);
        tvAirportGateWaitLevel1 = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateWaitLevel1);
        tvAirportGateWaitLevel0 = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateWaitLevel0);
        tvAirportGateWaitLevel9 = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateWaitLevel9);

        tvAirportGateInfoPassengerPerTime = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateInfoPassengerPerTime);
        tvAirportGateInfoAllDay = (TextView) mAirportInfoHeader.findViewById(R.id.tvAirportGateInfoAllDay);
        tvAirportGateInfoPassengerPerTime.setText(R.string.airport_info_passenger_per_time);
        tvAirportGateInfoAllDay.setText(R.string.airport_info_tomorrow);

        mAirportGateInfoAllDayBtn = (LinearLayout) mAirportInfoHeader.findViewById(R.id.mAirportGateInfoAllDayBtn);
        mAirportGateInfoAllDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("0".equals(searchCode)) {
                    searchCode = "1";
                    tvAirportGateInfoAllDay.setText(R.string.airport_info_today);
                } else {
                    searchCode = "0";
                    tvAirportGateInfoAllDay.setText(R.string.airport_info_tomorrow);
                }
                mProgressDialog.show();
                new Thread(getAirportPassengerInfoThread).start();
            }
        });

        mAirportInfoAdapter = new AirportInfoAdapter(mContext, mAirportPassengerInfoItem);
        lvAirportPassengerInfo = (ListView) findViewById(R.id.lvAirportPassengerInfo);

        lvAirportPassengerInfo.addHeaderView(mAirportInfoHeader, null, false);
        lvAirportPassengerInfo.setAdapter(mAirportInfoAdapter);

        tvAirportGateWaitLevel3.setText(R.string.airport_info_wait_level_3);
        tvAirportGateWaitLevel2.setText(R.string.airport_info_wait_level_2);
        tvAirportGateWaitLevel1.setText(R.string.airport_info_wait_level_1);
        tvAirportGateWaitLevel0.setText(R.string.airport_info_wait_level_0);
        tvAirportGateWaitLevel9.setText(R.string.airport_info_wait_level_9);

        mWaveGate5 = (WaveView) findViewById(R.id.mWaveGate5);
        mWaveGate4 = (WaveView) findViewById(R.id.mWaveGate4);
        mWaveGate3 = (WaveView) findViewById(R.id.mWaveGate3);
        mWaveGate2 = (WaveView) findViewById(R.id.mWaveGate2);

        mWaveGate5.setWaveColor(Color.parseColor("#00000000"), Color.parseColor("#00000000"));
        mWaveGate4.setWaveColor(Color.parseColor("#00000000"), Color.parseColor("#00000000"));
        mWaveGate3.setWaveColor(Color.parseColor("#00000000"), Color.parseColor("#00000000"));
        mWaveGate2.setWaveColor(Color.parseColor("#00000000"), Color.parseColor("#00000000"));

        mWaveGate5.setBorder(mBorderWidth, mBorderColor);
        mWaveGate4.setBorder(mBorderWidth, mBorderColor);
        mWaveGate3.setBorder(mBorderWidth, mBorderColor);
        mWaveGate2.setBorder(mBorderWidth, mBorderColor);

        mWaveHelper5 = new WaveHelper(mWaveGate5);
        mWaveHelper4 = new WaveHelper(mWaveGate4);
        mWaveHelper3 = new WaveHelper(mWaveGate3);
        mWaveHelper2 = new WaveHelper(mWaveGate2);

        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                        mProgressDialog.show();
                        new Thread(getAirportGateInfoThread).start();
                        new Thread(getAirportPassengerInfoThread).start();
                    }
                }, 500);
            }
        });

        mProgressDialog.show();
        new Thread(getAirportGateInfoThread).start();
        new Thread(getAirportPassengerInfoThread).start();
    }

    // 출국장 현황 정보 서비스
    private AirportGateInfoItem mAirportGateInfoItem = new AirportGateInfoItem();
    private String searchTerNo = "1"; /*터미널 구분 1: 1터미널 2: 2터미널 */
    private long searchTime = 0;
    private Runnable getAirportGateInfoThread = new Runnable() {
        public void run() {
            try {
                long currentTime = Long.valueOf(DateUtil.getCurrentDate("yyyyMMddHHmmss"));

                if (searchTime == 0 || currentTime - searchTime > 60) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "출국장 현황 정보 서비스");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "출국장 현황 정보 서비스");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "인천공항 오픈 API");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    searchTime = currentTime;
                    mWeakRefHandler.sendEmptyMessage(1000);

                    StringBuilder urlBuilder = new StringBuilder(Constants.API_AIRPORT_GATE_INFO_URL);
                    urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + Constants.API_AIRPORT_SERVICE_KEY);
                    urlBuilder.append("&" + URLEncoder.encode("terno", "UTF-8") + "=" + URLEncoder.encode(searchTerNo, "UTF-8"));
                    URL url = new URL(urlBuilder.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(Constants.TIMEOUT_HTTP_CONNECTION);
                    conn.setRequestProperty("Content-type", "application/json");

                    BufferedReader rd;
                    if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
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

                    LogUtil.e("출국장 현황 정보 서비스 : " + sb.toString());

                    InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new InputStreamReader(is, "UTF-8"));
                    int eventType = parser.getEventType();

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                String startTag = parser.getName();
                                if (startTag.equals("resultCode"))
                                    mAirportGateInfoItem.setResultCode(parser.nextText());
                                if (startTag.equals("resultMsg"))
                                    mAirportGateInfoItem.setResultMsg(parser.nextText());
                                if (startTag.equals("areadiv"))
                                    mAirportGateInfoItem.setAreadiv(parser.nextText());
                                if (startTag.equals("cgtdt"))
                                    mAirportGateInfoItem.setCgtdt(parser.nextText());
                                if (startTag.equals("cgthm"))
                                    mAirportGateInfoItem.setCgthm(parser.nextText());
                                if (startTag.equals("cgtlvlg2"))
                                    mAirportGateInfoItem.setCgtlvlg2(parser.nextText());
                                if (startTag.equals("cgtlvlg3"))
                                    mAirportGateInfoItem.setCgtlvlg3(parser.nextText());
                                if (startTag.equals("cgtlvlg4"))
                                    mAirportGateInfoItem.setCgtlvlg4(parser.nextText());
                                if (startTag.equals("cgtlvlg5"))
                                    mAirportGateInfoItem.setCgtlvlg5(parser.nextText());
                                if (startTag.equals("pwcntg2"))
                                    mAirportGateInfoItem.setPwcntg2(parser.nextText());
                                if (startTag.equals("pwcntg3"))
                                    mAirportGateInfoItem.setPwcntg3(parser.nextText());
                                if (startTag.equals("pwcntg4"))
                                    mAirportGateInfoItem.setPwcntg4(parser.nextText());
                                if (startTag.equals("pwcntg5"))
                                    mAirportGateInfoItem.setPwcntg5(parser.nextText());
                                if (startTag.equals("terno"))
                                    mAirportGateInfoItem.setTerno(parser.nextText());
                                break;
                        }
                        eventType = parser.next();
                    }
                    mWeakRefHandler.sendEmptyMessage(3000);
                } else {
                    mWeakRefHandler.sendEmptyMessage(2000);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e("출국장 현황 정보 서비스 ERROR : " + e.getMessage());
            }
        }
    };

    // 승객 예고 - 출입국별(국문)
    private ArrayList<AirportPassengerInfoItem> mAirportPassengerInfoItem = new ArrayList<>();
    private ArrayList<AirportPassengerInfoItem> mAirportPassengerInfoItemToday = new ArrayList<>();
    private ArrayList<AirportPassengerInfoItem> mAirportPassengerInfoItemTomorrow = new ArrayList<>();
    private String searchCode = "0"; /*오늘일자(D) ='0', 내일일자(D+1) ='1'*/
    private Runnable getAirportPassengerInfoThread = new Runnable() {
        public void run() {
            try {
                if ("0".equals(searchCode) && mAirportPassengerInfoItemToday.size() > 0) {
                    mAirportPassengerInfoItem = mAirportPassengerInfoItemToday;
                    mWeakRefHandler.sendEmptyMessage(4000);
                } else if ("1".equals(searchCode) && mAirportPassengerInfoItemTomorrow.size() > 0) {
                    mAirportPassengerInfoItem = mAirportPassengerInfoItemTomorrow;
                    mWeakRefHandler.sendEmptyMessage(4000);
                } else {
                    mWeakRefHandler.sendEmptyMessage(5000);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "승객 예고 - 출입국별(국문)");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "승객 예고 - 출입국별(국문)");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "인천공항 오픈 API");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    StringBuilder urlBuilder = new StringBuilder(Constants.API_AIRPORT_PASSENGER_INFO_URL);
                    urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + Constants.API_AIRPORT_SERVICE_KEY);
                    urlBuilder.append("&" + URLEncoder.encode("selectdate", "UTF-8") + "=" + URLEncoder.encode(searchCode, "UTF-8"));
                    URL url = new URL(urlBuilder.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(Constants.TIMEOUT_HTTP_CONNECTION);
                    conn.setRequestProperty("Content-type", "application/json");

                    BufferedReader rd;
                    if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
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
                    LogUtil.e("승객 예고 - 출입국별(국문) : " + sb.toString());

                    InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new InputStreamReader(is, "UTF-8"));
                    int eventType = parser.getEventType();
                    AirportPassengerInfoItem airportPassengerInfoItem = new AirportPassengerInfoItem();
                    airportPassengerInfoItem.setAdate(getString(R.string.airport_info_gate));
                    airportPassengerInfoItem.setAtime(getString(R.string.airport_info_gate));
                    airportPassengerInfoItem.setSum8(getString(R.string.airport_info_gate_5));
                    airportPassengerInfoItem.setSum7(getString(R.string.airport_info_gate_4));
                    airportPassengerInfoItem.setSum6(getString(R.string.airport_info_gate_3));
                    airportPassengerInfoItem.setSum5(getString(R.string.airport_info_gate_2));

                    mAirportPassengerInfoItem.add(airportPassengerInfoItem);
                    if ("0".equals(searchCode)) {
                        mAirportPassengerInfoItemToday.add(airportPassengerInfoItem);
                    } else if ("1".equals(searchCode)) {
                        mAirportPassengerInfoItemTomorrow.add(airportPassengerInfoItem);
                    }

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                String startTag = parser.getName();
                                if (startTag.equals("item"))
                                    airportPassengerInfoItem = new AirportPassengerInfoItem();
                                if (startTag.equals("adate"))
                                    airportPassengerInfoItem.setAdate(parser.nextText());
                                if (startTag.equals("atime"))
                                    airportPassengerInfoItem.setAtime(parser.nextText());
                                if (startTag.equals("sum1"))
                                    airportPassengerInfoItem.setSum1(parser.nextText());
                                if (startTag.equals("sum2"))
                                    airportPassengerInfoItem.setSum2(parser.nextText());
                                if (startTag.equals("sum3"))
                                    airportPassengerInfoItem.setSum3(parser.nextText());
                                if (startTag.equals("sum4"))
                                    airportPassengerInfoItem.setSum4(parser.nextText());
                                if (startTag.equals("sum5"))
                                    airportPassengerInfoItem.setSum5(parser.nextText());
                                if (startTag.equals("sum6"))
                                    airportPassengerInfoItem.setSum6(parser.nextText());
                                if (startTag.equals("sum7"))
                                    airportPassengerInfoItem.setSum7(parser.nextText());
                                if (startTag.equals("sum8"))
                                    airportPassengerInfoItem.setSum8(parser.nextText());
                                if (startTag.equals("sumset1"))
                                    airportPassengerInfoItem.setSumset1(parser.nextText());
                                if (startTag.equals("sumset2"))
                                    airportPassengerInfoItem.setSumset2(parser.nextText());
                                break;
                            case XmlPullParser.END_TAG:
                                String endTag = parser.getName();
                                if (endTag.equals("item")) {
                                    if (airportPassengerInfoItem.getAdate().length() == 8) {
                                        int currentHour = Integer.valueOf(DateUtil.getCurrentDate("HH"));
                                        int dataHour = Integer.valueOf(airportPassengerInfoItem.getAtime().substring(0, 2));

                                        if (currentHour <= dataHour || !"0".equals(searchCode)) {
                                            mAirportPassengerInfoItem.add(airportPassengerInfoItem);
                                            if ("0".equals(searchCode)) {
                                                mAirportPassengerInfoItemToday.add(airportPassengerInfoItem);
                                            } else if ("1".equals(searchCode)) {
                                                mAirportPassengerInfoItemTomorrow.add(airportPassengerInfoItem);
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                        eventType = parser.next();
                    }

                    mWeakRefHandler.sendEmptyMessage(4000);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e("승객 예고 - 출입국별(국문) ERROR : " + e.getMessage());
            }
        }
    };

    @Override
    public void onClick(final View v) {
        final int id = v.getId();

        switch (id) {
            case R.id.mCloseBtn:
                finish();
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1000:
                tvLatestDateTime.setText("");
                tvAirportGateInfo5.setText("");
                tvAirportGateInfo4.setText("");
                tvAirportGateInfo3.setText("");
                tvAirportGateInfo2.setText("");

                tvAirportGateInfo5.setBackgroundResource(getGateLevelResource(9));
                tvAirportGateInfo4.setBackgroundResource(getGateLevelResource(9));
                tvAirportGateInfo3.setBackgroundResource(getGateLevelResource(9));
                tvAirportGateInfo2.setBackgroundResource(getGateLevelResource(9));

                mWaveGate5.setWaveColor(Color.parseColor("#00000000"), Color.parseColor("#00000000"));
                mWaveGate4.setWaveColor(Color.parseColor("#00000000"), Color.parseColor("#00000000"));
                mWaveGate3.setWaveColor(Color.parseColor("#00000000"), Color.parseColor("#00000000"));
                mWaveGate2.setWaveColor(Color.parseColor("#00000000"), Color.parseColor("#00000000"));
                break;
            case 2000:
                mProgressDialog.dismiss();

                Toast toast = Toast.makeText(mContext, R.string.airport_info_update_not_yet, Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                toast.show();
                break;
            case 3000:
                mProgressDialog.dismiss();

                if ("00".equals(mAirportGateInfoItem.getResultCode())) {
                    try {
                        String latestUpdate = mAirportGateInfoItem.getCgtdt() + mAirportGateInfoItem.getCgthm();
                        DateFormat originalFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
                        DateFormat targetFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.ENGLISH);
                        Date date = originalFormat.parse(latestUpdate);
                        latestUpdate = targetFormat.format(date);

                        int waitPassengerGate5 = Integer.valueOf(mAirportGateInfoItem.getPwcntg5());
                        int waitPassengerGate4 = Integer.valueOf(mAirportGateInfoItem.getPwcntg4());
                        int waitPassengerGate3 = Integer.valueOf(mAirportGateInfoItem.getPwcntg3());
                        int waitPassengerGate2 = Integer.valueOf(mAirportGateInfoItem.getPwcntg2());
                        int waitLevelPassengerGate5 = Integer.valueOf(mAirportGateInfoItem.getCgtlvlg5());
                        int waitLevelPassengerGate4 = Integer.valueOf(mAirportGateInfoItem.getCgtlvlg4());
                        int waitLevelPassengerGate3 = Integer.valueOf(mAirportGateInfoItem.getCgtlvlg3());
                        int waitLevelPassengerGate2 = Integer.valueOf(mAirportGateInfoItem.getCgtlvlg2());

                        tvLatestDateTime.setText(getString(R.string.airport_info_latest_update, latestUpdate));
                        tvAirportGateInfo5.setText(getString(R.string.airport_info_wait_passenger, new DecimalFormat("#,###").format(waitPassengerGate5)));
                        tvAirportGateInfo4.setText(getString(R.string.airport_info_wait_passenger, new DecimalFormat("#,###").format(waitPassengerGate4)));
                        tvAirportGateInfo3.setText(getString(R.string.airport_info_wait_passenger, new DecimalFormat("#,###").format(waitPassengerGate3)));
                        tvAirportGateInfo2.setText(getString(R.string.airport_info_wait_passenger, new DecimalFormat("#,###").format(waitPassengerGate2)));

                        tvAirportGateInfo5.setBackgroundResource(getGateLevelResource(waitLevelPassengerGate5));
                        tvAirportGateInfo4.setBackgroundResource(getGateLevelResource(waitLevelPassengerGate4));
                        tvAirportGateInfo3.setBackgroundResource(getGateLevelResource(waitLevelPassengerGate3));
                        tvAirportGateInfo2.setBackgroundResource(getGateLevelResource(waitLevelPassengerGate2));

                        ViewGroup.LayoutParams mWaveGate5Params = mWaveGate5.getLayoutParams();
                        mWaveGate5Params.height = getWaveLevelHeight(waitPassengerGate5);
                        mWaveGate5.setLayoutParams(mWaveGate5Params);
                        mWaveGate5.requestLayout();

                        ViewGroup.LayoutParams mWaveGate4Params = mWaveGate4.getLayoutParams();
                        mWaveGate4Params.height = getWaveLevelHeight(waitPassengerGate4);
                        mWaveGate4.setLayoutParams(mWaveGate4Params);
                        mWaveGate4.requestLayout();

                        ViewGroup.LayoutParams mWaveGate3Params = mWaveGate3.getLayoutParams();
                        mWaveGate3Params.height = getWaveLevelHeight(waitPassengerGate3);
                        mWaveGate3.setLayoutParams(mWaveGate3Params);
                        mWaveGate3.requestLayout();

                        ViewGroup.LayoutParams mWaveGate2Params = mWaveGate2.getLayoutParams();
                        mWaveGate2Params.height = getWaveLevelHeight(waitPassengerGate2);
                        mWaveGate2.setLayoutParams(mWaveGate2Params);
                        mWaveGate2.requestLayout();

                        mWaveGate5.setWaveColor(getWaveLevelColor(waitLevelPassengerGate5, true), getWaveLevelColor(waitLevelPassengerGate5, false));
                        mWaveGate4.setWaveColor(getWaveLevelColor(waitLevelPassengerGate4, true), getWaveLevelColor(waitLevelPassengerGate4, false));
                        mWaveGate3.setWaveColor(getWaveLevelColor(waitLevelPassengerGate3, true), getWaveLevelColor(waitLevelPassengerGate3, false));
                        mWaveGate2.setWaveColor(getWaveLevelColor(waitLevelPassengerGate2, true), getWaveLevelColor(waitLevelPassengerGate2, false));

                        /*Toast updateToast = Toast.makeText(mContext, getString(R.string.airport_info_update, DateUtil.getCurrentDate("yyyy.MM.dd. HH:mm")), Toast.LENGTH_LONG);
                        TextView updateView = (TextView) updateToast.getView().findViewById(android.R.id.message);
                        if (updateView != null) updateView.setGravity(Gravity.CENTER);
                        updateToast.show();*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 4000:
                mProgressDialog.dismiss();

                if (mAirportPassengerInfoItem.size() > 0) {
                    mAirportInfoAdapter.addAllItems(mAirportPassengerInfoItem);
                }
                break;
            case 5000:
                mAirportInfoAdapter.addAllItems(null);
                break;
        }
    }

    private int getGateLevelResource(int waitLevel) {
        int rtResource = R.drawable.label_gate_leve_0;

        switch (waitLevel) {
            case 0:
                rtResource = R.drawable.label_gate_leve_0;
                break;
            case 1:
                rtResource = R.drawable.label_gate_leve_1;
                break;
            case 2:
                rtResource = R.drawable.label_gate_leve_2;
                break;
            case 3:
                rtResource = R.drawable.label_gate_leve_3;
                break;
            case 9:
                rtResource = R.drawable.label_gate_leve_9;
                break;
        }

        return rtResource;
    }

    private int getWaveLevelColor(int waveLevel, boolean isBehind) {
        int rtColor = 0;

        switch (waveLevel) {
            case 0:
                rtColor = isBehind ? ContextCompat.getColor(getApplicationContext(), R.color.airport_level_0_alpha_behind) : ContextCompat.getColor(getApplicationContext(), R.color.airport_level_0_alpha_front);
                break;
            case 1:
                rtColor = isBehind ? ContextCompat.getColor(getApplicationContext(), R.color.airport_level_1_alpha_behind) : ContextCompat.getColor(getApplicationContext(), R.color.airport_level_1_alpha_front);
                break;
            case 2:
                rtColor = isBehind ? ContextCompat.getColor(getApplicationContext(), R.color.airport_level_2_alpha_behind) : ContextCompat.getColor(getApplicationContext(), R.color.airport_level_2_alpha_front);
                break;
            case 3:
                rtColor = isBehind ? ContextCompat.getColor(getApplicationContext(), R.color.airport_level_3_alpha_behind) : ContextCompat.getColor(getApplicationContext(), R.color.airport_level_3_alpha_front);
                break;
            case 9:
                rtColor = isBehind ? ContextCompat.getColor(getApplicationContext(), R.color.airport_level_9_alpha_behind) : ContextCompat.getColor(getApplicationContext(), R.color.airport_level_9_alpha_front);
                break;
        }

        return rtColor;
    }

    private int getWaveLevelHeight(int waitCount) {
        if (waitCount <= 0) {
            Toast.makeText(getApplicationContext(), "waitCount : " + waitCount, Toast.LENGTH_SHORT).show();
            return 1;
        }
        if (waitCount > 120) waitCount = 120;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, waitCount, mContext.getResources().getDisplayMetrics());
    }
}
