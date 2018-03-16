package com.ycengine.yclibrary;

import com.google.android.gms.ads.AdRequest;

public class Constants {
    public static final String SERVICE_TAG = "YCEngine";

    public static final boolean DEBUG = true;
    public static final boolean DEBUG_DATABASE = true;

    public static AdRequest getAdRequest() {
        AdRequest mAdRequest;

        if (DEBUG) {
            mAdRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("4F7557A27162E054FC07CDEA72E1E1A8") // YCNOTE 5
                    //.addTestDevice("FE7E48B634719D6B987EE779BD254E65") // S 7
                    .build();
        } else
            mAdRequest = new AdRequest.Builder().build();

        return mAdRequest;
    }

    // DIALOG TYPE
    public static final int DIALOG_TYPE_UPDATE_NEED = 0;
    public static final int DIALOG_TYPE_UPDATE_SUPPORT = 1;
    public static final int DIALOG_TYPE_CHOICE_YEAR = 2;
    public static final int DIALOG_TYPE_CONFIRM = 3;
    public static final int DIALOG_TYPE_LOCATION_SERVICE = 4;
    public static final int DIALOG_TYPE_INFO = 5;
    public static final int DIALOG_TYPE_RADIO_TITLE = 6;
    public static final int DIALOG_TYPE_NEWS_AGENCY = 7;
    public static final int DIALOG_TYPE_STAMP_REQUIRED = 8;
    public static final int DIALOG_TYPE_NOTIFY = 9;
    public static final int DIALOG_TYPE_SHARE = 10;
    public static final int DIALOG_TYPE_NOTICE = 11;
    public static final int DIALOG_TYPE_CALENDAR = 12;

    /*
    100% — FF
    95% — F2
    90% — E6
    85% — D9
    80% — CC
    75% — BF
    70% — B3
    65% — A6
    60% — 99
    55% — 8C
    50% — 80
    45% — 73
    40% — 66
    35% — 59
    30% — 4D
    25% — 40
    20% — 33
    15% — 26
    10% — 1A
    5% — 0D
    0% — 00
    */
}
