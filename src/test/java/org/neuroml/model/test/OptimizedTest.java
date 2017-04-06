
package org.neuroml.model.test;

import java.io.File;

import junit.framework.TestCase;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;

import org.junit.Test;
import org.neuroml.model.util.hdf5.NetworkHelper;

public class OptimizedTest extends TestCase
{
    
    static String wdir = System.getProperty("user.dir");
    static String exampledirname = wdir + File.separator + "src/test/resources/examples";
    
    
    @Test
    public void testReadWrite() throws Exception {
        
        File exdir = new File(exampledirname);
        
        NeuroMLConverter neuromlConverter = new NeuroMLConverter();
        String[] tests = new String[]{"testnet.nml","MediumNet.net.nml","complete.nml"};
        //tests = new String[]{"MediumNet.net.nml"};
        //tests = new String[]{"complete.nml"};
        
        for (String fn : tests) {
            File xmlFile = new File(exdir,fn);
            File h5File = new File(exdir,fn+".h5");
            
            System.out.println("      Trying to load: " + xmlFile.getAbsolutePath());

            NeuroMLDocument nmlDoc0 = neuromlConverter.loadNeuroML(xmlFile, true);
            String xmlString = neuromlConverter.neuroml2ToXml(nmlDoc0);

            NeuroMLConverter nmlConv = new NeuroMLConverter();
            NetworkHelper netHelper1 = nmlConv.loadNeuroMLOptimized(xmlFile);

            
            nmlConv = new NeuroMLConverter();
            NetworkHelper netHelper2 = nmlConv.loadNeuroMLOptimized(h5File);
            
            nmlConv = new NeuroMLConverter();
            NetworkHelper netHelper3 = nmlConv.loadNeuroMLOptimized(xmlString);

            NetworkHelper[] helpers = new NetworkHelper[]{netHelper2,netHelper3};
            
            for (String p: netHelper1.getPopulationIds())
            {
                int size = netHelper1.getPopulationSize(p);
                System.out.println("Pop: "+p+" has "+size+" cells");
                System.out.println("Location "+(size-1)+": "+netHelper1.getLocation(p, (size-1), false));
                
                for(NetworkHelper nh: helpers)
                {
                    System.out.println("Testing: "+nh+ " "+nh.getPopulationSize(p));
                    
                    assert netHelper1.getPopulationSize(p)==nh.getPopulationSize(p);
                    assert netHelper1.getPopulationComponent(p).equals(nh.getPopulationComponent(p));
                    assert netHelper1.getPopulationType(p).equals(nh.getPopulationType(p));
                    
                    if (netHelper1.populationHasPositions(p))
                    {
                        assert netHelper1.getLocation(p, 0, false).getX() == nh.getLocation(p, 0, false).getX();
                        assert netHelper1.getLocation(p, 0, false).getY() == nh.getLocation(p, 0, false).getY();
                        assert netHelper1.getLocation(p, 0, false).getZ() == nh.getLocation(p, 0, false).getZ();
                    }
                }
            }
            
            for (String p: netHelper1.getProjectionIds())
            {
                int size = netHelper1.getNumberConnections(p);
                String conn01 = size>0 ? NeuroMLConverter.connectionInfo(netHelper1.getConnection(p, 0)) : "NONE";
                System.out.println("Proj: "+p+" has "+size+" conns: "+conn01);
                
                for(NetworkHelper nh: helpers)
                {
                    assert netHelper1.getNumberConnections(p)==nh.getNumberConnections(p);
                    
                    if (netHelper1.getNumberConnections(p)>0)
                    {
                        String conn0i = NeuroMLConverter.connectionInfo(nh.getConnection(p, 0));
                        System.out.println("  conn: "+conn0i);
                        assert conn01.equals(conn0i);
                    }
                }
            }
            
        }
    }
    
    
    
    
}
