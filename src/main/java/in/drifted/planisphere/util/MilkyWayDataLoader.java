package in.drifted.planisphere.util;

import in.drifted.planisphere.model.MilkyWayDataSet;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public final class MilkyWayDataLoader implements Serializable {

    private MilkyWayDataSet milkyWayDataSet;

    public MilkyWayDataLoader(List<String> filePathList) throws IOException {

        milkyWayDataSet = new MilkyWayDataSet();

        int i = 0;
        Double ngp = Math.toRadians(27.4);

        for (String filePath : filePathList) {

            List dataSet = new LinkedList<Point2D>();
            
            InputStream inputStream = MilkyWayDataLoader.class.getResourceAsStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ASCII"));

            try {
                String strLine;
                while ((strLine = reader.readLine()) != null) {
                    if (!strLine.isEmpty()) {
                        String[] values = strLine.split(" ");
                        Point2D coord = new Point2D.Double();
                        Double l = Math.toRadians(360.0 - 33.0 - (180.0 + Double.parseDouble(values[0]) / 100.0));
                        Double b = Math.toRadians(Double.parseDouble(values[1]) / 1000.0);
                        Double Dec = Math.toDegrees(Math.asin(Math.cos(b) * Math.cos(ngp) * Math.sin(l) + Math.sin(b) * Math.sin(ngp)));
                        Double RA = 90D + (282.25 + Math.toDegrees(Math.atan2(Math.cos(b) * Math.cos(l), Math.sin(b) * Math.cos(ngp) - Math.cos(b) * Math.sin(ngp) * Math.sin(l)))) / 15.0;
                        coord.setLocation(RA, Dec);
                        dataSet.add(coord);
                    }
                }
            } finally {
                reader.close();
            }
            inputStream.close();

            switch (i) {
                case 0:
                    milkyWayDataSet.setDarkNorth(dataSet);
                    break;
                case 1:
                    milkyWayDataSet.setDarkSouth(dataSet);
                    break;
                case 2:
                    milkyWayDataSet.setBrightNorth(dataSet);
                    break;
                case 3:
                    milkyWayDataSet.setBrightSouth(dataSet);
                    break;
                default:
            }
            i++;
        }
    }

    public MilkyWayDataSet getMilkyWayDataSet() {
        return milkyWayDataSet;
    }
}
