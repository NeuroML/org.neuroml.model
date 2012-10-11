package org.neuroml.model.test;

import java.io.File;

import org.junit.Test;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.Neuroml;
import org.neuroml.model.util.NeuroMLConverter;


public class NeuroML2Test
{
    
	@Test public void testCells() throws Exception
	{
			Neuroml nml2 = new Neuroml();

            IzhikevichCell iz1 = new IzhikevichCell();
            nml2.getIzhikevichCell().add(iz1);

            ExpTwoSynapse e2syn = new ExpTwoSynapse();
            e2syn.setTauRise("2ms");
            e2syn.setTauDecay("12ms");
            e2syn.setErev("-10mV");
            e2syn.setGbase("1nS");
            nml2.getExpTwoSynapse().add(e2syn);

    		String wdir = System.getProperty("user.dir");
    		String tempdir = wdir + File.separator + "src/test/resources/tmp";
            NeuroMLConverter conv = new NeuroMLConverter();
            String tempFile = tempdir + File.separator + "nml2.xml";
            conv.neuroml2ToXml(nml2, tempFile);

            System.out.println("Saved to: "+ tempFile);
            System.out.println("Done NeuroML2Test!");
    }
	

}
