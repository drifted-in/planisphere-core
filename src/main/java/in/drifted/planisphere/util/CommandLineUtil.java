/*
 * Copyright (c) 2012-present Jan Tošovský <jan.tosovsky.cz@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package in.drifted.planisphere.util;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.Settings;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class CommandLineUtil {

    public static Options getOptions(Path optionsPath) throws IOException {
        try ( InputStream inputStream = Files.newInputStream(optionsPath)) {
            return getOptions(inputStream);
        }
    }

    private static Options getOptions(InputStream inputStream) throws IOException {
        try {
            Node rootNode = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream).getDocumentElement();

            int latitude = 50;
            Locale locale = Locale.ENGLISH;
            String themeScreen = "screenDefault_blue";
            String themePrint = "printDefault_dark";
            int side = 1;
            boolean constellationLines = true;
            boolean constellationLabels = true;
            int constellationLabelsMode = 0;
            boolean constellationBoundaries = false;
            boolean milkyWay = false;
            boolean dayLightSavingTimeScale = true;
            boolean coordsRADec = false;
            boolean ecliptic = true;
            boolean allVisibleStars = false;

            for (int i = 0; i < rootNode.getAttributes().getLength(); i++) {
                Node attribute = rootNode.getAttributes().item(i);
                String value = attribute.getNodeValue();
                switch (attribute.getNodeName()) {
                    case "latitude":
                        latitude = Integer.parseInt(value);
                        break;
                    case "locale":
                        locale = Locale.forLanguageTag(value);
                        break;
                    case "themeScreen":
                        themeScreen = value;
                        break;
                    case "themePrint":
                        themePrint = value;
                        break;
                    case "constellationLines":
                        constellationLines = value.equals("1");
                        break;
                    case "constellationLabels":
                        constellationLabels = value.equals("1");
                        break;
                    case "constellationLabelsMode":
                        constellationLabelsMode = Integer.parseInt(value);
                        break;
                    case "constellationBoundaries":
                        constellationBoundaries = value.equals("1");
                        break;
                    case "milkyWay":
                        milkyWay = value.equals("1");
                        break;
                    case "dayLightSavingTimeScale":
                        dayLightSavingTimeScale = value.equals("1");
                        break;
                    case "coordsRADec":
                        coordsRADec = value.equals("1");
                        break;
                    case "ecliptic":
                        ecliptic = value.equals("1");
                        break;
                    case "allVisibleStars":
                        allVisibleStars = value.equals("1");
                        break;
                }
            }

            return new Options(latitude, locale, themeScreen, themePrint, side, constellationLines, constellationLabels,
                    constellationLabelsMode, constellationBoundaries, milkyWay, dayLightSavingTimeScale, coordsRADec,
                    ecliptic, allVisibleStars);

        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
    }

    public static String getUsage() throws IOException {

        StringBuilder usage = new StringBuilder();

        usage.append("Usage: java -jar planisphere.jar xmlConfigPath htmlOutputPath \n\n");
        usage.append("Sample XML config with default values: \n");

        try (
            InputStream inputStream = CommandLineUtil.class.getResourceAsStream("/in/drifted/planisphere/resources/options/options.xml");
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            char[] buffer = new char[1024];
            int pos;

            while ((pos = reader.read(buffer, 0, buffer.length)) > 0) {
                usage.append(buffer, 0, pos);
            }
        }

        usage.append("\n");

        usage.append("Supported values: \n");
        usage.append("(a) Locale values: \n");
        usage.append("\t");
        Boolean first = true;
        for (String localValue : Settings.getLocaleValueCollection()) {
            if (!first) {
                usage.append(", ");
            }
            usage.append(localValue);
            first = false;
        }
        usage.append("\n");

        usage.append("(b) Themes: \n");
        for (String templateName : Settings.getTemplateNameCollection(Settings.MEDIA_PRINT)) {
            for (String colorScheme : Settings.getColorSchemeCollection(templateName)) {
                usage.append("\t");
                usage.append(colorScheme);
                usage.append("\n");
            }
        }

        usage.append("(c) Constellation labels mode: \n");
        usage.append("\t0 (full names in the current language) \n");
        usage.append("\t1 (full names in latin) \n");
        usage.append("\t2 (abbreviations) \n");

        usage.append("\n");

        return usage.toString();
    }

}
