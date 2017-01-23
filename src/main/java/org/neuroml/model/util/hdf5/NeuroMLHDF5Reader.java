/*
   Originally based on code from neuroConstruct: https://github.com/NeuralEnsemble/neuroConstruct

   @ Author: p.gleeson 
*/


package org.neuroml.model.util.hdf5;


import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import ncsa.hdf.utils.SetNatives;
import org.neuroml.model.Connection;
import org.neuroml.model.Input;
import org.neuroml.model.InputList;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.Network;
import org.neuroml.model.NetworkTypes;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.Projection;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLElements;
import org.neuroml.model.util.NeuroMLException;
import static org.neuroml.model.util.hdf5.NeuroMLHDF5Writer.NEUROML_TOP_LEVEL_CONTENT;


public class NeuroMLHDF5Reader
{    
    
    Network currentNetwork = null;
    
    Population currentPopulation = null;
    Projection currentProjection = null;
    InputList currentInputList = null;
   
    NeuroMLConverter neuromlConverter;
    NeuroMLDocument neuroMLDocument;
    boolean verbose = false;
    
    boolean includeConnections = false;

    public NeuroMLHDF5Reader() throws IOException, NeuroMLException
    {        
		SetNatives.getInstance().setHDF5Native(System.getProperty("user.dir"));
        neuromlConverter = new NeuroMLConverter();
    }

    public NeuroMLDocument getNeuroMLDocument() {
        return neuroMLDocument;
    }
    
    public void setVerbose(boolean v)
    {
        this.verbose = v;
        Hdf5Utils.setVerbose(v);
    }
    
    private void printv(String msg)
    {
        String pre = "HDF5 R >> ";
        if (verbose) 
            System.out.println(pre+msg.replaceAll("\n", "\n"+pre));
    }
    
    public NeuroMLDocument parse(File hdf5File, boolean includeConnections) throws Hdf5Exception, NeuroMLException 
    {
        this.includeConnections = includeConnections;
        
        H5File h5File = Hdf5Utils.openForRead(hdf5File);
        
        Group root = Hdf5Utils.getRootGroup(h5File);
        printv("root: "+root);
        
        parseGroup(root);
        
        Hdf5Utils.close(h5File);
        
        return neuroMLDocument;
    }
        
        
    protected void startGroup(Group g) throws Hdf5Exception, NeuroMLException
    {
        printv("-----   Going into a group: "+g.getFullName());
        
        ArrayList<Attribute> attrs = Hdf5Utils.parseGroupForAttributes(g);
            
        for (Attribute attribute : attrs) 
        {
            printv("Group: "+g.getName()+ " has attribute: "+ attribute.getName()+" = "+ Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()));
        }
        
        if (g.getName().equals((NeuroMLElements.NEUROML_ROOT)))
        {
            printv("Found the main group");
            for (Attribute attr: attrs) {
                if (attr.getName().equals(NEUROML_TOP_LEVEL_CONTENT)){
                    String nml = Hdf5Utils.getFirstStringValAttr(attrs, attr.getName());
                    neuroMLDocument = neuromlConverter.loadNeuroML(nml);
                }
            }
            if (neuroMLDocument==null)
                neuroMLDocument = new NeuroMLDocument();
            

        }
        if (g.getName().equals((NeuroMLElements.NETWORK)))
        {
            printv("Found the network group");
            
            currentNetwork = new Network();
            currentNetwork.setId(Hdf5Utils.getFirstStringValAttr(attrs, "id"));
            currentNetwork.setNotes(Hdf5Utils.getFirstStringValAttr(attrs, "notes"));
            
            if (Hdf5Utils.getFirstStringValAttr(attrs, "temperature")!=null)
            {
                currentNetwork.setTemperature(Hdf5Utils.getFirstStringValAttr(attrs, "temperature"));
                currentNetwork.setType(NetworkTypes.NETWORK_WITH_TEMPERATURE);
            }
            
            neuroMLDocument.getNetwork().add(currentNetwork);
            
        }
        if (g.getName().startsWith(NeuroMLElements.POPULATION+"_"))
        {
            printv("Found a population group");
            
            currentPopulation = new Population();
            currentNetwork.getPopulation().add(currentPopulation);
            currentPopulation.setId(Hdf5Utils.getFirstStringValAttr(attrs, "id"));
            currentPopulation.setSize(Integer.parseInt(Hdf5Utils.getFirstStringValAttr(attrs, "size")));
            currentPopulation.setComponent(Hdf5Utils.getFirstStringValAttr(attrs, "component"));
            
            

        }
        if (g.getName().startsWith(NeuroMLElements.PROJECTION+"_"))
        {
            currentProjection = new Projection();
            currentNetwork.getProjection().add(currentProjection);
            currentProjection.setId(Hdf5Utils.getFirstStringValAttr(attrs, "id"));
            currentProjection.setPresynapticPopulation(Hdf5Utils.getFirstStringValAttr(attrs, "presynapticPopulation"));
            currentProjection.setPostsynapticPopulation(Hdf5Utils.getFirstStringValAttr(attrs, "postsynapticPopulation"));
            currentProjection.setSynapse(Hdf5Utils.getFirstStringValAttr(attrs, "synapse"));
                
            printv("Found a projection: "+ currentProjection.getId());
            
        }
        if (g.getName().startsWith(NeuroMLElements.INPUT_LIST+"_") || g.getName().startsWith("input_list_")) // inputList_ preferred!
        {
            currentInputList = new InputList();
            currentNetwork.getInputList().add(currentInputList);
            String id = Hdf5Utils.getFirstStringValAttr(attrs, "id");
            currentInputList.setId(id);
            currentInputList.setPopulation(Hdf5Utils.getFirstStringValAttr(attrs, "population"));
            currentInputList.setComponent(Hdf5Utils.getFirstStringValAttr(attrs, "component"));
                
            printv("Found an input list: "+ currentInputList.getId()+" on "+currentInputList.getPopulation());
            
        }
        
    }
    
    
    
    
    protected void endGroup(Group g) throws Hdf5Exception
    {
        printv("-----   Going out of a group: "+g.getFullName());
        
        if (g.getName().startsWith(NeuroMLElements.INPUT_LIST) || g.getName().startsWith("input_list_"))  // inputList_ preferred!
        {
            currentInputList = null;
        }        
        else if (g.getName().startsWith(NeuroMLElements.NETWORK))
        {
            currentNetwork = null;
        }       
        else if (g.getName().startsWith(NeuroMLElements.POPULATION))
        {
            currentPopulation = null;
        }
        else if (g.getName().startsWith(NeuroMLElements.PROJECTION))
        {
            currentProjection = null;
        }
    }
    
    
    protected void dataSet(Dataset d) throws Hdf5Exception
    {
        printv("-----   Looking through dataset: "+d);
        
        ArrayList<Attribute> attrs = Hdf5Utils.parseDatasetForAttributes(d);
            
        for (Attribute attribute : attrs) 
        {
            printv("Dataset: "+d.getName()+ " has attribute: "+ attribute.getName()+" = "+ Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()));
        }
        
        float[][] data = Hdf5Utils.parse2Ddataset(d);
        
        printv("Data has size: ("+data.length+", "+data[0].length+")");
        
        if (currentPopulation!=null)
        {
            currentPopulation.setType(PopulationTypes.POPULATION_LIST);
            
            for (float[] data1 : data)
            {
                Location l = new Location();
                l.setX(data1[1]);
                l.setY(data1[2]);
                l.setZ(data1[3]);
                Instance i = new Instance();
                i.setId(new BigInteger ((int)data1[0]+""));
                i.setLocation(l);
                currentPopulation.getInstance().add(i);
                
            }
        }
        else if (currentProjection!=null)
        {
            printv("Adding info for Projection: "+ currentProjection);
            
            if (!includeConnections) 
            {
                printv("Explicitly asked to ignore connection information...");
            }
            else
            {
                int id_col = -1;

                int pre_cell_id_col = -1;
                int pre_segment_id_col = -1;
                int pre_fraction_along_col = -1;

                int post_cell_id_col = -1;
                int post_segment_id_col = -1;
                int post_fraction_along_col = -1;

                int prop_delay_col = -1;


                for (Attribute attribute : attrs) 
                {
                    String storedInColumn = Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName());

                    if (storedInColumn.equals("id"))
                    {
                        id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                        printv("id col: "+id_col);
                    }
                    else if (storedInColumn.equals("pre_cell_id"))
                    {
                        pre_cell_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }
                    else if (storedInColumn.equals("post_cell_id"))
                    {
                        post_cell_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }
                    else if (storedInColumn.equals("pre_segment_id"))
                    {
                        pre_segment_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }
                    else if (storedInColumn.equals("post_segment_id"))
                    {
                        post_segment_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }

                    else if (storedInColumn.equals("pre_fraction_along"))
                    {
                        pre_fraction_along_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }
                    else if (storedInColumn.equals("post_fraction_along"))
                    {
                        post_fraction_along_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }

                }

                for (float[] data1 : data)
                {
                    int pre_seg_id = 0;
                    float pre_fract_along = 0.5f;
                    int post_seg_id = 0;
                    float post_fract_along = 0.5f;
                    int id = (int) data1[id_col];
                    int pre_cell_id = (int) data1[pre_cell_id_col];
                    int post_cell_id = (int) data1[post_cell_id_col];
                    if (pre_segment_id_col>=0)
                    {
                        pre_seg_id = (int) data1[pre_segment_id_col];
                    }
                    if (pre_fraction_along_col>=0)
                    {
                        pre_fract_along = data1[pre_fraction_along_col];
                    }
                    if (post_segment_id_col>=0)
                    {
                        post_seg_id = (int) data1[post_segment_id_col];
                    }
                    if (post_fraction_along_col>=0)
                    {
                        post_fract_along = data1[post_fraction_along_col];
                    }
                    Connection conn = new Connection();
                    conn.setId(id);
                    conn.setPreCellId("../"+currentProjection.getPresynapticPopulation()+"/"+pre_cell_id+"/???");
                    conn.setPostCellId("../"+currentProjection.getPostsynapticPopulation()+"/"+post_cell_id+"/???");
                    conn.setPreSegmentId(pre_seg_id);
                    conn.setPostSegmentId(post_seg_id);
                    conn.setPreFractionAlong(pre_fract_along);
                    conn.setPostFractionAlong(post_fract_along);
                    currentProjection.getConnection().add(conn);
                }
            }
            
        }
        else if (currentInputList !=null)
        {
            printv("Adding info for: "+ currentInputList.getId());
                    
            for(int i = 0;i<data.length;i++)
            {
                printv("Adding point "+i);
                Float fcellId = data[i][0];
                Float fsegmentId = data[i][1];
                Float fractionAlong = data[i][2];
                int cellId = fcellId.intValue();
                int segmentId = fsegmentId.intValue();                
                
                Input input = new Input();
                input.setId(i);
                input.setTarget("../"+currentInputList.getPopulation()+"/"+cellId+"/???");
                input.setSegmentId(segmentId);
                input.setFractionAlong(fractionAlong);
                currentInputList.getInput().add(input);
                
            }
        }
        
    }
        
        
    protected void parseGroup(Group g) throws Hdf5Exception, NeuroMLException
    {
        startGroup(g);
                
        java.util.List members = g.getMemberList();

        // NOTE: parsing contents twice to ensure subgroups are handled before datasets
        // This is mainly because synapse_props groups will need to be parsed before dataset of connections  
       
        
        for (int j=0; j<members.size(); j++)
        {
            HObject obj = (HObject)members.get(j);
            
            if (obj instanceof Group)
            {
                Group subGroup = (Group)obj;
                
                printv("---------    Found a sub group: "+subGroup.getName());
                
                parseGroup(subGroup);
            }
        }
        
        for (int j=0; j<members.size(); j++)
        {
            HObject obj = (HObject)members.get(j);
            
            if (obj instanceof Dataset)
            {
                Dataset ds = (Dataset)obj;
                
                printv("Found a dataset: "+ds.getName());
                
                dataSet(ds);
            }
        }
        
        endGroup(g);
    }    
            
    
    public static void main(String args[])
    {

        try
        {
            
            String[] files = new String[]{"src/test/resources/examples/simplenet.nml.h5"};
            //files = new String[]{"src/test/resources/tmp/MediumNet.net.nml.h5"};
            
            for (String file: files)
            {
                File h5File = new File(file);

                NeuroMLHDF5Reader nmlReader = new NeuroMLHDF5Reader();
                nmlReader.setVerbose(true);

                nmlReader.parse(h5File, true);

                System.out.println("File loaded: "+file+"\n"+NeuroMLConverter.summary(nmlReader.getNeuroMLDocument()));
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
