/*
   @ Author: p.gleeson 
*/
package org.neuroml.model.util.hdf5;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;


public class NetworkHelper
{
    
    NeuroMLDocument neuroMLDocument;
    HashMap<String,float[][]> populationInfo = new HashMap<String,float[][]>();
    HashMap<String, String> populationComponents = new HashMap<String, String>();
    HashMap<String, String> populationTypes = new HashMap<String, String>();
    
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
    
    protected void setPopulationArray(String popId, String component, String type, float[][] data)
    {
        populationInfo.put(popId, data);
        populationComponents.put(popId, component);
        populationTypes.put(popId, type);
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
            for (Population p: neuroMLDocument.getNetwork().get(0).getPopulation())
            {
                popIds.add(p.getId());
            }
        }
        return popIds;
    }
    
    public String getPopulationComponent(String populationId) throws NeuroMLException
    {
        if (!populationInfo.isEmpty())
        {
            return populationComponents.get(populationId);
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
    
    public String getPopulationType(String populationId) throws NeuroMLException
    {
        if (!populationInfo.isEmpty())
        {
            return populationTypes.get(populationId);
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
    
    
    public static void main(String args[])
    {

        try
        {
            
            String[] files = new String[]{"src/test/resources/examples/simplenet.nml.h5"};
            files = new String[]{"src/test/resources/examples/MediumNet.net.nml",
                "src/test/resources/examples/MediumNet.net.nml.h5"};
            
            for (String file: files)
            {
                File h5File = new File(file);

                NeuroMLConverter nmlConv = new NeuroMLConverter();
                NetworkHelper netHelper = nmlConv.loadNeuroMLOptimized(h5File);
                
                NeuroMLDocument nml2Doc = netHelper.getNeuroMLDocument();
                System.out.println("File loaded: "+file+"\n"+NeuroMLConverter.summary(nml2Doc));
                
                for (String p: netHelper.getPopulationIds())
                {
                    System.out.println("Pop: "+p+" has "+netHelper.getPopulationSize(p)+" cells");
                    System.out.println("Location 2: "+netHelper.getLocation(p, 2, false));
                }
                assert netHelper.getPopulationSize("pyramidals_48")==48;
                
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
