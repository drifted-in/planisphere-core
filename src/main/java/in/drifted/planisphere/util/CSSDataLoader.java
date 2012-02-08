package in.drifted.planisphere.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

public class CSSDataLoader implements Serializable {

    StringBuilder cssData = new StringBuilder();

    public CSSDataLoader(String filePath) throws Exception {
        FileInputStream inputStream = new FileInputStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String strLine;
        while ((strLine = reader.readLine()) != null) {
            this.cssData.append(strLine);
        }

        reader.close();
        inputStream.close();
    }

    public String getCssData() {
        return this.cssData.toString();
    }
}
