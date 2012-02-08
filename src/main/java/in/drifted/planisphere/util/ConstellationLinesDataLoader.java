package in.drifted.planisphere.util;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

public class ConstellationLinesDataLoader implements Serializable {

    private ArrayList<Point2D> constellationLines;

    public ConstellationLinesDataLoader(String filePath) throws Exception {
        this.constellationLines = new ArrayList();
        InputStream inputStream = getClass().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String strLine;
        while ((strLine = reader.readLine()) != null) {
            if (!strLine.isEmpty()) {
                String[] values = strLine.split(",");
                for (int i = 0; i < 2; i++) {
                    Point2D coord = new Point2D.Double();
                    coord.setLocation(Double.parseDouble(values[(2 * i)]) / 1000.0D, Double.parseDouble(values[(2 * i + 1)]) / 100.0D);
                    this.constellationLines.add(coord);
                }
            }
        }
        reader.close();
        inputStream.close();
    }

    public ArrayList<Point2D> getConstellationLines() {
        return this.constellationLines;
    }
}