package in.drifted.planisphere.util;

import in.drifted.planisphere.model.Coord;

public final class CoordUtil {

    public static final Boolean convert(Double RA, Double Dec, Coord result, Double latitude, Double scale) {
        Double RAInRads = RA * Math.PI / 12.0;
        Double radius;

        if (latitude > 0.0) {
            if (Dec < latitude - 90.0) {
                return false;
            }
            radius = scale * 0.89 * (90.0 - Dec) / (180.0 - latitude);
            result.setLocation(Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        } else {
            if (Dec > latitude + 90.0) {
                return false;
            }
            radius = scale * 0.89 * (90.0 + Dec) / (180.0 + latitude);
            result.setLocation(-Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        }

        return true;
    }

    public static final void convertWithoutCheck(Double RA, Double Dec, Coord result, Double latitude, Double scale) {
        Double RAInRads = RA * Math.PI / 12.0;
        Double radius;

        if (latitude > 0.0) {
            radius = scale * 0.89 * (90.0 - Dec) / (180.0 - latitude);
            result.setLocation(Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        } else {
            radius = scale * 0.89 * (90.0 + Dec) / (180.0 + latitude);
            result.setLocation(-Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        }
    }

    public static final Double getDistance(Coord coord1, Coord coord2) {
        return Math.sqrt(Math.pow(coord1.getX() - coord2.getX(), 2.0) + Math.pow(coord1.getY() - coord2.getY(), 2.0));
    }
}
