package org.neuroml.model.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.neuroml.model.Cell;
import org.neuroml.model.Morphology;
import org.neuroml.model.Neuroml;
import org.neuroml.model.Segment;

public class NeuroMLConverter
{
	protected static JAXBContext jaxb;
	
	protected static Marshaller marshaller;
	
	protected static Unmarshaller unmarshaller;	

    public static String NAMESPACE_URI_VERSION_2 = "http://www.neuroml.org/schema/neuroml2";

    public static String DEFAULT_SCHEMA_FILENAME_VERSION_2_ALPHA = "https://raw.github.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2alpha.xsd";
    public static String DEFAULT_SCHEMA_FILENAME_VERSION_2_BETA = "https://raw.github.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2beta.xsd";
	
	
	public NeuroMLConverter() throws Exception
	{
		jaxb = JAXBContext.newInstance("org.neuroml.model");
		
		marshaller = jaxb.createMarshaller();		
		//marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",new NeuroMLNamespacePrefixMapper());
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
				NAMESPACE_URI_VERSION_2+" "+DEFAULT_SCHEMA_FILENAME_VERSION_2_BETA);
		
		unmarshaller = jaxb.createUnmarshaller();
	}
	

	
	public Morphology xmlToMorphology(String xmlFile) throws Exception
	{
		File f = new File(xmlFile);
		if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
		
		JAXBElement<Morphology> jbe = (JAXBElement<Morphology>) unmarshaller.unmarshal(f);
		
		return jbe.getValue();		
	}

	
	public Neuroml loadNeuroML(File xmlFile) throws Exception
	{
		if (!xmlFile.exists()) throw new FileNotFoundException(xmlFile.getAbsolutePath());
		
		JAXBElement<Neuroml> jbe = (JAXBElement<Neuroml>) unmarshaller.unmarshal(xmlFile);
		
		return jbe.getValue();		
	}
	
	public Neuroml urlToNeuroML(URL url) throws Exception
	{
		JAXBElement<Neuroml> jbe = (JAXBElement<Neuroml>) unmarshaller.unmarshal(url);
		return jbe.getValue();		
	}
	
	

    /*
     * TODO: Needs to be made much more efficient
     */
	public File neuroml2ToXml(Neuroml nml2, String filename) throws Exception
	{
		JAXBElement<Neuroml> jbc =
			new JAXBElement<Neuroml>(new QName("neuroml"),
					                    Neuroml.class,
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
	



}
