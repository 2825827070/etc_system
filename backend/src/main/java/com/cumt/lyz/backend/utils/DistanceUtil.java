package com.cumt.lyz.backend.utils;

public class DistanceUtil {
    private static final double EARTH_RADIUS = 6371.393;

    public static double getDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) return 0.0;
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        return Math.round(s * EARTH_RADIUS * 1000.0) / 1000.0 * 1000; // 返回米或千米需根据你的业务调整
    }
}