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
import org.neuroml.model.Include;
import org.neuroml.model.Member;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.xml.sax.SAXException;

/*
 *        
 *        WORK IN PROGRESS!!!
 *        
 * TODO: Needs to be moved to a separate package for validation!
 */
public class NeuroML2Validator {

	StringBuilder validity;
	StringBuilder warnings;

	public static final String VALID_AGAINST_SCHEMA = "Valid against schema";
	public static final String VALID_AGAINST_TESTS = "Valid against all tests";
	public static final String VALID_AGAINST_SCHEMA_AND_TESTS = "Valid against schema and all tests";
	public static final String NO_WARNINGS = "No warnings";

	public StandardTest TEST_REPEATED_IDS =           new StandardTest(10000, "No repeated segment Ids allowed within a cell");
	public StandardTest TEST_ONE_SEG_MISSING_PARENT = new StandardTest(10002, "Only one segment should have no parent");
	public StandardTest TEST_MEMBER_SEGMENT_EXISTS =  new StandardTest(10003, "Segment Id used in the member element of segmentGroup should exist");
	public StandardTest TEST_REPEATED_GROUPS =        new StandardTest(10004, "No repeated segmentGroup Ids allowed within a cell");
	public StandardTest TEST_INCLUDE_SEGMENT_GROUP_EXISTS =  new StandardTest(10005, "Segment Group used in the include element of segmentGroup should exist");
	
	
	public StandardTest WARN_ROOT_ID_0 =              new StandardTest(100001, "Root segment has id == 0", StandardTest.LEVEL.WARNING);
	
	
	public NeuroML2Validator() {
		reset();
	}
	
	public void reset() {
		validity = new StringBuilder();
		warnings = new StringBuilder();
	}

	public String getValidity() {
		return validity.toString();
	}
	
	public boolean isValid() {
		return getValidity().equals(VALID_AGAINST_SCHEMA) || getValidity().equals(VALID_AGAINST_SCHEMA_AND_TESTS);
	}
	
	public String getWarnings() {
		return warnings.toString();
	}
	
	public boolean hasWarnings() {
		if (warnings.length()==0) return false;
		return (!getWarnings().equals(NO_WARNINGS));
	}

	public void validateWithTests(File xmlFile) throws SAXException, IOException, JAXBException
	{
		reset();
		testValidityAgainstNeuroML2Schema(xmlFile);
		
		if (!getValidity().equals(VALID_AGAINST_SCHEMA)) {
			return;
		}
		NeuroMLConverter conv = new NeuroMLConverter();
		NeuroMLDocument nml2 = conv.loadNeuroML(xmlFile);
		validateWithTests(nml2);
		
	}
	
	/*
	 * TODO: Needs to be moved to a separate package for validation!
	 */
	public void validateWithTests(NeuroMLDocument nml2)
	{
		// Checks the areas the Schema just can't reach...
		
		//////////////////////////////////////////////////////////////////
		// <cell>
		//////////////////////////////////////////////////////////////////
		
		for (Cell cell: nml2.getCell()){
			
			// Morphologies
			ArrayList<Integer> segIds = new ArrayList<Integer>();
			ArrayList<String> segGroups = new ArrayList<String>();
			
			boolean rootFound = false;
			int numParentless = 0;
			if (cell.getMorphology() != null) {
				for(Segment segment: cell.getMorphology().getSegment()) {
					int segId = segment.getId();
					
					test(TEST_REPEATED_IDS, "Current segment ID: "+segId, !segIds.contains(segId));
					segIds.add(segId);
					
					if (segId==0){
						rootFound = true;
					}
					if (segment.getParent()==null) {
						numParentless++;
					}
				}

				test(WARN_ROOT_ID_0, "", rootFound);
				test(TEST_ONE_SEG_MISSING_PARENT, "", (numParentless==1));

				for(SegmentGroup segmentGroup: cell.getMorphology().getSegmentGroup()) {
					
					test(TEST_REPEATED_GROUPS, "SegmentGroup: "+segmentGroup.getId(), !segGroups.contains(segmentGroup.getId()));
					
					segGroups.add(segmentGroup.getId());
					for (Member member: segmentGroup.getMember()) {
						test(TEST_MEMBER_SEGMENT_EXISTS, "SegmentGroup: "+segmentGroup.getId()+", member: "+member.getSegment(), segIds.contains(new Integer(member.getSegment().intValue())));
					}
					for (Include inc: segmentGroup.getInclude()) {
						test(TEST_INCLUDE_SEGMENT_GROUP_EXISTS, "SegmentGroup: "+segmentGroup.getId()+", includes: "+inc.getSegmentGroup(), segGroups.contains(inc.getSegmentGroup()));
					}
				}
				
			} else {
				//TODO: test for morphology attribute!
			}
			
		}

		if (validity.length()==0)
			validity.append(VALID_AGAINST_TESTS);
		
		if (getValidity().equals(VALID_AGAINST_SCHEMA))
			validity = new StringBuilder(VALID_AGAINST_SCHEMA_AND_TESTS);
		
		if (warnings.length()==0)
			warnings.append(NO_WARNINGS);
		
	}
	
	private void test(StandardTest st, String info, boolean test) {
		if (!test) {
			if (!st.isWarning()) {
				validity.append("\n  Test: "+st.id+" ("+st.description+") failed! ... "+info);
			} else {
				warnings.append("\n  Warning, check: "+st.id+" ("+st.description+") failed! ... "+info);
			}
		} else {
	        //System.out.println("Test: "+id+" ("+testName+") succeeded! "+info);
		}
	}
	

	public void testValidityAgainstNeuroML2Schema(File xmlFile) throws SAXException, IOException {
		InputStream in = getClass().getResourceAsStream(NeuroMLElements.TARGET_SCHEMA);

		try {
			testValidity(xmlFile, new StreamSource(in));
			validity.append(VALID_AGAINST_SCHEMA);
		} catch (Exception e) {
			validity.append("File: "+ xmlFile.getAbsolutePath()+" is not valid against the schema: "+NeuroMLElements.TARGET_SCHEMA+"!!\n"+e.getMessage());
		}
	}


	public static void testValidity(File xmlFile, String xsdFile) throws SAXException, IOException {
		StreamSource schemaFileSource = new StreamSource(xsdFile);
		testValidity(xmlFile, schemaFileSource);
	}

    public static void testValidity(File xmlFile, StreamSource schemaFileSource) throws SAXException, IOException{
		//System.out.println("Testing validity of: "+ xmlFile.getAbsolutePath());

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Schema schema = factory.newSchema(schemaFileSource);

        Validator validator = schema.newValidator();

        Source xmlFileSource = new StreamSource(xmlFile);

        validator.validate(xmlFileSource);

	         
	}
	

}
