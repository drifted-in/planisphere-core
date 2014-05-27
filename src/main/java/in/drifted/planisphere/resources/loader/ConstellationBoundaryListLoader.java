package in.drifted.planisphere.resources.loader;

import in.drifted.planisphere.model.Coord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class ConstellationBoundaryListLoader {

    public static List<Coord> getConstellationBoundaryList(String filePath) throws IOException {

        List<Coord> constellationBoundaryList = new ArrayList<>();

        try (
                InputStream inputStream = ConstellationBoundaryListLoader.class.getResourceAsStream(filePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ASCII"))) {

            Coord coordLast = new Coord();
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                if (!strLine.isEmpty() && !strLine.startsWith("#")) {
                    String[] values = strLine.split("\t");
                    Coord coord = new Coord(Double.parseDouble(values[1]) / 1000.0, Double.parseDouble(values[2]) / 100.0);
                    if (values[0].equals("1")) {
                        constellationBoundaryList.add(coordLast);
                        constellationBoundaryList.add(coord);
                    }
                    coordLast = coord;
                }
            }
        }

        return constellationBoundaryList;
    }
}
