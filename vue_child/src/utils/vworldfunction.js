let vmap = null;
let vectorLayer = null;
let vectorSource = null;

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

export function getVMap() {
  return vmap;
}

export function clearMarkers() {
  if (vectorSource) vectorSource.clear();
}

export function addMarker(lon, lat, props = {}) {
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
        src: "https://cdn-icons-png.flaticon.com/512/854/854878.png",
        scale: 0.06,
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

    console.log("📦 현재 bbox:", { minX, minY, maxX, maxY });
    return [minX, minY, maxX, maxY];
  } catch (e) {
    console.error("bbox 계산 실패:", e);
    return null;
  }
}

export function move(x, y, z = 15) {
  if (!vmap) return;
  const view = vmap.getView();
  try {
    view.setCenter([x, y]);
    view.setZoom(z);
  } catch (e) {
    console.error("지도 이동 실패", e);
  }
}

export default {
  initVworldMap,
  getVMap,
  addMarker,
  clearMarkers,
  getCurrentBBox4326,
  move,
};
