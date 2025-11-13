package com.lx.child

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.kakaomobility.knsdk.KNRouteAvoidOption
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_CitsGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_GuideStateDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_LocationGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_RouteGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_SafetyGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_VoiceGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuideRouteChangeReason
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.common.KNLocation
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// API 응답의 최상위 구조
data class HospitalResponse(
    val features: List<HospitalFeature>
)

// "features" 배열의 각 항목
data class HospitalFeature(
    val id: String,
    val properties: HospitalProperties
)

// 우리가 필요한 실제 병원 정보
data class HospitalProperties(
    val name: String,
    val message: String?,
    @SerializedName("bed_avail") // JSON 키와 변수 이름이 다를 경우
    val bedAvail: String?,
    @SerializedName("bed_total")
    val bedTotal: String?

)

class NaviActivity : AppCompatActivity(), KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate, KNGuidance_RouteGuideDelegate, KNGuidance_SafetyGuideDelegate, KNGuidance_VoiceGuideDelegate,
KNGuidance_CitsGuideDelegate{


    // API 서비스 (싱글톤)
    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.child119.com/")
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)
    }

    private lateinit var tvBedStatus: TextView      // "5/7"을 표시할 (R.id.card_value_1)
    private lateinit var tvHospitalMessage: TextView // "소아진료가능"을 표시할 (R.id.card_value_2)

    override fun didUpdateCitsGuide(
        aGuidance: KNGuidance,
        aCitsGuide: KNGuide_Cits
    ) {
        naviView.didUpdateCitsGuide(aGuidance, aCitsGuide)
    }

    override fun willPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice
    ) {
        naviView.willPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun shouldPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice,
        aNewData: MutableList<ByteArray>
    ): Boolean {
        return naviView.shouldPlayVoiceGuide(aGuidance, aVoiceGuide, aNewData)
    }

    override fun didFinishPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice
    ) {
        naviView.didFinishPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun guidanceDidUpdateSafetyGuide(
        aGuidance: KNGuidance,
        aSafetyGuide: KNGuide_Safety?
    ) {
        naviView.guidanceDidUpdateSafetyGuide(aGuidance, aSafetyGuide)
    }

    override fun guidanceDidUpdateAroundSafeties(
        aGuidance: KNGuidance,
        aSafeties: List<KNSafety>?
    ) {
        naviView.guidanceDidUpdateAroundSafeties(aGuidance, aSafeties)
    }

    override fun guidanceDidUpdateRouteGuide(
        aGuidance: KNGuidance,
        aRouteGuide: KNGuide_Route
    ) {
        naviView.guidanceDidUpdateRouteGuide(aGuidance, aRouteGuide)
    }

    override fun guidanceDidUpdateLocation(
        aGuidance: KNGuidance,
        aLocationGuide: KNGuide_Location
    ) {
        naviView.guidanceDidUpdateLocation(aGuidance, aLocationGuide)
    }

    override fun guidanceCheckingRouteChange(aGuidance: KNGuidance) {
        naviView.guidanceCheckingRouteChange(aGuidance)
    }

    override fun guidanceDidUpdateRoutes(
        aGuidance: KNGuidance,
        aRoutes: List<KNRoute>,
        aMultiRouteInfo: KNMultiRouteInfo?
    ) {
        naviView.guidanceDidUpdateRoutes(aGuidance, aRoutes, aMultiRouteInfo)
    }

    override fun guidanceGuideEnded(aGuidance: KNGuidance) {
        naviView.guidanceGuideEnded(aGuidance)
    }

    override fun guidanceGuideStarted(aGuidance: KNGuidance) {
        naviView.guidanceGuideStarted(aGuidance)
    }

    override fun guidanceOutOfRoute(aGuidance: KNGuidance) {
        naviView.guidanceOutOfRoute(aGuidance)
    }

    override fun guidanceRouteChanged(
        aGuidance: KNGuidance,
        aFromRoute: KNRoute,
        aFromLocation: KNLocation,
        aToRoute: KNRoute,
        aToLocation: KNLocation,
        aChangeReason: KNGuideRouteChangeReason
    ) {
        naviView.guidanceRouteChanged(aGuidance)
    }

    override fun guidanceRouteUnchanged(aGuidance: KNGuidance) {
        naviView.guidanceRouteUnchanged(aGuidance)
    }

    override fun guidanceRouteUnchangedWithError(
        aGuidnace: KNGuidance,
        aError: KNError
    ) {
        naviView.guidanceRouteUnchangedWithError(aGuidnace, aError)
    }



    lateinit var naviView : KNNaviView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navi)

        naviView = findViewById(R.id.navi_view)


        // 1. UI 요소 초기화 (새 ID 사용)
        tvBedStatus = findViewById(R.id.card_value_1)
        tvHospitalMessage = findViewById(R.id.card_value_2)

        // [추가] 2. Intent에서 featureID 받고 API 호출
        val featureID = intent.getStringExtra("FEATURE_ID")
        if (featureID != null) {
            tvBedStatus.text = "-/-"
            tvHospitalMessage.text = "로딩 중..."
            loadHospitalDetails(featureID)
        } else {
            Log.e("NaviActivity", "FeatureID가 전달되지 않았습니다.")
            tvBedStatus.text = "N/A"
            tvHospitalMessage.text = "정보 없음"
        }


        // status bar 영역까지 사용하기 위한 옵션
        window?.apply {
            statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }


        startNavigation()
    }




    // API 호출을 위한 함수
    private fun loadHospitalDetails(featureID: String) {
        lifecycleScope.launch {
            try {
                // API 호출
                val response = apiService.getHospitalDetails(featureID = featureID)

                if (response.features.isNotEmpty()) {
                    val properties = response.features[0].properties

                    // API 응답을 TextView에 매핑
                    val bedAvail = properties.bedAvail ?: "N/A"
                    val bedTotal = properties.bedTotal ?: "N/A"
                    tvBedStatus.text = "$bedAvail/$bedTotal" // ID: card_value_1

                    tvHospitalMessage.text = properties.message ?: "정보 없음" // ID: card_value_2

                } else {
                    tvBedStatus.text = "N/A"
                    tvHospitalMessage.text = "상세 정보 없음"
                }

            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                Log.e("NaviActivity", "API 호출 실패", e)
                tvBedStatus.text = "오류"
                tvHospitalMessage.text = "로드 실패"
            }
        }
    }


    /**
     * 주행 경로를 요청하고 길 안내를 시작합니다.
     */
    fun startNavigation() {
        Thread {
            // 출발지와 목적지를 설정합니다.

            val startName = intent.getStringExtra("START_NAME") ?: "현위치"
            val startX = intent.getIntExtra("START_X", 188836)
            val startY = intent.getIntExtra("START_Y", 443836)

            val startPoi = KNPOI(startName, startX, startY, "현위치")


            // (수정됨) 목적지는 MainActivity의 Intent로부터 받아옵니다.
            val goalName = intent.getStringExtra("GOAL_NAME") ?: "목적지" // 기본값
            val goalX = intent.getIntExtra("GOAL_X", 321497)        // 기본값
            val goalY = intent.getIntExtra("GOAL_Y", 532896)        // 기본값
            val goalAddress = intent.getStringExtra("GOAL_ADDRESS") ?: "목적지" // 기본값

            val goalPoi = KNPOI(goalName, goalX, goalY, goalAddress)

            KNApplication.knsdk.makeTripWithStart(
                aStart = startPoi,
                aGoal = goalPoi,
                aVias = null
            ) { aError, aTrip ->

                // 4. KNTrip 생성 콜백
                if (aError == null && aTrip != null) {
                    // KNTrip 생성 성공
                    // 문서에 따라 경로 옵션 설정
                    val curRoutePriority = KNRoutePriority.KNRoutePriority_Recommand
                    val curAvoidOptions = KNRouteAvoidOption.KNRouteAvoidOption_RoadEvent.value or KNRouteAvoidOption.KNRouteAvoidOption_SZone.value

                    // 5. 실제 경로 요청
                    aTrip.routeWithPriority(curRoutePriority, curAvoidOptions) { error, _ ->
                        if (error != null) {
                            // 경로 요청 실패
                            runOnUiThread {
                                Toast.makeText(this, "경로 요청 실패: ${error.msg}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 6. 경로 요청 성공 -> 길 안내 시작
                            KNApplication.knsdk.sharedGuidance()?.apply {
                                // 각 가이던스 델리게이트 등록
                                guideStateDelegate = this@NaviActivity
                                locationGuideDelegate = this@NaviActivity
                                routeGuideDelegate = this@NaviActivity
                                safetyGuideDelegate = this@NaviActivity
                                voiceGuideDelegate = this@NaviActivity
                                citsGuideDelegate = this@NaviActivity

                                // 7. (중요) UI 작업은 runOnUiThread에서 수행
                                runOnUiThread {
                                    naviView.initWithGuidance(
                                        this,
                                        aTrip,
                                        curRoutePriority,
                                        curAvoidOptions
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // KNTrip 생성 실패
                    runOnUiThread {
                        Toast.makeText(this, "Trip 생성 실패: ${aError?.msg}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    }




}
