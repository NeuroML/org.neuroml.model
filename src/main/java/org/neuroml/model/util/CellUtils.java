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
import org.neuroml.model.Morphology;
import org.neuroml.model.BiophysicalProperties;

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

    @Deprecated
    public static boolean hasUnbranchedNonOverlappingInfo(Cell cell) throws NeuroMLException
    {
        return hasUnbranchedNonOverlappingInfo(cell, null);
    }

    public static boolean hasUnbranchedNonOverlappingInfo(Cell cell, NeuroMLDocument nml2doc) throws NeuroMLException {
    
        Morphology morphology = getCellMorphology(cell, nml2doc);
        for (SegmentGroup sg : morphology.getSegmentGroup())
        {
            if (isUnbranchedNonOverlapping(sg))
            {
                return true;
            }
        }
        return false;
    }

    public static Morphology getCellMorphology(Cell cell, NeuroMLDocument nml2doc) throws NeuroMLException {
        
        if (cell.getMorphology()!=null) {
            return cell.getMorphology();
        }
        else if (cell.getMorphologyAttr() !=null)
        {        
            for (Morphology m: nml2doc.getMorphology()) 
            {        
                if (m.getId().equals(cell.getMorphologyAttr()))
                    return m;
            }
            throw new NeuroMLException("Cannot find morphology: "+cell.getMorphologyAttr()+" specified as an attribute in cell: "+cell.getId());
        }          
        else if (nml2doc==null)
        {
            return null;
            // Cannot get any morphology attribute
        } 
        return null; // may be expected...
    }

    public static BiophysicalProperties getCellBiophysicalProperties(Cell cell, NeuroMLDocument nml2doc) {
        
        if (cell.getBiophysicalProperties()!=null) {

            return cell.getBiophysicalProperties();
        }
        
        else if (cell.getBiophysicalPropertiesAttr() !=null)
        {
            for (BiophysicalProperties bp: nml2doc.getBiophysicalProperties()) {
                if (bp.getId().equals(cell.getBiophysicalPropertiesAttr()))
                    return bp;
            }
        }
        return null;
    }

    /**
     * @deprecated use LinkedHashMap<Integer, Segment> getIdsVsSegments(Cell cell, NeuroMLDocument nml2doc) instead.  
     */
    @Deprecated
    public static LinkedHashMap<Integer, Segment> getIdsVsSegments(Cell cell) {

        LinkedHashMap<Integer, Segment> idsVsSegments = new LinkedHashMap<Integer, Segment>();
        for (Segment seg : cell.getMorphology().getSegment()) {
            idsVsSegments.put(seg.getId(), seg);
        }
        return idsVsSegments;
    }

    public static LinkedHashMap<Integer, Segment> getIdsVsSegments(Cell cell, NeuroMLDocument nml2doc) throws NeuroMLException {

        LinkedHashMap<Integer, Segment> idsVsSegments = new LinkedHashMap<Integer, Segment>();
        Morphology morphology = getCellMorphology(cell, nml2doc);
        for (Segment seg : morphology.getSegment()) {
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

    @Deprecated
    public static Segment getSegmentWithId(Cell cell, int segmentId) throws NeuroMLException {
        return getSegmentWithId(cell, null, segmentId);
    }

    public static Segment getSegmentWithId(Cell cell, NeuroMLDocument nml2doc, int segmentId) throws NeuroMLException {
        List<Segment> segments = getCellMorphology(cell, nml2doc).getSegment();
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

    public static LinkedHashMap<String, SegmentGroup> getNamesVsSegmentGroups(Cell cell, NeuroMLDocument nml2doc) throws NeuroMLException {

        LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = new LinkedHashMap<String, SegmentGroup>();
        for (SegmentGroup sg : getCellMorphology(cell, nml2doc).getSegmentGroup()) {
            namesVsSegmentGroups.put(sg.getId(), sg);
        }
        return namesVsSegmentGroups;
    }

    public static LinkedHashMap<String, SegmentGroup> getNamesVsSegmentGroups(Cell cell) throws NeuroMLException {

        return getNamesVsSegmentGroups(cell, null);
    }

    public static ArrayList<Integer> getSegmentIdsInGroup(Cell cell, String segmentGroup) throws NeuroMLException {

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

    @Deprecated
    public static ArrayList<Segment> getSegmentsInGroup(Cell cell, String segmentGroup) throws NeuroMLException {
        return getSegmentsInGroup(cell, null, segmentGroup);
    }

    public static ArrayList<Segment> getSegmentsInGroup(Cell cell, NeuroMLDocument nml2doc, String segmentGroup) throws NeuroMLException {

        for (SegmentGroup sg : CellUtils.getCellMorphology(cell, nml2doc).getSegmentGroup()) {
            if (sg.getId().equals(segmentGroup)) {
                LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell, nml2doc);
                return getSegmentsInGroup(cell, nml2doc, namesVsSegmentGroups, sg);
            }
        }
        throw new NeuroMLException("No SegmentGroup: "+segmentGroup+" in cell with id: "+cell.getId());
    }


    public static ArrayList<Segment> getSegmentsInGroup(Cell cell, LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups, SegmentGroup segmentGroup) throws NeuroMLException {

        return getSegmentsInGroup(cell, null, namesVsSegmentGroups, segmentGroup);
    }

    public static ArrayList<Segment> getSegmentsInGroup(Cell cell, NeuroMLDocument nml2doc, LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups, SegmentGroup segmentGroup) throws NeuroMLException {

        ArrayList<Segment> segsHere = new ArrayList<Segment>();

        for (Member memb : segmentGroup.getMember()) {
            segsHere.add(getSegmentWithId(cell, nml2doc, memb.getSegment()));
        }
        for (Include inc : segmentGroup.getInclude()) {
            String sg = inc.getSegmentGroup();
            ArrayList<Segment> segs = getSegmentsInGroup(cell, nml2doc, namesVsSegmentGroups, namesVsSegmentGroups.get(sg));
            segsHere.addAll(segs);
        }

        return segsHere;
    }

    public static LinkedHashMap<SegmentGroup, ArrayList<Integer>> getSegmentGroupsVsSegIds(Cell cell) throws NeuroMLException {

        LinkedHashMap<SegmentGroup, ArrayList<Integer>> sgVsSegId = new LinkedHashMap<SegmentGroup, ArrayList<Integer>>();

        LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell);

        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            ArrayList<Integer> segsHere = getSegmentIdsInGroup(namesVsSegmentGroups, sg);
            sgVsSegId.put(sg, segsHere);
        }

        return sgVsSegId;
    }

    public static LinkedHashMap<SegmentGroup, ArrayList<Integer>> getSegmentGroupsVsSegIds(Cell cell, NeuroMLDocument nml2doc) throws NeuroMLException {

        LinkedHashMap<SegmentGroup, ArrayList<Integer>> sgVsSegId = new LinkedHashMap<SegmentGroup, ArrayList<Integer>>();

        Morphology morphology = getCellMorphology(cell, nml2doc);

        LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell, nml2doc);

        for (SegmentGroup sg : morphology.getSegmentGroup()) {
            ArrayList<Integer> segsHere = getSegmentIdsInGroup(namesVsSegmentGroups, sg);
            sgVsSegId.put(sg, segsHere);
        }

        return sgVsSegId;
    }

    public static double distance(Point3DWithDiam p, Point3DWithDiam d) {
        return Math.sqrt( Math.pow(p.getX()-d.getX(),2) + Math.pow(p.getY()-d.getY(),2) + Math.pow(p.getZ()-d.getZ(),2) );
    }

    public static double getFractionAlongSegGroupLength(Cell cell, String segmentGroup, int segmentId, float fractAlongSegment) throws NeuroMLException {
        return getFractionAlongSegGroupLength(cell, null, segmentGroup, segmentId, fractAlongSegment);
    }

    public static double getFractionAlongSegGroupLength(Cell cell, NeuroMLDocument nml2doc, String segmentGroup, int segmentId, float fractAlongSegment) throws NeuroMLException {

        ArrayList<Segment> segs = getSegmentsInGroup(cell, nml2doc, segmentGroup);
        if (segs.size()==1)
        {
            if(segs.get(0).getId()!=segmentId)
            {
                throw new NeuroMLException("Error in getFractionAlongSegGroupLength "+segs.get(0).getId() +" != "+segmentId);
            }
            return fractAlongSegment;
        }
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

            totalLength += dist;
            lastSeg = seg;
        }
        //System.out.println(" - Total length of "+segs.size()+" segments in "+cell.getId()+": "+totalLength+"; checking "+fractAlongSegment+" along "+segmentGroup);
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


        ArrayList<String> cellFiles = new ArrayList<String>();
        cellFiles.add("../neuroConstruct/osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/bask.cell.nml");
        //String test = "/home/padraig/neuroConstruct/osb/hippocampus/networks/nc_superdeep/neuroConstruct/generatedNeuroML2/pvbasketcell.cell.nml";
        cellFiles.add("../neuroConstruct/osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/pyr_4_sym.cell.nml");
        //test = "/Users/padraig/git/GoC_Varied_Inputs/Cells/Golgi/GoC.cell.nml";
        //test = "/home/padraig/neuroConstruct/osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/bask_soma.cell.nml";

        cellFiles.add("../git/morphology_include/pyr_soma_m_out_b_in.cell.nml");

        for(String cellFile : cellFiles)
        {
            NeuroMLDocument nml2doc = conv.loadNeuroML(new File(cellFile), true, true);

            Cell cell = nml2doc.getCell().get(0);
            System.out.println("--------    Cell loaded: " + cell.getId()+" from "+cellFile);

            LinkedHashMap<Integer, Segment> ids = getIdsVsSegments(cell, nml2doc);

            System.out.println("getIdsVsSegments: ");
            for (Integer id: ids.keySet()) {
                System.out.println("ID "+id+": "+ids.get(id));
            }

            System.out.println("hasUnbranchedNonOverlappingInfo: "+hasUnbranchedNonOverlappingInfo(cell, nml2doc));
            System.out.println("getSegmentWithId: "+getSegmentWithId(cell, nml2doc, 0));

            System.out.println("getSegmentsInGroup: "+getSegmentsInGroup(cell, nml2doc, "soma_group"));

            System.out.println("getFractionAlongSegGroupLength: "+getFractionAlongSegGroupLength(cell, nml2doc, "soma_group", 0, 0.1f));

            LinkedHashMap<SegmentGroup, ArrayList<Integer>> sgVsSegId = getSegmentGroupsVsSegIds(cell, nml2doc);
            for (SegmentGroup sg: sgVsSegId.keySet()) {
                System.out.println("SG "+sg.getId()+": "+sgVsSegId.get(sg));
            }
            //getFractionAlongSegGroupLength(cell, "basal2", 8, 0.1f);
            //getFractionAlongSegGroupLength(cell, "some_apicals", 2, 0.5f);
        }

    }

}
