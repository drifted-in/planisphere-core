package in.drifted.planisphere.renderer.pdf;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

public class Producer {

    MultiPagePDFTranscoder transcoder = new MultiPagePDFTranscoder();

    public void createPDF(List<InputStream> svgStreamList, String destinationFileName) throws Exception {

        TranscoderInput[] inputs = new TranscoderInput[svgStreamList.size()];

        for (int i = 0; i < svgStreamList.size(); i++) {
            inputs[i] = new TranscoderInput(svgStreamList.get(i));
        }

        OutputStream outputStream = new FileOutputStream(destinationFileName);
        TranscoderOutput output = new TranscoderOutput(outputStream);
        transcoder.transcode(inputs, output);
        outputStream.close();

    }

}
