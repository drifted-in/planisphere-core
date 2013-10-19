package in.drifted.planisphere.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;

public final class FontManager {

    private static final String FONT_BASE_PATH = "/in/drifted/planisphere/resources/fonts/";
    private final ResourceBundle resources;
    private final String country;

    public FontManager(Locale locale) {
        resources = ResourceBundle.getBundle("in.drifted.planisphere.resources.fonts.mapping");
        country = locale.getCountry();
    }

    public String translate(String content) throws IOException {
        String[] chunksRaw = content.split("\\$\\{");
        if (chunksRaw.length <= 1) {
            return content;
        }
        List<String> chunkList = new LinkedList<>();
        for (String chunk : chunksRaw) {
            if (!chunk.contains("}")) {
                if (chunk.length() > 0) {
                    chunkList.add(chunk);
                }
            } else {
                int index = chunk.indexOf("}");
                String key = chunk.substring(0, index);
                if (key.contains(".name")) {
                    chunkList.add(getValue(key.replace(".name", "")).replace(".ttf", ""));
                } else {
                    chunkList.add(getFontBase64Encoded(getValue(key)));
                }
                if (index != chunk.length() - 1) {
                    chunkList.add(chunk.substring(index + 1));
                }
            }
        }
        StringBuilder contentUpdated = new StringBuilder();
        for (String chunk : chunkList) {
            contentUpdated.append(chunk);
        }

        return contentUpdated.toString();
    }

    private String getValue(String key) {
        String keyLocal = key + "." + country.toLowerCase(Locale.ENGLISH);
        String keyDefault = key + ".default";
        if (resources.containsKey(keyLocal)) {
            return resources.getString(keyLocal);
        } else if (resources.containsKey(keyDefault)) {
            return resources.getString(keyDefault);
        } else {
            return key;
        }
    }

    private String getFontBase64Encoded(String fontFileName) throws IOException {

        String fontPath = FONT_BASE_PATH + fontFileName;
        String[] fileNameFragments = fontFileName.split("\\.");
        String fontFormat = fileNameFragments[fileNameFragments.length - 1];

        StringBuilder fontInfo = new StringBuilder();
        fontInfo.append("data:font/");
        fontInfo.append(fontFormat);
        fontInfo.append(";base64,");
        InputStream fontData = FontManager.class.getResourceAsStream(fontPath);
        fontInfo.append(DatatypeConverter.printBase64Binary(IOUtils.toByteArray(fontData)));

        return fontInfo.toString();
    }
}
