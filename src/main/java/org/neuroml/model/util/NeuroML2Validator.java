package org.neuroml.model.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.XMLConstants;
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
	
	public NeuroML2Validator() {
		
	}
	

	public boolean validateWithTests(File xmlFile) throws Exception
	{
		testValidity(xmlFile, "src/main/resources/Schemas/NeuroML2/NeuroML_v2beta.xsd");
		NeuroMLConverter conv = new NeuroMLConverter();
		NeuroMLDocument nml2 = conv.loadNeuroML(xmlFile);
		return validateWithTests(nml2);
		
	}
	
	/*
	 * TODO: Needs to be moved to a separate package for validation!
	 */
	public boolean validateWithTests(NeuroMLDocument nml2) throws Exception
	{
		
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
		
		return allTestsPassed;
		
	}
	
	private void test(int id, String testName, String info, boolean test) {
		if (!test) {
	        System.out.println("Test: "+id+" ("+testName+") failed! .. "+info);
	        allTestsPassed = false;
		} else {
	        //System.out.println("Test: "+id+" ("+testName+") succeeded! "+info);
		}
	}
	

	public static void testValidity(File xmlFile, String xsdFile) throws SAXException, IOException {
		System.out.println("Testing validity of: "+ xmlFile.getAbsolutePath()+" against: "+ xsdFile);

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


        Source schemaFileSource = new StreamSource(xsdFile);
        Schema schema = factory.newSchema(schemaFileSource);

        Validator validator = schema.newValidator();

        Source xmlFileSource = new StreamSource(xmlFile);

        validator.validate(xmlFileSource);

        System.out.println("File: "+ xmlFile.getAbsolutePath()+" is valid!!");
	}
	

}
