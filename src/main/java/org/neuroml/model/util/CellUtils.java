/*
 */
package org.neuroml.model.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.neuroml.model.Cell;
import org.neuroml.model.Include;
import org.neuroml.model.Member;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;

/**
 *
 */
public class CellUtils {
    
    
    /*
    * This is an initial attempt to tag a subset of a cell's segment groups as 
    * the non overlapping groups which correspond to the "cables" of MorphML
    */
    public static String NEUROML2_NEUROLEX_UNBRANCHED_NONOVERLAPPING_SEG_GROUP = "sao864921383";
    
    
    public static boolean isUnbranchedNonOverlapping(SegmentGroup sg) {
        return sg.getNeuroLexId()!=null &&
               sg.getNeuroLexId().equals(NEUROML2_NEUROLEX_UNBRANCHED_NONOVERLAPPING_SEG_GROUP);
    }

    public static LinkedHashMap<Integer, Segment> getIdsVsSegments(Cell cell) {

        LinkedHashMap<Integer, Segment> idsVsSegments = new LinkedHashMap<Integer, Segment>();
        for (Segment seg : cell.getMorphology().getSegment()) {
            idsVsSegments.put(seg.getId(), seg);
        }
        return idsVsSegments;
    }
    
    public static LinkedHashMap<String, SegmentGroup> getNamesVsSegmentGroups(Cell cell) {

        LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = new LinkedHashMap<String, SegmentGroup>();
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            namesVsSegmentGroups.put(sg.getId(), sg);
        }
        return namesVsSegmentGroups;
    }
    
    public static ArrayList<Integer> getSegmentsInGroup(Cell cell, String segmentGroup) throws NeuroMLException {
        
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            if (sg.getId().equals(segmentGroup)) {
                LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell);
                return getSegmentsInGroup(namesVsSegmentGroups, sg);
            }
        }
        throw new NeuroMLException("No SegmentGroup: "+segmentGroup+" in cell with id: "+cell.getId());
    }
    
    public static ArrayList<Integer> getSegmentsInGroup(LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups, SegmentGroup segmentGroup) {

        ArrayList<Integer> segsHere = new ArrayList<Integer>();
        
        for (Member memb : segmentGroup.getMember()) {
            segsHere.add(memb.getSegment());
        }
        for (Include inc : segmentGroup.getInclude()) {
            String sg = inc.getSegmentGroup();
            ArrayList<Integer> segs = getSegmentsInGroup(namesVsSegmentGroups, namesVsSegmentGroups.get(sg));
            segsHere.addAll(segs);
        }
        
        return segsHere;
    }
    
    public static LinkedHashMap<SegmentGroup, ArrayList<Integer>> getSegmentGroupsVsSegIds(Cell cell) {
        
        LinkedHashMap<SegmentGroup, ArrayList<Integer>> sgVsSegId = new LinkedHashMap<SegmentGroup, ArrayList<Integer>>();
        
        LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell);
        
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            //System.out.println("sggg: "+sg);
            ArrayList<Integer> segsHere = getSegmentsInGroup(namesVsSegmentGroups, sg);
            sgVsSegId.put(sg, segsHere);
        }
        
        return sgVsSegId;
    }

    public static void main(String[] args) throws Exception {
        NeuroMLConverter conv = new NeuroMLConverter();
        //String test = "/home/padraig/neuroConstruct/osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/bask.cell.nml";
        //String test = "/home/padraig/neuroConstruct/osb/hippocampus/networks/nc_superdeep/neuroConstruct/generatedNeuroML2/pvbasketcell.cell.nml";
        String test = "/home/padraig/neuroConstruct/osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/pyr_4_sym.cell.nml";
        //test = "/home/padraig/neuroConstruct/osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/bask_soma.cell.nml";
        NeuroMLDocument nml2 = conv.loadNeuroML(new File(test));

        Cell cell = nml2.getCell().get(0);
        System.out.println("cell: " + cell.getId());
        
        LinkedHashMap<Integer, Segment> ids = getIdsVsSegments(cell);
        
        System.out.println("getIdsVsSegments: ");
        for (Integer id: ids.keySet()) {
            System.out.println("ID "+id+": "+ids.get(id));
        }
        
        LinkedHashMap<SegmentGroup, ArrayList<Integer>> sgVsSegId = getSegmentGroupsVsSegIds(cell);
        for (SegmentGroup sg: sgVsSegId.keySet()) {
            System.out.println("SG "+sg.getId()+": "+sgVsSegId.get(sg));
        }

    }

}
