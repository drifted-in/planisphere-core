package in.drifted.planisphere;

import in.drifted.planisphere.renderer.html.HtmlRenderer;
import in.drifted.planisphere.renderer.svg.SvgRenderer;
import in.drifted.planisphere.util.CommandLineUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {

    public static void main(String[] args) throws IOException {

        if (args.length == 4) {

            Path optionsPath = Paths.get(args[2]);
            Path outputPath = Paths.get(args[3]);
            Options options = new Options();
            
            try {
                 options = CommandLineUtil.getOptions(optionsPath);
                 
            } catch (IOException e) {
                System.out.println("The options config file couldn't be found: " + optionsPath.toString());
                System.out.println("Using default options instead.");
            }
            
            new HtmlRenderer(new SvgRenderer()).createFromTemplate(args[0], args[1], options, outputPath);
            
            System.out.println("The file has been created successfully in the path: " + outputPath.toString());
            
        } else {
            System.out.println(CommandLineUtil.getUsage());
        }
    }

}
