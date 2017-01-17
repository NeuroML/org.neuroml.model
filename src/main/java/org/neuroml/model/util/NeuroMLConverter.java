package org.neuroml.model.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import ncsa.hdf.object.Group;
import org.neuroml.model.IafTauCell;
import org.neuroml.model.Include;
import org.neuroml.model.IncludeType;
import org.neuroml.model.Location;
import org.neuroml.model.Morphology;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.ObjectFactory;
import org.neuroml.model.Population;
import org.neuroml.model.Standalone;

public class NeuroMLConverter
{
	protected static JAXBContext jaxb;
	
	protected static Marshaller marshaller;
	
	protected static Unmarshaller unmarshaller;	
	
	
	public NeuroMLConverter() throws NeuroMLException
	{
		ClassLoader cl = ObjectFactory.class.getClassLoader();
        try {
            jaxb = JAXBContext.newInstance("org.neuroml.model",cl);

            marshaller = jaxb.createMarshaller();		
            //marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",new NeuroMLNamespacePrefixMapper());
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
                    NeuroMLElements.NAMESPACE_URI_VERSION_2+" "+NeuroMLElements.LATEST_SCHEMA_LOCATION);

            unmarshaller = jaxb.createUnmarshaller();
        } catch (JAXBException ex) {
            throw new NeuroMLException("Problem creating NeuroMLConverter", ex);
        }
	}
	

	
	public Morphology xmlToMorphology(String xmlFile) throws FileNotFoundException, NeuroMLException
	{
		File f = new File(xmlFile);
		if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
		
        try {
            @SuppressWarnings("unchecked")
            JAXBElement<Morphology> jbe = (JAXBElement<Morphology>) unmarshaller.unmarshal(f);

            return jbe.getValue();	
        } catch (JAXBException ex) {
            throw new NeuroMLException("Problem converting XML to Morphology in NeuroML", ex);
        }
	}

	
	public NeuroMLDocument loadNeuroML(File xmlFile) throws IOException, NeuroMLException
	{
        return loadNeuroML(xmlFile, false, false);
    }
	
	public NeuroMLDocument loadNeuroML(File xmlFile, boolean includeIncludes) throws IOException, NeuroMLException
	{
        return loadNeuroML(xmlFile, includeIncludes, true);
    }
	
	public NeuroMLDocument loadNeuroML(File xmlFile, boolean includeIncludes, boolean failOnMissingIncludes) throws IOException, NeuroMLException
	{
        return loadNeuroML(xmlFile, includeIncludes, failOnMissingIncludes, new ArrayList<String>());
    }
	
	public NeuroMLDocument loadNeuroML(File xmlFile, boolean includeIncludes, boolean failOnMissingIncludes, ArrayList<String> alreadyIncluded) throws NeuroMLException, IOException
	{
		if (!xmlFile.exists()) 
            throw new FileNotFoundException(xmlFile.getAbsolutePath());
		
        NeuroMLDocument nmlDocument;
        try {
            @SuppressWarnings("unchecked")
            JAXBElement<NeuroMLDocument> jbe = (JAXBElement<NeuroMLDocument>) unmarshaller.unmarshal(xmlFile);
            nmlDocument = jbe.getValue();
		} catch (JAXBException ex) {
            throw new NeuroMLException("Problem loading NeuroML document", ex);
        }	
        if (includeIncludes) {
            for (IncludeType include: nmlDocument.getInclude()) 
            {
                String relativeFileLocation = include.getHref();
                File subFile = new File(xmlFile.getAbsoluteFile().getParentFile(), relativeFileLocation);
                
                if (failOnMissingIncludes && !subFile.exists()) 
                {
                    throw new NeuroMLException("Missing file included by "+xmlFile.getAbsolutePath()+": "+relativeFileLocation);
                }
                if (subFile.exists() && !alreadyIncluded.contains(subFile.getCanonicalPath())) 
                {
                    NeuroMLDocument subDoc = loadNeuroML(subFile, includeIncludes, failOnMissingIncludes, alreadyIncluded);
                    LinkedHashMap<String,Standalone> saes = getAllStandaloneElements(subDoc);
                    for (Standalone sae: saes.values()) 
                    {
                        addElementToDocument(nmlDocument, sae);
                    }
                }
            }
        }
        
        alreadyIncluded.add(xmlFile.getCanonicalPath());
            
        return nmlDocument;	
	}
	
	public NeuroMLDocument loadNeuroML(String nml2Contents) throws NeuroMLException
	{	
		StringReader sr = new StringReader(nml2Contents);
        
        try {
            @SuppressWarnings("unchecked")
            JAXBElement<NeuroMLDocument> jbe = (JAXBElement<NeuroMLDocument>) unmarshaller.unmarshal(sr);
            return jbe.getValue();		
        } catch (JAXBException ex) {
            throw new NeuroMLException("Problem loading NeuroML document", ex);
        }
		
	}
	
	public NeuroMLDocument urlToNeuroML(URL url) throws NeuroMLException
	{
        try {
            @SuppressWarnings("unchecked")
            JAXBElement<NeuroMLDocument> jbe = (JAXBElement<NeuroMLDocument>) unmarshaller.unmarshal(url);
            return jbe.getValue();	
        } catch (JAXBException ex) {
            throw new NeuroMLException("Problem loading NeuroML document from URL", ex);
        }
	}
    
    public static String summary(NeuroMLDocument nmlDocument) throws NeuroMLException {
        
        String info = new String();
        info += "*******************************************************\n";
        info += "* NeuroMLDocument: "+nmlDocument.getId()+"\n";
        
        LinkedHashMap<String,Standalone> sae = getAllStandaloneElements(nmlDocument);
        for (String el: sae.keySet())
        {
            Standalone s = sae.get(el);
            if (!s.getClass().getSimpleName().equals("Network")) {
                
                info += "*  "+s.getClass().getSimpleName()+": "+s.getId()+"\n";
                
            }
        }
        
        for (Network network: nmlDocument.getNetwork()) {
            
            info += "*  Network: "+network.getId()+"\n";
        
            for (Population population: network.getPopulation())
            {
                info += "*   Population: "+population.getId()+
                    " with "+population.getSize() +" components of "+population.getComponent()+ "\n";
                if (population.getInstance().size()>0) 
                {
                    Location l = population.getInstance().get(0).getLocation();
                    info += "*     Locations: [("+ l.getX() + ","+ l.getY() + ","+ l.getZ() + "), ...]\n";
                }
            }
        }
        info += "*******************************************************\n";
        
        return info;
    }
	
	public static LinkedHashMap<String,Standalone> getAllStandaloneElements(NeuroMLDocument nmlDocument) throws NeuroMLException
    {
        LinkedHashMap<String,Standalone> elements = new LinkedHashMap<String,Standalone>();
        Class<?> c = NeuroMLDocument.class;
        
        XmlType annot = c.getAnnotation(XmlType.class);
        
        //System.out.println("Checking: "+annot);
        HashMap<String, List<Standalone>> output = new HashMap<String, List<Standalone>>();
        for (Method m: c.getDeclaredMethods()) {
            String elementName = m.getName().substring(3,4).toLowerCase()+m.getName().substring(4);
            
            // TODO: investigate why these are needed...
            if (elementName.startsWith("iF"))
                elementName = "if"+elementName.substring(2);
            if (elementName.startsWith("hH"))
                elementName = "hh"+elementName.substring(2);
            if (elementName.startsWith("eIF"))
                elementName = "eif"+elementName.substring(3);
            
            //System.out.println("----\nM: "+m.toString()+", "+elementName);
            try {
                m.setAccessible(true);
                if (m.toString().contains("List")) {
                    Object o = m.invoke(nmlDocument, (Object[])null);
                    //System.out.format("%s() returned %s\n", m, o.toString());
                    if (o instanceof List && !elementName.equals("include") && !elementName.equals("componentType"))
                    {
                        try {
                            List<Standalone> list = (List<Standalone>)o;
                            //if (!list.isEmpty())
                            //    System.out.println("Add it: "+elementName+", "+list.size());
                            output.put(elementName, list);

                        } catch (ClassCastException cce) {
                            //System.out.println("Failure: "+cce);
                        }
                    }
                    else
                    {
                        //System.out.println("Ignore it...");
                    }
                }

            } catch (IllegalAccessException ex) {
                throw new NeuroMLException("Error getting standalone elements in NeuroML", ex);
            } catch (InvocationTargetException ex) {
                throw new NeuroMLException("Error getting standalone elements in NeuroML", ex);
            }
            
        }
        //System.out.println("found: "+output.values());
        for (String element: annot.propOrder()) {
            //System.out.println("el: "+element);
            if (output.containsKey(element))
            {
                List<Standalone> list = output.get(element);
                for (Standalone s: list) {
                    if (elements.containsKey(s.getId()))
                    {
                        throw new NeuroMLException("Repeated top level (standalone element) Id: "+elements.get(s.getId())+" and "+s);
                    }
                    elements.put(s.getId(), s);
                }
            }
        }
        //System.out.println("found: "+elements.values());
	    
        return elements;
    }
    
	public static void addElementToDocument(NeuroMLDocument nmlDocument, Standalone nmlElement) throws NeuroMLException
    {
        Class<?> c = NeuroMLDocument.class;
        
        String elType = nmlElement.getClass().getSimpleName();
        //System.out.println("Checking: "+c.getDeclaredMethods()+", adding: "+elType);
        for (Method m: c.getDeclaredMethods()) {
            try {
                if (m.getName().startsWith("get")) {
                    m.setAccessible(true);
                    Object o = m.invoke(nmlDocument, (Object[])null);
                    //System.out.format("%s returned %s, %s, %s\n", m, o.toString(), o.getClass(), m.getName());
                    String expected = "get"+elType;
                    if (m.getName().equalsIgnoreCase(expected)) {
                        //System.out.println("Adding...");
                        ArrayList list = (ArrayList)o;
                        list.add(nmlElement);
                    }
                }
                
            } catch (IllegalAccessException ex) {
                throw new NeuroMLException("Error getting standalone elements in NeuroML", ex);
            } catch (InvocationTargetException ex) {
                throw new NeuroMLException("Error getting standalone elements in NeuroML", ex);
            }
            
        }
	    
    }

    /*
     * TODO: Needs to be made much more efficient
     */
	public String neuroml2ToXml(NeuroMLDocument nml2) throws NeuroMLException
	{
		JAXBElement<NeuroMLDocument> jbc =
			new JAXBElement<NeuroMLDocument>(new QName("neuroml"),
					NeuroMLDocument.class,
					                    nml2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			marshaller.marshal(jbc, baos);
		} catch (JAXBException e) {
			throw new NeuroMLException(e);
		}

        String withNs = baos.toString();
        String correctNs = withNs.replaceAll(NeuroMLNamespacePrefixMapper.TEMP_NAMESPACE+":", "");
        correctNs = correctNs.replaceAll(":"+NeuroMLNamespacePrefixMapper.TEMP_NAMESPACE, "");

		return correctNs;
	}
    
    /*
     * TODO: Needs to be made much more efficient
     */
	public File neuroml2ToXml(NeuroMLDocument nml2, String filename) throws NeuroMLException, IOException
	{
        String correctNs = neuroml2ToXml(nml2);

		File f = new File(filename);
		FileOutputStream fos = new FileOutputStream(f);
        fos.write(correctNs.getBytes());
        
		fos.close();
		return f;
	}
	
    /*
     * Convert a string of XML in NeuroML2 (i.e. root element <neuroml>) to LEMS
     * i.e. root element <Lems>
     */
    public static String convertNeuroML2ToLems(String nml2string)
    {
        if (nml2string.startsWith("<?xml")) {
            int index = nml2string.indexOf(">");
            nml2string = nml2string.substring(index + 1).trim();
        }
        
        if (nml2string.startsWith("<neuroml")) {

            int index = nml2string.indexOf(">");
            nml2string = nml2string.substring(index + 1);
            // Assume </neuroml> at end...
            nml2string = nml2string.replace("</neuroml>", "");

            nml2string = "<Lems>\n\n"
                    + "    <Include file=\"NeuroMLCoreDimensions.xml\"/>\n"
                    + "    <Include file=\"Cells.xml\"/>\n"
                    + "    <Include file=\"PyNN.xml\"/>\n"
                    + "    <Include file=\"Networks.xml\"/>\n"
                    + "    <Include file=\"Simulation.xml\"/>\n\n"
                    + nml2string + "\n"
                    + "</Lems>";


        }
        return nml2string;
    }
    
    
	public static void main(String[] args) throws Exception {
        String fileName = "../NeuroML2/examples/NML2_PyNNCells.nml";
		NeuroMLConverter nmlc = new NeuroMLConverter();
    	NeuroMLDocument nmlDocument = nmlc.loadNeuroML(new File(fileName));
        System.out.println("Loaded: "+nmlDocument.getId());
        
        /*
        IafTauCell iaf = new IafTauCell();
        iaf.setTau("10ms");
        iaf.setId("iaf00");
        addElementToDocument(nmlDocument, iaf);*/
        
        LinkedHashMap<String,Standalone> els = getAllStandaloneElements(nmlDocument);
        for (String el: els.keySet()) {
            System.out.println("A Found: "+ els.get(el).getId()+" ("+els.get(el)+")");
        }
        /*
        fileName = "../org.neuroml.export/src/test/resources/examples/TwoCell.net.nml";
		nmlc = new NeuroMLConverter();
    	nmlDocument = nmlc.loadNeuroML(new File(fileName), true);
        System.out.println("Loaded: "+nmlDocument.getId());
        
        
        
        els = getAllStandaloneElements(nmlDocument);
        for (String el: els.keySet()) {
            System.out.println("B Found: "+ els.get(el).getId()+" ("+els.get(el)+")");
        }*/
        
        
    }


}
