package com.lx.savechild;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 3. API 요청 메소드를 정의하는 인터페이스
 */
public interface ApiService {


    @GET("geoserver/hospital/wfs")
    Call<CardDataResponse> getHospitalData(
            @Query("service") String service,
            @Query("version") String version,
            @Query("request") String request,
            @Query("typeName") String typeName,
            @Query("outputFormat") String outputFormat,
            @Query("FeatureID") String featureID // ◀ 동적으로 받을 FeatureID
    );
}