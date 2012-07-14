package in.drifted.planisphere.renderer.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.util.PDFMergerUtility;

public class ProducerPDFBox {

    Transcoder transcoder = new PDFTranscoder();

    public ProducerPDFBox() {
    }

    public void createPDF(List<InputStream> svgStreamList, String destinationFileName) throws Exception {

        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(destinationFileName);
        for (InputStream svgStream : svgStreamList) {
            merger.addSource(new ByteArrayInputStream(getPDFByteArray(svgStream)));
        }
        merger.mergeDocuments();

    }

    private byte[] getPDFByteArray(InputStream svgStream) throws Exception {

        TranscoderInput input = new TranscoderInput(svgStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 1024);
        TranscoderOutput output = new TranscoderOutput(outputStream);
        transcoder.transcode(input, output);
        return outputStream.toByteArray();

    }
}
