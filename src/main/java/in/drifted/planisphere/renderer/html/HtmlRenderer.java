package in.drifted.planisphere.renderer.html;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.renderer.svg.SvgRenderer;
import in.drifted.planisphere.util.LocalizationUtil;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

public final class HtmlRenderer implements Serializable {

    private final SvgRenderer svgRenderer;
    
    public HtmlRenderer(SvgRenderer svgRenderer) {
        this.svgRenderer = svgRenderer;
    }
    
    public void createFromTemplateList(List<String> templateList, String outputPath, Options options) throws Exception {

        LocalizationUtil l10n = new LocalizationUtil(options.getCurrentLocale());

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath), StandardCharsets.UTF_8)) {

            writer.append("<html><head>");
            writer.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">");
            writer.append("<title>" + l10n.getValue("applicationName") + "</title>");
            writer.append("</head><body style=\"margin:0;\">");

            for (String template : templateList) {
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    svgRenderer.createFromTemplate(template, outputStream, options);
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
