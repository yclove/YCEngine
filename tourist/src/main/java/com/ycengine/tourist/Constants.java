package com.ycengine.tourist;

public class Constants {

    public static final int TIMEOUT_HTTP_CONNECTION = 30000;

    public static final String API_SERVICE_KEY = "vTd%2FkIKilSxb%2ByjKme560lIH3zL%2F3KGTqXdO6nbyEZV4P9mrekQNz59PiCVyKVUjGT286YKdMeblAiqUNMj1Cg%3D%3D";
    // 지역코드조회
    public static final String API_AREA_CODE_URL = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaCode";
    // 서비스분류코드조회
    public static final String API_CATEGORY_CODE_URL = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/categoryCode";
    // 지역기반 관광정보조회
    public static final String API_AREA_BASED_LIST_URL = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList";

    public static final String TYPE_AREA = "AREA";
    public static final String TYPE_CATEGORY = "CATEGORY";

    public static final String FILTER_DATA = "FILTER_DATA";
    public static final String FILTER_AREA_CODE = "FILTER_AREA_CODE";
    public static final String FILTER_SIGUNGU_CODE = "FILTER_SIGUNGU_CODE";
    public static final String FILTER_CATEGORY_1_CODE = "FILTER_CATEGORY_1_CODE";
    public static final String FILTER_CATEGORY_2_CODE = "FILTER_CATEGORY_2_CODE";
    public static final String FILTER_CATEGORY_3_CODE = "FILTER_CATEGORY_3_CODE";

    public static final int REQUEST_FILTER_INFO = 1000;
    public static final int REQUEST_AREA_CODE_INFO = 2000;
    public static final int REQUEST_SIGUNGU_CODE_INFO = 3000;
    public static final int REQUEST_CATEGORY_CODE_INFO = 4000;
    public static final int REQUEST_AREA_BASED_LIST = 5000;

}
