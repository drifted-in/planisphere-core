package in.drifted.planisphere.resources.loader;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public final class ConstellationLineListLoader implements Serializable {

    private List<Point2D> constellationLineList;

    public ConstellationLineListLoader(String filePath) throws IOException {

        constellationLineList = new LinkedList<Point2D>();

        InputStream inputStream = ConstellationLineListLoader.class.getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ASCII"));

        try {
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                if (!strLine.isEmpty()) {
                    String[] values = strLine.split(",");
                    for (int i = 0; i < 2; i++) {
                        Point2D coord = new Point2D.Double();
                        coord.setLocation(Double.parseDouble(values[(2 * i)]) / 1000.0, Double.parseDouble(values[(2 * i + 1)]) / 100.0);
                        this.constellationLineList.add(coord);
                    }
                }
            }
        } finally {
            reader.close();
        }
        inputStream.close();
    }

    public List<Point2D> getConstellationLineList() {
        return constellationLineList;
    }
}