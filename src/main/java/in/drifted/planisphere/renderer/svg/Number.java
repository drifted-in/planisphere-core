package in.drifted.planisphere.renderer.svg;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class Number {

    private static final NumberFormat numberFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.UK));

    public static String format(double number) {
        return numberFormat.format(number);
    }

}
