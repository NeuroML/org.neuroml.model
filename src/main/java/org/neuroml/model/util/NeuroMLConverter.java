package org.neuroml.model.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.neuroml.model.Morphology;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.ObjectFactory;
import org.neuroml.model.Standalone;

public class NeuroMLConverter
{
	protected static JAXBContext jaxb;
	
	protected static Marshaller marshaller;
	
	protected static Unmarshaller unmarshaller;	
	
	
	public NeuroMLConverter() throws JAXBException
	{
		ClassLoader cl = ObjectFactory.class.getClassLoader();
		jaxb = JAXBContext.newInstance("org.neuroml.model",cl);
		
		marshaller = jaxb.createMarshaller();		
		//marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",new NeuroMLNamespacePrefixMapper());
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
				NeuroMLElements.NAMESPACE_URI_VERSION_2+" "+NeuroMLElements.TARGET_SCHEMA_LOCATION);
		
		unmarshaller = jaxb.createUnmarshaller();
	}
	

	
	public Morphology xmlToMorphology(String xmlFile) throws Exception
	{
		File f = new File(xmlFile);
		if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
		
		@SuppressWarnings("unchecked")
		JAXBElement<Morphology> jbe = (JAXBElement<Morphology>) unmarshaller.unmarshal(f);
		
		return jbe.getValue();		
	}

	
	public NeuroMLDocument loadNeuroML(File xmlFile) throws FileNotFoundException, JAXBException
	{
		if (!xmlFile.exists()) throw new FileNotFoundException(xmlFile.getAbsolutePath());
		
		@SuppressWarnings("unchecked")
		JAXBElement<NeuroMLDocument> jbe = (JAXBElement<NeuroMLDocument>) unmarshaller.unmarshal(xmlFile);
		
		return jbe.getValue();		
	}
	
	public NeuroMLDocument loadNeuroML(String nml2Contents) throws JAXBException
	{	
		StringReader sr = new StringReader(nml2Contents);
		@SuppressWarnings("unchecked")
		JAXBElement<NeuroMLDocument> jbe = (JAXBElement<NeuroMLDocument>) unmarshaller.unmarshal(sr);
		
		return jbe.getValue();		
	}
	
	public NeuroMLDocument urlToNeuroML(URL url) throws JAXBException
	{
		@SuppressWarnings("unchecked")
		JAXBElement<NeuroMLDocument> jbe = (JAXBElement<NeuroMLDocument>) unmarshaller.unmarshal(url);
		return jbe.getValue();		
	}
	
	public static ArrayList<Standalone> getAllStandaloneElements(NeuroMLDocument nmlDocument)
    {
        ArrayList<Standalone> elements = new ArrayList<Standalone>();
        Class<?> c = NeuroMLDocument.class;
        
        //System.out.println("Checking: "+c.getDeclaredMethods());
        for (Method m: c.getDeclaredMethods()) {
            //System.out.println("M: "+m.toString());
            
            try {
                m.setAccessible(true);
                Object o = m.invoke(nmlDocument, null);
                //System.out.format("%s() returned %s\n", m, o.toString());
                if (o instanceof List)
                {
                    elements.addAll((List)o);
                }

            // Handle any exceptions thrown by method to be invoked.
            } catch (InvocationTargetException ex) {
                Logger.getLogger(NeuroMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IllegalAccessException ex)
            {
                Logger.getLogger(NeuroMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IllegalArgumentException ex)
            {
                Logger.getLogger(NeuroMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
	    
        return elements;
    }

    /*
     * TODO: Needs to be made much more efficient
     */
	public File neuroml2ToXml(NeuroMLDocument nml2, String filename) throws Exception
	{
		JAXBElement<NeuroMLDocument> jbc =
			new JAXBElement<NeuroMLDocument>(new QName("neuroml"),
					NeuroMLDocument.class,
					                    nml2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

		marshaller.marshal(jbc, baos);

        String withNs = baos.toString();
        String correctNs = withNs.replaceAll(NeuroMLNamespacePrefixMapper.TEMP_NAMESPACE+":", "");
        correctNs = correctNs.replaceAll(":"+NeuroMLNamespacePrefixMapper.TEMP_NAMESPACE, "");

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
        String fileName = "../NeuroML2/examples/NML2_SingleCompHHCell.nml";
		NeuroMLConverter nmlc = new NeuroMLConverter();
    	NeuroMLDocument nmlDocument = nmlc.loadNeuroML(new File(fileName));
        System.out.println("Loaded: "+nmlDocument.getId());
        ArrayList<Standalone> els = getAllStandaloneElements(nmlDocument);
        for (Standalone el: els) {
            System.out.println("Found: "+el.getId());
        }
        
    }


}
