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
package in.drifted.planisphere.renderer.html;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.Settings;
import in.drifted.planisphere.l10n.LocalizationUtil;
import in.drifted.planisphere.renderer.svg.SvgRenderer;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;

public final class HtmlRenderer {

    private final SvgRenderer svgRenderer;

    public HtmlRenderer(SvgRenderer svgRenderer) {
        this.svgRenderer = svgRenderer;
    }

    public void createFromTemplate(Options options, Path outputPath) throws IOException {
        Settings.normalizePrintTheme(options);
        createFromTemplateMap(Settings.getTemplateOptionsMap(options), options.getThemePrint(), outputPath);
    }

    private void createFromTemplateMap(Map<String, Options> templateMap, String colorScheme, Path outputPath) throws IOException {

        LocalizationUtil l10n = new LocalizationUtil(templateMap.values().iterator().next().getCurrentLocale());

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {

            writer.append("<html><head>");
            writer.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">");
            writer.append("<title>" + l10n.getValue("applicationName") + "</title>");
            writer.append("<style type=\"text/css\">@page {size: auto; margin:0mm}</style>");
            writer.append("</head><body style=\"margin:0;\">");

            for (Entry<String, Options> entry : templateMap.entrySet()) {
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    svgRenderer.createFromTemplate(entry.getKey(), colorScheme, outputStream, entry.getValue());
                    writer.append(getBase64EncodedImage(outputStream.toByteArray()));
                }
            }
            writer.append("</body></html>");
        }
    }

    private String getBase64EncodedImage(byte[] imageByteArray) {

        StringBuilder result = new StringBuilder();
        result.append("<img src=\"data:image/svg+xml;base64,");
        result.append(Base64.getEncoder().encodeToString(imageByteArray));
        result.append("\"/>");

        return result.toString();
    }
}
