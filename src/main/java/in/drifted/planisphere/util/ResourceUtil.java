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
package in.drifted.planisphere.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceUtil {

    public static Collection<String> getResourceCollection(Class clazz, String path) throws IOException {

        Collection<String> resourceCollection = new HashSet<>();

        URL dirUrl = clazz.getClassLoader().getResource(path);

        if (dirUrl == null) {

            try (
                    InputStream in = ResourceUtil.class.getResourceAsStream(path);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

                String line;
                while ((line = br.readLine()) != null) {
                    resourceCollection.add(line);
                }
            }

        } else {

            switch (dirUrl.getProtocol()) {

                case "file":
                    try {
                        resourceCollection.addAll(Arrays.asList(new File(dirUrl.toURI()).list()));

                    } catch (URISyntaxException e) {
                        throw new IOException(e);
                    }
                    break;

                case "jar":
                    String jarPath = dirUrl.getPath().substring(5, dirUrl.getPath().indexOf("!"));
                    JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        String name = "/" + entries.nextElement().getName();
                        if (name.startsWith(path)) {
                            resourceCollection.add(name.substring(path.length() + 1));
                        }
                    }
            }
        }

        return resourceCollection;
    }
}
