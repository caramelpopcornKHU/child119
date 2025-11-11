package com.lx.savechild;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 2. child119 API 응답 JSON의 '틀'입니다.
 * 중첩된 구조(features -> properties / geometry)를 모두 반영합니다.
 */
public class CardDataResponse {

    // "features": [ ... ]
    @SerializedName("features")
    private List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    // --- 중첩된 클래스들 ---

    /**
     * "features" 리스트 안의 개별 항목
     */
    public static class Feature {
        @SerializedName("properties")
        private Properties properties;

        @SerializedName("geometry")
        private Geometry geometry;

        public Properties getProperties() {
            return properties;
        }

        public Geometry getGeometry() {
            return geometry;
        }
    }

    /**
     * "properties" 객체
     */
    public static class Properties {
        @SerializedName("name")
        private String name;

        @SerializedName("message")
        private String message;

        @SerializedName("bed_avail")
        private String bedAvail;

        @SerializedName("bed_total")
        private String bedTotal;

        public String getName() {
            return name;
        }

        public String getMessage() {
            return message;
        }

        public String getBedAvail() {
            return bedAvail;
        }

        public String getBedTotal() {
            return bedTotal;
        }
    }

    /**
     * "geometry" 객체
     */
    public static class Geometry {
        // "coordinates": [ 127.0442, 37.5597 ]
        @SerializedName("coordinates")
        private List<Double> coordinates;

        public List<Double> getCoordinates() {
            return coordinates;
        }
    }
}