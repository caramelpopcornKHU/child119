package com.lx.child

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class NaviActivity : AppCompatActivity(), KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate, KNGuidance_RouteGuideDelegate, KNGuidance_SafetyGuideDelegate, KNGuidance_VoiceGuideDelegate,
KNGuidance_CitsGuideDelegate{

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


        // status bar 영역까지 사용하기 위한 옵션
        window?.apply {
            statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        //requestRoute()
        startNavigation()
    }

    /**
     * 주행 경로를 요청합니다.
     */
    fun requestRoute() {
        Thread {
            // 출발지와 목적지를 설정합니다.
            val startPoi = KNPOI("현위치", 309840, 552483, "현위치")
            val goalPoi = KNPOI("목적지",321497,532896,"목적지")

            KNApplication.knsdk.makeTripWithStart(
                aStart = startPoi,
                aGoal = goalPoi,
                aVias = null
            ) { aError, aTrip ->

                // 경로 요청이 성공하면 aError는 Null이 됩니다.
                if (aError == null) {

                }
            }
        }.start()
    }


    /**
     * 주행 경로를 요청하고 길 안내를 시작합니다.
     * (기존 requestRoute와 startGuide 함수를 통합했습니다.)
     */
    fun startNavigation() {
        Thread {
            // 출발지와 목적지를 설정합니다.
            val startPoi = KNPOI("현위치", 309840, 552483, "현위치")
            //val goalPoi = KNPOI("목적지", 321497, 532896, "목적지")

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

    fun startGuide(trip: KNTrip?) {
        KNApplication.knsdk.sharedGuidance()?.apply {
            // guidance delegate 등록
            guideStateDelegate = this@NaviActivity
            locationGuideDelegate = this@NaviActivity
            routeGuideDelegate = this@NaviActivity
            safetyGuideDelegate = this@NaviActivity
            voiceGuideDelegate = this@NaviActivity
            citsGuideDelegate = this@NaviActivity

            naviView.initWithGuidance(
                this,
                trip,
                KNRoutePriority.KNRoutePriority_Recommand,
                0
            )
        }
    }


}
