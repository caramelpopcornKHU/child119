//import { move } from '@/utils/vworldfunction';

// 1. vmap 변수를 모듈 스코프(최상단)에 선언합니다.
let vmap;
export function initVworldMap() {
    // 'vw' 객체가 로드되었는지 확인
    if (window.vw && window.vw.ol3) {
        vw.ol3.MapOptions = {
            basemapType: vw.ol3.BasemapType.GRAPHIC
            , controlDensity: vw.ol3.DensityType.EMPTY
            , interactionDensity: vw.ol3.DensityType.BASIC
            , controlsAutoArrange: true
            , homePosition: vw.ol3.CameraPosition.center
            , initPosition: vw.ol3.CameraPosition.zoom
        };
        vmap = new vw.ol3.Map("vmap", vw.ol3.MapOptions);

        // --- ⬇️ 현재 위치 초기화 로직 ⬇️ ---

        // 1. 기본 위치 (기존에 사용하시던 좌표)
        const defaultCoords = [14129709.590359, 4512313.7639686];
        const defaultZoom = 15;

        // 2. 브라우저가 Geolocation을 지원하는지 확인
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                // 3. (성공) 위치 정보를 가져왔을 때
                pos => {
                    const lon = pos.coords.longitude; // 경도
                    const lat = pos.coords.latitude;  // 위도

                    try {
                        // 4. (필수!) VWorld 좌표계(EPSG:3857)로 변환
                        const transformedCoords = ol.proj.transform(
                            [lon, lat],    // [경도, 위도]
                            'EPSG:4326', // GPS가 주는 표준 좌표계 (WGS84)
                            'EPSG:3857'  // VWorld가 사용하는 좌표계 (Web Mercator)
                        );

                        // 맵이 렌더링될 시간을 확보한 후 move 호출
                        setTimeout(() => {
                            move(transformedCoords[0], transformedCoords[1], 17); // 현재 위치는 줌 17로 설정
                        }, 100);

                        // (선택) TMap 예제처럼 마커를 추가할 수 있습니다.
                        // addMarker(transformedCoords[0], transformedCoords[1]);

                    } catch (e) {
                        console.error("좌표 변환 오류:", e);
                        // 변환 실패 시 기본 위치로 이동
                        setTimeout(() => {
                            move(defaultCoords[0], defaultCoords[1], defaultZoom);
                        }, 100);
                    }
                },
                // 5. (실패) 위치 정보를 가져오지 못했을 때 (예: 사용자가 권한 거부)
                err => {
                    console.error('Geolocation 오류:', err);
                    alert('현재 위치를 가져올 수 없습니다. 기본 위치에서 시작합니다.');
                    
                    setTimeout(() => {
                        move(defaultCoords[0], defaultCoords[1], defaultZoom);
                    }, 100);
                }
            );
        } else {
            // 6. (지원 안함) 브라우저가 Geolocation을 지원하지 않을 때
            alert('이 브라우저에서는 위치 정보를 지원하지 않습니다. 기본 위치에서 시작합니다.');
            
            setTimeout(() => {
                move(defaultCoords[0], defaultCoords[1], defaultZoom);
            }, 100);
        }

    } else {
        console.error("VWorld(vw) 스크립트가 로드되지 않았습니다.");
    }
}


export function move(x, y, z) {
    if (!vmap) {
        console.error("vmap이 준비되지 않았습니다.");
        return;
    }

    const view = vmap.getView(); // 뷰 객체를 가져옵니다.
    const currentCenter = view.getCenter(); // 현재 중심점
    const currentResolution = view.getResolution(); // 현재 해상도(줌)

    // 1. 2초간의 "이동(Pan)" 애니메이션 설정
    const pan = ol.animation.pan({
        duration: 0,
        source: currentCenter // 현재 위치에서 시작
    });

    // 2. 2초간의 "줌(Zoom)" 애니메이션 설정
    const zoom = ol.animation.zoom({
        duration: 0,
        resolution: currentResolution // 현재 줌에서 시작
    });

    // 3. 지도 렌더링 전에 "이동"과 "줌" 애니메이션을 모두 등록(예약)합니다.
    vmap.beforeRender(pan, zoom);

    // 4. 애니메이션의 "최종 목표" 위치와 줌을 설정합니다.
    // -> 3번에서 애니메이션이 예약되었기 때문에 순간이동하지 않고
    //    2초에 걸쳐 부드럽게 이동 및 줌인/아웃합니다.
    view.setCenter([x, y]);
    view.setZoom(z);
}