package in.drifted.planisphere.renderer.pdf;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.fop.Version;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.AbstractFOPTranscoder;
import org.apache.fop.svg.PDFBridgeContext;
import org.apache.fop.svg.PDFDocumentGraphics2D;
import org.apache.fop.svg.PDFDocumentGraphics2DConfigurator;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGLength;

public class MultiPagePDFTranscoder extends AbstractFOPTranscoder {

    /**
     * Graphics2D instance that is used to paint to
     */
    protected PDFDocumentGraphics2D graphics = null;

    /**
     * Constructs a new {@link MultiPagePDFTranscoder}.
     */
    public MultiPagePDFTranscoder() {
        super();
        //this.handler = new FOPErrorHandler();
    }

    /**
     * {@inheritDoc}
     */
    protected UserAgent createUserAgent() {
        return new AbstractFOPTranscoder.FOPTranscoderUserAgent() {
            // The PDF stuff wants everything at 72dpi
            public float getPixelUnitToMillimeter() {
                return super.getPixelUnitToMillimeter();
                //return 25.4f / 72; //72dpi = 0.352778f;
            }
        };
    }

    public void transcode(TranscoderInput[] inputs, TranscoderOutput output)
            throws TranscoderException {

        String parserClassname = (String) hints.get(KEY_XML_PARSER_CLASSNAME);
        String namespaceURI = (String) hints.get(KEY_DOCUMENT_ELEMENT_NAMESPACE_URI);
        String documentElement = (String) hints.get(KEY_DOCUMENT_ELEMENT);
        DOMImplementation domImpl = (DOMImplementation) hints.get(KEY_DOM_IMPLEMENTATION);

        if (parserClassname == null) {
            parserClassname = XMLResourceDescriptor.getXMLParserClassName();
        }

        if (domImpl == null) {
            handler.fatalError(new TranscoderException("Unspecified transcoding hints: KEY_DOM_IMPLEMENTATION"));
            return;
        }

        if (namespaceURI == null) {
            handler.fatalError(new TranscoderException("Unspecified transcoding hints: KEY_DOCUMENT_ELEMENT_NAMESPACE_URI"));
            return;
        }

        if (documentElement == null) {
            handler.fatalError(new TranscoderException("Unspecified transcoding hints: KEY_DOCUMENT_ELEMENT"));
            return;
        }

        // common Document factory
        DocumentFactory factory = createDocumentFactory(domImpl, parserClassname);
        Boolean validating = (Boolean) hints.get(KEY_XML_PARSER_VALIDATING);
        factory.setValidating(validating);

        Document[] documents = new Document[inputs.length];
        String uri = null;

        for (int i = 0; i < inputs.length; i++) {

            TranscoderInput input = inputs[i];
            Document document = null;
            uri = input.getURI();

            try {
                if (input.getInputStream() != null) {
                    document = factory.createDocument(namespaceURI,
                            documentElement,
                            input.getURI(),
                            input.getInputStream());
                } else if (input.getReader() != null) {
                    document = factory.createDocument(namespaceURI,
                            documentElement,
                            input.getURI(),
                            input.getReader());
                } else if (input.getXMLReader() != null) {
                    document = factory.createDocument(namespaceURI,
                            documentElement,
                            input.getURI(),
                            input.getXMLReader());
                } else if (uri != null) {
                    document = factory.createDocument(namespaceURI,
                            documentElement,
                            uri);
                }
            } catch (DOMException ex) {
                handler.fatalError(new TranscoderException(ex));
            } catch (IOException ex) {
                handler.fatalError(new TranscoderException(ex));
            }

            documents[i] = document;
        }

        // call the dedicated transcode method
        try {
            transcode(documents, uri, output);
        } catch (TranscoderException ex) {
            // at this time, all TranscoderExceptions are fatal errors
            handler.fatalError(ex);
            return;
        }
    }

    protected void transcode(Document[] documents, String uri, TranscoderOutput output) throws TranscoderException {

        graphics = new PDFDocumentGraphics2D(isTextStroked());
        graphics.getPDFDocument().getInfo().setProducer("Apache FOP Version "
                + Version.getVersion()
                + ": Multi-page PDF Transcoder for Batik");
        if (hints.containsKey(KEY_DEVICE_RESOLUTION)) {
            graphics.setDeviceDPI(getDeviceResolution());
        }

        setupImageInfrastructure(uri);

        try {
            Configuration effCfg = getEffectiveConfiguration();

            if (effCfg != null) {
                PDFDocumentGraphics2DConfigurator configurator = new PDFDocumentGraphics2DConfigurator();
                configurator.configure(graphics, effCfg, false);
            } else {
                graphics.setupDefaultFontInfo();
            }
        } catch (Exception e) {
            throw new TranscoderException("Error while setting up PDFDocumentGraphics2D", e);
        }

        try {

            OutputStream out = output.getOutputStream();
            if (!(out instanceof BufferedOutputStream)) {
                out = new BufferedOutputStream(out);
            }

            for (int i = 0; i < documents.length; i++) {

                Document document = documents[i];

                super.transcode(document, uri, null);

                if (getLogger().isTraceEnabled()) {
                    getLogger().trace("document size: " + width + " x " + height);
                }

                // prepare the image to be painted

                UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, document.getDocumentElement());
                float widthInPt = UnitProcessor.userSpaceToSVG(width, SVGLength.SVG_LENGTHTYPE_PT, UnitProcessor.HORIZONTAL_LENGTH, uctx);
                int w = (int) (widthInPt + 0.5);
                float heightInPt = UnitProcessor.userSpaceToSVG(height, SVGLength.SVG_LENGTHTYPE_PT, UnitProcessor.HORIZONTAL_LENGTH, uctx);
                int h = (int) (heightInPt + 0.5);

                if (getLogger().isTraceEnabled()) {
                    getLogger().trace("document size: " + w + "pt x " + h + "pt");
                }

                if (i == 0) {
                    graphics.setupDocument(out, w, h);
                } else {
                    graphics.nextPage(w, h);
                }

                graphics.setSVGDimension(width, height);

                if (hints.containsKey(ImageTranscoder.KEY_BACKGROUND_COLOR)) {
                    graphics.setBackgroundColor((Color) hints.get(ImageTranscoder.KEY_BACKGROUND_COLOR));
                }

                graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
                //graphics.preparePainting();

                graphics.transform(curTxf);
                graphics.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING, RenderingHintsKeyExt.VALUE_TRANSCODING_VECTOR);

                this.root.paint(graphics);
            }

            graphics.finish();

        } catch (IOException ex) {
            throw new TranscoderException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected BridgeContext createBridgeContext() {
        //For compatibility with Batik 1.6
        return createBridgeContext("1.x");
    }

    /**
     * {@inheritDoc}
     */
    public BridgeContext createBridgeContext(String version) {

        FontInfo fontInfo = null;

        if (!isTextStroked()) {
            fontInfo = graphics.getFontInfo();
        }

        return new PDFBridgeContext(userAgent, fontInfo, getImageManager(), getImageSessionContext());
    }
}
