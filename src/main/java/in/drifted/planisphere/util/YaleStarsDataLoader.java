package in.drifted.planisphere.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

public class YaleStarsDataLoader implements StarsDataLoader, Serializable {

    private ArrayList<Star> stars;

    public YaleStarsDataLoader(String filePath) throws Exception {
        this.stars = new ArrayList();
        InputStream inputStream = getClass().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String strLine;
        while ((strLine = reader.readLine()) != null) {
            if (!strLine.isEmpty()) {
                Star star = new Star();
                String[] values = strLine.split(",");
                star.setRA(Double.parseDouble(values[0]) / 1000.0D);
                star.setDec(Double.parseDouble(values[1]) / 100.0D);
                star.setMag(Float.parseFloat(values[2]) / 100.0F);

                Integer colorRaw = Integer.parseInt(values[3]);
                if (colorRaw < 0) {
                    star.setColor(new Color(1.0F + colorRaw * 0.01F, 1.0F + colorRaw * 0.01F, 0.95F));
                } else if (colorRaw < 290) {
                    star.setColor(new Color(1.0F, 1.0F - colorRaw * 0.0015F, 0.98F - colorRaw * 0.0025F));
                } else {
                    star.setColor(Color.WHITE);
                }
                this.stars.add(star);
            }
        }

        reader.close();
        inputStream.close();
    }

    public ArrayList<Star> getStars() {
        return this.stars;
    }

    public Integer size() {
        return this.stars.size();
    }
}
