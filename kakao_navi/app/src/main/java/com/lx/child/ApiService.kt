package com.lx.child

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("geoserver/hospital/ows")
    suspend fun getHospitalDetails(
        @Query("service") service: String = "WFS",
        @Query("version") version: String = "1.0.0",
        @Query("request") request: String = "GetFeature",
        @Query("typeName") typeName: String = "hospital:emergency",
        @Query("outputFormat") outputFormat: String = "application/json",
        @Query("featureID") featureID: String // <-- 이 값을 동적으로 받음
    ): HospitalResponse
}