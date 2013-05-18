package in.drifted.planisphere.resources.loader;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class ConstellationBoundaryListLoader {

    public static List<Point2D> getConstellationBoundaryList(String filePath) throws IOException {
        
        List<Point2D> constellationBoundaryList = new ArrayList<>();
        
        try (InputStream inputStream = ConstellationBoundaryListLoader.class.getResourceAsStream(filePath); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ASCII"))) {
            Point2D coordLast = new Point2D.Double();
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                if (!strLine.isEmpty()) {
                    String[] values = strLine.split("\t");
                    Point2D coord = new Point2D.Double();
                    coord.setLocation(Double.parseDouble(values[1]) / 1000.0D, Double.parseDouble(values[2]) / 100.0D);
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