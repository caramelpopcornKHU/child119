package com.myapplication;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.skt.tmap.engine.navigation.SDKLocationProvider;
import com.skt.tmap.engine.navigation.location.LocationProviderListener;
import com.skt.tmap.engine.navigation.location.OnErrorListener;

public class CustomLocationProvider extends SDKLocationProvider {

    private static final String NAME = "custom"; // Provider 식별자
    private LocationProviderListener listener;
    private Handler handler;

    // 위치 정보 기본값 (원하는 값으로 수정)
    private static final double DEFAULT_LATITUDE = 37.56649022367982;
    private static final double DEFAULT_LONGITUDE = 126.98503808253983;

    @Override
    public void initializeProvider(LocationProviderListener locationProviderListener) {
        // SDK 초기화 시 호출됨. listener 저장 및 locationThreadLooper에서 handler 초기화
        this.listener = locationProviderListener;
        if (locationThreadLooper != null) {
            handler = new Handler(locationThreadLooper); // locationThreadLooper는 Navigation SDK에서 주입됨
        }
    }

    @Override
    public String getName() {
        // SDK에서 Provider 구분 시 사용되는 식별자 반환
        Log.d("CustomLocationProvider", "getName");
        return NAME;
    }

    @Override
    public void stop() {
        // Location Provider를 release 하는 작업 수행
        Log.d("CustomLocationProvider", "stop");
        removeLocationUpdates();
    }

    @Override
    public void requestUpdateOnce() {
        // 위치 데이터 1회 제공
        Log.d("CustomLocationProvider", "requestUpdateOnce");
        Location location = new Location(NAME);
        location.setLatitude(DEFAULT_LATITUDE);   // 위도
        location.setLongitude(DEFAULT_LONGITUDE); // 경도
        location.setAccuracy(5f);                 // 정확도 (meters)
        location.setSpeed(10f);                   // 속도 (m/s)
        location.setBearing(180f);                // 방향 (degrees)
        location.setTime(System.currentTimeMillis()); // 위치 획득 시간

        setSatelliteCount(location, 4); // 위성 수: 4 이상이어야 MapMatching 사용 가능

        if (handler != null && listener != null) {
            handler.post(() -> listener.onLocationChanged(location));
        }
    }

    @Override
    public void requestUpdate(int intervalTime) {
        // intervalTime을 기준으로 주기적으로 위치 데이터 제공
        Log.d("CustomLocationProvider", "requestUpdate");
    }

    @Override
    public void removeLocationUpdates() {
        // 위치 업데이트 중단 시 자원 해제 로직
        Log.d("CustomLocationProvider", "removeLocationUpdates");
    }

    @Override
    public void turnOnGPS() {
        // GPS 켜기 로직이 필요할 경우 구현
        Log.d("CustomLocationProvider", "turnOnGPS");
    }

    @Override
    public void setErrorListener(OnErrorListener onErrorListener) {
        // 에러 처리 리스너 설정
        Log.d("CustomLocationProvider", "setErrorListener");
    }

    // Location 객체에 위치 정보를 생성하기 위해 참조한 위성의 수를 추가하는 유틸 메서드
    private void setSatelliteCount(Location location, int satelliteCount) {
        Log.d("CustomLocationProvider", "setSatelliteCount");
        Bundle extras = location.getExtras();
        if (extras == null) {
            extras = new Bundle();
            location.setExtras(extras);
        }
        extras.putInt("satelliteCount", satelliteCount);
    }
}
