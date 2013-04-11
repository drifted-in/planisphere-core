package in.drifted.planisphere.util;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

public final class MilkyWayDataLoader implements Serializable {

    private MilkyWayDataSet milkyWayDataSet;

    public MilkyWayDataLoader(ArrayList<String> filePaths) throws Exception {

        this.milkyWayDataSet = new MilkyWayDataSet();
        int i = 0;
        Double ngp = Math.toRadians(27.4D);

        for (String filePath : filePaths) {
            ArrayList dataSet = new ArrayList();
            InputStream inputStream = getClass().getResourceAsStream(filePath);

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ASCII"));

                try {
                    String strLine;
                    while ((strLine = reader.readLine()) != null) {
                        if (!strLine.isEmpty()) {
                            String[] values = strLine.split(" ");
                            Point2D coord = new Point2D.Double();
                            Double l = Math.toRadians(360.0D - 33.0D - (180.0D + Double.parseDouble(values[0]) / 100.0D));
                            Double b = Math.toRadians(Double.parseDouble(values[1]) / 1000.0D);
                            Double Dec = Math.toDegrees(Math.asin(Math.cos(b) * Math.cos(ngp) * Math.sin(l) + Math.sin(b) * Math.sin(ngp)));
                            Double RA = 90D + (282.25D + Math.toDegrees(Math.atan2(Math.cos(b) * Math.cos(l), Math.sin(b) * Math.cos(ngp) - Math.cos(b) * Math.sin(ngp) * Math.sin(l)))) / 15.0D;
                            coord.setLocation(RA, Dec);
                            dataSet.add(coord);
                        }
                    }
                } finally {
                    reader.close();
                }
            } finally {
                inputStream.close();
            }

            switch (i) {
                case 0:
                    this.milkyWayDataSet.setDarkNorth(dataSet);
                case 1:
                    this.milkyWayDataSet.setDarkSouth(dataSet);
                case 2:
                    this.milkyWayDataSet.setBrightNorth(dataSet);
                case 3:
                    this.milkyWayDataSet.setBrightSouth(dataSet);
            }
            i++;
        }
    }

    public MilkyWayDataSet getMilkyWayDataSet() {
        return this.milkyWayDataSet;
    }
}
