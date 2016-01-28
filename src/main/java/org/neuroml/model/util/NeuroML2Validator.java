package org.neuroml.model.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.Connection;
import org.neuroml.model.ElectricalProjection;
import org.neuroml.model.Include;
import org.neuroml.model.IncludeType;
import org.neuroml.model.Member;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;
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
    
    File baseDirectory = null;

	public static final String VALID_AGAINST_SCHEMA = "Valid against schema";
	public static final String VALID_AGAINST_TESTS = "Valid against all tests";
	public static final String VALID_AGAINST_SCHEMA_AND_TESTS = "Valid against schema and all tests";
	public static final String NO_WARNINGS = "No warnings";

	public StandardTest TEST_REPEATED_IDS =           new StandardTest(10000, "No repeated segment Ids allowed within a cell");
	public StandardTest TEST_ONE_SEG_MISSING_PARENT = new StandardTest(10002, "Only one segment should have no parent");
	public StandardTest TEST_MEMBER_SEGMENT_EXISTS =  new StandardTest(10003, "Segment Id used in the member element of segmentGroup should exist");
	public StandardTest TEST_REPEATED_GROUPS =        new StandardTest(10004, "No repeated segmentGroup Ids allowed within a cell");
	public StandardTest TEST_INCLUDE_SEGMENT_GROUP_EXISTS =  new StandardTest(10005, "Segment Group used in the include element of segmentGroup should exist");
	public StandardTest TEST_SEGMENT_GROUP_IN_BIOPHYSICS_EXISTS =  new StandardTest(10006, "Segment Groups used in biophysicalProperties should exist");
	
    public StandardTest TEST_INCLUDED_FILES_EXIST =  new StandardTest(10010, "Included files should exist");
    
	//public StandardTest TEST_POPULATION_COMPONENT_EXISTS =  new StandardTest(10020, "Component in population should exist");
    
    public StandardTest TEST_POPULATION_SIZE_MATCHES_INSTANCES =  new StandardTest(10030, "Size attribute in population should match instance number");
	
    public StandardTest TEST_POPULATIONS_IN_PROJECTIONS =  new StandardTest(10040, "Population ids in projections should exist");
	
    public StandardTest TEST_SEGMENT_ID_IN_CONNECTION =  new StandardTest(10050, "Segment id used in connection should exist in target cell");
	
    public StandardTest TEST_FORMATTING_CELL_ID_IN_CONNECTION =  new StandardTest(10060, "Pre/post cell id in connection should be correctly formatted");
	
	
	public StandardTest WARN_ROOT_ID_0 =              new StandardTest(100001, "Root segment has id == 0", StandardTest.LEVEL.WARNING);
	
	
	public NeuroML2Validator() {
		reset();
	}
	
	public final void reset() {
		validity = new StringBuilder();
		warnings = new StringBuilder();
        baseDirectory = null;
	}

	public String getValidity() {
		return validity.toString();
	}
	
	public boolean isValid() {
		return getValidity().equals(VALID_AGAINST_SCHEMA) || getValidity().equals(VALID_AGAINST_SCHEMA_AND_TESTS) || getValidity().equals(VALID_AGAINST_TESTS);
	}
	
	public String getWarnings() {
		return warnings.toString();
	}
	
	public boolean hasWarnings() {
		if (warnings.length()==0) return false;
		return (!getWarnings().equals(NO_WARNINGS));
	}

	public void validateWithTests(File xmlFile) throws SAXException, IOException, NeuroMLException
	{
		reset();
        baseDirectory = xmlFile.getParentFile().getCanonicalFile();
		testValidityAgainstNeuroML2Schema(xmlFile);
		
		if (!getValidity().equals(VALID_AGAINST_SCHEMA)) {
			return;
		}
		NeuroMLConverter conv = new NeuroMLConverter();
		NeuroMLDocument nml2 = conv.loadNeuroML(xmlFile, true, false);
		validateWithTests(nml2);
		
	}
	
	/*
	 * TODO: Needs to be moved to a separate package for validation!
	 */
	public void validateWithTests(NeuroMLDocument nml2)
	{
		// Checks the areas the Schema just can't reach...
        
		//////////////////////////////////////////////////////////////////
		// <include ...>
		//////////////////////////////////////////////////////////////////
        for (IncludeType include: nml2.getInclude()) {
            File inclFile = new File(baseDirectory, include.getHref());
            
		    test(TEST_INCLUDED_FILES_EXIST, "Included file: "+include.getHref()
                +" could not be found relative to path: "+baseDirectory, inclFile.exists());
        }
		
		//////////////////////////////////////////////////////////////////
		// <cell>
		//////////////////////////////////////////////////////////////////
		
        HashMap<String, ArrayList<Integer>> cellidsVsSegs = new HashMap<String, ArrayList<Integer>>();
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
            
            if (cell.getBiophysicalProperties()!=null) {
                MembraneProperties mp = cell.getBiophysicalProperties().getMembraneProperties();
                for (ChannelDensity cd: mp.getChannelDensity()) {
                    if (cd.getSegmentGroup()!=null) {
                        test(TEST_SEGMENT_GROUP_IN_BIOPHYSICS_EXISTS, 
                            "ChannelDensity: "+cd.getId()+" specifies: "+cd.getSegmentGroup()+" which doesn't exist", 
                            segGroups.contains(cd.getSegmentGroup()) || cd.getSegmentGroup().equals(NeuroMLElements.SEGMENT_GROUP_ALL));
                    }
                }
            }
            cellidsVsSegs.put(cell.getId(), segIds);
			
		}
        
        for (Network network: nml2.getNetwork()) {
            
            //////////////////////////////////////////////////////////////////
            // <population>
            //////////////////////////////////////////////////////////////////

            ArrayList<String> popIds = new ArrayList<String>();
            HashMap<String, String> popVsComponent = new HashMap<String, String>();
            HashMap<String, Integer> popVsSize = new HashMap<String, Integer>();
            
            for (Population pop: network.getPopulation()) {
                popIds.add(pop.getId());
                popVsComponent.put(pop.getId(), pop.getComponent());
                popVsSize.put(pop.getId(), pop.getSize());
                
                if (pop.getType()!=null && pop.getType().value().equals(NeuroMLElements.POPULATION_LIST)) {
                    if (pop.getSize()!=null) {
                        int numInstances = pop.getInstance().size();
                        test(TEST_POPULATION_SIZE_MATCHES_INSTANCES,
                            "Size of population "+pop.getId()+" is specified as "+pop.getSize()+", but there are "+numInstances+" instance elements",
                            pop.getSize()!=null && numInstances==pop.getSize());
                    }
                }
            }
            
            //////////////////////////////////////////////////////////////////
            // <projection>
            //////////////////////////////////////////////////////////////////

            
            for (Projection proj: network.getProjection()) {
                test(TEST_POPULATIONS_IN_PROJECTIONS,
                        "Pre population id: "+proj.getPresynapticPopulation()+" in projection "+proj.getId()+" not found",
                        popIds.contains(proj.getPresynapticPopulation()));
                test(TEST_POPULATIONS_IN_PROJECTIONS,
                        "Post population id: "+proj.getPostsynapticPopulation()+" in projection "+proj.getId()+" not found",
                        popIds.contains(proj.getPostsynapticPopulation()));
                
                ArrayList<Integer> preCellSegs = cellidsVsSegs.get(popVsComponent.get(proj.getPresynapticPopulation()));
                ArrayList<Integer> postCellSegs = cellidsVsSegs.get(popVsComponent.get(proj.getPostsynapticPopulation()));
               
                for (Connection conn: proj.getConnection()) {
                    
                    String p = proj.getPresynapticPopulation();
                    String max = popVsSize.get(p)!=null ? (popVsSize.get(p)-1)+"" : "N";
                    String form = null;
                    boolean test = false;
                    
                    if (!conn.getPreCellId().contains("[")) {
                        String[] split = conn.getPreCellId().split("/");
                        test = split[0].equals("..") && 
                                        split[1].equals(p) &&
                                        (popVsSize.get(p)==null || Integer.parseInt(split[2])<popVsSize.get(p)) &&
                                        split[3].equals(popVsComponent.get(p));
                        form = "should be of form: ../"+p+"/0/"+popVsComponent.get(p)+" -> ../"+p+"/"+max+"/"+popVsComponent.get(p);
                    } else {
                        String[] split1 = conn.getPreCellId().split("/");
                        String[] split2 = split1[1].split("\\[");
                        String[] split3 = split2[1].split("\\]");
                        test = split1[0].equals("..") && 
                                        split2[0].equals(p) &&
                                        Integer.parseInt(split3[0])<popVsSize.get(p);
                        form = "should be of form: ../"+p+"[0] -> ../"+p+"["+max+"]";
                        
                    }
                    test(TEST_FORMATTING_CELL_ID_IN_CONNECTION,
                        "Badly formatted Cell Id attribute in connection "+conn.getId()+" of "+proj.getId()+": "+conn.getPreCellId()+
                        "; "+form, test);
                    
                    p = proj.getPostsynapticPopulation();
                    max = popVsSize.get(p)!=null ? (popVsSize.get(p)-1)+"" : "N";
                    
                    if (!conn.getPostCellId().contains("[")) {
                        String[] split = conn.getPostCellId().split("/");
                        test = split[0].equals("..") && 
                                        split[1].equals(p) &&
                                        (popVsSize.get(p)==null || Integer.parseInt(split[2])<popVsSize.get(p)) &&
                                        split[3].equals(popVsComponent.get(p));
                        form = "should be of form: ../"+p+"/0/"+popVsComponent.get(p)+" -> ../"+p+"/"+max+"/"+popVsComponent.get(p);
                    } else {
                        String[] split1 = conn.getPostCellId().split("/");
                        String[] split2 = split1[1].split("\\[");
                        String[] split3 = split2[1].split("\\]");
                        test = split1[0].equals("..") && 
                                        split2[0].equals(p) &&
                                        Integer.parseInt(split3[0])<popVsSize.get(p);
                        form = "should be of form: ../"+p+"[0] -> ../"+p+"["+max+"]";
                        
                    }
                    test(TEST_FORMATTING_CELL_ID_IN_CONNECTION,
                        "Badly formatted Cell Id attribute in connection "+conn.getId()+" of "+proj.getId()+": "+conn.getPostCellId()+
                        "; "+form, test);
                    
                    
                    
                    if (preCellSegs!=null) {
                        test(TEST_SEGMENT_ID_IN_CONNECTION,
                            "Segment id "+conn.getPreSegmentId()+" in connection "+conn.getId()+" of "+proj.getId()+" not present in target cell",
                            preCellSegs.contains(conn.getPreSegmentId()));
                    }
                    if (postCellSegs!=null) {
                        test(TEST_SEGMENT_ID_IN_CONNECTION,
                            "Segment id "+conn.getPostSegmentId()+" in connection "+conn.getId()+" of "+proj.getId()+" not present in target cell",
                            postCellSegs.contains(conn.getPostSegmentId()));
                    }
                }
            }
            
            for (ElectricalProjection proj: network.getElectricalProjection()) {
                test(TEST_POPULATIONS_IN_PROJECTIONS,
                        "Pre population id: "+proj.getPresynapticPopulation()+" in projection "+proj.getId()+" not found",
                        popIds.contains(proj.getPresynapticPopulation()));
                test(TEST_POPULATIONS_IN_PROJECTIONS,
                        "Post population id: "+proj.getPostsynapticPopulation()+" in projection "+proj.getId()+" not found",
                        popIds.contains(proj.getPostsynapticPopulation()));
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
		InputStream in = getClass().getResourceAsStream(NeuroMLElements.LATEST_SCHEMA);

		try {
			testValidity(xmlFile, new StreamSource(in));
			validity.append(VALID_AGAINST_SCHEMA);
		} catch (Exception e) {
			validity.append("File: "+ xmlFile.getAbsolutePath()+" is not valid against the schema: "+NeuroMLElements.LATEST_SCHEMA+"!!\n"+e.getMessage());
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
    
    
	public static void main(String[] args) throws Exception {
        //File f = new File("../neuroConstruct/osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/pyr_4_sym.cell.nml");
        //File f = new File("../neuroConstruct/osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/MediumNet.net.nml");
        File f = new File("../git/GPUShowcase/NeuroML2/simplenet.nml");
        NeuroML2Validator nv = new NeuroML2Validator();
        nv.validateWithTests(f);
        System.out.println("Validity: "+nv.getValidity());
    }
	

}
