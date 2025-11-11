package com.lx.savechild;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 1. Retrofit 인스턴스 관리 헬퍼 클래스
 */
public class ApiClient {


    public static final String BASE_URL = "https://api.child119.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // JSON -> Java 객체
                    .build();
        }
        return retrofit;
    }
}