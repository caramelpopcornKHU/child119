package com.lx.child

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.kakaomobility.knsdk.KNLanguageType


class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var myTextView: TextView // 1. XML의 뷰와 연결할 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        // 1. WebView 초기화
        webView = findViewById(R.id.webView)
        setupWebView()

        // 2. www.child119.com 로드
        webView.loadUrl("https://www.child119.com/hospitalInformationApp")

        // 3. 앱 시작 시 자동으로 권한 확인 및 인증 시작 (기존 onClick에서 이동)
        checkPermission()

        // 4. 뒤로가기 버튼 처리 (WebView용)
        setupOnBackPressed()
    }

    /**
     * WebView의 상세 설정을 담당합니다.
     */
    private fun setupWebView() {
        webView.apply {
            // 1. [수정] WebViewClient 상세 설정
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url.toString()

                    // "tel:", "sms:", "mailto:" 등 웹 URL이 아닌 스킴 처리
                    if (url.startsWith("tel:")) {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                            // 'this@MainActivity'는 현재 Activity Context를 의미합니다.
                            this@MainActivity.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("WebView", "전화 앱 열기 실패", e)
                        }
                        return true // WebView가 로드하지 않음
                    }

                    // "intent:" 스킴 처리 (안드로이드 앱 링크)
                    if (url.startsWith("intent:")) {
                        try {
                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            val existPackage = packageManager.getLaunchIntentForPackage(intent.getPackage()!!)
                            if (existPackage != null) {
                                startActivity(intent)
                            } else {
                                // 마켓으로 이동
                                val marketIntent = Intent(Intent.ACTION_VIEW)
                                marketIntent.data = Uri.parse("market://details?id=" + intent.getPackage()!!)
                                startActivity(marketIntent)
                            }
                            return true
                        } catch (e: Exception) {
                            Log.e("WebView", "인텐트 처리 실패", e)
                        }
                    }

                    // http, https 등 일반 웹 링크는 WebView가 처리
                    return false
                }
            }







            // (NEW) 3. WebChromeClient 설정 (위치 정보 권한 요청 처리)
            webChromeClient = object : WebChromeClient() {
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {
                    // (NEW) 4. 웹페이지의 위치 정보 요청을 즉시 승인
                    callback?.invoke(origin, true, false)
                }
            }

            // 2. WebSettings 설정
            settings.apply {
                javaScriptEnabled = true // (필수) JavaScript 허용
                domStorageEnabled = true   // (권장) 로컬 스토리지 사용
                useWideViewPort = true     // (권장) Viewport 메타 태그 지원
                loadWithOverviewMode = true // (권장) 컨텐츠를 WebView 크기에 맞춤
            }

            // 3. (중요) JavaScript 브릿지 연결
            // "AndroidBridge"라는 이름으로 웹페이지의 JavaScript가 호출할 수 있는 인터페이스를 추가합니다.
            addJavascriptInterface(WebAppInterface(this@MainActivity), "AndroidBridge")
        }
    }

    /**
     * WebView의 뒤로가기 버튼 로직을 설정합니다.
     * (웹 기록이 있으면 웹 뒤로가기, 없으면 앱 종료)
     */
    private fun setupOnBackPressed() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack() // 웹 기록이 있으면 뒤로가기
                } else {
                    // 웹 기록이 없으면, 콜백을 비활성화하고 기본 뒤로가기 실행 (앱 종료)
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    // --- (이하 카카오 SDK 인증 로직은 그대로 사용) ---

    /**
     * GPS 위치 권한을 확인합니다.
     */
    fun checkPermission() {
        // FINE과 COARSE 권한을 모두 확인 (혹은 FINE만 해도 됨)
        val finePermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (finePermission != PackageManager.PERMISSION_GRANTED || coarsePermission != PackageManager.PERMISSION_GRANTED) {
            // GPS 퍼미션 체크
            gpsPermissionCheck()
        } else {
            // 길찾기 SDK 인증
            knsdkAuth()
        }
    }

    /**
     * GPS 위치 권한을 요청합니다.
     */
    fun gpsPermissionCheck() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1234)
    }

    /**
     * GPS 위치 권한 요청의 실패 여부를 확인합니다.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1234 -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {

                    // 다시 권한 요청하는 곳으로 돌아갑니다.
                    checkPermission()
                } else {
                    // (권장) 권한이 거부되었을 때 사용자에게 알림
                    Toast.makeText(this, "내비게이션을 사용하려면 위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    /**
     * 길찾기 SDK 인증을 진행합니다.
     */
    fun knsdkAuth() {
        // 인증 성공 시 NaviActivity로 이동하는 로직은 제거합니다.
        // 웹뷰가 메인이므로, 인증은 백그라운드에서만 수행합니다.
        KNApplication.knsdk.apply {
            initializeWithAppKey(
                aAppKey = "81f5286c3ed43f391809939c991b77a0",
                aClientVersion = "1.0.0",
                aUserKey = "testUser",
                aLangType = KNLanguageType.KNLanguageType_KOREAN,
                aCompletion = {
                    runOnUiThread {
                        if (it != null) {
                            Toast.makeText(applicationContext, "SDK 인증 실패", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "SDK 인증 성공", Toast.LENGTH_SHORT).show()
                            // (참고) 인증 성공 후 자동으로 NaviActivity를 시작했던 코드는 여기서 제거했습니다.
                            // 이제 웹에서의 요청으로만 NaviActivity가 시작됩니다.
                        }
                    }
                })
        }
    }

    /**
     * 웹 페이지의 JavaScript가 호출할 브릿지 클래스
     */
    class WebAppInterface(private val context: Context) {

        /**
         * 웹에서 이 함수를 호출하면 NaviActivity를 시작시킵니다.
         * @param startName 출발지 이름 (예: "내 위치")
         * @param startX 출발지 X좌표 (Katec)
         * @param startY 출발지 Y좌표 (Katec)
         * @param goalName 목적지 이름 (예: "카카오판교오피스")
         * @param goalX 목적지 X좌표 (Katec) (예: 321497)
         * @param goalY 목적지 Y좌표 (Katec) (예: 532896)
         * @param goalAddress 목적지 주소 (예: "경기 성남시 분당구...")
         */
        @JavascriptInterface
        fun startNavigation(
            startName: String, startX: Int, startY: Int,
            goalName: String, goalX: Int, goalY: Int, goalAddress: String,
            featureID: String // <-- [추가] 병원 고유 ID (예: "emergency.A1100001")
             ) {

            // (참고) 현위치는 NaviActivity에서 하드코딩된 값을 사용한다고 가정합니다.
            // val startPoi = KNPOI("현위치", 309840, 552483, "현위치")

            // 웹에서 받은 정보로 KNPOI 객체 생성
            //val goalPoi = KNPOI(goalName, goalX, goalY, goalAddress)

            // NaviActivity로 정보를 전달하며 시작
            val intent = Intent(context, NaviActivity::class.java).apply {
                // (중요) NaviActivity가 받을 수 있도록 POI 객체를 직렬화하여 전달

                // KNPOI가 Serializable 또는 Parcelable이어야 합니다.
                putExtra("START_NAME", startName)
                putExtra("START_X", startX)
                putExtra("START_Y", startY)
                putExtra("GOAL_NAME", goalName)
                putExtra("GOAL_X", goalX)
                putExtra("GOAL_Y", goalY)
                putExtra("GOAL_ADDRESS", goalAddress)

                // [추가] NaviActivity로 featureID 전달
                putExtra("FEATURE_ID", featureID)
            }
            context.startActivity(intent)
        }
    }
}