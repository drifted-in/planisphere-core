package in.drifted.planisphere.renderer.svg;

import in.drifted.planisphere.Settings;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class RendererUtil {
    
    public static void renderPath(XMLStreamWriter writer, String pathData, String id, String style) throws XMLStreamException {
        writer.writeStartElement("path");
        if (id != null) {
            writer.writeAttribute("id", id);
        }
        writer.writeAttribute("d", pathData);
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeEndElement();
    }

    public static void renderTextOnPath(XMLStreamWriter writer, String pathID, Double startOffset, String text, String style) throws XMLStreamException {

        writer.writeStartElement("text");
        writer.writeStartElement("textPath");
        writer.writeAttribute("xlink:href", "#" + pathID);
        writer.writeAttribute("startOffset", PathUtil.format(startOffset) + "%");
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeCharacters(text);
        writer.writeEndElement();
        writer.writeEndElement();
    }
        
    public static void renderDefsInstance(XMLStreamWriter writer, String id, Double x, Double y, String transform, String style) throws XMLStreamException {
        writer.writeStartElement("use");
        if (x != 0) {
            writer.writeAttribute("x", PathUtil.format(x));
        }
        if (y != 0) {
            writer.writeAttribute("y", PathUtil.format(y));
        }
        writer.writeAttribute("xlink", "http://www.w3.org/1999/xlink", "href", "#" + id);
        if (transform != null) {
            writer.writeAttribute("transform", transform);
        }
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeEndElement();
    }

    public static void renderSymbol(XMLInputFactory inputFactory, XMLStreamWriter writer, String id) throws XMLStreamException, IOException {
        try (InputStream is = RendererUtil.class.getResourceAsStream(Settings.RESOURCE_BASE_PATH + "templates/resources/symbols/" + id + ".svg")) {
            writeStreamContent(inputFactory, writer, is);
        }
    }
    
    public static void writeAttributes(XMLStreamWriter writer, Iterator attributes) throws XMLStreamException {
        while (attributes.hasNext()) {
            Attribute attr = (Attribute) attributes.next();
            writer.writeAttribute(attr.getName().getPrefix(), attr.getName().getNamespaceURI(), attr.getName().getLocalPart(), attr.getValue());
        }
    }

    public static void writeNamespaces(XMLStreamWriter writer, Iterator namespaces) throws XMLStreamException {
        while (namespaces.hasNext()) {
            Namespace ns = (Namespace) namespaces.next();
            writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
        }
    }

    public static void writeGroupStart(XMLStreamWriter writer, String id) throws XMLStreamException {
        writer.writeStartElement("g");
        if (id != null) {
            writer.writeAttribute("id", id);
        }
    }

    public static void writeGroupEnd(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEndElement();
    }
    
    public static Element getParamElement(DocumentBuilder documentBuilder, XMLOutputFactory outputFactory, XMLEventReader parser, XMLEvent event) throws XMLStreamException {
        Element paramElement = documentBuilder.newDocument().createElement("g");
        XMLEventWriter xmlWriter = outputFactory.createXMLEventWriter(new DOMResult(paramElement));
        Integer level = 1;
        XMLEvent paramEvent = event;
        while (!(parser.hasNext() && paramEvent.getEventType() == XMLStreamConstants.END_ELEMENT && level == 0)) {
            paramEvent = parser.nextEvent();
            xmlWriter.add(paramEvent);
            if (paramEvent.isStartElement()) {
                level++;
            }
            if (paramEvent.isEndElement()) {
                level--;
            }
        }
        return paramElement;
    }
    
    public static Element replaceTextElementContent(Element element, String originalText, String newText) {
        Element resultElement = (Element) element.cloneNode(true);
        NodeList textNodes = resultElement.getElementsByTagName("text");
        for (Integer i = 0; i < textNodes.getLength(); i++) {
            if (textNodes.item(i).getTextContent().contains(originalText)) {
                textNodes.item(i).setTextContent(newText);
            }
        }
        return resultElement;
    }
    
    public static void writeElementContent(XMLInputFactory inputFactory, XMLStreamWriter writer, Element element) throws XMLStreamException {

        //http://stackoverflow.com/questions/7257508/convert-java-w3c-document-to-xmlstreamreader
        //http://bugs.sun.com/view_bug.do?bug_id=6631274
        DOMSource domSource = new DOMSource(element);
        XMLEventReader parser = inputFactory.createXMLEventReader(domSource);

        StartElement startElement;
        Characters characters;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    startElement = (StartElement) event;
                    writer.writeStartElement(startElement.getName().getLocalPart());
                    writeAttributes(writer, startElement.getAttributes());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    writer.writeEndElement();
                    break;
                case XMLStreamConstants.CHARACTERS:
                    characters = (Characters) event;
                    writer.writeCharacters(characters.getData());
                    break;
                default:
            }
        }
        parser.close();
    }

    private static void writeStreamContent(XMLInputFactory inputFactory, XMLStreamWriter writer, InputStream content) throws XMLStreamException {

        XMLEventReader parser = inputFactory.createXMLEventReader(content);
        StartElement startElement;
        Characters characters;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    startElement = (StartElement) event;
                    writer.writeStartElement(startElement.getName().getLocalPart());
                    writeAttributes(writer, startElement.getAttributes());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    writer.writeEndElement();
                    break;
                case XMLStreamConstants.CHARACTERS:
                    characters = (Characters) event;
                    writer.writeCharacters(characters.getData());
                    break;
                /*
                 case XMLStreamConstants.CDATA:
                 characters = (Characters) event;
                 writer.writeCData(characters.getData());
                 break;
                 */
                default:
            }
        }
        parser.close();
    }
    
}
