let vmap = null;
let vectorLayer = null;
let vectorSource = null;

// 지도 초기화
export function initVworldMap({
  containerId = "vmap",
  defaultCenter,
  defaultZoom = 14,
  autoFetch = true,
} = {}) {
  if (!window.vw || !window.vw.ol3) {
    console.error("VWorld ol3 라이브러리가 로드되지 않았습니다.");
    return null;
  }

  //  if (vmap) {
  //    console.warn('vmap이 이미 초기화되어 있습니다.')
  //    return vmap
  //  }

  try {
    vw.ol3.MapOptions = {
      basemapType: vw.ol3.BasemapType.GRAPHIC,
      controlDensity: vw.ol3.DensityType.EMPTY,
      interactionDensity: vw.ol3.DensityType.BASIC,
      controlsAutoArrange: true,
      homePosition: vw.ol3.CameraPosition.center,
      initPosition: vw.ol3.CameraPosition.zoom,
    };

    vmap = new vw.ol3.Map(containerId, vw.ol3.MapOptions);

    // 벡터 레이어 준비
    vectorSource = new ol.source.Vector({});
    vectorLayer = new ol.layer.Vector({ source: vectorSource });

    const internalMap = vmap.getMap
      ? vmap.getMap()
      : vmap.map
      ? vmap.map
      : null;
    if (internalMap && internalMap.addLayer) {
      internalMap.addLayer(vectorLayer);
    } else if (vmap.addLayer) {
      vmap.addLayer(vectorLayer);
    }

    // 초기 중심 지정
    if (Array.isArray(defaultCenter) && defaultCenter.length === 2) {
      const view = vmap.getView();
      if (view) {
        view.setCenter(defaultCenter);
        view.setZoom(defaultZoom);
      }
    }

    console.log("✅ vmap 초기화 완료");
    return vmap;
  } catch (e) {
    console.error("vmap 초기화 중 오류:", e);
    vmap = null;
    return null;
  }
}

// 기존 marker 삭제
export function clearMarkers() {
  if (vectorSource) vectorSource.clear();
}

// Homeview marker 생성
export function addMarkerEmergency(lon, lat, props = {}) {
  if (!vectorSource) {
    console.warn("vectorSource가 초기화되지 않았습니다.");
    return null;
  }

  try {
    const point4326 = [Number(lon), Number(lat)];
    const p3857 = ol.proj.transform(point4326, "EPSG:4326", "EPSG:3857");
    const feature = new ol.Feature({
      geometry: new ol.geom.Point(p3857),
      ...props,
    });
    
    const svgUrl = createMarkerIcon(
      props.bedAvail,
      props.bedTotal,
      props.name,
      props.message,
      false
    )

    const style = new ol.style.Style({
      image: new ol.style.Icon({
        src: svgUrl,
        anchor: [0.5, 1],
        scale: 1,
      }),
    });

    feature.setStyle(style);
    vectorSource.addFeature(feature);
    return feature;
    
  } catch (e) {
    console.error("마커 추가 실패", e);
    return null;
  }
}

// Homeview marker click 함수
export function setupMarkerClick(onMarkerClickCallback) {
  if(!vmap) {
    console.warn("VMap이 초기화 되지 않아 클릭 리스너 설정 불가")
    return;
  }

  vmap.on('click', function (evt) {
    const feature = vmap.forEachFeatureAtPixel(evt.pixel, function (feature) {
      return feature;
    })

    if(feature) {
      const featureId = feature.get('id')
      if(featureId && onMarkerClickCallback) {
        onMarkerClickCallback(featureId)
      }
    }
  })
}

// svg url 만들기
const createMarkerIcon = (bedAvail, bedTotal, hospitalName, emergencyMessage, isHover = false) => {

    
    // ✅ 인자 안전성 강화 (undefined/null을 빈 문자열로 변환)
    const name = hospitalName || '';
    const message = emergencyMessage || '';

    // 이미지 기반으로 측정한 SVG 전체 영역 크기
    const svgWidth = 100;
    const svgHeight = 100; 

    // 흰색 둥근 직사각형 배경 설정
    const rectWidth = 100;
    const rectHeight = 90; 
    const rectX = (svgWidth - rectWidth) / 2; 
    const rectY = 0;
    const borderRadius = 10;

    // 원형 배지 설정
    const cirRadius = rectHeight/2*0.6 ; 
    const cirCenterX = svgWidth / 2;
    const cirCenterY = rectY + cirRadius + rectHeight/2*0.1; // 원형 배지 Y 중심

    // 메세지 텍스트 설정
    const messageFontSize = 16;
    const messageY = rectY + rectHeight - 20; 

    // ✅ 응급실 메시지 뱃지/텍스트 설정
    const badgeHeight = 24; // 이미지에 맞게 높이 증가 (20 -> 24)
    const badgeWidth = 10; // 뱃지 고정 너비 (텍스트에 맞게 조정)
    const badgeX = (svgWidth - badgeWidth) / 2; // 중앙 정렬

    // 💡 메시지 뱃지 Y 좌표 (원형 배지 아래에 밀착)
    const messageCenterY = cirCenterY + cirRadius + 20; // 원형 배지 바닥 + 20px 간격으로 분리
    const badgeBgY = messageCenterY - (badgeHeight / 2); // 뱃지 배경의 시작 Y


    // 원형 배지 내부 폰트 및 아이콘 크기
    const bedTextFontSize = 18; 
    const iconSizePx = 24;      
    const iconBaseSize = 24;    
    const iconScale = iconSizePx / iconBaseSize; 

    // 원형 배지 내부 간격 및 중앙 정렬 계산
    const gap = 2; 
    const iconRenderedHeight = iconBaseSize * iconScale; 
    const textRenderedHeight = bedTextFontSize;          
    const contentTotalHeight = iconRenderedHeight + gap + textRenderedHeight; 
    const iconCenterYOffset = -((contentTotalHeight / 2) - (iconRenderedHeight / 2)); 
    const textCenterYOffset = iconCenterYOffset + iconRenderedHeight + gap; 

    // 병상 정보 및 색상 로직
    let bedText = '';
    let badgeBgColor = '#16a085'; 
    let badgeTextColor = 'white';

    if (Number.isFinite(bedAvail) && Number.isFinite(bedTotal) && bedTotal > 0) {
        bedText = `${bedAvail}/${bedTotal}`;
        if (bedAvail/bedTotal <= 0.5) {
            badgeBgColor = '#e74c3c'; 
        } else {
            badgeBgColor = '#16a085'; 
        }
    } else {
        badgeBgColor = '#95a5a6'; 
        bedText = '?/?';
    }

    const strokeWidth = isHover ? '4' : '3';
    
    // 마커 핀 설정
    const pinSvgPath = "M12 11.5A2.5 2.5 0 0 1 9.5 9A2.5 2.5 0 0 1 12 6.5A2.5 2.5 0 0 1 14.5 9a2.5 2.5 0 0 1-2.5 2.5M12 2a7 7 0 0 0-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 0 0-7-7";
    const pinColor = "#00000"; 
    const pinWidth = 24; 
    const pinHeight = 24; 
    const pinScaleFactor = 1.3; 
    const scaledPinWidth = pinWidth * pinScaleFactor; 
    const scaledPinHeight = pinHeight * pinScaleFactor; 
    const pinY = rectY + rectHeight - 10; 

    // 응급실 메시지 렌더링을 위한 색상과 텍스트 처리
    let messageColor = '#333'; 



    // SVG 문자열 조립
    return 'data:image/svg+xml;utf8,' + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="${svgWidth}" height="${svgHeight + scaledPinHeight - 20}" viewBox="0 0 ${svgWidth} ${svgHeight + scaledPinHeight - 20}">
            

            <rect 
                x="${rectX}" y="${rectY}" 
                width="${rectWidth}" height="${rectHeight}" 
                rx="${borderRadius}" ry="${borderRadius}" 
                fill="white" 
                stroke="#ddd" 
                stroke-width="1.5"/>

            <circle 
                cx="${cirCenterX}" cy="${cirCenterY}" 
                r="${cirRadius - (strokeWidth/2)}" 
                fill="${badgeBgColor}" 
                stroke="white" 
                stroke-width="${strokeWidth}"/>
            
            <g transform="translate(${cirCenterX}, ${cirCenterY})">
                <path transform="translate(${-iconBaseSize / 2 * iconScale}, ${-iconBaseSize / 2 * iconScale}) translate(0, ${iconCenterYOffset})" fill="white"
                    d="M19 7h-8v7H3V5H1v15h2v-3h18v3h2v-9a4 4 0 0 0-4-4M7 13a3 3 0 0 0 3-3a3 3 0 0 0-3-3a3 3 0 0 0-3 3a3 3 0 0 0 3 3"/>
                
                <text x="0" y="${textCenterYOffset}" text-anchor="middle" dominant-baseline="central"
                    fill="${badgeTextColor}" font-size="${bedTextFontSize}" font-weight="500" font-family="pretendard">
                    ${bedText}
                </text>
            </g>
            
            ${message ? `
                <rect 
                    x="${badgeX}" y="${badgeBgY}" 
                    height="${badgeHeight}" 
                    rx="12" ry="12" 
                    />

                <text x="${badgeX}" y="${messageCenterY}" text-anchor="middle" dominant-baseline="central"
                    fill="${messageColor}" font-weight="500" font-family="pretendard">
                    ${message}
                </text>
            ` : ''}

            <text x="${svgWidth / 2}" y="${messageY}" text-anchor="middle" dominant-baseline="central"
                fill="#333" font-size="${messageFontSize}" font-weight="500" font-family="pretendard">
                ${name || ''}
            </text>

            <g transform="translate(${svgWidth / 2 - scaledPinWidth / 2}, ${pinY}) scale(${pinScaleFactor})">
                <path fill="${pinColor}" d="${pinSvgPath}"/>
            </g>

        </svg>
    `);
};

// HospitalInformation marker 생성 
export function addMarkerHospital(lon, lat, props = {}) {
  if (!vectorSource) {
    console.warn("vectorSource가 초기화되지 않았습니다.");
    return null;
  }

  try {
    const point4326 = [Number(lon), Number(lat)];
    const p3857 = ol.proj.transform(point4326, "EPSG:4326", "EPSG:3857");
    const feature = new ol.Feature({
      geometry: new ol.geom.Point(p3857),
      ...props,
    });
    const style = new ol.style.Style({
      image: new ol.style.Icon({
        src: props.iconUrl || "https://cdn-icons-png.flaticon.com/512/854/854878.png",
        scale: props.scale || 0.5,
        anchor: props.anchor || [0.5, 1]
      }),
    });
    feature.setStyle(style);
    vectorSource.addFeature(feature);
    return feature;
  } catch (e) {
    console.error("마커 추가 실패", e);
    return null;
  }
}

// bbox 계산
export function getCurrentBBox4326() {
  if (!vmap) {
    console.warn("vmap이 초기화되지 않았습니다.");
    return null;
  }

  try {
    const view = vmap.getView();
    if (!view) return null;

    const internalMap = vmap.getMap
      ? vmap.getMap()
      : vmap.map
      ? vmap.map
      : null;

    const size = internalMap
      ? internalMap.getSize()
      : [window.innerWidth, window.innerHeight];

    const extent3857 = view.calculateExtent(size);
    const extent4326 = ol.proj.transformExtent(
      extent3857,
      "EPSG:3857",
      "EPSG:4326"
    );
    const [minX, minY, maxX, maxY] = extent4326;

    // bbox NaN 값 확인
    if(isNaN(minX) && isNaN(minY) && isNaN(maxX) && isNaN(maxY)  ) {
      console.warn(`bbox 계산 실패 `)
      return;
    } 

    console.log("📦 현재 bbox:", { minX, minY, maxX, maxY });
    return [minX, minY, maxX, maxY];
  } catch (e) {
    console.error("bbox 계산 실패:", e);
    return null;
  }
}

// 기존에 가진 레이어 삭제
export async function removeRouteLayer(map) {
  const layersToRemove = map.getLayers().getArray().filter(layer =>
    layer.get('name') && layer.get('name').startsWith('tmap_route_layer')
  );

  layersToRemove.forEach(layer => {
    map.removeLayer(layer);
  });

  console.log("✅ 기존 Tmap 경로 레이어 제거 완료.");
}

// 경로 그리기
export async function drawRoute(map, geoJsonData, routeIndex = 1) {
    // 1. GeoJSON 데이터를 파싱합니다. (안전하게 객체로 변환)
    const data = typeof geoJsonData === 'string' ? JSON.parse(geoJsonData) : geoJsonData;
    
    // LineString 지오메트리를 가진 GeoJSON Feature만 수동으로 필터링
    const lineStringGeoJSONs = data.features.filter(feature => 
        feature.geometry && feature.geometry.type === 'LineString'
    );

    // 2. OpenLayers 포맷터 정의 (좌표계 설정 수정)
    const format = new ol.format.GeoJSON({
        // Tmap 데이터가 이미 EPSG:3857이므로, 별도의 변환을 하지 않도록 설정합니다.
        // featureProjection만 지도 좌표계인 'EPSG:3857'로 설정합니다.
        // defaultDataProjection을 설정하지 않거나 'EPSG:3857'로 설정하면 변환을 건너뜁니다.
        featureProjection: 'EPSG:3857'
    });

    const featuresToAdd = [];

    // 3. LineString GeoJSON 객체 각각을 OpenLayers Feature로 변환
    lineStringGeoJSONs.forEach(lineFeatureGeoJSON => {
        try {
            // readFeature를 사용하여 개별 LineString Feature를 변환
            const olFeature = format.readFeature(lineFeatureGeoJSON);
            featuresToAdd.push(olFeature);
        } catch (e) {
            console.error("LineString Feature 변환 실패:", e, lineFeatureGeoJSON);
        }
    });

    // 4. 디버그 로그 및 유효성 검사
    console.log(`[DrawRoute Debug] 원본 LineString GeoJSON 개수: ${lineStringGeoJSONs.length}개`);
    console.log(`[DrawRoute Debug] 최종 OpenLayers Feature 개수: ${featuresToAdd.length}개`);
    
    if (featuresToAdd.length === 0) {
        console.error("경로를 그릴 수 있는 유효한 OpenLayers Feature가 없습니다. 데이터 구조를 확인하세요.");
        return;
    }

    // 5. 경로를 담을 Vector Source 생성
    const vectorSource = new ol.source.Vector({
        features: featuresToAdd // LineString만 담긴 배열 사용
    });

    // 6. 경로 스타일 정의 (보이는 문제 해결을 위해 불투명하고 두꺼운 스타일 유지)
    const routeStyle = new ol.style.Style({
        stroke: new ol.style.Stroke({
            // 경로 순번에 따라 색상 또는 투명도를 조절할 수 있습니다.
            color: 'rgba(0, 0, 255, 1)', // 불투명 파란색
            width: 6,
        }),
    });

    // 7. Vector Layer 생성 및 지도에 추가
    const vectorLayer = new ol.layer.Vector({
        source: vectorSource,
        style: routeStyle,
        name: `tmap_route_layer_${routeIndex}`
    });

    map.addLayer(vectorLayer);
    
    // 8. 레이어 순서 강제 설정 (다른 레이어 위에 표시되도록 Z-Index 높임)
    vectorLayer.setZIndex(999);
    console.log("✅ 새 Tmap 경로 레이어 추가 완료.");

    

}



export default {
  initVworldMap,
  addMarkerEmergency,
  clearMarkers,
  getCurrentBBox4326,
  drawRoute,
  removeRouteLayer
};
