package in.drifted.planisphere.resources.loader;

import in.drifted.planisphere.model.ConstellationName;
import in.drifted.planisphere.model.Coord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class ConstellationNameListLoader {

    public static List<ConstellationName> getConstellationNameList(String filePath) throws IOException {

        List<ConstellationName> constellationNameList = new ArrayList<>();

        try (
                InputStream inputStream = ConstellationNameListLoader.class.getResourceAsStream(filePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

            String strLine;
            while ((strLine = reader.readLine()) != null) {
                if (!strLine.isEmpty() && !strLine.startsWith("#")) {
                    String[] values = strLine.split(",");
                    Coord coord = new Coord(Double.parseDouble(values[0]) / 1000.0, Double.parseDouble(values[1]) / 100.0);
                    constellationNameList.add(new ConstellationName(values[2], values[3], coord));
                }
            }
        }

        return constellationNameList;
    }
}
