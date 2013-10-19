package in.drifted.planisphere.util;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public final class CoordUtil implements Serializable {

    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("###.##", new DecimalFormatSymbols(Locale.ENGLISH));

    public static Boolean convert(Double RA, Double Dec, Point2D result, Double latitude, Double scale) {
        Double RAInRads = RA * Math.PI / 12.0;
        Double radius;

        if (latitude > 0.0D) {
            if (Dec < latitude - 90.0D) {
                return false;
            }
            radius = scale * 0.89D * (90.0D - Dec) / (180.0D - latitude);
            result.setLocation(Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        } else {
            if (Dec > latitude + 90.0D) {
                return false;
            }
            radius = scale * 0.89D * (90.0D + Dec) / (180.0D + latitude);
            result.setLocation(-Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        }

        return true;
    }

    public static void convertWithoutCheck(Double RA, Double Dec, Point2D result, Double latitude, Double scale) {
        Double RAInRads = RA * Math.PI / 12.0;
        Double radius;

        if (latitude > 0.0D) {
            radius = scale * 0.89D * (90.0D - Dec) / (180.0D - latitude);
            result.setLocation(Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        } else {
            radius = scale * 0.89D * (90.0D + Dec) / (180.0D + latitude);
            result.setLocation(-Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        }
    }

    public static String format(Double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String getCoordsChunk(Point2D coord) {
        return format(Double.valueOf(coord.getX())) + " " + format(Double.valueOf(coord.getY()));
    }
}
