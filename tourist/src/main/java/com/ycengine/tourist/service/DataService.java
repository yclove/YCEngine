package com.ycengine.tourist.service;

import com.ycengine.tourist.model.CodeItem;
import com.ycengine.tourist.model.ResponseItem;

import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

interface DataService {
    HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://api.visitkorea.or.kr/")
            //.addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .client(okHttpClient)
            .build();

    @GET("openapi/service/rest/KorService/areaCode")
    Call<ResponseItem> getAreaCode(
            @QueryMap(encoded=true) Map<String, String> options
    );

    @GET("openapi/service/rest/KorService/areaCode")
    Call<List<CodeItem>> getAreaCodeEn(
            @QueryMap(encoded=true) Map<String, String> options
    );

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @GET("repos/{owner}/{repo}/contributors")
    Call<List<CodeItem>> repoContributors(
            @Path("owner") String owner,
            @Path("repo") String repo
    );
}