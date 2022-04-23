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

import com.seisw.util.geom.Point2D;
import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;
import in.drifted.planisphere.Options;
import in.drifted.planisphere.Settings;
import in.drifted.planisphere.l10n.LocalizationUtil;
import in.drifted.planisphere.model.CardinalPoint;
import in.drifted.planisphere.model.ConstellationName;
import in.drifted.planisphere.model.Point;
import in.drifted.planisphere.model.MilkyWay;
import in.drifted.planisphere.model.Star;
import in.drifted.planisphere.util.CacheHandler;
import in.drifted.planisphere.util.CoordUtil;
import in.drifted.planisphere.util.FontManager;
import in.drifted.planisphere.util.LanguageUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public final class SvgRenderer {

    /*
    public static byte[] createFromTemplate(Options options) throws IOException {

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        FontManager fontManager = new FontManager(options.getLocale());
        CacheHandler cacheHandler = CacheHandler.getInstance();

        RenderingContext renderingContext = new RenderingContext(options, inputFactory,
                outputFactory, eventFactory, fontManager, cacheHandler);

        ResourceBundle resources = ResourceBundle.getBundle("in.drifted.planisphere.resources.templates.templates");
        String resourcePath = Settings.RESOURCE_BASE_PATH + "templates/core/" + resources.getString(options.getThemePrint());

        try (InputStream input = SvgRenderer.class.getResourceAsStream(resourcePath)) {

            XMLEventReader reader = inputFactory.createXMLEventReader(input);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(output);

            createFromTemplate(reader, writer, renderingContext);

            return output.toByteArray();

        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
    */

    public static void createFromTemplate(String templateName, String colorScheme, OutputStream output, Options options) throws IOException {

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        FontManager fontManager = new FontManager(options.getLocale());
        CacheHandler cacheHandler = CacheHandler.getInstance();

        RenderingContext renderingContext = new RenderingContext(options, inputFactory,
                outputFactory, eventFactory, fontManager, cacheHandler);

        try (InputStream input = SvgRenderer.class.getResourceAsStream(Settings.RESOURCE_BASE_PATH + "templates/core/" + templateName + ".svg")) {

            XMLEventReader reader = inputFactory.createXMLEventReader(input);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(output);

            String colorSchemeData = cacheHandler.getColorSchemeData(templateName, colorScheme);

            createFromTemplate(reader, writer, fontManager.translate(colorSchemeData), renderingContext);

        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static void createFromTemplate(XMLEventReader reader, XMLStreamWriter writer, String colorSchemeData, RenderingContext renderingContext) throws IOException {

        Options options = renderingContext.getOptions();

        double scaleFixed = 0; // cover and main layout
        double scale = 0;      // map content

        List<Point> mapAreaPointList = null;
        List<CardinalPoint> cardinalPointList = null;

        try {
            Map<String, byte[]> paramMap = new HashMap<>();

            boolean isUsed = true;
            boolean isSuppressed = false;
            int level = 0;
            StartElement startElement;
            Characters characters;
            String elementName;
            Attribute idAttr;
            Attribute dirAttr;
            String id;

            String direction = LanguageUtil.getWritingDirection(options.getLocale().getLanguage());

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        startElement = (StartElement) event;
                        elementName = startElement.getName().getLocalPart();
                        idAttr = startElement.getAttributeByName(new QName("id"));
                        dirAttr = startElement.getAttributeByName(new QName("direction"));

                        if (dirAttr != null && dirAttr.getValue().equals("!" + direction)) {
                            isUsed = false;
                            isSuppressed = true;
                            level++;
                        } else {

                            if (idAttr != null) {
                                id = idAttr.getValue();
                                isUsed = false;
                                switch (id) {
                                    case "mapArea":
                                        renderDefsMapArea(writer, mapAreaPointList);
                                        break;
                                    case "monthsArea":
                                        renderDefsMonthsArea(writer, options.isDoubleSided(), scaleFixed);
                                        break;
                                    case "dialHoursMarkerMajorSingle":
                                    case "dialHoursMarkerMajorDouble":
                                    case "dialHoursMarkerMinorSingle":
                                    case "dialHoursMarkerMinorDouble":
                                        paramMap.put(id, RendererUtil.getParamStream(renderingContext.getOutputFactory(), renderingContext.getEventFactory(), reader, event));
                                        break;
                                    case "dialMonthsLabelMajorPath":
                                        renderDefsDialMonthsLabelMajorPath(writer, options.isDoubleSided(), scaleFixed);
                                        break;
                                    case "dialMonthsLabelMinorPath":
                                        renderDefsDialMonthsLabelMinorPath(writer, options.isDoubleSided(), scaleFixed);
                                        break;
                                    case "coordLabelPaths":
                                        RendererUtil.writeGroupStart(writer, "coordLabelPaths");
                                        renderDefsCoordLabelPaths(writer, options, scale);
                                        RendererUtil.writeGroupEnd(writer);
                                        break;
                                    case "wheel":
                                        RendererUtil.writeGroupStart(writer, "wheel");
                                        renderMapBackground(writer, scaleFixed);
                                        renderDialMonths(writer, options);
                                        renderDialMonthsTicks(writer, options, scaleFixed);
                                        if (options.hasMilkyWay()) {
                                            renderMilkyWay(writer, renderingContext, scale);
                                        }
                                        if (options.hasCoordsRADec()) {
                                            renderCoords(writer, options, scale);
                                            renderCoordLabels(writer, options);
                                        }
                                        if (options.hasEcliptic()) {
                                            renderEcliptic(writer, options, scale);
                                        }
                                        if (options.hasConstellationBoundaries()) {
                                            renderConstellationBoundaries(writer, renderingContext, scale);
                                        }
                                        if (options.hasConstellationLines()) {
                                            renderConstellationLines(writer, renderingContext, scale);
                                        }
                                        renderStars(writer, renderingContext, scale);
                                        if (options.hasConstellationLabels()) {
                                            renderConstellationNames(writer, renderingContext, scale);
                                        }
                                        RendererUtil.writeGroupEnd(writer);
                                        break;
                                    case "scales":
                                        RendererUtil.writeGroupStart(writer, "scales");
                                        renderDialHours(writer, renderingContext, scale, paramMap, options.hasDayLightSavingTimeScale());
                                        renderCardinalPointsTicks(writer, cardinalPointList);
                                        renderCardinalPointsLabels(writer, options, scale, cardinalPointList);
                                        RendererUtil.writeGroupEnd(writer);
                                        break;
                                    case "monthsAreaBorder":
                                        RendererUtil.writeGroupStart(writer, "monthsAreaBorder");
                                        renderMonthsAreaBorder(writer);
                                        RendererUtil.writeGroupEnd(writer);
                                        break;
                                    case "mapAreaBorder":
                                        RendererUtil.writeGroupStart(writer, "mapAreaBorder");
                                        renderMapAreaBorder(writer);
                                        RendererUtil.writeGroupEnd(writer);
                                        break;
                                    case "spacer":
                                        renderSpacer(writer, scaleFixed);
                                        break;
                                    case "cover":
                                        renderCover(writer, options, scaleFixed);
                                        break;
                                    case "pinMark":
                                        renderPinMark(writer, scaleFixed, false, options.isDoubleSided());
                                        break;
                                    case "guide_S":
                                    case "guide_D":
                                    case "worldmap":
                                    case "starSizeComparison":
                                    case "buttonFlip":
                                    case "buttonSettings":
                                    case "buttonExport":
                                    case "buttonMoreInfo":
                                        if (id.equals("buttonFlip") && !options.isDoubleSided()) {
                                            // ignore
                                        } else {
                                            writer.writeStartElement("g");
                                            if (id.contains("button")) {
                                                writer.writeStartElement("title");
                                                writer.writeCharacters(options.getLocalizationUtil().getValue(id));
                                                writer.writeEndElement();
                                            }
                                            RendererUtil.renderSymbol(renderingContext.getInputFactory(), writer, id, options.getLocalizationUtil());
                                            writer.writeEndElement();
                                        }
                                        break;
                                    case "latitudeMarker":
                                        writer.writeStartElement(elementName);
                                        Iterator<Attribute> it = startElement.getAttributes();
                                        while (it.hasNext()) {
                                            Attribute a = it.next();
                                            QName attr = a.getName();
                                            if (attr.getLocalPart().equals("y")) {
                                                double range = Double.valueOf(a.getValue());
                                                double ratio = range / 180;
                                                double y = ratio * (Math.abs(options.getLatitudeFixed() - 90) - 90);
                                                writer.writeAttribute("y", String.valueOf(y));
                                            } else {
                                                writer.writeAttribute(attr.getPrefix(), attr.getNamespaceURI(), attr.getLocalPart(), a.getValue());
                                            }
                                        }
                                        writer.writeEndElement();
                                        break;
                                    default:
                                        isUsed = true;
                                        writer.writeStartElement(elementName);
                                        RendererUtil.writeAttributes(writer, startElement.getAttributes());
                                        break;
                                }
                            } else if (elementName.equals("svg")) {
                                String[] values = startElement.getAttributeByName(new QName("viewBox")).getValue().split(" ");
                                scaleFixed = Math.min(Double.valueOf(values[2]) / 2, Double.valueOf(values[3]) / 2);
                                // the 'scale' for a map is set to maximum by default, but if an extra space on cover is needed, the map can be smaller
                                scale = 1.0 * scaleFixed;
                                mapAreaPointList = getMapAreaPointList(options, scale);
                                cardinalPointList = getCardinalPointList(options, scale, mapAreaPointList);
                                writer.writeStartElement(elementName);
                                RendererUtil.writeNamespaces(writer, startElement.getNamespaces());
                                RendererUtil.writeAttributes(writer, startElement.getAttributes());
                                writer.writeAttribute("direction", direction);
                            } else {
                                if (isSuppressed) {
                                    level++;
                                } else {
                                    writer.writeStartElement(elementName);
                                    RendererUtil.writeAttributes(writer, startElement.getAttributes());
                                }
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (isUsed) {
                            if (!isSuppressed) {
                                writer.writeEndElement();
                            }
                        } else {
                            isUsed = true;
                        }
                        if (isSuppressed) {
                            level--;
                            if (level == 0) {
                                isSuppressed = false;
                            }
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if (isUsed && !isSuppressed) {
                            characters = (Characters) event;
                            String text = characters.getData();
                            if (!(text.trim().isEmpty())) {
                                writer.writeCharacters(options.getLocalizationUtil().translate(text, options.getLatitudeFixed()));
                            }
                        }
                        break;
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        if (isUsed && !isSuppressed) {
                            ProcessingInstruction pi = (ProcessingInstruction) event;
                            switch (pi.getTarget()) {
                                case "mouseEventsScript":
                                    writer.writeStartElement("script");
                                    writer.writeAttribute("type", "application/ecmascript");

                                    StringWriter cdata = new StringWriter();
                                    InputStream is = SvgRenderer.class.getResourceAsStream(Settings.RESOURCE_BASE_PATH + "templates/resources/js/mouseEvents.js");

                                    try (Reader streamReader = new InputStreamReader(is, "UTF-8")) {
                                        char[] buffer = new char[1024];
                                        int n;
                                        while ((n = streamReader.read(buffer)) >= 0) {
                                            cdata.write(buffer, 0, n);
                                        }
                                    }
                                    writer.writeCData(cdata.toString());
                                    writer.writeEndElement();
                                    break;

                                case "colorScheme":
                                    writer.writeStartElement("style");
                                    writer.writeAttribute("type", "text/css");
                                    writer.writeCData(colorSchemeData);
                                    writer.writeEndElement();
                                    break;
                            }
                        }
                        break;

                    default:
                }
            }
            reader.close();
            writer.writeEndDocument();

        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    protected static List<Point> getMapAreaPointList(Options options, double scale) {

        List<Point> mapAreaPointList = new ArrayList<>();

        double step = 3.0;

        // in this mode we don't care the path direction, so absolute values are used
        double latitudeAbs = Math.abs(options.getLatitude());
        double latitudeInRads = Math.toRadians(latitudeAbs);

        if (options.isDoubleSided()) {

            double latitudeFixedAbs = Math.abs(options.getLatitudeFixed());
            double latitudeFixedInRads = Math.toRadians(latitudeFixedAbs);
            double scaleFix = scale * (180 - latitudeFixedAbs) / (180 - latitudeAbs);

            if (latitudeFixedAbs == 0) {
                // just starting point
                mapAreaPointList.add(getMapAreaPoint(options, -90, latitudeAbs, latitudeInRads, scale, false));
            } else {
                for (double Az = -90; Az < 90; Az = Az + step) {
                    mapAreaPointList.add(getMapAreaPoint(options, Az, latitudeFixedAbs, latitudeFixedInRads, scaleFix, true));
                }
            }
            for (double Az = 90; Az < 270; Az = Az + step) {
                mapAreaPointList.add(getMapAreaPoint(options, Az, latitudeAbs, latitudeInRads, scale, false));
            }

        } else {

            for (double Az = 90; Az < 450; Az = Az + step) {
                mapAreaPointList.add(getMapAreaPoint(options, Az, latitudeAbs, latitudeInRads, scale, false));
            }
        }

        return mapAreaPointList;
    }

    private static Point getMapAreaPoint(Options options, double Az, double latitudeInDegs, double latitudeInRads, double mapAreaScale, boolean useDoubleSidedSign) {

        double shift = options.isDoubleSided() ? 6 : -6;
        double AzInRads = Math.toRadians(Az);
        double Dec = Math.asin(Math.cos(AzInRads) * Math.cos(latitudeInRads));
        double RA = Math.atan2(Math.sin(AzInRads), Math.tan(latitudeInRads) * Math.sin(Dec));

        Point mapAreaPoint = CoordUtil.convertWithoutCheck(Math.toDegrees(RA) / 15 + shift, Math.toDegrees(Dec), latitudeInDegs, mapAreaScale);
        if (useDoubleSidedSign) {
            mapAreaPoint = new Point(mapAreaPoint.getX(), Math.signum(options.getLatitudeFixed()) * options.getDoubleSidedSign() * mapAreaPoint.getY());
        }

        return mapAreaPoint;
    }

    protected static List<CardinalPoint> getCardinalPointList(Options options, double scale, List<Point> mapAreaPointList) {

        List<CardinalPoint> cardinalPointList = new ArrayList<>();
        double latitudeAbs = Math.abs(options.getLatitude());
        List<String> cardinalPointLabelList = getCardinalPointLabelList(options);

        if (options.isDoubleSided()) {

            double latitudeFixedAbs = Math.abs(options.getLatitudeFixed());
            double latitudeFixedInRads = Math.toRadians(latitudeFixedAbs);
            double scaleFix = scale * (180 - latitudeFixedAbs) / (180 - latitudeAbs);

            if (options.getLatitudeFixed() == 0) {

                Point pointRight = mapAreaPointList.get(0);
                Point pointLeft = mapAreaPointList.get(1);
                double x0 = pointRight.getX();
                double y0 = pointRight.getY();
                double step = pointRight.getDistance(pointLeft) / 4;

                for (int i = 0; i <= 4; i++) {

                    double x = x0 - i * step;
                    Point tickStart = new Point(x, y0);
                    Point tickEnd = new Point(x, y0 + scale * 0.03);
                    int labelIndex = options.getDoubleSidedSign() < 0 ? i : (i + 4) % 8;
                    String label = cardinalPointLabelList.get(labelIndex);

                    // first three params are unused
                    cardinalPointList.add(new CardinalPoint(0, 0, null, tickStart, tickEnd, label));
                }

            } else {

                // right edge
                Point pointOutsideRight = getMapAreaPoint(options, -10.0, latitudeFixedAbs, latitudeFixedInRads, scaleFix, true);
                cardinalPointList.add(getCardinalPoint(pointOutsideRight, mapAreaPointList.get(0), mapAreaPointList.get(10), cardinalPointLabelList.get(0), options, scale));

                for (int i = 1; i <= 3; i++) {

                    int index = i * 15;
                    Point pointA = mapAreaPointList.get(index - 10);
                    Point pointB = mapAreaPointList.get(index);
                    Point pointC = mapAreaPointList.get(index + 10);

                    cardinalPointList.add(getCardinalPoint(pointA, pointB, pointC, cardinalPointLabelList.get(i), options, scale));
                }

                // left edge
                Point pointOutsideLeft = getMapAreaPoint(options, 100.0, latitudeFixedAbs, latitudeFixedInRads, scaleFix, true);
                cardinalPointList.add(getCardinalPoint(mapAreaPointList.get(50), mapAreaPointList.get(60), pointOutsideLeft, cardinalPointLabelList.get(4), options, scale));
            }

            for (int i = 0; i < 3; i++) {
                cardinalPointList.add(null);
            }

        } else {

            for (int i = 0; i < 8; i++) {

                if (((i == 0 || i == 4) && latitudeAbs > 70) || ((i == 5 || i == 7) && options.getLatitude() > 78)) {
                    cardinalPointList.add(null);

                } else {

                    int index = i * 15;
                    Point pointA = mapAreaPointList.get((index - 10 + 120) % 120);
                    Point pointB = mapAreaPointList.get(index);
                    Point pointC = mapAreaPointList.get(index + 10);

                    cardinalPointList.add(getCardinalPoint(pointA, pointB, pointC, cardinalPointLabelList.get(i), options, scale));
                }
            }
        }

        return cardinalPointList;
    }

    private static CardinalPoint getCardinalPoint(Point pointA, Point pointB, Point pointC, String label, Options options, double scale) {

        Point intersection = RendererUtil.getIntersection(pointA, pointB, pointC);
        double radius = intersection.getDistance(pointB);

        double sign = options.isDoubleSided() ? options.getDoubleSidedSign() * options.getLatitudeFixedSign() : 1;
        double delta = -sign * Math.PI / 2 + Math.atan2(pointB.getX() - intersection.getX(), pointB.getY() - intersection.getY());

        double dx = pointB.getX() + scale * 0.03 * Math.cos(delta);
        double dy = pointB.getY() - scale * 0.03 * Math.sin(delta);
        double startOffset = getNormalizedPercent(100 * (90 - Math.toDegrees(delta)) / 360);

        return new CardinalPoint(startOffset, radius, intersection, pointB, new Point(dx, dy), label);
    }

    private static List<String> getCardinalPointLabelList(Options options) {

        LocalizationUtil localizationUtil = options.getLocalizationUtil();
        List<String> cardinalPointLabelList = new ArrayList<>();
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointWest"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointSouthWest"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointSouth"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointSouthEast"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointEast"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointNorthEast"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointNorth"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointNorthWest"));

        if (options.isDoubleSided()) {
            if (options.getDoubleSidedSign() > 0 && options.getLatitudeFixed() != 0) {
                cardinalPointLabelList = getSwappedList(cardinalPointLabelList);
            }
        } else {
            if (options.getLatitude() < 0) {
                cardinalPointLabelList = getSwappedList(cardinalPointLabelList);
            }
        }

        return cardinalPointLabelList;
    }

    private static List<String> getSwappedList(List<String> cardinalPointLabelList) {

        List<String> swappedList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            swappedList.add(i, cardinalPointLabelList.get(i + 4));
        }
        for (int i = 0; i < 4; i++) {
            swappedList.add(i + 4, cardinalPointLabelList.get(i));
        }

        return swappedList;
    }

    private static void renderDefsMapArea(XMLStreamWriter writer, List<Point> mapAreaPointList) throws XMLStreamException {
        StringBuilder path = new StringBuilder();
        Iterator iterator = mapAreaPointList.iterator();
        path.append("M");
        path.append(RendererUtil.getCoordsChunk((Point) iterator.next()));
        while (iterator.hasNext()) {
            path.append("L");
            path.append(RendererUtil.getCoordsChunk((Point) iterator.next()));
        }
        path.append("z");
        RendererUtil.renderPath(writer, path.toString(), "mapArea", null);
    }

    private static void renderDefsMonthsArea(XMLStreamWriter writer, boolean isDoubleSided, double scaleFixed) throws XMLStreamException {

        double angle = Math.toRadians(30);
        double x1 = Math.cos(angle) * 0.9 * scaleFixed;
        double y1 = Math.sin(angle) * 0.9 * scaleFixed;
        String sweep1 = "1";
        double x2 = Math.cos(angle) * scaleFixed;
        double y2 = Math.sin(angle) * scaleFixed;
        String sweep2 = "0";

        if (isDoubleSided) {
            y1 = -y1;
            y2 = -y2;
            sweep1 = "0";
            sweep2 = "1";
        }

        StringBuilder pathData = new StringBuilder();
        // inner arc
        pathData.append("M");
        pathData.append(Number.format(-x1));
        pathData.append(" ");
        pathData.append(Number.format(y1));
        pathData.append("A");
        // rx
        pathData.append(Number.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(Number.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 ");
        pathData.append(sweep1);
        pathData.append(" ");
        pathData.append(Number.format(x1));
        pathData.append(" ");
        pathData.append(Number.format(y1));

        pathData.append("L");
        pathData.append(Number.format(x2));
        pathData.append(" ");
        pathData.append(Number.format(y2));

        // outer arc
        pathData.append("A");
        pathData.append(Number.format(scaleFixed));
        pathData.append(" ");
        pathData.append(Number.format(scaleFixed));
        pathData.append(" 0 1 ");
        pathData.append(sweep2);
        pathData.append(" ");
        pathData.append(Number.format(-x2));
        pathData.append(" ");
        pathData.append(Number.format(y2));

        pathData.append("Z");

        RendererUtil.renderPath(writer, pathData.toString(), "monthsArea", "null");
    }

    private static void renderDefsDialMonthsLabelMajorPath(XMLStreamWriter writer, boolean isDoubleSided, double scaleFixed) throws XMLStreamException {
        String pathData = isDoubleSided ?
                RendererUtil.getCircleCoordsChainReverse(new Point(0, 0), 0.98 * scaleFixed) :
                RendererUtil.getCircleCoordsChain(0.95 * scaleFixed);
        RendererUtil.renderPath(writer, pathData, "dialMonthsLabelMajorPath", null);
    }

    private static void renderDefsDialMonthsLabelMinorPath(XMLStreamWriter writer, boolean isDoubleSided, double scaleFixed) throws XMLStreamException {
        String pathData = isDoubleSided ?
                RendererUtil.getCircleCoordsChainReverse(new Point(0, 0), 0.935 * scaleFixed) :
                RendererUtil.getCircleCoordsChain(0.92 * scaleFixed);
        RendererUtil.renderPath(writer, pathData, "dialMonthsLabelMinorPath", null);
    }

    private static void renderDefsCoordLabelPaths(XMLStreamWriter writer, Options options, double scale) throws XMLStreamException {

        for (double Dec = 60; Dec >= Math.abs(options.getLatitude()) - 90; Dec = Dec - 30) {
            StringBuilder path = new StringBuilder("M");
            for (double RA = 24; RA >= 0; RA = RA - 0.5) {
                if (RA < 24) {
                    path.append("L");
                }
                double finalRA = (options.getLatitude() >= 0) ? RA : 24 - RA;
                Point point = CoordUtil.convert(finalRA, options.getLatitudeSign() * Dec, options.getLatitude(), scale);
                path.append(RendererUtil.getCoordsChunk(point));
            }
            RendererUtil.renderPath(writer, path.toString(), "coordLabelPath" + (int) Dec, null);
        }

        // slightly shifted for RA labels
        StringBuilder path = new StringBuilder("M");
        for (double RA = 24; RA >= 0; RA = RA - 0.5) {
            if (RA < 24) {
                path.append("L");
            }
            double finalRA = (options.getLatitude() >= 0) ? RA : 24 - RA;
            Point point = CoordUtil.convert(finalRA, -options.getLatitudeSign() * 3, options.getLatitude(), scale);
            path.append(RendererUtil.getCoordsChunk(point));
        }

        RendererUtil.renderPath(writer, path.toString(), "coordLabelPath00", null);
    }

    private static void renderMapBackground(XMLStreamWriter writer, double scaleFixed) throws XMLStreamException {
        RendererUtil.renderPath(writer, RendererUtil.getCircleCoordsChain(1.0 * scaleFixed), null, "mapBackground");
    }

    private static void renderDialMonths(XMLStreamWriter writer, Options options) throws XMLStreamException {

        String[] monthNames = getMonthNames(options);
        int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int daysInYear = 365;

        GregorianCalendar calendar = new GregorianCalendar();
        if (calendar.isLeapYear(Calendar.getInstance().get(Calendar.YEAR))) {
            daysInMonth[1] = 29;
            daysInYear = 366;
        }

        double sign = options.isDoubleSided() ? - options.getDoubleSidedSign() : options.getLatitudeSign();
        double angleInPercent;

        double dayIncrement = 360.0 / daysInYear;
        double percent = 100 / 360.0;
        double startAngle = (daysInMonth[0] + daysInMonth[1] + 21) * dayIncrement;

        double angle = 90 - startAngle;
        for (int month = 0; month < 12; month++) {
            String monthName = monthNames[month];
            angleInPercent = getNormalizedPercent(percent * sign * (angle + daysInMonth[month] / 2));
            angle = angle + daysInMonth[month] * dayIncrement;
            // december
            if (month == 11) {
                RendererUtil.writeGroupStart(writer, null);
                writer.writeAttribute("transform", "rotate(180)");
                RendererUtil.renderTextOnPath(writer, "dialMonthsLabelMajorPath", getNormalizedPercent(angleInPercent - 50), monthName, "dialMonthsLabelMajor");
                RendererUtil.writeGroupEnd(writer);
            } else {
                RendererUtil.renderTextOnPath(writer, "dialMonthsLabelMajorPath", angleInPercent, monthName, "dialMonthsLabelMajor");
            }
        }

        angle = 90 - startAngle;
        for (int month = 0; month < 12; month++) {
            for (int day = 0; day < daysInMonth[month]; day++) {
                angleInPercent = getNormalizedPercent(percent * sign * angle);
                angle = angle + dayIncrement;
                if (day != 0 && day % 5 == 0) {
                    if (month == 11) {
                        RendererUtil.writeGroupStart(writer, null);
                        writer.writeAttribute("transform", "rotate(180)");
                        RendererUtil.renderTextOnPath(writer, "dialMonthsLabelMinorPath", getNormalizedPercent(angleInPercent - 50), String.valueOf(day), "dialMonthsLabelMinor");
                        RendererUtil.writeGroupEnd(writer);
                    } else {
                        RendererUtil.renderTextOnPath(writer, "dialMonthsLabelMinorPath", angleInPercent, String.valueOf(day), "dialMonthsLabelMinor");
                    }
                }
            }
        }
    }

    private static String[] getMonthNames(Options options) {

        String[] monthNames = new String[12];
        String[] monthNamesEn = new DateFormatSymbols(Locale.ENGLISH).getMonths();

        for (int i = 0; i < 12; i++) {
            monthNames[i] = options.getLocalizationUtil().getValue(monthNamesEn[i].toLowerCase(Locale.ENGLISH));
        }

        return monthNames;
    }

    private static void renderDialMonthsTicks(XMLStreamWriter writer, Options options, double scaleFixed) throws XMLStreamException {

        int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int daysInYear = 365;

        GregorianCalendar calendar = new GregorianCalendar();
        if (calendar.isLeapYear(Calendar.getInstance().get(Calendar.YEAR))) {
            daysInMonth[1] = 29;
            daysInYear = 366;
        }

        int sign = options.isDoubleSided() ? options.getDoubleSidedSign() : options.getLatitudeSign();
        double angleInRads;

        double dayIncrement = 360.0 / daysInYear;
        double startAngle = (daysInMonth[0] + daysInMonth[1] + 21) * dayIncrement;

        double angle = startAngle + (sign < 0 ? 180 : 0);

        for (int month = 0; month < 12; month++) {

            StringBuilder pathData = new StringBuilder();
            for (int day = 0; day < daysInMonth[month]; day++) {
                angleInRads = Math.toRadians(sign * angle);
                angle = angle - dayIncrement;
                switch (day) {
                    case 0:
                        pathData.append(getDialMonthsTickPathData(angleInRads, 1.0, scaleFixed));
                        break;
                    case 5:
                    case 10:
                    case 15:
                    case 20:
                    case 25:
                    case 30:
                        pathData.append(getDialMonthsTickPathData(angleInRads, 0.914, scaleFixed));
                        break;
                    default:
                        pathData.append(getDialMonthsTickPathData(angleInRads, 0.908, scaleFixed));
                }
            }
            RendererUtil.renderPath(writer, pathData.toString(), null, "dialMonthsTick");
        }
    }

    private static String getDialMonthsTickPathData(double angle, double radius, double scaleFixed) {
        return "M" + Number.format(Math.cos(angle) * 0.89 * scaleFixed)
                + " " + Number.format(-Math.sin(angle) * 0.89 * scaleFixed)
                + "L" + Number.format(Math.cos(angle) * radius * scaleFixed)
                + " " + Number.format(-Math.sin(angle) * radius * scaleFixed);
    }

    private static void renderMilkyWay(XMLStreamWriter writer, RenderingContext renderingContext, double scale) throws XMLStreamException, IOException {

        double latitude = renderingContext.getOptions().getLatitude();
        MilkyWay milkyWay = renderingContext.getCacheHandler().getMilkyWay();
        Poly sourcePolygon = new PolyDefault();
        Poly destPolygon = new PolyDefault();

        destPolygon.add(createClipArea(scale, latitude));

        sourcePolygon.add(createContour(scale, milkyWay.getDarkNorth(), latitude));
        sourcePolygon.add(createContour(scale, milkyWay.getDarkSouth(), latitude));

        renderMilkyWay(writer, sourcePolygon.intersection(destPolygon), "milkyWayBright");

        sourcePolygon.add(createContour(scale, milkyWay.getBrightNorth(), latitude));
        sourcePolygon.add(createContour(scale, milkyWay.getBrightSouth(), latitude));

        renderMilkyWay(writer, sourcePolygon.intersection(destPolygon), "milkyWayDark");
    }

    private static void renderMilkyWay(XMLStreamWriter writer, Poly polygon, String style) throws XMLStreamException {
        StringBuilder pathData = new StringBuilder();
        for (int i = 0; i < polygon.getNumInnerPoly(); i++) {
            Poly contour = polygon.getInnerPoly(i);
            for (int j = 0; j < contour.getNumPoints(); j++) {
                if (j == 0) {
                    pathData.append("M");
                } else {
                    pathData.append("L");
                }
                pathData.append(Number.format(contour.getX(j)));
                pathData.append(" ");
                pathData.append(Number.format(contour.getY(j)));
            }
        }
        RendererUtil.renderPath(writer, pathData.toString(), null, style);
    }

    private static void renderCoords(XMLStreamWriter writer, Options options, double scale) throws XMLStreamException {

        StringBuilder path = new StringBuilder();
        Point point;

        // declination circle (it cannot be rendered using circles because of the rotation at vernal point)
        for (double Dec = 60; Dec >= Math.abs(options.getLatitude()) - 90; Dec = Dec - 30) {
            for (double RA = 0; RA <= 24; RA = RA + 0.5) {
                point = CoordUtil.convert(RA, options.getLatitudeSign() * Dec, options.getLatitude(), scale);
                if (RA == 0) {
                    path.append("M");
                } else {
                    path.append("L");
                }
                path.append(RendererUtil.getCoordsChunk(point));
            }
        }

        // RA
        double start;
        for (int RA = 0; RA < 24; RA++) {
            switch (RA % 6) {
                case 1:
                case 3:
                case 5:
                    start = 30;
                    break;
                case 2:
                case 4:
                    start = 60;
                    break;
                default:
                    start = 90;
            }

            point = CoordUtil.convert(RA, options.getLatitudeSign() * start, options.getLatitude(), scale);
            path.append("M");
            path.append(RendererUtil.getCoordsChunk(point));
            point = CoordUtil.convert(RA, options.getLatitude() - options.getLatitudeSign() * 90, options.getLatitude(), scale);
            path.append("L");
            path.append(RendererUtil.getCoordsChunk(point));
        }
        RendererUtil.renderPath(writer, path.toString(), null, "coords");
    }

    private static void renderCoordLabels(XMLStreamWriter writer, Options options) throws XMLStreamException {

        for (int RA = 1; RA < 24; RA++) {
            int finalRA = (options.getLatitude() >= 0) ? RA : 24 - RA;
            RendererUtil.renderTextOnPath(writer, "coordLabelPath00", 100 - (RA * 100.0 / 24), finalRA + "h", "coordLabelRa");
        }
        RendererUtil.renderTextOnPath(writer, "coordLabelPath00", 0, "0h", "coordLabelRa");

        for (double Dec = 60; Dec >= Math.abs(options.getLatitude()) - 90; Dec = Dec - 30) {
            double finalDec = options.getLatitudeSign() * Dec;
            String pathId = "coordLabelPath" + (int) Dec;
            String strSign = (finalDec > 0) ? "+" : "";
            RendererUtil.renderTextOnPath(writer, pathId, 100, strSign + (int) finalDec + "°", "coordLabelDec");
            for (int i = 1; i < 4; i++) {
                RendererUtil.renderTextOnPath(writer, pathId, i * 25, strSign + (int) finalDec + "°", "coordLabelDec");
            }
        }
    }

    private static void renderEcliptic(XMLStreamWriter writer, Options options, double scale) throws XMLStreamException {

        double epsilon = Math.toRadians(23.44);
        boolean flag = false;
        String coordsChunk = "";
        StringBuilder pathData = new StringBuilder();

        for (int i = 0; i <= 360; i = i + 2) {
            double lambda = Math.toRadians(i);
            double RA = (Math.atan2(Math.sin(lambda) * Math.cos(epsilon), Math.cos(lambda))) * 12 / Math.PI;
            double Dec = Math.toDegrees(Math.asin(Math.sin(epsilon) * Math.sin(lambda)));
            Point point = CoordUtil.convert(RA, Dec, options.getLatitude(), scale);
            if (point != null) {
                if (!flag) {
                    coordsChunk = RendererUtil.getCoordsChunk(point);
                    flag = true;
                } else {
                    pathData.append("M");
                    pathData.append(coordsChunk);
                    pathData.append("L");
                    coordsChunk = RendererUtil.getCoordsChunk(point);
                    pathData.append(coordsChunk);
                }
            } else {
                flag = false;
            }
        }
        RendererUtil.renderPath(writer, pathData.toString(), null, "ecliptic");
    }

    private static void renderConstellationBoundaries(XMLStreamWriter writer, RenderingContext renderingContext, double scale) throws XMLStreamException, IOException {

        StringBuilder pathData = new StringBuilder();
        Options options = renderingContext.getOptions();

        for (Iterator<Point> i = renderingContext.getCacheHandler().getConstellationBoundaryList().iterator(); i.hasNext();) {
            Point pointStartRaw = i.next();
            Point pointStart = CoordUtil.convert(pointStartRaw.getX(), pointStartRaw.getY(), options.getLatitude(), scale);
            if (pointStart != null) {
                Point pointEndRaw = i.next();
                Point pointEnd = CoordUtil.convert(pointEndRaw.getX(), pointEndRaw.getY(), options.getLatitude(), scale);
                if (pointEnd != null) {
                    if ((Math.abs(pointStartRaw.getY() - pointEndRaw.getY()) < 0.7) && (Math.abs(pointStartRaw.getX() - pointEndRaw.getX()) > 0)
                            || (Math.abs(pointStartRaw.getY()) > 86)) {

                        int bDiv = (int) (5 + (15 * ((options.getLatitude() > 0) ? (90 - pointEndRaw.getY()) : (90 + pointEndRaw.getY())) / 90));

                        double startRA = pointStartRaw.getX();
                        double endRA = pointEndRaw.getX();
                        double incRA = endRA - startRA;

                        if (incRA > 12) {
                            startRA = pointEndRaw.getX();
                            incRA = 24 - incRA;
                        }
                        if (incRA < -12) {
                            incRA = incRA + 24;
                        }

                        double incDec = (pointEndRaw.getY() - pointStartRaw.getY()) / bDiv;
                        double Dec = pointStartRaw.getY();

                        for (int j = 0; j <= bDiv; j++) {
                            Point pointTemp = CoordUtil.convert(startRA + j * incRA / bDiv, Dec, options.getLatitude(), scale);
                            if (j == 0) {
                                pathData.append("M");

                            } else {
                                pathData.append("L");
                            }
                            pathData.append(RendererUtil.getCoordsChunk(pointTemp));
                            Dec = Dec + incDec;
                        }
                    } else {
                        pathData.append("M");
                        pathData.append(RendererUtil.getCoordsChunk(pointStart));
                        pathData.append("L");
                        pathData.append(RendererUtil.getCoordsChunk(pointEnd));
                    }
                }
            } else {
                i.next();
            }
        }
        RendererUtil.renderPath(writer, pathData.toString(), null, "constellationBoundaries");
    }

    private static void renderConstellationLines(XMLStreamWriter writer, RenderingContext renderingContxt, double scale) throws XMLStreamException, IOException {

        StringBuilder pathData = new StringBuilder();

        double latitude = renderingContxt.getOptions().getLatitude();

        for (Iterator<Point> i = renderingContxt.getCacheHandler().getConstellationLineList().iterator(); i.hasNext();) {
            Point pointStartRaw = i.next();
            Point pointStart = CoordUtil.convert(pointStartRaw.getX(), pointStartRaw.getY(), latitude, scale);
            if (pointStart != null) {
                Point pointEndRaw = i.next();
                Point pointEnd = CoordUtil.convert(pointEndRaw.getX(), pointEndRaw.getY(), latitude, scale);
                if (pointEnd != null) {
                    pathData.append("M");
                    pathData.append(RendererUtil.getCoordsChunk(pointStart));
                    pathData.append("L");
                    pathData.append(RendererUtil.getCoordsChunk(pointEnd));
                }
            } else {
                i.next();
            }
        }
        RendererUtil.renderPath(writer, pathData.toString(), null, "constellationLines");
    }

    private static void renderConstellationNames(XMLStreamWriter writer, RenderingContext renderingContext, double scale) throws XMLStreamException, IOException {

        Options options = renderingContext.getOptions();

        for (ConstellationName constellationName : renderingContext.getCacheHandler().getConstellationNameList()) {
            String name = "";
            switch (options.getConstellationLabelsMode()) {
                case 0:
                    name = options.getLocalizationUtil().getValue(constellationName.getId());
                    break;
                case 1:
                    name = constellationName.getLatinName();
                    break;
                case 2:
                    name = constellationName.getAbbreviation();
                    break;
                default:
            }

            Point pointRaw = constellationName.getPoint();
            Point point = CoordUtil.convert(pointRaw.getX(), pointRaw.getY(), options.getLatitude(), scale);
            if (point != null) {
                String id = "con" + constellationName.getAbbreviation();
                RendererUtil.renderPath(writer, getCirclePathDataForConstellationName(point), id, "constellationNamesPath");
                RendererUtil.renderTextOnPath(writer, id, 50d, name, "constellationNames");
            }
        }
    }

    private static void renderStars(XMLStreamWriter writer, RenderingContext renderingContext, double scale) throws XMLStreamException, IOException {

        Map<Integer, StringBuilder> pathMap = new HashMap<>();
        Options options = renderingContext.getOptions();

        for (Star star : renderingContext.getCacheHandler().getStarList()) {
            Point point = CoordUtil.convert(star.getRA(), star.getDec(), options.getLatitude(), scale);
            if (point != null) {
                String coordsChunk = RendererUtil.getCoordsChunk(point);

                StringBuilder path = new StringBuilder();
                path.append("M");
                path.append(coordsChunk);
                path.append("L");
                path.append(coordsChunk);

                int magnitudeIndex = Math.round((float) star.getMag() + 1);
                if (pathMap.containsKey(magnitudeIndex)) {
                    pathMap.get(magnitudeIndex).append(path);
                } else {
                    pathMap.put(magnitudeIndex, path);
                }
            }
        }
        for (Map.Entry<Integer, StringBuilder> entry : pathMap.entrySet()) {
            int magnitude = entry.getKey();
            if (!(!options.hasAllVisibleStars() && magnitude > 5)) {
                RendererUtil.renderPath(writer, entry.getValue().toString(), null, "star level" + magnitude);
            }
        }
    }

    private static void renderMapAreaBorder(XMLStreamWriter writer) throws XMLStreamException {
        RendererUtil.renderDefsInstance(writer, "mapArea", 0, 0, null, "mapAreaBorder");
    }

    private static void renderCardinalPointsTicks(XMLStreamWriter writer, List<CardinalPoint> cardinalPointList) throws XMLStreamException {
        StringBuilder path = new StringBuilder();
        for (CardinalPoint cardinalPoint : cardinalPointList) {
            if (cardinalPoint != null) {
                path.append("M");
                path.append(RendererUtil.getCoordsChunk(cardinalPoint.getTickStart()));
                path.append("L");
                path.append(RendererUtil.getCoordsChunk(cardinalPoint.getTickEnd()));
            }
        }
        RendererUtil.renderPath(writer, path.toString(), null, "cardinalPointTick");
    }

    private static void renderCardinalPointsLabels(XMLStreamWriter writer, Options options, double scale, List<CardinalPoint> cardinalPointList) throws XMLStreamException {

        double gap = scale * 0.006;
        double tickLength = scale * 0.03;
        double letterHeight = scale * 0.032;
        double textLineLength = scale * 0.25;
        double outerOffset = tickLength + gap;
        double innerOffset = outerOffset + letterHeight;

        if (options.isDoubleSided()) {

            int index = 0;
            for (CardinalPoint cardinalPoint : cardinalPointList) {
                if (cardinalPoint != null) {

                    String pathId = "cid" + index;

                    if (options.getLatitudeFixed() == 0) {
                        Point point = cardinalPoint.getTickStart();
                        Point center = new Point(point.getX(), point.getY() + innerOffset);
                        RendererUtil.renderPath(writer, RendererUtil.getLineHorizontalPathData(center, textLineLength), pathId, "invisible");
                        RendererUtil.renderTextOnPath(writer, pathId, 50, cardinalPoint.getLabel(), "cardinalPointLabel");

                    } else {
                        if (options.getLatitudeFixed() * options.getDoubleSidedSign() > 0) {
                            RendererUtil.renderPath(writer, RendererUtil.getCircleCoordsChainReverse(cardinalPoint.getCenter(), cardinalPoint.getRadius() + innerOffset), pathId, "invisible");
                            RendererUtil.renderTextOnPath(writer, pathId, 100 - cardinalPoint.getStartOffset(), cardinalPoint.getLabel(), "cardinalPointLabel");
                        } else {
                            RendererUtil.renderPath(writer, RendererUtil.getCircleCoordsChain(cardinalPoint.getCenter(), cardinalPoint.getRadius() - innerOffset), pathId, "invisible");
                            if (index == 2) {
                                RendererUtil.writeGroupStart(writer, null);
                                writer.writeAttribute("transform", "rotate(180, " + cardinalPoint.getCenter().getX() + "," + cardinalPoint.getCenter().getY() + ")");
                                RendererUtil.renderTextOnPath(writer, pathId, 50, cardinalPoint.getLabel(), "cardinalPointLabel");
                                RendererUtil.writeGroupEnd(writer);
                            } else {
                                RendererUtil.renderTextOnPath(writer, pathId, getNormalizedPercent(cardinalPoint.getStartOffset() - 50.0), cardinalPoint.getLabel(), "cardinalPointLabel");
                            }
                        }
                    }
                }
                index++;
            }

        } else {

            int index = 0;

            for (CardinalPoint cardinalPoint : cardinalPointList) {

                if (cardinalPoint != null) {

                    String pathId = "cid" + index;
                    boolean isInner = index > 0 && index < 4;

                    if (isInner) {
                        RendererUtil.renderPath(writer, RendererUtil.getCircleCoordsChainReverse(cardinalPoint.getCenter(), cardinalPoint.getRadius() + innerOffset), pathId, "invisible");
                        RendererUtil.renderTextOnPath(writer, pathId, 100 - cardinalPoint.getStartOffset(), cardinalPoint.getLabel(), "cardinalPointLabel");

                    } else {
                        RendererUtil.renderPath(writer, RendererUtil.getCircleCoordsChain(cardinalPoint.getCenter(), cardinalPoint.getRadius() + outerOffset), pathId, "invisible");
                        if (index == 6) {
                            RendererUtil.writeGroupStart(writer, null);
                            writer.writeAttribute("transform", "rotate(180, " + cardinalPoint.getCenter().getX() + "," + cardinalPoint.getCenter().getY() + ")");
                            RendererUtil.renderTextOnPath(writer, pathId, 50, cardinalPoint.getLabel(), "cardinalPointLabel");
                            RendererUtil.writeGroupEnd(writer);
                        } else {
                            RendererUtil.renderTextOnPath(writer, pathId, cardinalPoint.getStartOffset(), cardinalPoint.getLabel(), "cardinalPointLabel");
                        }
                    }
                }
                index++;
            }
        }
    }

    private static void renderDialHours(XMLStreamWriter writer, RenderingContext renderingContext, double scaleFixed, Map<String, byte[]> paramMap, Boolean isDayLightSavingTimeScale) throws XMLStreamException {

        Options options = renderingContext.getOptions();
        double latitude = options.getLatitude();
        boolean isDoubleSided = options.isDoubleSided();
        byte[] markMajor = isDoubleSided ? paramMap.get("dialHoursMarkerMajorDouble") : paramMap.get("dialHoursMarkerMajorSingle");
        byte[] markMinor = isDoubleSided ? paramMap.get("dialHoursMarkerMinorDouble") : paramMap.get("dialHoursMarkerMinorSingle");

        int rangeMajor = 8;
        int rangeMinor = 7;
        double latitudeAbs = Math.abs(latitude);
        double shiftAngle = isDoubleSided ? 180 : 0;

        if (latitudeAbs > 76) {
            rangeMajor = 5;
        } else if (latitudeAbs > 70) {
            rangeMajor = 6;
            rangeMinor = 5;
        } else if (latitudeAbs > 60) {
            rangeMajor = 7;
            rangeMinor = 5;
        } else if (!isDoubleSided) {
            rangeMajor = 7;
        }

        // default labels
        int hour;

        for (int i = -rangeMajor; i <= rangeMajor; i++) {
            hour = latitude < 0 ? i : -i;
            if (hour < 0) {
                hour = hour + 24;
            }
            renderDialHoursMarker(writer, renderingContext, scaleFixed, markMajor, String.valueOf(hour), i * 15 + shiftAngle, "dialHoursMarkerMajor");
        }

        // summer time labels
        if (isDayLightSavingTimeScale && latitudeAbs <= 75) {
            for (int i = -rangeMinor; i <= rangeMinor; i++) {
                hour = latitude < 0 ? i + 1 : -i + 1;
                if (hour < 0) {
                    hour = hour + 24;
                }
                if (hour > 23) {
                    hour = hour - 24;
                }
                renderDialHoursMarker(writer, renderingContext, scaleFixed, markMinor, String.valueOf(hour), i * 15 + shiftAngle, "dialHoursMarkerMinor");
            }
        }

        for (double i = 112.5; i >= -120; i = i - 15) {
            String strTranslate = Number.format(0.9 * scaleFixed);
            RendererUtil.renderDefsInstance(writer, "dialHoursMarkerHalf", 0d, 0d, "translate(0,-" + strTranslate + ") rotate(" + (i + shiftAngle) + ",0," + strTranslate + ")", null);
        }
    }

    private static void renderDialHoursMarker(XMLStreamWriter writer, RenderingContext renderingContext, double scaleFixed, byte[] mark, String replacement, double angle, String style) throws XMLStreamException {
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("#", replacement);
        String strTranslate = Number.format(0.9 * scaleFixed);
        writer.writeStartElement("g");
        writer.writeAttribute("class", style);
        writer.writeAttribute("transform", "translate(0,-" + strTranslate + ") rotate(" + angle + ",0," + strTranslate + ")");
        RendererUtil.writeStreamContent(renderingContext.getInputFactory(), writer, new ByteArrayInputStream(mark), replacementMap, renderingContext.getOptions().getLocalizationUtil());
        writer.writeEndElement();
    }

    private static void renderMonthsAreaBorder(XMLStreamWriter writer) throws XMLStreamException {
        RendererUtil.renderDefsInstance(writer, "monthsArea", 0, 0, null, "monthsAreaBorder");
    }

    private static void renderCover(XMLStreamWriter writer, Options options, double scaleFixed) throws XMLStreamException {

        boolean isDoubleSided = options.isDoubleSided();

        if (isDoubleSided) {
            renderCoverDoubleSided(writer, scaleFixed);
        } else {
            renderCoverSingleSided(writer, scaleFixed);
        }

        renderPinMark(writer, scaleFixed, true, isDoubleSided);
        renderBendLine(writer, scaleFixed, isDoubleSided);
    }

    private static void renderCoverSingleSided(XMLStreamWriter writer, double scaleFixed) throws XMLStreamException {

        StringBuilder pathData = new StringBuilder();

        double x1 = Math.cos(Math.toRadians(30)) * 0.9 * scaleFixed;
        double y1 = Math.sin(Math.toRadians(30)) * 0.9 * scaleFixed;
        // main arc
        pathData.append("M");
        pathData.append(Number.format(-x1));
        pathData.append(" ");
        pathData.append(Number.format(y1));
        pathData.append("A");
        // rx
        pathData.append(Number.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(Number.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 1 ");
        pathData.append(Number.format(x1));
        pathData.append(" ");
        pathData.append(Number.format(y1));

        // joiners
        double y2 = Math.tan(Math.toRadians(30)) * scaleFixed;
        double height = 1.12 * scaleFixed - y2;
        double y3 = y2 + 2 * height;
        double y4 = y3 + (y2 - y1);
        double x5 = Math.cos(Math.toRadians(340)) * 0.9 * scaleFixed;
        double y5 = 2.24 * scaleFixed + Math.sin(Math.toRadians(20)) * 0.9 * scaleFixed;

        // right side
        pathData.append("L");
        pathData.append(Number.format(scaleFixed));
        pathData.append(" ");
        pathData.append(Number.format(y2));
        pathData.append("L");
        pathData.append(Number.format(scaleFixed));
        pathData.append(" ");
        pathData.append(Number.format(y3));
        pathData.append("L");
        pathData.append(Number.format(x1));
        pathData.append(" ");
        pathData.append(Number.format(y4));
        pathData.append("A");
        // rx
        pathData.append(Number.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(Number.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 0 1 ");
        pathData.append(Number.format(x5));
        pathData.append(" ");
        pathData.append(Number.format(y5));

        // left side
        pathData.append("L");
        pathData.append(Number.format(-x5));
        pathData.append(" ");
        pathData.append(Number.format(y5));
        pathData.append("A");
        // rx
        pathData.append(Number.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(Number.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 0 1 ");
        pathData.append(Number.format(-x1));
        pathData.append(" ");
        pathData.append(Number.format(y4));
        pathData.append("L");
        pathData.append(Number.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(Number.format(y3));
        pathData.append("L");
        pathData.append(Number.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(Number.format(y2));
        pathData.append("L");
        pathData.append(Number.format(-x1));
        pathData.append(" ");
        pathData.append(Number.format(y1));
        pathData.append("Z");

        RendererUtil.renderPath(writer, pathData.toString(), "cover", "cover");
    }

    private static void renderCoverDoubleSided(XMLStreamWriter writer, double scaleFixed) throws XMLStreamException {

        StringBuilder pathData = new StringBuilder();

        double x1 = Math.cos(Math.toRadians(30.0)) * 0.9 * scaleFixed;
        double y1 = -Math.sin(Math.toRadians(30.0)) * 0.9 * scaleFixed;
        double y2 = -Math.tan(Math.toRadians(30.0)) * scaleFixed;

        // right side
        pathData.append("M");
        pathData.append(Number.format(scaleFixed));
        pathData.append(" ");
        pathData.append(Number.format(-1.12 * scaleFixed));
        pathData.append("V");
        pathData.append(Number.format(y2));
        pathData.append("L");
        pathData.append(Number.format(x1));
        pathData.append(" ");
        pathData.append(Number.format(y1));

        // main arc
        pathData.append("A");
        // rx
        pathData.append(Number.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(Number.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 1 ");
        pathData.append(Number.format(-x1));
        pathData.append(" ");
        pathData.append(Number.format(y1));

        // left side
        pathData.append("L");
        pathData.append(Number.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(Number.format(y2));
        pathData.append("V");
        pathData.append(Number.format(-1.12 * scaleFixed));
        pathData.append("z");

        RendererUtil.renderPath(writer, pathData.toString(), "cover", "cover");
    }

    private static void renderBendLine(XMLStreamWriter writer, double scaleFixed, boolean isDoubleSided) throws XMLStreamException {

        StringBuilder pathData = new StringBuilder();

        double y = 1.12 * scaleFixed;
        if (isDoubleSided) {
            y = -y;
        }

        pathData.append("M");
        pathData.append(Number.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(Number.format(y));
        pathData.append("L");
        pathData.append(Number.format(scaleFixed));
        pathData.append(" ");
        pathData.append(Number.format(y));

        RendererUtil.renderPath(writer, pathData.toString(), "bendLine", "bendLine");
    }

    private static void renderSpacer(XMLStreamWriter writer, double scaleFixed) throws XMLStreamException {

        StringBuilder pathData = new StringBuilder();

        double angle = Math.toRadians(210.0);
        double ratio = 1.03 * scaleFixed;
        String strRadius = Number.format(ratio);

        // main arc
        pathData.append("M");
        pathData.append(Number.format(Math.cos(angle) * ratio));
        pathData.append(" ");
        pathData.append(Number.format(-Math.sin(angle) * ratio));
        pathData.append("A");
        // rx
        pathData.append(strRadius);
        pathData.append(" ");
        // ry
        pathData.append(strRadius);
        // rotation, large arc flag, sweep flag
        angle = Math.toRadians(330.0);
        pathData.append(" 0 0 0 ");
        pathData.append(Number.format(Math.cos(angle) * ratio));
        pathData.append(" ");
        pathData.append(Number.format(-Math.sin(angle) * ratio));

        List<Point> coordList = new ArrayList<>();
        double dy = Math.tan(Math.toRadians(30.0)) * scaleFixed;
        coordList.add(new Point(scaleFixed, dy));
        coordList.add(new Point(scaleFixed, 1.12 * scaleFixed));
        coordList.add(new Point(-scaleFixed, 1.12 * scaleFixed));
        coordList.add(new Point(-scaleFixed, dy));

        RendererUtil.renderPath(writer, pathData + RendererUtil.getPathData(coordList, true), "spacer", null);
    }

    private static void renderPinMark(XMLStreamWriter writer, double scaleFixed, boolean isCover, boolean isDoubleSided) throws XMLStreamException {
        StringBuilder pathData = new StringBuilder();
        double size = 0.02 * scaleFixed;
        double dy = 0d;
        if (isCover && !isDoubleSided) {
            dy = 2.24 * scaleFixed;
        }
        pathData.append("M");
        pathData.append(Number.format(-size));
        pathData.append(" ");
        pathData.append(Number.format(dy));
        pathData.append("L");
        pathData.append(Number.format(size));
        pathData.append(" ");
        pathData.append(Number.format(dy));
        pathData.append("M 0 ");
        pathData.append(Number.format(dy - size));
        pathData.append("L 0 ");
        pathData.append(Number.format(dy + size));

        RendererUtil.renderPath(writer, pathData.toString(), "pinMark", "pinMark");
    }

    private static Poly createContour(double scale, List<Point> pointList, double latitude) {
        Poly contour = new PolyDefault();
        for (Point pointRaw : pointList) {
            Point point = CoordUtil.convertWithoutCheck(pointRaw.getX(), pointRaw.getY(), latitude, scale);
            contour.add(new Point2D(point.getX(), point.getY()));
        }
        return contour;
    }

    private static Poly createClipArea(double scale, double latitude) {
        Poly contour = new PolyDefault();
        double Dec = latitude > 0 ? latitude - 90 : latitude + 90;
        for (double RA = 0.0; RA <= 24; RA = RA + 0.5) {
            Point point = CoordUtil.convertWithoutCheck(RA, Dec, latitude, scale);
            contour.add(new Point2D(point.getX(), point.getY()));
        }
        return contour;
    }

    private static String getCirclePathDataForConstellationName(Point point) {
        double radius = point.getDistance(new Point(0, 0));
        double angle = 90 + Math.toDegrees(Math.atan2(point.getY(), point.getX()));
        return RendererUtil.getCircleCoordsChain(new Point(0, 0), radius, angle);
    }

    private static double getNormalizedPercent(double percent) {
        return (100 + percent % 100) % 100;
    }

}
