package in.drifted.planisphere.util;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

public class ConstellationNamesDataLoader implements Serializable {

    private ArrayList<ConstellationName> constellationNames;

    public ConstellationNamesDataLoader(String filePath) throws Exception {
        this.constellationNames = new ArrayList();
        InputStream inputStream = getClass().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String strLine;
        while ((strLine = reader.readLine()) != null) {
            if (!strLine.isEmpty()) {
                String[] values = strLine.split(",");
                Point2D coord = new Point2D.Double();
                coord.setLocation(Double.parseDouble(values[0]) / 1000.0D, Double.parseDouble(values[1]) / 100.0D);
                this.constellationNames.add(new ConstellationName(values[2], values[3], coord));
            }
        }
        reader.close();
        inputStream.close();
    }

    public ArrayList<ConstellationName> getConstellationNames() {
        return this.constellationNames;
    }
}
