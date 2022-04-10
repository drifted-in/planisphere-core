package in.drifted.planisphere.renderer.svg;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.util.CacheHandler;
import in.drifted.planisphere.util.FontManager;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

public class RenderingContext {

    private final Options options;
    private final XMLInputFactory inputFactory;
    private final XMLOutputFactory outputFactory;
    private final XMLEventFactory eventFactory;
    private final FontManager fontManager;
    private final CacheHandler cacheHandler;

    public RenderingContext(Options options, XMLInputFactory inputFactory, XMLOutputFactory outputFactory,
            XMLEventFactory eventFactory, FontManager fontManager, CacheHandler cacheHandler) {
        this.options = options;
        this.inputFactory = inputFactory;
        this.outputFactory = outputFactory;
        this.eventFactory = eventFactory;
        this.fontManager = fontManager;
        this.cacheHandler = cacheHandler;
    }

    public Options getOptions() {
        return options;
    }

    public XMLInputFactory getInputFactory() {
        return inputFactory;
    }

    public XMLOutputFactory getOutputFactory() {
        return outputFactory;
    }

    public XMLEventFactory getEventFactory() {
        return eventFactory;
    }

    public FontManager getFontManager() {
        return fontManager;
    }

    public CacheHandler getCacheHandler() {
        return cacheHandler;
    }

}
