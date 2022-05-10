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
