package in.drifted.planisphere.resources.loader;

import in.drifted.planisphere.model.Coord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public final class ConstellationLineListLoader {

    public static List<Coord> getConstellationLineList(String filePath) throws IOException {

        List<Coord> constellationLineList = new LinkedList<>();

        try (
                InputStream inputStream = ConstellationLineListLoader.class.getResourceAsStream(filePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ASCII"))) {

            String strLine;
            while ((strLine = reader.readLine()) != null) {
                if (!strLine.isEmpty()) {
                    String[] values = strLine.split(",");
                    for (int i = 0; i < 2; i++) {
                        constellationLineList.add(new Coord(Double.parseDouble(values[(2 * i)]) / 1000.0, Double.parseDouble(values[(2 * i + 1)]) / 100.0));
                    }
                }
            }
        }

        return constellationLineList;
    }
}
