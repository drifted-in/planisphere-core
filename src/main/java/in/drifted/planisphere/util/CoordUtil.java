package in.drifted.planisphere.util;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public final class CoordUtil implements Serializable {

    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("###.##", new DecimalFormatSymbols(Locale.ENGLISH));

    public static final Boolean convert(Double RA, Double Dec, Point2D result, Double latitude, Double scale) {
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

    public static final void convertWithoutCheck(Double RA, Double Dec, Point2D result, Double latitude, Double scale) {
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

    public static final String format(Double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static final String getCoordsChunk(Point2D coord) {
        return format(Double.valueOf(coord.getX())) + " " + format(Double.valueOf(coord.getY()));
    }
}
