package in.drifted.planisphere.resources.loader;

import in.drifted.planisphere.model.Star;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class StarListLoader {

    public static List<Star> getStarList(String filePath) throws IOException {

        List<Star> starList = new ArrayList<>();

        try (InputStream inputStream = StarListLoader.class.getResourceAsStream(filePath); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ASCII"))) {

            String strLine;
            while ((strLine = reader.readLine()) != null) {
                if (!strLine.isEmpty()) {
                    Star star = new Star();
                    String[] values = strLine.split(",");
                    star.setRA(Double.parseDouble(values[0]) / 1000.0);
                    star.setDec(Double.parseDouble(values[1]) / 100.0);
                    star.setMag(Double.parseDouble(values[2]) / 100.0);
                    starList.add(star);
                }
            }
        }

        return starList;
    }
}
