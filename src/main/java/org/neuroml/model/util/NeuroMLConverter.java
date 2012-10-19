package org.neuroml.model.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.neuroml.model.Morphology;
import org.neuroml.model.Neuroml;

public class NeuroMLConverter
{
	protected JAXBContext jaxb;
	
	protected Marshaller marshaller;
	
	protected Unmarshaller unmarshaller;	
	
	
	public NeuroMLConverter() throws Exception
	{
		jaxb = JAXBContext.newInstance("org.neuroml.model");
		
		marshaller = jaxb.createMarshaller();		
		//marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",new NeuroMLNamespacePrefixMapper());
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
                "http://www.neuroml.org/schema/neuroml2  http://neuroml.svn.sourceforge.net/viewvc/neuroml/NeuroML2/Schemas/NeuroML2/NeuroML_v2alpha.xsd");
		
		unmarshaller = jaxb.createUnmarshaller();
	}
	

	
	public Morphology xmlToMorphology(String xmlFile) throws Exception
	{
		File f = new File(xmlFile);
		if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
		
		JAXBElement<Morphology> jbe = (JAXBElement<Morphology>) unmarshaller.unmarshal(f);
		
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
	public void neuroml2ToXml(Neuroml nml2, String filename) throws Exception
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
	}
}
