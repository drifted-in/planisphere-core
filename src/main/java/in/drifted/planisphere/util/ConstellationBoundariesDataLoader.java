package in.drifted.planisphere.util;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

public class ConstellationBoundariesDataLoader implements Serializable {

    private ArrayList<Point2D> constellationBoundaries;

    public ConstellationBoundariesDataLoader(String filePath) throws Exception {
        this.constellationBoundaries = new ArrayList();
        InputStream inputStream = getClass().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Point2D coordLast = new Point2D.Double();
        String strLine;
        while ((strLine = reader.readLine()) != null) {
            if (!strLine.isEmpty()) {
                String[] values = strLine.split("\t");
                Point2D coord = new Point2D.Double();
                coord.setLocation(Double.parseDouble(values[1]) / 1000.0D, Double.parseDouble(values[2]) / 100.0D);
                if (values[0].equals("1")) {
                    this.constellationBoundaries.add(coordLast);
                    this.constellationBoundaries.add(coord);
                }
                coordLast = coord;
            }
        }
        reader.close();
        inputStream.close();
    }

    public ArrayList<Point2D> getConstellationBoundaries() {
        return this.constellationBoundaries;
    }
}