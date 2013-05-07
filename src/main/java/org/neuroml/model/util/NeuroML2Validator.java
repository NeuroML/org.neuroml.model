package org.neuroml.model.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.neuroml.model.Cell;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Segment;
import org.xml.sax.SAXException;

/*
 *        
 *        WORK IN PROGRESS!!!
 *        
 * TODO: Needs to be moved to a separate package for validation!
 */
public class NeuroML2Validator {

	boolean allTestsPassed = true;

	public static final String VALID_AGAINST_SCHEMA = "Valid against schema";
	public static final String VALID_AGAINST_TESTS = "Valid against all tests";
	public static final String VALID_AGAINST_SCHEMA_AND_TESTS = "Valid against schema and all tests";
	
	public NeuroML2Validator() {
		
	}
	

	public String validateWithTests(File xmlFile) throws SAXException, IOException, JAXBException
	{
		InputStream in = getClass().getResourceAsStream("/Schemas/NeuroML2/NeuroML_v2beta.xsd");
		String val = testValidity(xmlFile, new StreamSource(in));
		if (!val.equals(VALID_AGAINST_SCHEMA)) 
			return val;
		NeuroMLConverter conv = new NeuroMLConverter();
		NeuroMLDocument nml2 = conv.loadNeuroML(xmlFile);
		val = validateWithTests(nml2);
		if (!val.equals(VALID_AGAINST_TESTS)) 
			return val;
		
		return VALID_AGAINST_SCHEMA_AND_TESTS;
		
	}
	
	/*
	 * TODO: Needs to be moved to a separate package for validation!
	 */
	public String validateWithTests(NeuroMLDocument nml2)
	{
		StringBuilder validity = new StringBuilder();
		// Checks the areas the Schema just can't reach...
		
		//////////////////////////////////////////////////////////////////
		// <cell>
		//////////////////////////////////////////////////////////////////
		
		for (Cell cell: nml2.getCell()){
			
			// Morphologies
			ArrayList<Integer> segIds = new ArrayList<Integer>();
			boolean rootFound = false;
			if (cell.getMorphology() != null) {
				for(Segment segment: cell.getMorphology().getSegment()) {
					int segId = Integer.parseInt(segment.getId());
					
					test(10000, "No repeated segment Ids allowed within a cell", "Current segment ID: "+segId, !segIds.contains(segId));
					segIds.add(segId);
					
					if (segId==0){
						rootFound = true;
					}
				}

				test(10001,"Root segment has id == 0", "", rootFound);
			} else {
				//TODO: test for morphology attribute!
			}
			
		}
		
		if (validity.length()==0)
			validity.append(VALID_AGAINST_TESTS);
		
		return validity.toString();
		
	}
	
	private String test(int id, String testName, String info, boolean test) {
		if (!test) {
	        allTestsPassed = false;
	        return "Test: "+id+" ("+testName+") failed! .. "+info;
		} else {
	        //System.out.println("Test: "+id+" ("+testName+") succeeded! "+info);
			return "";
		}
	}


	public static String testValidity(File xmlFile, String xsdFile) throws SAXException, IOException {
		StreamSource schemaFileSource = new StreamSource(xsdFile);
		return testValidity(xmlFile, schemaFileSource);
	}

    public static String testValidity(File xmlFile, StreamSource schemaFileSource){
		//System.out.println("Testing validity of: "+ xmlFile.getAbsolutePath());

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		try {
			Schema schema = factory.newSchema(schemaFileSource);

	        Validator validator = schema.newValidator();

	        Source xmlFileSource = new StreamSource(xmlFile);

	        validator.validate(xmlFileSource);

	        return VALID_AGAINST_SCHEMA;
		} catch (Exception e) {
			return "File: "+ xmlFile.getAbsolutePath()+" is not valid!!\n"+e.getMessage();
		} 
	}
	

}
