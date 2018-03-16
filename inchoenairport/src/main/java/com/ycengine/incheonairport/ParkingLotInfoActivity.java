package com.ycengine.incheonairport;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yalantis.phoenix.PullToRefreshView;
import com.ycengine.incheonairport.protocol.data.ParkingLotInfoItem;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.ycengine.yclibrary.Constants.getAdRequest;

public class ParkingLotInfoActivity extends BaseActivity implements View.OnClickListener, IOnHandlerMessage {
    private WeakRefHandler mWeakRefHandler;
    private FirebaseAnalytics mFirebaseAnalytics;

    private TextView tvLatestDateTime, tvShortPeriod1F, tvShortPeriodB1, tvShortPeriodB2, tvShortPeriodB3, tvTowerP1, tvTowerP2, tvLongPeriodP1, tvLongPeriodP2, tvLongPeriodP3, tvLongPeriodP4;

    private PullToRefreshView mPullToRefreshView;

    @Override
    protected int layoutResId() {
        return R.layout.activity_parking_lot_info;
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

        tvLatestDateTime = (TextView) findViewById(R.id.tvLatestDateTime);
        tvShortPeriod1F = (TextView) findViewById(R.id.tvShortPeriod1F);
        tvShortPeriodB1 = (TextView) findViewById(R.id.tvShortPeriodB1);
        tvShortPeriodB2 = (TextView) findViewById(R.id.tvShortPeriodB2);
        tvShortPeriodB3 = (TextView) findViewById(R.id.tvShortPeriodB3);
        tvTowerP1 = (TextView) findViewById(R.id.tvTowerP1);
        tvTowerP2 = (TextView) findViewById(R.id.tvTowerP2);
        tvLongPeriodP1 = (TextView) findViewById(R.id.tvLongPeriodP1);
        tvLongPeriodP2 = (TextView) findViewById(R.id.tvLongPeriodP2);
        tvLongPeriodP3 = (TextView) findViewById(R.id.tvLongPeriodP3);
        tvLongPeriodP4 = (TextView) findViewById(R.id.tvLongPeriodP4);

        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                        mProgressDialog.show();
                        new Thread(getParkingLotInfoThread).start();
                    }
                }, 500);
            }
        });

        mProgressDialog.show();
        new Thread(getParkingLotInfoThread).start();
    }

    // 인천공항 주차정보 서비스
    private ArrayList<ParkingLotInfoItem> mParkingLotInfoItem = new ArrayList<>();
    private long searchTime = 0;

    private Runnable getParkingLotInfoThread = new Runnable() {
        public void run() {
            try {
                long currentTime = Long.valueOf(DateUtil.getCurrentDate("yyyyMMddHHmmss"));

                if (searchTime == 0 || currentTime - searchTime > 60) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "인천공항 주차정보 서비스");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "인천공항 주차정보 서비스");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "인천공항 오픈 API");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    searchTime = currentTime;
                    mWeakRefHandler.sendEmptyMessage(1000);

                    StringBuilder urlBuilder = new StringBuilder(Constants.API_PARKING_LOT_INFO_URL);
                    urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + Constants.API_AIRPORT_SERVICE_KEY);
                    urlBuilder.append("&" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + URLEncoder.encode(Constants.API_AIRPORT_SERVICE_KEY, "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
                    urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
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

                    LogUtil.e("인천공항 주차정보 서비스 : " + sb.toString());

                    InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new InputStreamReader(is, "UTF-8"));
                    int eventType = parser.getEventType();

                    ParkingLotInfoItem parkingLotInfoItem = new ParkingLotInfoItem();

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                String startTag = parser.getName();
                                if (startTag.equals("item"))
                                    parkingLotInfoItem = new ParkingLotInfoItem();
                                if (startTag.equals("datetm"))
                                    parkingLotInfoItem.setDatetm(parser.nextText());
                                if (startTag.equals("floor"))
                                    parkingLotInfoItem.setFloor(parser.nextText());
                                if (startTag.equals("parking"))
                                    parkingLotInfoItem.setParking(parser.nextText());
                                if (startTag.equals("parkingarea"))
                                    parkingLotInfoItem.setParkingarea(parser.nextText());
                                break;
                            case XmlPullParser.END_TAG:
                                String endTag = parser.getName();
                                if (endTag.equals("item")) {
                                    mParkingLotInfoItem.add(parkingLotInfoItem);
                                }
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
                LogUtil.e("인천공항 주차정보 서비스 ERROR : " + e.getMessage());
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
                tvShortPeriod1F.setText("");
                tvShortPeriodB1.setText("");
                tvShortPeriodB2.setText("");
                tvShortPeriodB3.setText("");
                tvTowerP1.setText("");
                tvTowerP2.setText("");
                tvLongPeriodP1.setText("");
                tvLongPeriodP2.setText("");
                tvLongPeriodP3.setText("");
                tvLongPeriodP4.setText("");
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

                if (mParkingLotInfoItem.size() > 0) {
                    try {
                        String latestUpdateResult = "";

                        for (ParkingLotInfoItem item : mParkingLotInfoItem) {
                            String latestUpdate = item.getDatetm();
                            String mFloor = item.getFloor();
                            String mParking = item.getParking();
                            String mParkingArea = item.getParkingarea();

                            if (latestUpdate.length() >= 12 && latestUpdateResult.isEmpty()) {
                                DateFormat originalFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
                                DateFormat targetFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.ENGLISH);
                                Date date = originalFormat.parse(latestUpdate.substring(0, 12));
                                latestUpdate = targetFormat.format(date);
                                latestUpdateResult = latestUpdate;
                                tvLatestDateTime.setText(getString(R.string.airport_info_latest_update, latestUpdateResult));
                            }

                            String mParkingResult = "";
                            int txtColor = Color.parseColor("#888888");
                            if (Integer.valueOf(mParkingArea) - Integer.valueOf(mParking) > 0) {
                                mParkingResult = getString(R.string.parking_lot_info_usable, String.valueOf(Integer.valueOf(mParkingArea) - Integer.valueOf(mParking)));
                            } else {
                                mParkingResult = getString(R.string.parking_lot_info_full);
                                txtColor = Color.parseColor("#f44336");
                            }

                            switch (mFloor) {
                                case "단기주차장지상층":
                                    tvShortPeriod1F.setText(mParkingResult);
                                    tvShortPeriod1F.setTextColor(txtColor);
                                    break;
                                case "단기주차장지하1층":
                                    tvShortPeriodB1.setText(mParkingResult);
                                    tvShortPeriodB1.setTextColor(txtColor);
                                    break;
                                case "단기주차장지하2층":
                                    tvShortPeriodB2.setText(mParkingResult);
                                    tvShortPeriodB2.setTextColor(txtColor);
                                    break;
                                case "단기주차장지하3층":
                                    tvShortPeriodB3.setText(mParkingResult);
                                    tvShortPeriodB3.setTextColor(txtColor);
                                    break;
                                case "장기 P1 주차타워":
                                    tvTowerP1.setText(mParkingResult);
                                    tvTowerP1.setTextColor(txtColor);
                                    break;
                                case "장기 P2 주차타워":
                                    tvTowerP2.setText(mParkingResult);
                                    tvTowerP2.setTextColor(txtColor);
                                    break;
                                case "장기 P1 주차장":
                                    tvLongPeriodP1.setText(mParkingResult);
                                    tvLongPeriodP1.setTextColor(txtColor);
                                    break;
                                case "장기 P2 주차장":
                                    tvLongPeriodP2.setText(mParkingResult);
                                    tvLongPeriodP2.setTextColor(txtColor);
                                    break;
                                case "장기 P3 주차장":
                                    tvLongPeriodP3.setText(mParkingResult);
                                    tvLongPeriodP3.setTextColor(txtColor);
                                    break;
                                case "장기 P4 주차장":
                                    tvLongPeriodP4.setText(mParkingResult);
                                    tvLongPeriodP4.setTextColor(txtColor);
                                    break;
                            }
                        }

                        /*Toast updateToast = Toast.makeText(mContext, getString(R.string.airport_info_update, DateUtil.getCurrentDate("yyyy.MM.dd. HH:mm")), Toast.LENGTH_LONG);
                        TextView updateView = (TextView) updateToast.getView().findViewById(android.R.id.message);
                        if (updateView != null) updateView.setGravity(Gravity.CENTER);
                        updateToast.show();*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
