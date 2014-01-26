package in.drifted.planisphere.util;

import in.drifted.planisphere.model.Coord;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public final class CoordUtil {
    
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("###.##", new DecimalFormatSymbols(Locale.ENGLISH));
    
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
    
    public static final String format(Double number) {
        return NUMBER_FORMAT.format(number);
    }
    
    public static final String getCoordsChunk(Coord coord) {
        return format(coord.getX()) + " " + format(coord.getY());
    }
    
    public static final Double getDistance(Coord coord1, Coord coord2) {
        return Math.sqrt(Math.pow(coord1.getX() - coord2.getX(), 2.0) + Math.pow(coord1.getY() - coord2.getY(), 2.0));
    }
}
