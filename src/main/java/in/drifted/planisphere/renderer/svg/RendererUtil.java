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
package in.drifted.planisphere.renderer.svg;

import in.drifted.planisphere.Settings;
import in.drifted.planisphere.l10n.LocalizationUtil;
import in.drifted.planisphere.model.Point;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLEventFactory;
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class RendererUtil {

    public static void writeGroupStart(XMLStreamWriter writer, String id) throws XMLStreamException {
        writer.writeStartElement("g");
        if (id != null) {
            writer.writeAttribute("id", id);
        }
    }

    public static void writeGroupEnd(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEndElement();
    }

    public static void close(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEndDocument();
        writer.flush();
        writer.close();
    }

    public static void writeAttributes(XMLStreamWriter writer, Iterator attributes) throws XMLStreamException {
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            writer.writeAttribute(attribute.getName().getPrefix(), attribute.getName().getNamespaceURI(), attribute.getName().getLocalPart(), attribute.getValue());
        }
    }

    public static void writeNamespaces(XMLStreamWriter writer, Iterator namespaces) throws XMLStreamException {
        while (namespaces.hasNext()) {
            Namespace ns = (Namespace) namespaces.next();
            writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
        }
    }

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

    public static void renderTextOnPath(XMLStreamWriter writer, String pathID, double startOffset, String text, String style) throws XMLStreamException {

        writer.writeStartElement("text");

        writer.writeStartElement("textPath");
        writer.writeAttribute("xlink:href", "#" + pathID);
        writer.writeAttribute("startOffset", Number.format(startOffset) + "%");
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeCharacters(text);
        writer.writeEndElement();

        writer.writeEndElement();
    }

    public static void renderDefsInstance(XMLStreamWriter writer, String id, double x, double y, String transform, String style) throws XMLStreamException {
        writer.writeStartElement("use");
        if (x != 0) {
            writer.writeAttribute("x", Number.format(x));
        }
        if (y != 0) {
            writer.writeAttribute("y", Number.format(y));
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

    public static void renderSymbol(XMLInputFactory inputFactory, XMLStreamWriter writer, String id, LocalizationUtil localizationUtil) throws XMLStreamException, IOException {
        try (InputStream is = RendererUtil.class.getResourceAsStream(Settings.RESOURCE_BASE_PATH + "templates/resources/symbols/" + id + ".svg")) {
            writeStreamContent(inputFactory, writer, is, null, localizationUtil);
        }
    }

    public static byte[] getParamStream(XMLOutputFactory outputFactory, XMLEventFactory eventFactory, XMLEventReader parser, XMLEvent paramEvent) throws XMLStreamException, IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLEventWriter writer = outputFactory.createXMLEventWriter(outputStream);

        // this skips all the attributes of the original <g> element to avoid ID duplications
        writer.add(eventFactory.createStartElement("", null, "g"));

        Integer level = 1;
        while (!(parser.hasNext() && paramEvent.isEndElement() && level == 0)) {
            paramEvent = parser.nextEvent();
            writer.add(paramEvent);
            if (paramEvent.isStartElement()) {
                level++;
            }
            if (paramEvent.isEndElement()) {
                level--;
            }
        }

        writer.flush();
        writer.close();

        return outputStream.toByteArray();
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

    public static void writeStreamContent(XMLInputFactory inputFactory, XMLStreamWriter writer, InputStream content, Map<String, String> replacementMap, LocalizationUtil localizationUtil) throws XMLStreamException {

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
                    String text = characters.getData().trim();
                    if (!text.isEmpty()) {
                        if (replacementMap != null && !replacementMap.isEmpty()) {
                            for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
                                text = text.replace(entry.getKey(), entry.getValue());
                            }
                        }
                        writer.writeCharacters(localizationUtil.translate(text, 0.0));
                    }
                    break;
                default: // CDATA containing button styling is ignored intentionally
            }
        }
        parser.close();
    }

    public static String getPathData(List<Point> pointList, boolean append) {
        StringBuilder pathData = new StringBuilder();
        boolean isFirst = true;
        for (Point point : pointList) {
            if (isFirst) {
                if (append) {
                    pathData.append("L");
                } else {
                    pathData.append("M");
                }
                pathData.append(Number.format(point.getX()));
                pathData.append(" ");
                pathData.append(Number.format(point.getY()));
                isFirst = false;
            }
            pathData.append("L");
            pathData.append(Number.format(point.getX()));
            pathData.append(" ");
            pathData.append(Number.format(point.getY()));
        }
        pathData.append("z");
        return pathData.toString().replaceAll(" -", "-");
    }

    public static String getCircleCoordsChain(double radius) {
        BezierCircle circle = new BezierCircle(radius);
        return getCircleCoordsChain(circle.getPointList());
    }

    public static String getCircleCoordsChain(Point center, double radius) {
        BezierCircle circle = new BezierCircle(center, radius);
        return getCircleCoordsChain(circle.getPointList());
    }

    public static String getCircleCoordsChain(Point center, double radius, double angle) {
        BezierCircle circle = new BezierCircle(center, radius, angle);
        return getCircleCoordsChain(circle.getPointList());
    }

    public static String getCircleCoordsChainReverse(double radius) {
        BezierCircle circle = new BezierCircle(radius);
        return getCircleCoordsChainReverse(circle.getPointList());
    }

    public static String getCircleCoordsChainReverse(Point center, double radius) {
        BezierCircle circle = new BezierCircle(center, radius);
        return getCircleCoordsChainReverse(circle.getPointList());
    }

    public static String getCoordsChunk(Point point) {
        return Number.format(point.getX()) + " " + Number.format(point.getY());
    }

    private static String getCircleCoordsChainReverse(List<Point> pointList) {
        Iterator<Point> it = pointList.iterator();
        StringBuilder result = new StringBuilder();
        String firstPoint = getCoordsChunk(it.next());
        while (it.hasNext()) {
            result.insert(0, getCoordsChunk(it.next()));
            result.insert(0, " ");
            result.insert(0, getCoordsChunk(it.next()));
            result.insert(0, "C");
            if (it.hasNext()) {
                result.insert(0, getCoordsChunk(it.next()));
                result.insert(0, " ");
            } else {
                result.insert(0, firstPoint);
                result.insert(0, "M");
            }
        }
        result.append(" ");
        result.append(firstPoint);

        return result.toString();
    }

    private static String getCircleCoordsChain(List<Point> pointList) {
        Iterator<Point> it = pointList.iterator();
        StringBuilder result = new StringBuilder();
        String firstPoint = getCoordsChunk(it.next());
        result.append("M");
        result.append(firstPoint);
        while (it.hasNext()) {
            result.append("C");
            result.append(getCoordsChunk(it.next()));
            result.append(" ");
            result.append(getCoordsChunk(it.next()));
            result.append(" ");
            if (it.hasNext()) {
                result.append(getCoordsChunk(it.next()));
            } else {
                result.append(firstPoint);
            }
        }
        return result.toString();
    }

    public static String getLineHorizontalPathData(Point center, Double length) {

        StringBuilder path = new StringBuilder();
        path.append("M");
        path.append(Number.format(center.getX() - length / 2.0));
        path.append(" ");
        path.append(Number.format(center.getY()));
        path.append("h");
        path.append(length);

        return path.toString();
    }

    public static Point getIntersection(Point pointA, Point pointB, Point pointC) {

        // Get the perpendicular bisector of (x1, y1) and (x2, y2)
        double x1 = (pointB.getX() + pointA.getX()) / 2;
        double y1 = (pointB.getY() + pointA.getY()) / 2;
        double dy1 = pointB.getX() - pointA.getX();
        double dx1 = -(pointB.getY() - pointA.getY());

        // Get the perpendicular bisector of (x2, y2) and (x3, y3)
        double x2 = (pointC.getX() + pointB.getX()) / 2;
        double y2 = (pointC.getY() + pointB.getY()) / 2;
        double dy2 = pointC.getX() - pointB.getX();
        double dx2 = -(pointC.getY() - pointB.getY());

        // See where the lines intersect
        double ox = (y1 * dx1 * dx2 + x2 * dx1 * dy2 - x1 * dy1 * dx2 - y2 * dx1 * dx2)
                / (dx1 * dy2 - dy1 * dx2);
        double oy = (ox - x1) * dy1 / dx1 + y1;

        return new Point(ox, oy);
    }

}
