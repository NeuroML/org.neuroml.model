/*
 */
package org.neuroml.model.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.neuroml.model.Cell;
import org.neuroml.model.Include;
import org.neuroml.model.Member;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Point3DWithDiam;
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
    
    public static SegmentGroup getSegmentGroup(Cell cell, String id) throws NeuroMLException {
        for (SegmentGroup sg: cell.getMorphology().getSegmentGroup()) {
            if (sg.getId().equals(id))
                return sg;
        }
        throw new NeuroMLException("No SegmentGroup with id: "+id+" in cell with id: "+cell.getId());
    }
    
    public static Segment getSegmentWithId(Cell cell, int segmentId) throws NeuroMLException {
        List<Segment> segments = cell.getMorphology().getSegment();
        if (segments.size()>segmentId) {
            Segment guess = segments.get(segmentId);
            if (guess.getId()==segmentId)
                return guess;
        }
        for (Segment seg : segments) {
            if (seg.getId()==segmentId)
                return seg;
        }
        throw new NeuroMLException("No Segment with id: "+segmentId+" in cell with id: "+cell.getId());
    }
    
    public static LinkedHashMap<String, SegmentGroup> getNamesVsSegmentGroups(Cell cell) {

        LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = new LinkedHashMap<String, SegmentGroup>();
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            namesVsSegmentGroups.put(sg.getId(), sg);
        }
        return namesVsSegmentGroups;
    }
    
    public static ArrayList<Integer> getSegmentIdsInGroup(Cell cell, String segmentGroup) {
        
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            if (sg.getId().equals(segmentGroup)) {
                LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell);
                return getSegmentIdsInGroup(namesVsSegmentGroups, sg);
            }
        }
        return new ArrayList<Integer>();
    }
    
    public static ArrayList<Integer> getSegmentIdsInGroup(LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups, SegmentGroup segmentGroup) {

        ArrayList<Integer> segsHere = new ArrayList<Integer>();
        
        for (Member memb : segmentGroup.getMember()) {
            segsHere.add(memb.getSegment());
        }
        for (Include inc : segmentGroup.getInclude()) {
            String sg = inc.getSegmentGroup();
            ArrayList<Integer> segs = getSegmentIdsInGroup(namesVsSegmentGroups, namesVsSegmentGroups.get(sg));
            segsHere.addAll(segs);
        }
        
        return segsHere;
    }
    
    public static boolean hasSegmentGroup(Cell cell, String segmentGroup) {
        
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            if (sg.getId().equals(segmentGroup)) {
                return true;
            }
        }
        return false;
    }
    
    public static ArrayList<Segment> getSegmentsInGroup(Cell cell, String segmentGroup) throws NeuroMLException {
        
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            if (sg.getId().equals(segmentGroup)) {
                LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell);
                return getSegmentsInGroup(cell, namesVsSegmentGroups, sg);
            }
        }
        throw new NeuroMLException("No SegmentGroup: "+segmentGroup+" in cell with id: "+cell.getId());
    }
    
    public static ArrayList<Segment> getSegmentsInGroup(Cell cell, LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups, SegmentGroup segmentGroup) throws NeuroMLException {

        ArrayList<Segment> segsHere = new ArrayList<Segment>();
        
        for (Member memb : segmentGroup.getMember()) {
            segsHere.add(getSegmentWithId(cell, memb.getSegment()));
        }
        for (Include inc : segmentGroup.getInclude()) {
            String sg = inc.getSegmentGroup();
            ArrayList<Segment> segs = getSegmentsInGroup(cell, namesVsSegmentGroups, namesVsSegmentGroups.get(sg));
            segsHere.addAll(segs);
        }
        
        return segsHere;
    }
    
    public static LinkedHashMap<SegmentGroup, ArrayList<Integer>> getSegmentGroupsVsSegIds(Cell cell) {
        
        LinkedHashMap<SegmentGroup, ArrayList<Integer>> sgVsSegId = new LinkedHashMap<SegmentGroup, ArrayList<Integer>>();
        
        LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell);
        
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            ArrayList<Integer> segsHere = getSegmentIdsInGroup(namesVsSegmentGroups, sg);
            sgVsSegId.put(sg, segsHere);
        }
        
        return sgVsSegId;
    }
    
    public static double distance(Point3DWithDiam p, Point3DWithDiam d) {
        return Math.sqrt( Math.pow(p.getX()-d.getX(),2) + Math.pow(p.getY()-d.getY(),2) + Math.pow(p.getZ()-d.getZ(),2) );
    }
    
    public static double getFractionAlongSegGroupLength(Cell cell, String segmentGroup, int segmentId, float fractAlongSegment) throws NeuroMLException {

        ArrayList<Segment> segs = getSegmentsInGroup(cell, segmentGroup);
        double totalLength = 0;
        
        Segment lastSeg = null;
        for(Segment seg: segs) {
            Point3DWithDiam p;
            if (lastSeg==null && seg.getProximal()!=null) {
                p = seg.getProximal();
            } else {
                int parId = seg.getParent().getSegment();
                if (lastSeg.getId()==parId) {
                    p = lastSeg.getDistal();
                } else {
                    throw new NeuroMLException("Getting length along a disjoint segment group!");
                }
            }
            double dist = distance(p, seg.getDistal());
            //System.out.println("Length of "+seg.getId()+": "+dist);
            totalLength += dist;
            lastSeg = seg;
        }
        //System.out.println("Total length of "+segs.size()+" segments: "+totalLength);
        double segGrpLength = 0;
        
        lastSeg = null;
        boolean foundSeg = false;
        for(Segment seg: segs) {
            Point3DWithDiam p;
            if (!foundSeg) {
                if (lastSeg==null && seg.getProximal()!=null) {
                    p = seg.getProximal();
                } else {
                    int parId = seg.getParent().getSegment();
                    if (lastSeg.getId()==parId) {
                        p = lastSeg.getDistal();
                    } else {
                        throw new NeuroMLException("Getting length along a disjoint segment group!");
                    }
                }
                double lenSeg = distance(p, seg.getDistal());
                if (seg.getId()==segmentId) {
                    segGrpLength += fractAlongSegment*lenSeg;
                    foundSeg = true;
                }
                else {
                    segGrpLength += lenSeg;
                }
                //System.out.println("sg length so far: "+segGrpLength);
                lastSeg = seg;
            }
        }
        if (!foundSeg)
            throw new NeuroMLException("Segment "+segmentId+" not found in "+segmentGroup);
        
        return segGrpLength/totalLength;
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
