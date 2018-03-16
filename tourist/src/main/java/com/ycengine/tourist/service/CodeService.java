package com.ycengine.tourist.service;

import com.ycengine.tourist.model.ResponseItem;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CodeService {
    final DataService dataService = DataService.retrofit.create(DataService.class);

    private ResponseItem responseItem;

    private ArrayList<AreaCodeListener> areaCodeListeners = new ArrayList<>();
    private ArrayList<SigunguCodeListener> sigunguCodeListener = new ArrayList<>();
    private ArrayList<Category1CodeListener> category1CodeListener = new ArrayList<>();
    private ArrayList<Category2CodeListener> category2CodeListener = new ArrayList<>();
    private ArrayList<Category3CodeListener> category3CodeListener = new ArrayList<>();

    public void addAreaCodeListener(AreaCodeListener listener) {
        areaCodeListeners.add(listener);
    }

    public void addSigunguCodeListener(SigunguCodeListener listener) {
        sigunguCodeListener.add(listener);
    }

    public void addCategory1CodeListener(Category1CodeListener listener) {
        category1CodeListener.add(listener);
    }

    public void addCategory2CodeListener(Category2CodeListener listener) {
        category2CodeListener.add(listener);
    }

    public void addCategory3CodeListener(Category3CodeListener listener) {
        category3CodeListener.add(listener);
    }

    public void removeAreaCodeListener(AreaCodeListener listener) {
        areaCodeListeners.remove(listener);
    }

    public void removeSigunguCodeListener(SigunguCodeListener listener) {
        sigunguCodeListener.remove(listener);
    }

    public void removeCategory1CodeListener(Category1CodeListener listener) {
        category1CodeListener.remove(listener);
    }

    public void removeCategory2CodeListener(Category2CodeListener listener) {
        category2CodeListener.remove(listener);
    }

    public void removeCategory3CodeListener(Category3CodeListener listener) {
        category3CodeListener.remove(listener);
    }

    public void getAreaCode(Map<String, String> data) {
        if (data.size() == 0) {
            for (AreaCodeListener listener : areaCodeListeners) {
                listener.onAreaCodesLoadFailed("Invalid arguments");
            }
            return;
        }

        final Call<ResponseItem> call = dataService.getAreaCode(data);
        /**
         * enqueue()는 비동기로 Request를 보내고 Response가 돌아 왔을 때 콜백으로 앱에게 알립니다.
         * 이 Request는 비동기식이므로 Retrofit에서 Main UI 스레드가 차단되거나 간섭받지 않도록 Background 스레드에서 Request를 처리합니다.
         */
        call.enqueue(new Callback<ResponseItem>() {
            @Override
            public void onResponse(Call<ResponseItem> call, Response<ResponseItem> response) {
                if (response.isSuccessful()) {
                    responseItem = response.body();
                    for (AreaCodeListener listener : areaCodeListeners) {
                        listener.onAreaCodesLoaded(responseItem);
                    }
                } else {
                    for (AreaCodeListener listener : areaCodeListeners) {
                        listener.onAreaCodesLoadFailed(response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseItem> call, Throwable t) {
                for (AreaCodeListener listener : areaCodeListeners) {
                    listener.onAreaCodesLoadFailed(t.getMessage());
                }
            }

        });
    }

    public void requestCachedAreaCodes() {
        for (AreaCodeListener listener : areaCodeListeners) {
            listener.onAreaCodesLoaded(responseItem);
        }
    }

    public interface AreaCodeListener {
        void onAreaCodesLoaded(ResponseItem responseItem);
        void onAreaCodesLoadFailed(String message);
    }

    public interface SigunguCodeListener {
        void onSigunguCodesLoaded(ResponseItem responseItem);
        void onSigunguCodesLoadFailed(String message);
    }

    public interface Category1CodeListener {
        void onCategory1CodesLoaded(ResponseItem responseItem);
        void onCategory1CodesLoadFailed(String message);
    }

    public interface Category2CodeListener {
        void onCategory2CodesLoaded(ResponseItem responseItem);
        void onCategory2CodesLoadFailed(String message);
    }

    public interface Category3CodeListener {
        void onCategory3CodesLoaded(ResponseItem responseItem);
        void onCategory3CodesLoadFailed(String message);
    }
}
