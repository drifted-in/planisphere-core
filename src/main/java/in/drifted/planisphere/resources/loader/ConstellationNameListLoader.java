package in.drifted.planisphere.resources.loader;

import in.drifted.planisphere.model.ConstellationName;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class ConstellationNameListLoader implements Serializable {

    private List<ConstellationName> constellationNameList;

    public ConstellationNameListLoader(String filePath) throws IOException {

        constellationNameList = new ArrayList();

        InputStream inputStream = ConstellationNameListLoader.class.getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

        try {
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                if (!strLine.isEmpty()) {
                    String[] values = strLine.split(",");
                    Point2D coord = new Point2D.Double();
                    coord.setLocation(Double.parseDouble(values[0]) / 1000.0, Double.parseDouble(values[1]) / 100.0);
                    constellationNameList.add(new ConstellationName(values[2], values[3], coord));
                }
            }
        } finally {
            reader.close();
        }
        inputStream.close();
    }

    public List<ConstellationName> getConstellationNameList() {
        return this.constellationNameList;
    }
}
