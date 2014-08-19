
package org.neuroml.model.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.neuroml.model.Cell;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.util.CellUtils;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;


public class CellUtilTest {
    
    static String wdir = System.getProperty("user.dir");
    static String exampledirname = wdir + File.separator + "src/test/resources/examples";

    
	private static Cell getCell() throws NeuroMLException, FileNotFoundException {
        
        NeuroMLConverter neuromlConverter = new NeuroMLConverter();
        File exCell = new File(exampledirname + File.separator+"pyr_4_sym.cell.nml");
        System.out.println("Loading: "+exCell);
        NeuroMLDocument nmlDoc = neuromlConverter.loadNeuroML(exCell);
        Cell cell = nmlDoc.getCell().get(0);
        return cell;
    }
    
    @Test
    public void testGetIdsVsSegments() throws NeuroMLException, FileNotFoundException {
        System.out.println("Testing: getIdsVsSegments");
        Cell cell = getCell();
        LinkedHashMap<Integer, Segment> idsVsSegments = CellUtils.getIdsVsSegments(cell);
        assertEquals(idsVsSegments.size(), cell.getMorphology().getSegment().size());
        for (int id: idsVsSegments.keySet()) {
            assertEquals(id, (int)idsVsSegments.get(id).getId());
        }
    }
    
    @Test
    public void testGetSegmentGroupsVsSegIds() throws NeuroMLException, FileNotFoundException {
        System.out.println("Testing: testGetSegmentGroupsVsSegIds");
        Cell cell = getCell();
        LinkedHashMap<SegmentGroup, ArrayList<Integer>> sgVsSegId = CellUtils.getSegmentGroupsVsSegIds(cell);
        
        for (SegmentGroup sg: sgVsSegId.keySet()) {
            ArrayList<Integer> ids = sgVsSegId.get(sg);
            System.out.println("SG "+sg.getId()+": "+ids);
            if (sg.getId().equals("all")) {
                assertEquals(cell.getMorphology().getSegment().size(), ids.size());
            }
            
            if (sg.getId().equals("dendrite_group")) {
                assertEquals(cell.getMorphology().getSegment().size()-1, ids.size());
            }
        }
        
    }
    
    
    
}
