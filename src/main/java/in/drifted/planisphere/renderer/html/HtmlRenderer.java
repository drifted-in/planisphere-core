package in.drifted.planisphere.renderer.html;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.renderer.svg.SvgRenderer;
import in.drifted.planisphere.util.LocalizationUtil;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLStreamException;

public final class HtmlRenderer {

    private final SvgRenderer svgRenderer;

    public HtmlRenderer(SvgRenderer svgRenderer) {
        this.svgRenderer = svgRenderer;
    }

    public void createFromTemplateMap(Map<String, Options> templateMap, Path outputPath) throws XMLStreamException, IOException {

        LocalizationUtil l10n = new LocalizationUtil(templateMap.values().iterator().next().getCurrentLocale());

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {

            writer.append("<html><head>");
            writer.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">");
            writer.append("<title>" + l10n.getValue("applicationName") + "</title>");
            writer.append("<style type=\"text/css\">@page {size: auto; margin:0mm}</style>");
            writer.append("</head><body style=\"margin:0;\">");

            for (Entry<String, Options> entry : templateMap.entrySet()) {
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    svgRenderer.createFromTemplate(entry.getKey(), outputStream, entry.getValue());
                    writer.append(getBase64EncodedImage(outputStream.toByteArray()));
                }
            }
            writer.append("</body></html>");
        }
    }

    private String getBase64EncodedImage(byte[] imageByteArray) {

        StringBuilder result = new StringBuilder();
        result.append("<img src=\"data:image/svg+xml;base64,");
        result.append(DatatypeConverter.printBase64Binary(imageByteArray));
        result.append("\"/>");

        return result.toString();
    }
}
