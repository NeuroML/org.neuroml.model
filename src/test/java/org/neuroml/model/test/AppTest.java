
package org.neuroml.model.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import junit.framework.TestCase;
import org.neuroml.model.util.NeuroMLElements;

/**
 *
 * @author padraig
 */
public class AppTest extends TestCase {

    private boolean isInPom(String s) throws IOException{
        File pom = new File("pom.xml");
        InputStream ins = new FileInputStream(pom);
        InputStreamReader insr = new InputStreamReader(ins);
        BufferedReader fr = new BufferedReader(insr);

        StringBuilder sb = new StringBuilder();
        while (fr.ready()) {
            sb.append(fr.readLine().trim());
        }
        fr.close();
        String sdat = sb.toString();
        
        return sdat.indexOf(s)>0;
    }

    public void testVersions() throws IOException {
        System.out.println("Running a test on version usage, making all references to versions are: v" + NeuroMLElements.ORG_NEUROML_MODEL_VERSION + "...");


        String toFind = "<artifactId>org.neuroml.model</artifactId><version>"+NeuroMLElements.ORG_NEUROML_MODEL_VERSION+"</version>";
    
        assert(isInPom(toFind));

        toFind = "<build.version2>"+NeuroMLElements.LATEST_SCHEMA_VERSION+"</build.version2>";
    
        assert(isInPom(toFind));

        assert(NeuroMLElements.LATEST_SCHEMA_LOCATION.indexOf(NeuroMLElements.LATEST_SCHEMA_VERSION)>0);

    }

}
