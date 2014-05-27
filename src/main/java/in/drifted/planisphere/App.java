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
package in.drifted.planisphere;

import in.drifted.planisphere.renderer.html.HtmlRenderer;
import in.drifted.planisphere.renderer.svg.SvgRenderer;
import in.drifted.planisphere.util.CommandLineUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {

    public static void main(String[] args) throws IOException {

        if (args.length == 2) {

            Path optionsPath = Paths.get(args[0]);
            Path outputPath = Paths.get(args[1]);
            Options options = new Options();
            
            try {
                 options = CommandLineUtil.getOptions(optionsPath);
                 
            } catch (IOException e) {
                System.out.println("The options config file couldn't be found: " + optionsPath.toString());
                System.out.println("Using default options instead.");
            }
            
            new HtmlRenderer(new SvgRenderer()).createFromTemplate(options, outputPath);
            
            System.out.println("The file has been created successfully in the path: " + outputPath.toString());
            
        } else {
            System.out.println(CommandLineUtil.getUsage());
        }
    }

}
