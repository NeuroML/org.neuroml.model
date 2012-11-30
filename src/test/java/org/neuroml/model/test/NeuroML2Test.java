package org.neuroml.model.test;

import java.io.File;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.IaFCell;
import org.neuroml.model.IaFTauCell;
import org.neuroml.model.Instances;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.Morphology;
import org.neuroml.model.Network;
import org.neuroml.model.Neuroml;
import org.neuroml.model.Population;
import org.neuroml.model.util.NeuroMLConverter;

public class NeuroML2Test {

    @Test
    public void testCells() throws Exception {
        Neuroml nml2 = new Neuroml();
        nml2.setId("SomeCells");

        IzhikevichCell iz1 = new IzhikevichCell();
        iz1.setId("Izh0");
        iz1.setV0("-70mV");
        iz1.setThresh("30mV");
        iz1.setA("0.02");
        iz1.setB("0.2");
        iz1.setC("-50");
        iz1.setD("2");
        iz1.setIamp("2");
        iz1.setIdel("100 ms");
        iz1.setIdur("100 ms");
        nml2.getIzhikevichCell().add(iz1);

        ExpTwoSynapse e2syn = new ExpTwoSynapse();
        e2syn.setId("Syn0");
        e2syn.setTauRise("2ms");
        e2syn.setTauDecay("12ms");
        e2syn.setErev("-10mV");
        e2syn.setGbase("1nS");
        nml2.getExpTwoSynapse().add(e2syn);
        
        neuroml2ToXml(nml2, nml2.getId()+ ".xml");

    }
    
    private void neuroml2ToXml(Neuroml nml2, String name) throws Exception 
    {
        String wdir = System.getProperty("user.dir");
        String tempdir = wdir + File.separator + "src/test/resources/tmp";
        NeuroMLConverter conv = new NeuroMLConverter();
        String tempFile = tempdir + File.separator + name;
        conv.neuroml2ToXml(nml2, tempFile);
        System.out.println("Saved to: " + tempFile);
    }
    

    @Test
    public void testNetwork() throws Exception {
        Neuroml nml2 = new Neuroml();
        nml2.setId("InstanceBasedNet");
        
        IaFCell iaf = new IaFCell();
        iaf.setId("iaf0");
        iaf.setLeakReversal("-60mV");
        iaf.setThresh("-55mV");
        iaf.setReset("-65mV");
        iaf.setC("1.0nF");
        iaf.setLeakConductance("0.05uS");
        nml2.getIafCell().add(iaf); 
        
        Network net = new Network();
        net.setId("Net1");
        nml2.getNetwork().add(net); 
        
        Population pop = new Population();
        pop.setId("pop1");
        pop.setComponent(iaf.getId());
        
        net.getPopulation().add(pop);
        
        Instances instances = new Instances();
        pop.setInstances(instances);
        
        int size = 9;
        float maxX = 100;
        float maxY = 100;
        float maxZ = 100;
        
        for (int i=0;i<size;i++)
        {
            Instance instance = new Instance();
            Location loc = new Location();
            instance.setLocation(loc);
            loc.setX((float)Math.random()*maxX);
            loc.setY((float)Math.random()*maxY);
            loc.setZ((float)Math.random()*maxZ);
            instances.getInstance().add(instance);
        }

        neuroml2ToXml(nml2, nml2.getId()+ ".xml");
        
    }

	
	@Test public void testMorphology() throws Exception
	{
		NeuroMLConverter neuromlConverter=new NeuroMLConverter();
		Neuroml neuroml = neuromlConverter.urlToNeuroML(new URL("http://www.opensourcebrain.org/projects/celegans/repository/revisions/master/raw/CElegans/generatedNeuroML2/RIGL.nml"));
		
		Morphology morphology=neuroml.getCell().get(0).getMorphology();
		Assert.assertNotNull(morphology);
		Assert.assertTrue(!morphology.getSegment().isEmpty());
	}

}
