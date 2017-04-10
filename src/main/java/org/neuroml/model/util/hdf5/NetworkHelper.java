/*
   @ Author: p.gleeson 
*/
package org.neuroml.model.util.hdf5;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.neuroml.model.BaseConnectionOldFormat;
import org.neuroml.model.BaseProjection;
import org.neuroml.model.Connection;
import org.neuroml.model.ConnectionWD;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.Projection;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;


public class NetworkHelper
{
    
    NeuroMLDocument neuroMLDocument;
    
    HashMap<String,float[][]> populationInfo = new HashMap<String,float[][]>();
    HashMap<String, Population> emptyPopulations = new HashMap<String, Population>();
    
    HashMap<String,float[][]> projectionInfo = new HashMap<String,float[][]>();
    HashMap<String, HashMap<String,Integer>> projectionColumns = new HashMap<String, HashMap<String,Integer>>();
    HashMap<String, BaseProjection> emptyProjections = new HashMap<String, BaseProjection>();
    
    HashMap<String,float[][]> electricalProjectionInfo = new HashMap<String,float[][]>();
    HashMap<String, HashMap<String,Integer>> electricalProjectionColumns = new HashMap<String, HashMap<String,Integer>>();
    HashMap<String, BaseProjection> emptyElectricalProjections = new HashMap<String, BaseProjection>();
    
    HashMap<String,float[][]> continuousProjectionInfo = new HashMap<String,float[][]>();
    HashMap<String, HashMap<String,Integer>> continuousProjectionColumns = new HashMap<String, HashMap<String,Integer>>();
    HashMap<String, BaseProjection> emptyContinuousProjections = new HashMap<String, BaseProjection>();
    
    public NetworkHelper()
    {
        
    }
    
    public NetworkHelper(NeuroMLDocument nmlDoc)
    {
        this.neuroMLDocument = nmlDoc;
    }

    public NeuroMLDocument getNeuroMLDocument()
    {
        return neuroMLDocument;
    }

    protected void setNeuroMLDocument(NeuroMLDocument neuroMLDocument)
    {
        this.neuroMLDocument = neuroMLDocument;
    }
    
    protected void setPopulationArray(Population emptyPopulation, float[][] data)
    {
        populationInfo.put(emptyPopulation.getId(), data);
        emptyPopulations.put(emptyPopulation.getId(), emptyPopulation);
    }
    
    protected void setProjectionArray(BaseProjection emptyProjection, HashMap<String,Integer> columns, float[][] data)
    {
        projectionInfo.put(emptyProjection.getId(), data);
        emptyProjections.put(emptyProjection.getId(), emptyProjection);
        projectionColumns.put(emptyProjection.getId(), columns);
    }
    
    public ArrayList<String> getPopulationIds()
    {
        ArrayList<String> popIds = new ArrayList<String>();
        if (!populationInfo.isEmpty())
        {
            return new ArrayList<String>(populationInfo.keySet());
        }
        else 
        {
            if (neuroMLDocument!=null && !neuroMLDocument.getNetwork().isEmpty())
            {
                for (Population p: neuroMLDocument.getNetwork().get(0).getPopulation())
                {
                    popIds.add(p.getId());
                }
            }
        }
        return popIds;
    }
    
    public String getPopulationComponent(String populationId) throws NeuroMLException
    {
        if (!populationInfo.isEmpty())
        {
            return emptyPopulations.get(populationId).getComponent();
        }
        else 
        {
            for (Population p: neuroMLDocument.getNetwork().get(0).getPopulation())
            {
                if (p.getId().equals(populationId))
                {
                    return p.getComponent();
                }
            }
        }
        throw new NeuroMLException("Population "+populationId+" not found!");
    }
    
    public boolean populationHasPositions(String populationId) throws NeuroMLException
    {
        return getPopulationType(populationId).equals(PopulationTypes.POPULATION_LIST.value());
    }
    
    public String getPopulationType(String populationId) throws NeuroMLException
    {
        if (!populationInfo.isEmpty())
        {
            if (emptyPopulations.get(populationId).getType()==null || emptyPopulations.get(populationId).getType().value()==null)
                return PopulationTypes.POPULATION.value();
            return emptyPopulations.get(populationId).getType().value();
        }
        else 
        {
            for (Population p: neuroMLDocument.getNetwork().get(0).getPopulation())
            {
                if (p.getId().equals(populationId))
                {
                    if (p.getType()==null  || p.getType().value()==null)
                        return PopulationTypes.POPULATION.value();
                    return p.getType().value();
                }
            }
        }
        throw new NeuroMLException("Population "+populationId+" not found!");
    }
    
    public int getPopulationSize(String populationId) throws NeuroMLException
    {
        if (!populationInfo.isEmpty())
        {
            if (populationInfo.get(populationId).length==0)
                return emptyPopulations.get(populationId).getSize();
            
            return populationInfo.get(populationId).length;
        }
        else 
        {
            for (Population p: neuroMLDocument.getNetwork().get(0).getPopulation())
            {
                if (p.getId().equals(populationId))
                {
                    if (p.getSize()==null)
                        return p.getInstance().size();
                    return p.getSize();
                }
            }
        }
        throw new NeuroMLException("Population "+populationId+" not found!");
    }
    
    public Location getLocation(String populationId, int index, boolean failIfNonSpatialPop) throws NeuroMLException
    {
        
        if (!populationInfo.isEmpty())
        {
            Location loc = new Location();
            float[] locInfo = populationInfo.get(populationId)[index]; 
            loc.setX(locInfo[1]);
            loc.setY(locInfo[2]);
            loc.setZ(locInfo[3]);
            return loc;
        }
        else 
        {
            for (Population p: neuroMLDocument.getNetwork().get(0).getPopulation())
            {
                if (p.getId().equals(populationId))
                {
                    if (p.getInstance().isEmpty())
                    {
                        if (failIfNonSpatialPop)
                            throw new NeuroMLException("No 3D locations associated with population: "+p.getId());
                        else
                            return new Location(); // 0,0,0
                    }
                    if (p.getInstance().get(index).getId().intValue()==index)
                    {
                        return p.getInstance().get(index).getLocation();
                    }
                    for (Instance i: p.getInstance())
                    {
                        if (i.getId().intValue()==index)
                        {
                            return i.getLocation();
                        }
                    }
                }
            }
        }
        throw new NeuroMLException("Location "+index+" in population "+populationId+" not found!");
    }
    
    
    public ArrayList<String> getProjectionIds()
    {
        ArrayList<String> projIds = new ArrayList<String>();
        if (!projectionInfo.isEmpty())
        {
            return new ArrayList<String>(projectionInfo.keySet());
        }
        else
        {
            for (Projection proj: neuroMLDocument.getNetwork().get(0).getProjection())
            {
                projIds.add(proj.getId());
            }
        }
        return projIds;
    }
    
    public int getNumberConnections(String projectionId) throws NeuroMLException
    {
        if (!projectionInfo.isEmpty())
        {
            return projectionInfo.get(projectionId).length;
        }
        else 
        {
            for (Projection proj: neuroMLDocument.getNetwork().get(0).getProjection())
            {
                if (proj.getConnection().size()>0 && proj.getConnectionWD().size()>0)
                {
                    throw new NeuroMLException("Cannot yet handle projections that mix connections/connectionWDs ");
                }
                
                if (proj.getId().equals(projectionId))
                {
                    return proj.getConnection().size()+proj.getConnectionWD().size();
                }
            }
        }
        throw new NeuroMLException("Projection "+projectionId+" not found!");
    }
    
    public BaseConnectionOldFormat getConnection(String projectionId, int index) throws NeuroMLException
    {
        
        if (!projectionInfo.isEmpty())
        {
            
            float[] connInfo = projectionInfo.get(projectionId)[index];
            HashMap<String,Integer> columns = projectionColumns.get(projectionId);
            
            BaseConnectionOldFormat conn = new Connection();
            
            if (columns.containsKey("weight") && columns.get("weight")>=0)
            {
                ConnectionWD connw = new ConnectionWD();
                conn = connw;
                connw.setWeight(connInfo[columns.get("weight")]);
                connw.setDelay(connInfo[columns.get("delay")]+" ms");
            }
                
            conn.setId((int)connInfo[columns.get("id")]);
            if (index!=conn.getId())
                throw new NeuroMLException("Problem in projection "+projectionId+", connections are not ordered in increasing id...");
            
            conn.setPreCellId((int)connInfo[columns.get("pre_cell_id")]+"");
            conn.setPostCellId((int)connInfo[columns.get("post_cell_id")]+"");
            
            conn.setPreSegmentId((int)connInfo[columns.get("pre_segment_id")]);
            conn.setPostSegmentId((int)connInfo[columns.get("post_segment_id")]);
            
            conn.setPreFractionAlong(connInfo[columns.get("pre_fraction_along")]);
            conn.setPostFractionAlong(connInfo[columns.get("post_fraction_along")]);
            
            
            return conn;
        }
        else
        {
            for (Projection proj: neuroMLDocument.getNetwork().get(0).getProjection())
            {
                if (proj.getId().equals(projectionId))
                {
                    if (proj.getConnection().size()>0 && proj.getConnection().get(index).getId()==index)
                        return proj.getConnection().get(index);
                    if (proj.getConnectionWD().size()>0 && proj.getConnectionWD().get(index).getId()==index)
                        return proj.getConnectionWD().get(index);
                        
                }
            }
        }
        
        throw new NeuroMLException("Connection "+index+" not found in projection "+projectionId);
    }
    
    @Override
    public String toString()
    {
        String nmlDocInfo = neuroMLDocument==null ? "NONE" : neuroMLDocument.getId()
            +( (neuroMLDocument.getNetwork()==null || neuroMLDocument.getNetwork().size()==0) ? ", no network" : ", network: "+neuroMLDocument.getNetwork().get(0).getId()
            + " with " + neuroMLDocument.getNetwork().get(0).getPopulation().size()+" populations");
        
        return "NetworkHelper containing NML doc: ("+nmlDocInfo+
            ") and optimised elements on "+populationInfo.size()+" populations and "+projectionInfo.size()+" projections";
    }
    
    
    public static void main(String args[])
    {

        try
        {
            
            String[] files = new String[]{"src/test/resources/examples/simplenet.nml.h5"};
            files = new String[]{"src/test/resources/examples/MediumNet.net.nml",
                "src/test/resources/examples/MediumNet.net.nml.h5",
                "src/test/resources/examples/complete.nml",
                "src/test/resources/examples/complete.nml.h5"};
            
            for (String file: files)
            {
                File h5File = new File(file);

                NeuroMLConverter nmlConv = new NeuroMLConverter();
                NetworkHelper netHelper = nmlConv.loadNeuroMLOptimized(h5File);
                System.out.println("Using: "+netHelper);
                NeuroMLDocument nml2Doc = netHelper.getNeuroMLDocument();
                System.out.println("File loaded: "+file+"\n"+NeuroMLConverter.summary(nml2Doc));
                
                for (String p: netHelper.getPopulationIds())
                {
                    System.out.println("Pop: "+p+" has "+netHelper.getPopulationSize(p)+" cells, positions: "+netHelper.populationHasPositions(p));
                    if (netHelper.getPopulationSize(p)>0 && netHelper.populationHasPositions(p))
                        System.out.println("Location 2: "+netHelper.getLocation(p, 2, false));
                }
                assert netHelper.getPopulationSize("pyramidals_48")==48;
                
                
                for (String p: netHelper.getProjectionIds())
                {
                    System.out.println("Proj: "+p+" has "+netHelper.getNumberConnections(p)+" conns");
                    if (netHelper.getNumberConnections(p)>0)
                        System.out.println("Conn 0: "+ NeuroMLConverter.connectionInfo(netHelper.getConnection(p, 0)));
                }
                
                /*
                NeuroML2Validator nmlv = new NeuroML2Validator();
                nmlv.setBaseDirectory(h5File.getAbsoluteFile().getParentFile());
                nmlv.validateWithTests(nml2Doc);
                System.out.println("Status: "+nmlv.getValidity());*/
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }
    
}
