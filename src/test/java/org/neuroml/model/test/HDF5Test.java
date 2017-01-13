
package org.neuroml.model.test;

import java.io.File;
import java.net.URL;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.hdf5.NeuroMLHDF5Writer;

import org.junit.Test;

public class HDF5Test
{
    
    static String wdir = System.getProperty("user.dir");
    static String exampledirname = wdir + File.separator + "src/test/resources/examples";
    
    @Test
    public void testWrite() throws Exception {
        System.out.println("---  Testing local examples... ");
        File exdir = new File(exampledirname);
        NeuroMLConverter neuromlConverter = new NeuroMLConverter();
        for (File f : exdir.listFiles()) {
            if (f.getName().endsWith("net.nml")) {
                String filepath = f.getAbsolutePath();
                System.out.println("      Trying to load: " + filepath);
                NeuroMLDocument nmlDoc = neuromlConverter.loadNeuroML(new File(filepath));
          
                File h5File = new File(filepath.replaceAll("examples", "tmp").replaceAll(".nml", ".nml.h5"));
                
                //NeuroMLHDF5Writer.createNeuroMLH5file(nmlDoc, h5File);
                //System.out.println("Written "+f+" to "+h5File.getAbsolutePath());
            }
        }

    }
    
}
