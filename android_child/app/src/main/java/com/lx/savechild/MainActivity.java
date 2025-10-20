package com.lx.savechild;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.skt.tmap.engine.navigation.SDKLocationProvider;
import com.skt.tmap.engine.navigation.SDKManager;
import com.skt.tmap.engine.navigation.data.RoadInfo;
import com.skt.tmap.engine.navigation.data.RoadInfoFacilityCode;
import com.skt.tmap.engine.navigation.data.RoadInfoRoadType;
import com.skt.tmap.engine.navigation.data.TruckType;
import com.skt.tmap.engine.navigation.livedata.ObservableRouteProgressData;
import com.skt.tmap.engine.navigation.network.ndds.CarOilType;
import com.skt.tmap.engine.navigation.network.ndds.TollCarType;
import com.skt.tmap.engine.navigation.route.RoutePlanType;
import com.skt.tmap.engine.navigation.route.RouteResult;
import com.skt.tmap.engine.navigation.route.data.MapPoint;
import com.skt.tmap.engine.navigation.route.data.WayPoint;
import com.skt.tmap.vsm.coordinates.VSMCoordinates;
import com.skt.tmap.vsm.data.VSMMapPoint;
import com.skt.tmap.vsm.location.LocationComponent;
import com.skt.tmap.vsm.location.VSMLocationData;
import com.skt.tmap.vsm.map.MapEngine;
import com.skt.tmap.vsm.map.marker.MarkerImage;
import com.skt.tmap.vsm.map.marker.VSMMarkerBase;
import com.skt.tmap.vsm.map.marker.VSMMarkerManager;
import com.skt.tmap.vsm.map.marker.VSMMarkerPoint;
import com.tmapmobility.tmap.tmapsdk.ui.data.CarOption;
import com.tmapmobility.tmap.tmapsdk.ui.data.LocationInitParam;
import com.tmapmobility.tmap.tmapsdk.ui.data.MapSetting;
import com.tmapmobility.tmap.tmapsdk.ui.data.MapViewMode;
import com.tmapmobility.tmap.tmapsdk.ui.data.NavigationScreenState;
import com.tmapmobility.tmap.tmapsdk.ui.data.NavigationScreenStateListener;
import com.tmapmobility.tmap.tmapsdk.ui.data.ObservableRouteData;
import com.tmapmobility.tmap.tmapsdk.ui.data.RouteDataCoord;
import com.tmapmobility.tmap.tmapsdk.ui.data.RouteDataTraffic;
import com.tmapmobility.tmap.tmapsdk.ui.data.RouteDataTurnInfo;
import com.tmapmobility.tmap.tmapsdk.ui.data.RouteGuidanceConfig;
import com.tmapmobility.tmap.tmapsdk.ui.data.TruckInfoKey;
import com.tmapmobility.tmap.tmapsdk.ui.data.UIVisibleOption;
import com.tmapmobility.tmap.tmapsdk.ui.fragment.NavigationFragment;
import com.tmapmobility.tmap.tmapsdk.ui.util.TmapUISDK;
import com.tmapmobility.tmap.tmapsdk.ui.view.MapConstant;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TMapUISDKSample";

    private final static String CLIENT_ID = "";
    private final static String API_KEY = "TMG8wFNgNI4KLaTXNXGfi2Edo8sBEp8J50JVxgDQ"; //발급받은 KEY
    private final static String USER_KEY = "";
    private final static String DEVICE_KEY = "";

    private NavigationFragment navigationFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    private Button button1, button2, button3, button4, button5, button6, button7, button8, button9, button10;
    private Button stopButton, changeViewModeButton;
    private Button btnGoToWeb;
    private int VIEW_MODE = 0;

    private LinearLayout buttonLayout;

    boolean isEDC; // edc 수신 여부
    boolean isRoute; // route data 수신 여부;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private int satelliteCount = 0;

    final com.myapplication.LocationDatabaseHelper dbHelper = new com.myapplication.LocationDatabaseHelper(MainActivity.this);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        checkPermission();
    }

    private void checkPermission() {

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initUI();
            initUISDK();
        } else {
            String[] permissionArr = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissionArr, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            initUI();
            initUISDK();
        } else {
            Toast.makeText(this, "위치 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initUI() {
        fragmentManager = getSupportFragmentManager();

        navigationFragment = TmapUISDK.Companion.getFragment();

        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.tmapUILayout, navigationFragment);
        transaction.commitAllowingStateLoss();

        buttonLayout = findViewById(R.id.buttonLayout);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        button10 = findViewById(R.id.button10);

        button1.setOnClickListener(onClickButton);
        button2.setOnClickListener(onClickButton);
        button3.setOnClickListener(onClickButton);
        button4.setOnClickListener(onClickButton);
        button5.setOnClickListener(onClickButton);
        button6.setOnClickListener(onClickButton);
        button7.setOnClickListener(onClickButton);
        button8.setOnClickListener(onClickButton);
        button9.setOnClickListener(onClickButton);
        button10.setOnClickListener(onClickButton);


        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(onClickButton);
        stopButton.setVisibility(View.GONE);

        changeViewModeButton = findViewById(R.id.changeViewModeButton);
        changeViewModeButton.setOnClickListener(onClickButton);
        changeViewModeButton.setVisibility(View.GONE);

        btnGoToWeb = findViewById(R.id.btnGoToWeb); // (activity_main.xml에 ID가 btnGoToWeb이어야 함)
        btnGoToWeb.setOnClickListener(onClickButton);
        btnGoToWeb.setVisibility(View.VISIBLE);

        isEDC = false;

        // 야간 모드 상태 수신
        navigationFragment.getNightModeLiveData().observe(this, isNight -> {
            Log.d("isNight", "night mode changed: " + isNight);
        });

        // 네비게이션 상태 변경 시 callback
        navigationFragment.setDrivingStatusCallback(new TmapUISDK.DrivingStatusCallback() {

            @Override
            public void onStartNavigationInfo(int totalDistanceInMeter, int totalTimeInSec, int tollFree) {
                //경로 시작 정보
                Log.d(TAG, "onStartNavigationInfo " + totalDistanceInMeter + "::" + totalTimeInSec + "::" + tollFree);

                File downloadsDir = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS);
                String path = downloadsDir.getAbsolutePath();
                RouteResult routeResult = TmapUISDK.getRouteResult();
                RouteResult.save(path + "/routeResult.json", routeResult);

                setLocation();
            }

            @Override
            public void onUserRerouteComplete() {
                // 사용자 재탐색 동작 완료 시 호출
                Log.e(TAG, "onUserRerouteComplete");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onUserRerouteComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onStartNavigation() {

                // 네비게이션 시작 시 호출
                Log.e(TAG, "onStartNavigation");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onStartNavigation", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onStopNavigation() {
                exportCsvFromDb(MainActivity.this);

                // 네비게이션 종료 시 호출
                buttonLayout.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
                changeViewModeButton.setVisibility(View.GONE);

                btnGoToWeb.setVisibility(View.VISIBLE); // ◀◀◀ [추가] 운행 종료 시 버튼 보이기

                Log.e(TAG, "onStopNavigation");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onStopNavigation", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public boolean onTryToStopNavigation() {
                /**
                 * 경로 종료를 별도의 UI로 처리할 경우
                 * false 반환 시 SDK의 종료 버튼을 선택해도 종료 동작을 수행하지 않음
                 * 별도의 ui로 navigation 종료
                 * MapSetting setShowClosedPopup(true) 설정 필요
                 */
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String message = "경로 안내를 종료하시겠습니까?";

                builder.setMessage(message)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                navigationFragment.stopDrive();
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return false;
            }

            @Override
            public void onRouteChanged(int i) {
                // 경로 변경 완료 시 호출
                Log.e(TAG, "onRouteChanged " + i);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onRouteChanged", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onRouteOptionChanged(@NonNull RoutePlanType routePlanType, @NonNull RoutePlanType routePlanType1) {
                // 경로 옵션 변경 시 호출
                Log.e(TAG, "onRouteOptionChanged");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onRouteOptionChanged", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onPermissionDenied(int i, @Nullable String s) {
                // 권한 에러 발생 시 호출
                Log.e(TAG, "onPermissionDenied " + i + "::" + s);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onPermissionDenied", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPeriodicRerouteComplete() {
                // 정주기 재탐색 동작 완료 시 호출
                Log.e(TAG, "onPeriodicRerouteComplete");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onPeriodicRerouteComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPeriodicReroute() {
                // 정주기 재탐색 발생 시점에 호출
                Log.e(TAG, "onPeriodicReroute");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onPeriodicReroute", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPassedViaPoint() {
                // 경유지 통과 시 호출
                Log.e(TAG, "onPassedViaPoint");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onPassedViaPoint", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPassedTollgate(int i) {
                // 톨게이트 통과 시 호출
                // i 요금

                Log.e(TAG, "onPassedTollgate " + i);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onPassedTollgate", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onPassedAlternativeRouteJunction() {
                // 대안 경로 통과 시 호출
                Log.e(TAG, "onPassedAlternativeRouteJunction");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onPassedAlternativeRouteJunction", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onNoLocationSignal(boolean b) {
                // GPS 상태 변화 시점에 호출
                Log.e(TAG, "onPassedAlternativeRouteJunction :: " + b);

                runOnUiThread(() -> Toast.makeText(MainActivity.this, "onNoLocationSignal " + b, Toast.LENGTH_SHORT).show());


            }

            @Override
            public void onLocationChanged() {
                //위치 갱신 때 마다 호출
                Log.e(TAG, "onLocationChanged");
            }

            @Override
            public void onFailRouteRequest(@NonNull String errorCode, @NonNull String errorMsg) {
                //경로 탐색 실패 시 호출
                Log.e(TAG, "onFailRouteRequest " + errorCode + "::" + errorMsg);

                runOnUiThread(() -> Toast.makeText(MainActivity.this, "onFailRouteRequest", Toast.LENGTH_SHORT).show());

            }

            @Override
            public void onDoNotRerouteToDestinationComplete() {
                //미리 종료 안내 동작 탐색 완료 시점에 호출
                Log.e(TAG, "onDoNotRerouteToDestinationComplete");
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "onDoNotRerouteToDestinationComplete", Toast.LENGTH_SHORT).show());


            }

            @Override
            public void onDestinationDirResearchComplete() {
                // 건너편 안내 동작 탐색 완료 시점에 호출
                Log.e(TAG, "onDestinationDirResearchComplete");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onDestinationDirResearchComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onChangeRouteOptionComplete(@NonNull RoutePlanType routePlanType) {
                // 경로 옵션 변경 완료 시 호출
                Log.e(TAG, "onChangeRouteOptionComplete");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onChangeRouteOptionComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onBreakawayFromRouteEvent() {
                // 경로 이탈 재탐색 발생 시점에 호출
                Log.e(TAG, "onBreakawayFromRouteEvent");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onBreakawayFromRouteEvent", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onBreakAwayRequestComplete() {
                //경로 이탈 재탐색 동작 완료 시점에 호출
                Log.e(TAG, "onBreakAwayRequestComplete");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onBreakAwayRequestComplete", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onArrivedDestination(@NonNull String dest, int drivingTime, int drivingDistance) {
                // 목적지 도착 시 호출
                // dest 목적지 명
                // drivingTime 운전시간
                // drivingDistance 운전거리

                Log.e(TAG, "onArrivedDestination");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onArrivedDestination", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onApproachingViaPoint() {
                // 경유지 접근 시점에 호출 (1km 이내)
                Log.e(TAG, "onApproachingViaPoint");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onApproachingViaPoint", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onApproachingAlternativeRoute() {
                // 대안 경로 접근 시 호출
                Log.e(TAG, "onApproachingAlternativeRoute");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onApproachingAlternativeRoute", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onForceReroute(@NonNull com.skt.tmap.engine.navigation.network.ndds.NddsDataType.DestSearchFlag destSearchFlag) {
                // 경로 재탐색 발생 시점에 호출
                Log.e(TAG, "onForceReroute");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "onForceReroute", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        // 화면 상태 수신
        navigationFragment.setNavigationScreenStateListener(new NavigationScreenStateListener() {
            @Override
            public void onChanged(@NonNull NavigationScreenState navigationScreenState) {
                if (navigationScreenState instanceof NavigationScreenState.DefaultScreen) {
                    Log.d(TAG, "기본 화면");
                } else if (navigationScreenState instanceof NavigationScreenState.SelectRouteScreen) {
                    Log.d(TAG, "경로 선택 화면");
                } else if (navigationScreenState instanceof NavigationScreenState.AndoScreen) {
                    Log.d(TAG, "안심 주행 화면");
                } else if (navigationScreenState instanceof NavigationScreenState.NavigationScreen) {
                    Log.d(TAG, "길 안내 화면");
                } else if (navigationScreenState instanceof NavigationScreenState.SimulationScreen) {
                    Log.d(TAG, "모의 주행 화면");
                } else if (navigationScreenState instanceof NavigationScreenState.RouteSummaryScreen) {
                    Log.d(TAG, "전체 경로 요약 화면");
                } else {
                    Log.d(TAG, "알 수 없는 상태");
                }
            }
        });

    }

    private final View.OnClickListener onClickButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v.equals(button1)) {
                clickButton1();
            } else if (v.equals(button2)) {
                clickButton2();
            } else if (v.equals(button3)) {
                clickButton3();
            } else if (v.equals(button4)) {
                clickButton4();
            } else if (v.equals(button5)) {
                clickButton5();
            } else if (v.equals(button6)) {
                clickButton6();
            } else if (v.equals(button7)) {
                clickButton7();
            } else if (v.equals(button8)) {
                clickButton8();
            } else if (v.equals(button9)) {
                clickButton9();
            } else if (v.equals(button10)) {
                clickButton10();
            } else if (v.equals(stopButton)) {
                navigationFragment.stopDrive();
            } else if (v.equals(changeViewModeButton)) {
                switch(VIEW_MODE) {
                    case 0:
                        navigationFragment.applyMapViewMode(MapViewMode.DEFAULT); // 기본
                        break;
                    case 1:
                        navigationFragment.applyMapViewMode(MapViewMode.NORTH_BOUND); // 정북 고정
                        break;
                    case 2:
                        navigationFragment.applyMapViewMode(MapViewMode.HEAD_UP_2D); // 2D
                        break;
                    default:
                        navigationFragment.applyMapViewMode(MapViewMode.BIRD_VIEW_3D); // 3D
                        break;
                }
                Log.d(TAG, navigationFragment.getCurrentMapViewMode().name());
                Log.d(TAG, "view mode: " + VIEW_MODE);
                if (VIEW_MODE != 3){
                    VIEW_MODE++;
                }



                else {
                    VIEW_MODE = 0;
                }
            }
            // 수정 추가
            else if (v.equals(btnGoToWeb)) {
                finish(); // 현재 Activity 종료하고 웹으로 돌아가기
            }
        }
    };

    /**
     * 안전운행
     */
    private void clickButton1() {
        buttonLayout.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);

        btnGoToWeb.setVisibility(View.VISIBLE); // ◀◀◀ [추가] 운행 시작 시 버튼 숨기기

        navigationFragment.startSafeDrive();
    }

    /**
     * 경로안내
     */
    private void clickButton2() {

        buttonLayout.setVisibility(View.GONE);
        changeViewModeButton.setVisibility(View.VISIBLE);

        btnGoToWeb.setVisibility(View.GONE); // ◀◀◀ [추가] 운행 시작 시 버튼 숨기기

        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.PremiumGasoline);
        carOption.setHipassOn(true);

        //현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());


        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        //목적지
        WayPoint endPoint = new WayPoint("강남역", new MapPoint(127.027813, 37.497999));

        navigationFragment.setCarOption(carOption);

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Free);

        // 경로안내
        navigationFragment.requestRoute(startPoint, null, endPoint, false, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
                stopButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(MainActivity.this, i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);

//        // Custom UI를 위한 경로 안내
//        navigationFragment.requestRoutesForJava(startPoint, null, endPoint, planTypeList, new RouteRequestCallback() {
//            @Override
//            public void onSuccess(@NonNull RouteResult routeResult) {
//                // Custom UI 모의 주행 요청
//                navigationFragment.startDrivingForJava(0, routeResult, true, new DrivingStartCallback() {
//                    @Override
//                    public void onSuccess() {
//                        Log.d(TAG, "모의 주행 시작");
//                    }
//                    @Override
//                    public void onError(@NotNull Throwable throwable) {
//                        Log.e(TAG, "모의 주행 실패");
//                    }
//                });
//                // Custom UI 경로 안내 요청
//                navigationFragment.startDrivingForJava(0, routeResult, false, new DrivingStartCallback() {
//                    @Override
//                    public void onSuccess() {
//                        Log.d(TAG, "모의 주행 시작");
//                    }
//                    @Override
//                    public void onError(@NotNull Throwable throwable) {
//                        Log.e(TAG, "모의 주행 실패");
//                    }
//                });
//            }
//
//            @Override
//            public void onError(@NonNull Throwable throwable) {
//                Log.e(TAG, "onError: " + throwable.getMessage());
//            }
//        });

    }

    /**
     * 경유지 추가 경로안내
     */
    private void clickButton3() {
        buttonLayout.setVisibility(View.GONE);

        btnGoToWeb.setVisibility(View.GONE); // ◀◀◀ [추가] 운행 시작 시 버튼 숨기기

        //자동차 옵션 설정
        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.Gasoline);
        carOption.setHipassOn(true);

        //현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());
        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        //경유지 설정
        ArrayList<WayPoint> passList = new ArrayList<>();

        WayPoint pass1 = new WayPoint("가", new MapPoint(126.9628, 37.5382));
        passList.add(pass1);

        WayPoint pass2 = new WayPoint("나", new MapPoint(126.9385, 37.5199));
        passList.add(pass2);

        WayPoint pass3 = new WayPoint("다", new MapPoint(126.9620, 37.5223));
        passList.add(pass3);

        //목적지
        WayPoint endPoint = new WayPoint("강남역", new MapPoint(127.027813, 37.497999), "280181", (byte) 5);


        navigationFragment.setCarOption(carOption);

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Free);

        navigationFragment.requestRoute(startPoint, passList, endPoint, false, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
                stopButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(MainActivity.this, i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);
    }

    private void clickButton4() {
        buttonLayout.setVisibility(View.GONE);

        btnGoToWeb.setVisibility(View.GONE); // ◀◀◀ [추가] 운행 시작 시 버튼 숨기기

        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.PremiumGasoline);
        carOption.setHipassOn(true);

        //트럭 경로 요청하기 위한 추가 정보
        HashMap<String, String> truckDetailInfo = new HashMap<>();
        truckDetailInfo.put(TruckInfoKey.TruckType.getValue(), TruckType.Truck.toString());

        truckDetailInfo.put(TruckInfoKey.TruckWeight.getValue(), "2000.0"); // 단위 kg 화물의 무게
        truckDetailInfo.put(TruckInfoKey.TruckHeight.getValue(), "400.0"); // 단위 cm 화물차 높이

        carOption.setTruckInfo(truckDetailInfo);

        //현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());

        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        //목적지
        WayPoint endPoint = new WayPoint("강남역", new MapPoint(127.027813, 37.497999), "280181", (byte) 5);
        endPoint.setCenterPoint(127.028113, 37.5); //중심점 좌표 설정

        navigationFragment.setCarOption(carOption);

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Highway);

        //길안내 바로 시작
        navigationFragment.requestRoute(startPoint, null, endPoint, true, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
                stopButton.setVisibility(View.VISIBLE);
            }


            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(MainActivity.this, i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);
    }

    /**
     * 즐겨찾기 경로 안내
     */
    private void clickButton5() {
        buttonLayout.setVisibility(View.GONE);

        btnGoToWeb.setVisibility(View.GONE); // ◀◀◀ [추가] 운행 시작 시 버튼 숨기기

        //자동차 옵션 설정
        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.Gasoline);
        carOption.setHipassOn(true);

        //현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());
        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        //목적지
        WayPoint endPoint = new WayPoint("", new MapPoint(126.78933292570062, 37.63989758960467));

        navigationFragment.setCarOption(carOption);

        // 기존에 탐색했던 경로 데이터
        String preferredRouteInfo = "45716560,13522554:260,25:-1,44:267,68:15,-110:43,-238:-266,-45:-89,-18:-51,-11:-107,-24:-275,-11:-27,81:-9,194:-10,274:-72,28:-368,-15:-200,-7:-35,-1:-67,-3:-401,-1:-129,-1:-197,2:-50,-3:-238,-15:-67,-3:-174,-9:-202,2:-330,-1:-19,0:-321,2:-119,3:-108,2:-50,-82:-94,-199:-68,-106:-309,-12:-104,-1:-96,-12:-47,-184:-137,-107:-594,-226:-293,-105:-192,-62:-173,-39:-190,-40:-142,-29:-108,-53:-2018,-605:-101,-52:-354,-218:-139,-79:-324,-170:-40,-13:-14,-3:-225,-19:-380,-17:-328,-259:-205,-161:-271,-209:-17,-13:-105,-86:-21,-17:-129,-101:-74,-59:-306,-274:-228,-442:-23,-47:-22,-43:-22,-44:-201,-415:-129,-256:-27,-54:-103,-207:-194,-392:-27,-52:-66,-134:-92,-203:-72,-133:-26,-54:-19,-43:-72,-154:-182,-311:-185,-256:-42,-56:-106,-138:-319,-430:-69,-92:-258,-363:-137,-187:-83,-122:-182,-207:-126,-102:-152,-110:-254,-184:-35,-23:-466,-335:-229,-169:-136,-99:-191,-141:-122,-84:-57,-39:-36,-25:-15,-11:-118,-61:-835,-173:-339,144:-65,45:-1144,1009:-85,40:-2682,568:-602,244:-867,255:-94,9:-387,0:-298,-41:-514,-151:-1680,-483:-445,79:-2157,892:-686,484:-109,84:-1000,667:-523,351:-555,412:-176,132:-316,235:-198,143:-1191,881:-1584,1460:-175,160:-1076,689:-655,379:-286,163:-885,497:-670,387:-658,377:-257,149:-1011,584:-1420,823:-117,68:-60,35:-714,415:-888,517:-1290,744:-1552,905:-159,96:-594,329:-1706,975:-633,356:-451,260:-583,336:-454,263:-479,274:-796,460:-255,155:-1766,1009:-883,583:-243,233:-631,795:-92,122:-96,122:-220,285:-457,583:-203,259:-540,694:-2116,1271:-275,73:-632,170:-217,61:-68,16:-208,55:-743,200:-1468,460:-2167,1869:-610,451:-477,313:-848,860:-108,1271:54,122:157,187:640,1274:86,660:6,85:10,176:99,1546:6,103:20,307:3,35:-931,1318:-1267,180:-1300,313:-280,214";

        // 즐겨 찾는 경로 안내
        navigationFragment.requestUserFavoriteRoute(startPoint, null, endPoint, false, preferredRouteInfo, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestUserFavoriteRoute Success");
                stopButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(MainActivity.this, i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, RoutePlanType.Traffic_Highway);

        // Custom UI를 위한 즐겨 찾는 경로 안내
//        navigationFragment.requestRouteWithFavoriteRouteForJava(startPoint, null, endPoint, preferredRouteInfo, new RouteRequestCallback() {
//            @Override
//            public void onSuccess(@NonNull RouteResult routeResult) {
//                Log.e(TAG, "requestUserFavoriteRouteForJava Success");
//                stopButton.setVisibility(View.VISIBLE);
//
//                // Custom UI 모의 주행 요청
//                navigationFragment.startDrivingForJava(0, routeResult, true, new DrivingStartCallback() {
//                    @Override
//                    public void onSuccess() {
//                        Log.d(TAG, "모의 주행 시작");
//                    }
//                    @Override
//                    public void onError(@NotNull Throwable throwable) {
//                        Log.e(TAG, "모의 주행 실패");
//                    }
//                });
//                // Custom UI 경로 안내 요청
//                navigationFragment.startDrivingForJava(0, routeResult, false, new DrivingStartCallback() {
//                    @Override
//                    public void onSuccess() {
//                        Log.d(TAG, "모의 주행 시작");
//                    }
//                    @Override
//                    public void onError(@NotNull Throwable throwable) {
//                        Log.e(TAG, "모의 주행 실패");
//                    }
//                });
//            }
//
//            @Override
//            public void onError(@NonNull Throwable throwable) {
//                Log.e(TAG, "onError: " + throwable.getMessage());
//            }
//        });
    }

    /**
     * 경유지 맥스
     */
    private void clickButton6() {
        buttonLayout.setVisibility(View.GONE);

        btnGoToWeb.setVisibility(View.GONE); // ◀◀◀ [추가] 운행 시작 시 버튼 숨기기

        CarOption carOption = new CarOption();
        carOption.setCarType(TollCarType.Car);
        carOption.setOilType(CarOilType.PremiumGasoline);
        carOption.setHipassOn(true);

        //현재 위치
        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        String currentName = VSMCoordinates.getAddressOffline(currentLocation.getLongitude(), currentLocation.getLatitude());

        WayPoint startPoint = new WayPoint(currentName, new MapPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));

        //목적지
        WayPoint endPoint = new WayPoint("강남역", new MapPoint(127.027813, 37.497999), "280181", (byte) 5);

        navigationFragment.setCarOption(carOption);

        ArrayList<RoutePlanType> planTypeList = new ArrayList<>();
        planTypeList.add(RoutePlanType.Traffic_Recommend);
        planTypeList.add(RoutePlanType.Traffic_Free);

        //길안내 시작
        navigationFragment.requestRoute(startPoint, com.myapplication.TestWayPoint.point, endPoint, true, new TmapUISDK.RouteRequestListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "requestRoute Success");
                stopButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(MainActivity.this, i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }
        }, planTypeList);
    }


    private void clickButton7() {

        String markerID = "TEST";
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.poi);
        VSMMarkerPoint marker = new VSMMarkerPoint(markerID);

        marker.setIcon(MarkerImage.fromBitmap(icon));
        marker.setShowPriority(MapConstant.MarkerRenderingPriority.DEFAULT_PRIORITY);
        marker.setText("TEST");

        //현재 위치 보다 조금 옆에 마커를 찍는다.
        VSMMapPoint position = new VSMMapPoint(SDKManager.getInstance().getCurrentPosition().getLongitude() - 0.0002, SDKManager.getInstance().getCurrentPosition().getLatitude() + 0.0002);
        marker.setPosition(position);

        VSMMarkerManager markerManager = navigationFragment.getMapView().getMarkerManager();
        if (markerManager == null) {
            Log.e(TAG, "마커 매니저 NULL");
            return;
        }

        markerManager.removeMarker(markerID);
        markerManager.addMarker(marker);

        navigationFragment.setHitEventListener(
                new MapEngine.OnHitObjectListener() {
                    @Override
                    public boolean OnHitObjectPOI(String s, int i, VSMMapPoint vsmMapPoint, Bundle bundle) {
                        return false;
                    }

                    @Override
                    public boolean OnHitObjectMarker(VSMMarkerBase vsmMarkerBase, Bundle bundle) {
                        return false;
                    }

                    @Override
                    public boolean OnHitObjectOilInfo(String s, int i, VSMMapPoint vsmMapPoint) {
                        return false;
                    }

                    @Override
                    public boolean OnHitObjectTraffic(String s, int i, String s1, String s2, String s3, VSMMapPoint vsmMapPoint) {
                        return false;
                    }

                    @Override
                    public boolean OnHitObjectCctv(String s, int i, VSMMapPoint vsmMapPoint, Bundle bundle) {
                        return false;
                    }

                    @Override
                    public boolean OnHitObjectAlternativeRoute(String s, VSMMapPoint vsmMapPoint) {
                        return false;
                    }

                    @Override
                    public boolean OnHitObjectRouteFlag(String s, int i, VSMMapPoint vsmMapPoint) {
                        return false;
                    }

                    @Override
                    public boolean OnHitObjectRouteLine(String s, int i, VSMMapPoint vsmMapPoint) {
                        return false;
                    }

                    @Override
                    public boolean OnHitObjectLocationComponent(LocationComponent locationComponent, VSMMapPoint vsmMapPoint) {
                        return false;
                    }

                    @Override
                    public boolean OnHitObjectNone(VSMMapPoint vsmMapPoint) {
                        return false;
                    }
                },

                new MapEngine.OnHitCalloutPopupListener() {
                    @Override
                    public void OnHitCalloutPopupPOI(String s, int i, VSMMapPoint vsmMapPoint, Bundle bundle) {

                    }

                    @Override
                    public void OnHitCalloutPopupMarker(VSMMarkerBase vsmMarkerBase) {

                    }

                    @Override
                    public void OnHitCalloutPopupTraffic(String s, int i, String s1, String s2, String s3, VSMMapPoint vsmMapPoint) {

                    }

                    @Override
                    public void OnHitCalloutPopupCctv(String s, int i, VSMMapPoint vsmMapPoint, Bundle bundle) {

                    }

                    @Override
                    public void OnHitCalloutPopupUserDefine(String s, int i, VSMMapPoint vsmMapPoint) {

                    }
                });
    }


    private void clickButton8() {
        subscribeEDCData();

    }


    private Observer<Bundle> edcListener = new Observer<Bundle>() {
        @Override
        public void onChanged(Bundle bundle) {
            if (bundle != null) {
                Log.e(TAG, bundle.toString());
            }
        }
    };

    private void subscribeEDCData() {
        if (isEDC) {
            isEDC = false;
            TmapUISDK.observableEDCData.removeObserver(edcListener);
            button8.setText("EDC 수신 등록");
        } else {
            isEDC = true;
            TmapUISDK.observableEDCData.observe(this, edcListener);
            button8.setText("EDC 수신 해제");
        }
    }

    private void clickButton9(){
        subscribeRouteData();
    }

    private Observer<ObservableRouteData> routeDataListener = new Observer<ObservableRouteData>() {
        @Override
        public void onChanged(ObservableRouteData data) {
            Log.e(TAG, data.toString());
            int distance = data.getNTotalDist(); // 목적지까지 총 남은거리(m)
            int time = data.getNTotalTime(); // 목적지까지 총 남은 시간(초)
            int toll = data.getTollFare(); // 톨게이트 요금
            int taxi = data.getTaxiFare(); // 택시 요금

            List<RouteDataCoord> coordList = data.getRouteCoordinates(); // 경로 좌표 데이터(위도, 경도)
            if (!coordList.isEmpty()) {
                double lat = coordList.get(0).getLatitude();
                double lon = coordList.get(0).getLongitude();
                Log.e(TAG, "Coord: Lat=" + lat + ", Lon=" + lon);
            }

            List<RouteDataTraffic> trafficInfoList = data.getRouteTrafficInfos(); // 경로 복잡도 정보
            if (!trafficInfoList.isEmpty()) {
                int endIndex = trafficInfoList.get(0).getEndIndexInCoordinate();
                int startIndex = trafficInfoList.get(0).getStartIndexInCoordinate();
                Log.e(TAG, "Traffic: StartIndex=" + startIndex + ", EndIndex=" + endIndex);
            }

            List<RoadInfo> roadInfos = data.getRoadInfos(); // 도로 정보
            if (!roadInfos.isEmpty()) {
                RoadInfo roadInfo = roadInfos.get(0);
                int startVtxIndex = roadInfo.getStartVtxIndex();
                int endVtxIndex = roadInfo.getEndVtxIndex();
                RoadInfoRoadType roadType = roadInfo.getRoadInfoRoadType();
                RoadInfoFacilityCode roadFacilityCode = roadInfo.getRoadInfoFacilityCode();
                int roadLengthInMeter = roadInfo.getRoadLengthInMeter();
                int maxSpeedKmh = roadInfo.getMaxSpeedKmh();

                Log.e(TAG, "RoadInfo: " +
                        "StartVtx=" + startVtxIndex +
                        ", EndVtx=" + endVtxIndex +
                        ", RoadType=" + roadType +
                        ", FacilityCode=" + roadFacilityCode +
                        ", Length=" + roadLengthInMeter +
                        ", MaxSpeed=" + maxSpeedKmh);
            }

            List<RouteDataTurnInfo> turnInfos = data.getTurnInfos(); // 턴 정보 요약
            if (!turnInfos.isEmpty()) {
                RouteDataTurnInfo turnInfo = turnInfos.get(0);
                RouteDataCoord turnPoint = turnInfo.getTurnPoint();
                int turnCode = turnInfo.getTurnCode();
                Log.e(TAG, "TurnInfo: " + turnPoint + ", TurnCode=" + turnCode);
            }

            List<RouteDataTurnInfo> guidePoints = data.getGuidePoints(); // 안내점 턴 정보
            if (!guidePoints.isEmpty()) {
                RouteDataTurnInfo guidePoint = guidePoints.get(0);
                RouteDataCoord turnPoint = guidePoint.getTurnPoint();
                int turnCode = guidePoint.getTurnCode();
                Log.e(TAG, "TurnInfo: " + turnPoint + ", TurnCode=" + turnCode);
            }

            if (!trafficInfoList.isEmpty()) {
                ObservableRouteProgressData.TrafficStatus status = trafficInfoList.get(0).getTrafficStatus();
                Log.e(TAG, "Traffic Status: " + status);
            }
        }
    };

    private void subscribeRouteData() {
        if (isRoute) {
            isRoute = false;
            TmapUISDK.observableRouteData.removeObserver(routeDataListener);
            button9.setText("Route Data 수신 등록");
        } else {
            isRoute = true;
            TmapUISDK.observableRouteData.observe(this,routeDataListener);
            button9.setText("Route Data 수신 해제");
        }
    }

    private void clickButton10(){
        subscribeScreenState();
    }

    private void subscribeScreenState() {

    }

    private void initUISDK() {
        SDKLocationProvider customProvider = new com.myapplication.CustomLocationProvider();
        LocationInitParam locationInitParam = new LocationInitParam(
                Collections.singletonList(customProvider)
        );

        TmapUISDK.Companion.initialize(this, CLIENT_ID, API_KEY, USER_KEY, DEVICE_KEY, new TmapUISDK.InitializeListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "success initialize");

                MapSetting setting = new MapSetting();
                setting.setShowClosedPopup(false); // 종료 안내 팝업 미표출
//                setting.setMapLayerType(MapLayerType.Default); // 기본 Map Style
//                setting.setMapLayerType(MapLayerType.Aerial); // 항공 사진 Map Style
                navigationFragment.setSettings(setting);
//                setTmapSDKRGOption(); // 음성 안내 설정
//                setDrivingUIVisibility(); // 경로안내 화면 레이아웃 on/off
                TmapUISDK.setUsePeriodicReroute(getApplicationContext(), true); // true 입력 시 정주기 재탐색 사용, false 입력시 정주기 재탐색 미사용

                VSMLocationData vsmLocationData =  navigationFragment.getMapView().getLocationManager().getLastLocation();
                Log.d(TAG, String.valueOf(vsmLocationData.getLatitude()));
                Log.d(TAG, String.valueOf(vsmLocationData.getLongitude()));
                Log.d(TAG, String.valueOf(vsmLocationData.getAccuracy()));

            }

            @Override
            public void onFail(int i, @Nullable String s) {
                Toast.makeText(MainActivity.this, i + "::" + s, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFail " + i + " :: " + s);
            }

            @Override
            public void savedRouteInfoExists(@Nullable String dest) {

                Log.e(TAG,"목적지 : " + dest);
                if (dest != null) {
                    showDialogContinueRoute(dest);
                }
            }
        }, locationInitParam);
        // TmapUISDK 초기화 시 위치 정보를 제공하는 CustomLocationProvider를 설정 가능
    }

    private void showDialogContinueRoute(String dest) {
        String message = dest + "(으)로 경로 안내를 이어서 안내 받으시겠습니까?";
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigationFragment.continueDrive(true, new TmapUISDK.RouteRequestListener() {
                            @Override
                            public void onSuccess() {
                                Log.e("MainActivity", "경로 계속 운행 성공");
                                buttonLayout.setVisibility(View.GONE);
                                stopButton.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onFail(int i, @Nullable String s) {
                                Log.e("MainActivity", "경로 계속 운행 실패 " + s);
                            }
                        });
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        navigationFragment.clearContinueDriveInfo();
                    }
                })
                .show();
    }

    // 음성 안내 설정
    private void setTmapSDKRGOption() {
        TmapUISDK.setRouteGuidanceConfig(new RouteGuidanceConfig(
                false,  // sectionSpeedCameraOverSpeed : 구간 과속 단속 초과 속도 경고음 발성 여부 설정
                true,   // signalSpeedCameraOverSpeed : 신호 과속 단속 초과 속도 경고음 발성 여부 설정
                false,  // fixedSpeedCameraOverSpeed : 고정식 과속 단속 초과 속도 경고음 발성 여부 설정
                true,   // mobileSpeedCameraOverSpeed : 이동식 과속 단속 초과 속도 경고음 발성 여부 설정
                false,  // boxSpeedCameraOverSpeed : 박스형 과속 단속 초과 속도 경고음 발성 여부 설정
                true,   // signalViolationEnforced : 신호 위반 단속 음성 안내 여부 설정
                false,  // cutInViolationEnforced : 끼어들기 단속 음성 안내 여부 설정
                true,   // parkingViolationEnforced : 주정차 위반 단속 음성 안내 여부 설정
                false,  // busLaneEnforced : 버스 전용 차로 위반 단속 음성 안내 여부 설정
                true,   // shoulderEnforced : 갓길 단속 음성 안내 여부 설정
                false,  // speedBump : 과속 방지턱 음성 안내 여부 설정
                true,   // schoolZone : 스쿨존 음성 안내 여부 설정
                false,  // accidentZone : 사고 다발 지역 음성 안내 여부 설정
                true,   // sharpCurve : 급커브 구간 음성 안내 여부 설정
                false,  // fogZone : 안개 구간 음성 안내 여부 설정
                true,   // trafficInfoCollection : 교통 정보 수집 구간 음성 안내 여부 설정
                false,  // sleepyShelter : 졸음 쉼터 음성 안내 여부 설정
                true,   // icyRoad : 결빙 위험 구간 음성 안내 여부 설정
                false,  // greenCameraEnforced : 녹색 카메라 음성 안내 여부 설정
                true,   // railroadCrossing : 철길 건널목 음성 안내 여부 설정
                false,  // floodWarning : 홍수, 침수 주의 구간 음성 안내 여부 설정
                true,   // landSlideRisk : 산사태, 낙석 주의 구간 음성 안내 여부 설정
                false,  // pedestrianPriorityRoad : 보행자 우선 도로 음성 안내 여부 설정
                false,  // detailCrossDirName : 방면 교차로 명칭 음성 안내 여부 설정
                true,   // currentRoadName : 현재 도로 명칭 음성 안내 여부 설정
                false,  // realtimeRouteChange : 실시간 경로 변경 음성 안내 여부 설정
                true,   // routeRecalculation : 경로 재탐색 음성 안내 여부 설정
                false,  // serviceArea : 휴게소 안내 음성 안내 여부 설정
                true,   // tollgateGuide : 요금소 음성 안내 여부 설정
                false,  // hiPassGuide : 하이패스 음성 안내 여부 설정
                true    // gpsClockEnabled : 시보 음성 안내 여부 설정
        ));
    }

    // 경로안내 화면 레이아웃 on/off
    private void setDrivingUIVisibility() {
        navigationFragment.setUIVisibleOption(new UIVisibleOption(
                true,  // isTbtViewVisible : Turn 정보를 보여주는 TBT View
                true,  // isBottomInfoBoxVisible : 하단 정보바 전체와 도로명/목적지/행정동 정보 표시 여부
                true,  // isRerouteButtonVisible : 하단 정보바 경로 재탐색 버튼 표시 여부
                true   // isStopButtonVisible : 하단 정보바 경로 안내 종료 버튼 표시 여부
        ));

    }


    @Override
    public void onBackPressed() {
        if (!navigationFragment.onBackKeyPressed()) {
            buttonLayout.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.GONE);
        }
    }

    public void setLocation() {
        dbHelper.deleteAll();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, // 정확도 우선
                1000 // 업데이트 간격 (1초) // 2~5초 권장
        ).setMinUpdateIntervalMillis(500) // 최소 간격 (0.5초)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    dbHelper.insertLocation(location, satelliteCount, location.getProvider());
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        // 위성 수 확인
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        GnssStatus.Callback gnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                satelliteCount = status.getSatelliteCount();
            }
        };
        locationManager.registerGnssStatusCallback(gnssStatusCallback, new Handler(Looper.getMainLooper()));
    }

    public void saveFileToDownloads(Context context, String filename, String content) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        ContentResolver resolver = context.getContentResolver();

        //Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29) 이상일 경우
            collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            // Android 10 미만일 경우
            collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        }


        Uri fileUri = resolver.insert(collection, values);

        try {
            if (fileUri != null) {
                try (OutputStream os = resolver.openOutputStream(fileUri)) {
                    assert os != null;
                    os.write(content.getBytes(StandardCharsets.UTF_8));
                }

                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(fileUri, values, null, null);

                Log.d(TAG, "파일 저장 성공: " + fileUri);
                Toast.makeText(context, "파일이 저장되었습니다: " + filename, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "파일 저장 실패: " + e.getMessage());
        }
    }

    public void exportCsvFromDb(Context context) {
        Cursor cursor = dbHelper.getAllRows();

        String[] headers = {
                "longitude", "latitude", "bearing", "speed", "Time (초)", "위성 수",
                "측위 방법", "HDOP", "맵 매칭 코드", "맵 매칭 경도", "맵 매칭 위도",
                "맵 매칭 각도", "실제 시간", "고도", "수직 각도", "provider",
                "activity type", "time (ms)"
        };

        StringBuilder csv = new StringBuilder();
        csv.append(TextUtils.join(",", headers)).append("\n");

        while (cursor.moveToNext()) {
            List<String> row = new ArrayList<>();
            for (String col : headers) {
                String dbCol = columnNameToDbField(col);
                row.add(cursor.getString(cursor.getColumnIndexOrThrow(dbCol)));
            }
            csv.append(TextUtils.join(",", row)).append("\n");
        }
        cursor.close();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formatted = now.format(formatter);
        saveFileToDownloads(context, "location_log_" + formatted  + ".csv", csv.toString());
    }

    // 헤더명과 DB 컬럼명을 매핑
    private String columnNameToDbField(String header) {
        switch (header) {
            case "Time (초)": return "time_sec";
            case "HDOP": return "accuracy";
            case "고도": return "altitude";
            case "수직 각도": return "vertical_angle";
            case "activity type": return "activity_type";
            case "time (ms)": return "time_ms";
            case "위성 수": return "satellite_count";
            case "맵 매칭 경도": return "map_match_lng";
            case "맵 매칭 위도": return "map_match_lat";
            case "맵 매칭 각도": return "map_match_bearing";
            case "맵 매칭 코드": return "map_match_code";
            case "실제 시간": return "real_time";
            case "측위 방법": return "method";
            default: return header;  // 나머지는 동일
        }
    }
}