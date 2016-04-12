
package org.neuroml.model.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.junit.Assert;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neuroml.model.Cell;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.util.CellUtils;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;


public class CellUtilTest {
    
    static String wdir = System.getProperty("user.dir");
    static String exampledirname = wdir + File.separator + "src/test/resources/examples";

    
	private static Cell getCell() throws NeuroMLException, IOException {
        
        NeuroMLConverter neuromlConverter = new NeuroMLConverter();
        File exCell = new File(exampledirname + File.separator+"pyr_4_sym.cell.nml");
        System.out.println("Loading: "+exCell);
        NeuroMLDocument nmlDoc = neuromlConverter.loadNeuroML(exCell);
        Cell cell = nmlDoc.getCell().get(0);
        return cell;
    }
    
    @Test
    public void testGetIdsVsSegments() throws NeuroMLException, IOException {
        System.out.println("Testing: getIdsVsSegments");
        Cell cell = getCell();
        LinkedHashMap<Integer, Segment> idsVsSegments = CellUtils.getIdsVsSegments(cell);
        assertEquals(idsVsSegments.size(), cell.getMorphology().getSegment().size());
        for (int id: idsVsSegments.keySet()) {
            assertEquals(id, (int)idsVsSegments.get(id).getId());
        }
    }
    
    @Test
    public void testGetSegmentGroupsVsSegIds() throws NeuroMLException, IOException {
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
    
    @Test
    public void testIsUnbranchedNonOverlapping() throws NeuroMLException, IOException {
        System.out.println("Testing: testIsUnbranchedNonOverlapping");
        Cell cell = getCell();
        
        Assert.assertTrue(CellUtils.isUnbranchedNonOverlapping(CellUtils.getSegmentGroup(cell, "soma")));
        Assert.assertTrue(CellUtils.isUnbranchedNonOverlapping(CellUtils.getSegmentGroup(cell, "basal0")));
        Assert.assertFalse(CellUtils.isUnbranchedNonOverlapping(CellUtils.getSegmentGroup(cell, "dendrite_group")));
    
    }
    
    
    @Test
    public void testGetSegmentsInGroup() throws NeuroMLException, IOException {
        System.out.println("Testing: testIsUnbranchedNonOverlapping");
        Cell cell = getCell();
        
        ArrayList<Integer> segIds = CellUtils.getSegmentIdsInGroup(cell, "dendrite_group");
        ArrayList<Segment> segs = CellUtils.getSegmentsInGroup(cell, "dendrite_group");
        
        for (int i=0;i<segIds.size();i++) {
            System.out.println("- segId: "+segIds.get(i));
            assertEquals(segIds.get(i), segs.get(i).getId());
        }
    }
    
    
    @Test
    public void testDistance() throws NeuroMLException, IOException {
        System.out.println("Testing: distance");
        Point3DWithDiam p = new Point3DWithDiam();
        p.setX(0);
        p.setY(1);
        p.setZ(0);
        Point3DWithDiam d = new Point3DWithDiam();
        d.setX(0);
        d.setY(2);
        d.setZ(0);
        assertEquals(CellUtils.distance(p, d), 1, 0);
        d.setX(1);
        d.setZ(1);
        assertEquals(CellUtils.distance(p, d), Math.sqrt(3), 0);
    }
        
    @Test 
    public void testGetFractionAlongSegGroupLength() throws NeuroMLException, IOException {
        System.out.println("Testing: testGetFractionAlongSegGroupLength");
        Cell cell = getCell();
        assertEquals(CellUtils.getFractionAlongSegGroupLength(cell, "soma_group", 0, 0.1f), 0.1, 1e-6);
        assertEquals(CellUtils.getFractionAlongSegGroupLength(cell, "basal2", 8, 0.1f), 0.1, 1e-6);
        
        assertEquals(CellUtils.getFractionAlongSegGroupLength(cell, "some_apicals", 1, 0f), 0, 1e-6);
        assertEquals(CellUtils.getFractionAlongSegGroupLength(cell, "some_apicals", 1, 1f), 60f/(60+400+400+250), 1e-6);
        assertEquals(CellUtils.getFractionAlongSegGroupLength(cell, "some_apicals", 2, 0.5f), (60+400*0.5)/(60+400+400+250), 1e-6);
        assertEquals(CellUtils.getFractionAlongSegGroupLength(cell, "some_apicals", 4, 1f), 1, 1e-6);
    }
}
