package org.neuroml.model.test;

import static org.junit.Assert.assertTrue;

import java.io.File;


import org.junit.Test;
import org.neuroml.model.ExpOneSynapse;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.ExplicitInput;
import org.neuroml.model.IaFCell;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.Network;
import org.neuroml.model.Neuroml;
import org.neuroml.model.Population;
import org.neuroml.model.PulseGenerator;
import org.neuroml.model.SynapticConnection;
import org.neuroml.model.util.NeuroML2Validator;
import org.neuroml.model.util.NeuroMLConverter;

public class NeuroML2Test {

    @Test
    public void testCellSave() throws Exception {
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
        /*iz1.setIamp("2");
        iz1.setIdel("100 ms");
        iz1.setIdur("100 ms");*/
        nml2.getIzhikevichCell().add(iz1);

        ExpTwoSynapse e2syn = new ExpTwoSynapse();
        e2syn.setId("Syn0");
        e2syn.setTauRise("2ms");
        e2syn.setTauDecay("12ms");
        e2syn.setErev("-10mV");
        e2syn.setGbase("1nS");
        nml2.getExpTwoSynapse().add(e2syn);
        
        neuroml2ToXml(nml2, nml2.getId()+ ".xml", true);

    }

    @Test
    public void testNetworkSave() throws Exception {
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
        

        //<expOneSynapse id="syn1" gbase="5nS" erev="0mV" tauDecay="3ms" />
        ExpOneSynapse e1 = new ExpOneSynapse();
        e1.setId("syn1");
        e1.setGbase("5nS");
        e1.setErev("0mV");
        e1.setTauDecay("3ms");
        
        nml2.getExpOneSynapse().add(e1);

        
        
        int size = 5;
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
            pop.getInstance().add(instance);

            PulseGenerator pg = new PulseGenerator();
            pg.setId("pulseGen"+i);
            pg.setDelay("100ms");
            pg.setDuration("800ms");
            pg.setAmplitude(0.5*Math.random()+ "nA");
            nml2.getPulseGenerator().add(pg);
            
            ExplicitInput ei = new ExplicitInput();
            ei.setTarget(pop.getId()+"["+i+"]");
            ei.setInput(pg.getId());
            net.getExplicitInput().add(ei);
        }
        
        
        float probConn = 0.5f;

        for (int pre=0;pre<size;pre++)
        {

            for (int post=0;post<size;post++)
            {
            	if (pre!=post)
            	{
            		if (Math.random()<probConn)
            		{
            	        //<synapticConnection from="iafCells[0]" to="iafCells[1]" synapse="syn1"/>
            			SynapticConnection sc = new SynapticConnection();
            			sc.setFrom(pop.getId()+"["+pre+"]");
            			sc.setTo(pop.getId()+"["+post+"]");
            			sc.setSynapse(e1.getId());
            			
            			net.getSynapticConnection().add(sc);
            		}
            	}
            }
            	
        }
        
        neuroml2ToXml(nml2, nml2.getId()+ ".xml", true);
        
    }
    
    private void neuroml2ToXml(Neuroml nml2, String name, boolean validate) throws Exception 
    {
        String wdir = System.getProperty("user.dir");
        String tempdirname = wdir + File.separator + "src/test/resources/tmp";
        File tempdir = new File(tempdirname);
        if (!tempdir.exists()) tempdir.mkdir();
        
        NeuroMLConverter conv = new NeuroMLConverter();
        String tempFilename = tempdirname + File.separator + name;
        File tempFile = conv.neuroml2ToXml(nml2, tempFilename);
        System.out.println("Saved to: " + tempFile.getAbsolutePath());
        if (!tempFile.exists()) 
        	throw new Exception("Not successfully saved to: "+tempFilename);
        
        if (validate) {
        	NeuroML2Validator.testValidity(tempFile, "src/main/resources/Schemas/NeuroML2/NeuroML_v2beta.xsd");
        }
        
    }
    

	/*
	@Test public void testMorphology() throws Exception
	{
		NeuroMLConverter neuromlConverter=new NeuroMLConverter();
		String url = "file:///home/padraig/Cvapp-NeuroMorpho.org/temp/Case1_new.nml";
		Neuroml neuroml = neuromlConverter.urlToNeuroML(new URL(url));
		
		Morphology morphology=neuroml.getCell().get(0).getMorphology();
		Assert.assertNotNull(morphology);
		Assert.assertTrue(!morphology.getSegment().isEmpty());

        System.out.println("Successfully loaded NeuroML 2 cell model "+neuroml.getCell().get(0).getId()+" from: " + url);
        
        NeuroML2Validator nmlv = new NeuroML2Validator();
        assertTrue(nmlv.validateWithTests(neuroml));
	}*/
	
	@Test public void testLocalExamples() throws Exception
	{
		String wdir = System.getProperty("user.dir");
        String tempdirname = wdir + File.separator + "src/test/resources/examples";
        File tempdir = new File(tempdirname);
        for (File f: tempdir.listFiles())
        {
        	if (f.getName().endsWith(".nml"))
        	{
                System.out.println("---  Testing: " + f);
                NeuroML2Validator nmlv = new NeuroML2Validator();
                assertTrue(nmlv.validateWithTests(f));
        	}
        }
		

	}

}
