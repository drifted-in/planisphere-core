/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package in.drifted.planisphere;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 * @author Honza
 */
public class Test {

    public Test() {
    }

    public void test1() throws Exception {
        String contextPath = "/in/drifted/planisphere/resources/templates/world_map.svg";
        InputStream reader = this.getClass().getResourceAsStream(contextPath);
        //Reader reader = new InputStreamReader(is, "UTF-8");
        while (reader.read() != -1) {
            System.out.write(reader.read());
        }
    }
}
