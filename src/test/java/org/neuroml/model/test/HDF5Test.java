
package org.neuroml.model.test;

import java.io.File;

import junit.framework.TestCase;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.hdf5.NeuroMLHDF5Writer;

import org.junit.Test;
import org.neuroml.model.util.hdf5.NeuroMLHDF5Reader;

public class HDF5Test extends TestCase
{
    
    static String wdir = System.getProperty("user.dir");
    static String exampledirname = wdir + File.separator + "src/test/resources/examples";
    
    @Test
    public void testWrite() throws Exception {
        System.out.println("---  Testing local examples... ");
        File exdir = new File(exampledirname);
        NeuroMLConverter neuromlConverter = new NeuroMLConverter();
        for (File f : exdir.listFiles()) {
            if (f.getName().endsWith(".nml")) {
                String filepath = f.getAbsolutePath();
                System.out.println(">>      Trying to load: " + filepath);
                NeuroMLDocument nmlDoc = neuromlConverter.loadNeuroML(new File(filepath));
          
                File h5File = new File(filepath.replaceAll("examples", "tmp").replaceAll(".nml", ".nml.h5"));
                if (!h5File.getParentFile().exists())
                    h5File.getParentFile().mkdir();
                
                NeuroMLHDF5Writer.createNeuroMLH5file(nmlDoc, h5File);
                System.out.println(">>     Written "+f+" to "+h5File.getAbsolutePath());
            }
        }
    }    
    
    
    @Test
    public void testRead() throws Exception {
        System.out.println("---  Testing local examples... ");
        File exdir = new File(exampledirname);
        
        for (File f : exdir.listFiles()) {
            if (f.getName().endsWith("net.nml.h5")) {
                String filepath = f.getAbsolutePath();
                System.out.println("      Trying to load: " + filepath);
                
                NeuroMLHDF5Reader nmlReader = new NeuroMLHDF5Reader();
                //nmlReader.setVerbose(true);
                nmlReader.parse(f,false);
                
                System.out.println("File loaded: "+filepath+"\n"
                        +NeuroMLConverter.summary(nmlReader.getNeuroMLDocument()));
            }
        }
    }
    
    @Test
    public void testReadWrite() throws Exception {
        
        File exdir = new File(exampledirname);
        
        NeuroMLConverter neuromlConverter = new NeuroMLConverter();
        String[] tests = new String[]{"testnet.nml","MediumNet.net.nml"};
        
        for (String fn : tests) {
            File f = new File(exdir,fn);
            String filepath = f.getAbsolutePath();
            System.out.println("      Trying to load: " + filepath);

            NeuroMLDocument nmlDoc = neuromlConverter.loadNeuroML(new File(filepath));
            
            String summary0 = NeuroMLConverter.summary(nmlDoc);
            System.out.println("nmlDoc loaded: \n"+summary0);

            File h5File = new File(filepath.replaceAll("examples", "tmp").replaceAll(".nml", ".nml.h5"));
            if (!h5File.getParentFile().exists())
                h5File.getParentFile().mkdir();

            NeuroMLHDF5Writer.createNeuroMLH5file(nmlDoc, h5File);

            NeuroMLHDF5Reader nmlReader = new NeuroMLHDF5Reader();

            nmlReader.parse(h5File, false);
            String summary1 = NeuroMLConverter.summary(nmlReader.getNeuroMLDocument());

            System.out.println("File loaded: "+h5File+"\n"+summary1);

            assert(summary0.equals(summary1));
            
            System.out.println("Strings are equal!!");

        }
    }
    
    
    
    
}
