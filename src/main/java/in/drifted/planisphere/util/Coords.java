package in.drifted.planisphere.util;

import java.awt.geom.Point2D;
import java.text.NumberFormat;
import in.drifted.planisphere.Settings;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class Coords implements Serializable {
    
    private static NumberFormat numberFormat = new DecimalFormat("###.##", new DecimalFormatSymbols(Locale.ENGLISH));

    public static Boolean convert(Double RA, Double Dec, Point2D result) {
        Double RAInRads = RA * Math.PI / 12.0;
        Double radius = 0.0;

        if (Settings.latitude > 0.0D) {
            if (Dec < Settings.latitude - 90.0D) {
                return false;
            }
            radius = Settings.scale * 0.89D * (90.0D - Dec) / (180.0D - Settings.latitude);
            result.setLocation(Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        } else {
            if (Dec > Settings.latitude + 90.0D) {
                return false;
            }
            radius = Settings.scale * 0.89D * (90.0D + Dec) / (180.0D + Settings.latitude);
            result.setLocation(-Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        }

        return true;
    }

    public static void convertWithoutCheck(Double RA, Double Dec, Point2D result) {
        Double RAInRads = RA * Math.PI / 12.0;
        Double radius = 0.0;

        if (Settings.latitude > 0.0D) {
            radius = Settings.scale * 0.89D * (90.0D - Dec) / (180.0D - Settings.latitude);
            result.setLocation(Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        } else {
            radius = Settings.scale * 0.89D * (90.0D + Dec) / (180.0D + Settings.latitude);
            result.setLocation(-Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        }
    }

    public static String format(Double number) {
        return numberFormat.format(number);
    }

    public static String getCoordsChunk(Point2D coord) {
        return format(Double.valueOf(coord.getX())) + " " + format(Double.valueOf(coord.getY()));
    }
}
