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
import java.util.HashMap;
import ncsa.hdf.utils.SetNatives;
import org.neuroml.model.BaseConnectionNewFormat;
import org.neuroml.model.BaseConnectionOldFormat;
import org.neuroml.model.BaseProjection;
import org.neuroml.model.Connection;
import org.neuroml.model.ConnectionWD;
import org.neuroml.model.ContinuousConnection;
import org.neuroml.model.ContinuousConnectionInstance;
import org.neuroml.model.ContinuousConnectionInstanceW;
import org.neuroml.model.ContinuousProjection;
import org.neuroml.model.ElectricalConnection;
import org.neuroml.model.ElectricalConnectionInstance;
import org.neuroml.model.ElectricalConnectionInstanceW;
import org.neuroml.model.ElectricalProjection;
import org.neuroml.model.Input;
import org.neuroml.model.InputList;
import org.neuroml.model.InputW;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.Network;
import org.neuroml.model.NetworkTypes;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.Projection;
import org.neuroml.model.util.NeuroML2Validator;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLElements;
import org.neuroml.model.util.NeuroMLException;
import static org.neuroml.model.util.hdf5.NeuroMLHDF5Writer.NEUROML_TOP_LEVEL_CONTENT;


public class NeuroMLHDF5Reader
{    
    
    Network currentNetwork = null;
    
    Population currentPopulation = null;
    HashMap<String, Boolean> populationUsesList = new HashMap<String, Boolean>();
    HashMap<String, String> populationComponent = new HashMap<String, String>();
    
    BaseProjection currentProjection = null;
    String currentProjectionType = "";
    HashMap<String, String> projectionSynapse = new HashMap<String, String>();
    HashMap<String, String> projectionComponentPre = new HashMap<String, String>();
    HashMap<String, String> projectionComponentPost = new HashMap<String, String>();
    
    InputList currentInputList = null;
   
    NeuroMLConverter neuromlConverter;
    NeuroMLDocument neuroMLDocument;
    NetworkHelper networkHelper;
    
    boolean verbose = false;
    
    boolean includeConnections = false;
    boolean optimized = false;
    boolean includeIncludes = true;
    
    File sourceDocument;
    
    ArrayList<String> alreadyIncluded = new ArrayList<String>();

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
        return parse(hdf5File, includeConnections, new ArrayList<String>());
    }
    
    /*
        alreadyIncluded is used if e.g. a parser has already read in other NML files before getting to the HDF5 file
    */
    public NeuroMLDocument parse(File hdf5File, boolean includeConnections, ArrayList<String> alreadyIncluded) throws Hdf5Exception, NeuroMLException 
    {
        return parse(hdf5File, includeConnections, alreadyIncluded, true);
    }
    /*
        alreadyIncluded is used if e.g. a parser has already read in other NML files before getting to the HDF5 file
    */
    public NeuroMLDocument parse(File hdf5File, boolean includeConnections, ArrayList<String> alreadyIncluded, boolean includeIncludes) throws Hdf5Exception, NeuroMLException 
    {
        sourceDocument = hdf5File;
        this.includeConnections = includeConnections;
        this.alreadyIncluded = alreadyIncluded;
        this.includeIncludes = includeIncludes;
        
        H5File h5File = Hdf5Utils.openForRead(hdf5File);
        
        Group root = Hdf5Utils.getRootGroup(h5File);
        printv("root: "+root);
        
        parseGroup(root);
        
        Hdf5Utils.close(h5File);
        
        return neuroMLDocument;
    }
    
    public NetworkHelper parseOptimized(File hdf5File) throws Hdf5Exception, NeuroMLException 
    {
        return parseOptimized(hdf5File, true);
    }

    public NetworkHelper parseOptimized(File hdf5File, boolean includeIncludes) throws Hdf5Exception, NeuroMLException
    {
        sourceDocument = hdf5File;
        this.includeIncludes = includeIncludes;
        
        includeConnections = true;
        optimized = true;
        networkHelper = new NetworkHelper();
        
        H5File h5File = Hdf5Utils.openForRead(hdf5File);
        
        Group root = Hdf5Utils.getRootGroup(h5File);
        printv("root: "+root);
        
        parseGroup(root);
        
        Hdf5Utils.close(h5File);
        
        networkHelper.setNeuroMLDocument(neuroMLDocument);
        
        return networkHelper;
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
                    neuroMLDocument = neuromlConverter.loadNeuroML(nml, includeIncludes, sourceDocument.getAbsoluteFile().getParentFile(), alreadyIncluded);
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
            populationComponent.put(currentPopulation.getId(),currentPopulation.getComponent());
            populationUsesList.put(currentPopulation.getId(), false); // until further notice...
            

        }
        if (g.getName().startsWith(NeuroMLElements.PROJECTION+"_"))
        {
            currentProjectionType = Hdf5Utils.getFirstStringValAttr(attrs, "type");
            //currentProjection = new Projection();
            
            if (currentProjectionType.equals("projection"))
            {
                currentProjection = new Projection();
                ((Projection)currentProjection).setSynapse(Hdf5Utils.getFirstStringValAttr(attrs, "synapse"));
                currentNetwork.getProjection().add((Projection)currentProjection);
            }
            
            if (currentProjectionType.equals("electricalProjection"))
            {
                currentProjection = new ElectricalProjection();
                projectionSynapse.put(Hdf5Utils.getFirstStringValAttr(attrs, "id"), Hdf5Utils.getFirstStringValAttr(attrs, "synapse"));
                currentNetwork.getElectricalProjection().add((ElectricalProjection)currentProjection);
            }
            
            if (currentProjectionType.equals("continuousProjection"))
            {
                currentProjection = new ContinuousProjection();
                projectionComponentPre.put(Hdf5Utils.getFirstStringValAttr(attrs, "id"), Hdf5Utils.getFirstStringValAttr(attrs, "preComponent"));
                projectionComponentPost.put(Hdf5Utils.getFirstStringValAttr(attrs, "id"), Hdf5Utils.getFirstStringValAttr(attrs, "postComponent"));
                currentNetwork.getContinuousProjection().add((ContinuousProjection)currentProjection);
            }
            
            currentProjection.setId(Hdf5Utils.getFirstStringValAttr(attrs, "id"));
            currentProjection.setPresynapticPopulation(Hdf5Utils.getFirstStringValAttr(attrs, "presynapticPopulation"));
            currentProjection.setPostsynapticPopulation(Hdf5Utils.getFirstStringValAttr(attrs, "postsynapticPopulation"));
                
            printv("Found a projection: "+ currentProjection.getId()+" type: "+currentProjectionType);
            
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
            if (optimized)
            {
                if (networkHelper.getPopulationIds().isEmpty() || !networkHelper.getPopulationIds().contains(currentPopulation.getId()))
                    networkHelper.setPopulationArray(currentPopulation, new float[0][0]);
            }
            currentPopulation = null;
        }
        else if (g.getName().startsWith(NeuroMLElements.PROJECTION))
        {
            if (optimized)
            {
                if (currentProjectionType.equals("projection"))
                {
                    if (networkHelper.getProjectionIds().isEmpty() || !networkHelper.getProjectionIds().contains(currentProjection.getId()))
                        networkHelper.setProjectionArray(currentProjection, new HashMap<String,Integer>(), new float[0][0]);
                }
            }
            currentProjection = null;
            currentProjectionType = "";
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
            
            populationUsesList.put(currentPopulation.getId(), true);
            
            if (optimized)
            {
                networkHelper.setPopulationArray(currentPopulation, data);
            }
            else
            {
                int id_col = 0;
                int x_col = 1;
                int y_col = 2;
                int z_col = 3;

                for (Attribute attribute : attrs) 
                {
                    String storedInColumn = Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName());

                    if (storedInColumn.equals("id"))
                    {
                        id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }
                    if (storedInColumn.equals("x"))
                    {
                        x_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }
                    if (storedInColumn.equals("y"))
                    {
                        y_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }
                    if (storedInColumn.equals("z"))
                    {
                        z_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    }
                }

                for (float[] data1 : data)
                {
                    Location l = new Location();
                    l.setX(data1[x_col]);
                    l.setY(data1[y_col]);
                    l.setZ(data1[z_col]);
                    Instance i = new Instance();
                    i.setId(new BigInteger ((int)data1[id_col]+""));
                    i.setLocation(l);
                    currentPopulation.getInstance().add(i);
                }
            }
        }
        else if (currentProjection!=null)
        {
            printv("-----------------------Adding info for Projection: "+ currentProjection+" type "+currentProjectionType);
            printv(populationUsesList+"");
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

                int delay_col = -1;
                int weight_col = -1;


                for (Attribute attribute : attrs) 
                {
                    String storedInColumn = Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName());
                    int colNum = attribute.getName().indexOf("column_")==0 ? Integer.parseInt(attribute.getName().substring("column_".length())) : -1;

                    if (storedInColumn.equals("id"))
                    {
                        id_col = colNum;
                        printv("id col: "+id_col);
                    }
                    else if (storedInColumn.equals("pre_cell_id"))
                    {
                        pre_cell_id_col = colNum;
                    }
                    else if (storedInColumn.equals("post_cell_id"))
                    {
                        post_cell_id_col = colNum;
                    }
                    else if (storedInColumn.equals("pre_segment_id"))
                    {
                        pre_segment_id_col = colNum;
                    }
                    else if (storedInColumn.equals("post_segment_id"))
                    {
                        post_segment_id_col = colNum;
                    }
                    else if (storedInColumn.equals("pre_fraction_along"))
                    {
                        pre_fraction_along_col = colNum;
                    }
                    else if (storedInColumn.equals("post_fraction_along"))
                    {
                        post_fraction_along_col = colNum;
                    }
                    else if (storedInColumn.equals("weight"))
                    {
                        weight_col = colNum;
                    }
                    else if (storedInColumn.equals("delay"))
                    {
                        delay_col = colNum;
                    }

                }
                
                if (optimized)
                {
                    HashMap<String,Integer> columns = new HashMap<String, Integer>();
                    columns.put("id", id_col);
                    columns.put("pre_cell_id", pre_cell_id_col);
                    columns.put("post_cell_id", post_cell_id_col);
                    columns.put("pre_segment_id", pre_segment_id_col);
                    columns.put("post_segment_id", post_segment_id_col);
                    columns.put("pre_fraction_along", pre_fraction_along_col);
                    columns.put("post_fraction_along", post_fraction_along_col);
                    columns.put("weight", weight_col);
                    columns.put("delay", delay_col);
                    

                    if (currentProjectionType.equals("projection"))
                    {
                        networkHelper.setProjectionArray(currentProjection, columns, data);
                    }
                }
                else
                {

                    for (float[] data1 : data)
                    {
                        int pre_seg_id = 0;
                        float pre_fract_along = 0.5f;
                        int post_seg_id = 0;
                        float post_fract_along = 0.5f;
                        int id = (int) data1[id_col];
                        int pre_cell_id = (int) data1[pre_cell_id_col];
                        int post_cell_id = (int) data1[post_cell_id_col];
                        float weight = 1;
                        float delay = 0;

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
                        if (weight_col>=0)
                        {
                            weight = data1[weight_col];
                        }
                        if (delay_col>=0)
                        {
                            delay = data1[delay_col];
                        }

                        if (currentProjectionType.equals("projection"))
                        {
                            BaseConnectionOldFormat connc = new Connection();

                            if ( (weight_col>=0 && weight!=1) || (delay_col>=0 && delay !=0))
                            {
                                ConnectionWD connw = new ConnectionWD();
                                connw.setWeight(weight);
                                connw.setDelay(delay+" ms");
                                connc = connw;
                                ((Projection)currentProjection).getConnectionWD().add(connw);
                            }
                            else
                            {
                                ((Projection)currentProjection).getConnection().add((Connection)connc);
                            }

                            connc.setPreSegmentId(pre_seg_id);
                            connc.setPostSegmentId(post_seg_id);
                            connc.setPreFractionAlong(pre_fract_along);
                            connc.setPostFractionAlong(post_fract_along);
                            connc.setPreCellId("../"+currentProjection.getPresynapticPopulation()+"/"+pre_cell_id+"/"+populationComponent.get(currentProjection.getPresynapticPopulation()));
                            connc.setPostCellId("../"+currentProjection.getPostsynapticPopulation()+"/"+post_cell_id+"/"+populationComponent.get(currentProjection.getPostsynapticPopulation()));

                            connc.setId(id);
                        }

                        if (currentProjectionType.equals("electricalProjection"))
                        {
                            BaseConnectionNewFormat conn = new ElectricalConnection();

                            if (populationUsesList.get(currentProjection.getPresynapticPopulation()) ||
                                populationUsesList.get(currentProjection.getPostsynapticPopulation()))
                            {
                                if ( (weight_col>=0 && weight!=1))
                                {
                                    ElectricalConnectionInstanceW connw = new ElectricalConnectionInstanceW();
                                    connw.setWeight(weight);
                                    conn = connw;
                                    ((ElectricalProjection)currentProjection).getElectricalConnectionInstanceW().add(connw);
                                }
                                else
                                {
                                    ElectricalConnectionInstance connc = new ElectricalConnectionInstance();
                                    conn = connc;
                                    ((ElectricalProjection)currentProjection).getElectricalConnectionInstance().add((ElectricalConnectionInstance)connc);

                                }
                                conn.setPreCell("../"+currentProjection.getPresynapticPopulation()+"/"+pre_cell_id+"/"+populationComponent.get(currentProjection.getPresynapticPopulation()));
                                conn.setPostCell("../"+currentProjection.getPostsynapticPopulation()+"/"+post_cell_id+"/"+populationComponent.get(currentProjection.getPostsynapticPopulation()));
                            }
                            else
                            {
                                conn.setPreCell(pre_cell_id+"");
                                conn.setPostCell(post_cell_id+"");
                                ((ElectricalProjection)currentProjection).getElectricalConnection().add((ElectricalConnection)conn);
                            }

                            conn.setPreSegment(pre_seg_id);
                            conn.setPostSegment(post_seg_id);
                            conn.setPreFractionAlong(pre_fract_along);
                            conn.setPostFractionAlong(post_fract_along);
                            conn.setId(id);
                            ((ElectricalConnection)conn).setSynapse(projectionSynapse.get(currentProjection.getId()));
                        }

                        if (currentProjectionType.equals("continuousProjection"))
                        {
                            BaseConnectionNewFormat conn = new ContinuousConnection();

                            if (populationUsesList.get(currentProjection.getPresynapticPopulation()) ||
                                populationUsesList.get(currentProjection.getPostsynapticPopulation()))
                            {
                                if ( (weight_col>=0 && weight!=1))
                                {
                                    ContinuousConnectionInstanceW connw = new ContinuousConnectionInstanceW();
                                    connw.setWeight(weight);
                                    conn = connw;
                                    ((ContinuousProjection)currentProjection).getContinuousConnectionInstanceW().add(connw);
                                }
                                else
                                {
                                    ContinuousConnectionInstance connc = new ContinuousConnectionInstance();
                                    conn = connc;
                                    ((ContinuousProjection)currentProjection).getContinuousConnectionInstance().add((ContinuousConnectionInstance)connc);

                                }
                                conn.setPreCell("../"+currentProjection.getPresynapticPopulation()+"/"+pre_cell_id+"/"+populationComponent.get(currentProjection.getPresynapticPopulation()));
                                conn.setPostCell("../"+currentProjection.getPostsynapticPopulation()+"/"+post_cell_id+"/"+populationComponent.get(currentProjection.getPostsynapticPopulation()));
                            }
                            else
                            {
                                conn.setPreCell(pre_cell_id+"");
                                conn.setPostCell(post_cell_id+"");
                                ((ContinuousProjection)currentProjection).getContinuousConnection().add((ContinuousConnection)conn);
                            }

                            conn.setPreSegment(pre_seg_id);
                            conn.setPostSegment(post_seg_id);
                            conn.setPreFractionAlong(pre_fract_along);
                            conn.setPostFractionAlong(post_fract_along);
                            conn.setId(id);
                            ((ContinuousConnection)conn).setPreComponent(projectionComponentPre.get(currentProjection.getId()));
                            ((ContinuousConnection)conn).setPostComponent(projectionComponentPost.get(currentProjection.getId()));

                        }
                    }
                     
                }
            }
            
        }
        else if (currentInputList !=null)
        {
            printv("Adding info for: "+ currentInputList.getId());
            
            int id_col = -1;
            int targ_col = -1;
            int seg_col = -1;
            int fract_col = -1;
            int weight_col = -1;


            for (Attribute attribute : attrs) 
            {
                String storedInColumn = Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName());
                int colNum = attribute.getName().indexOf("column_")==0 ? Integer.parseInt(attribute.getName().substring("column_".length())) : -1;

                if (storedInColumn.equals("id"))
                {
                    id_col = colNum;
                }
                if (storedInColumn.equals("target_cell_id"))
                {
                    targ_col = colNum;
                }
                if (storedInColumn.equals("segment_id"))
                {
                    seg_col = colNum;
                }
                if (storedInColumn.equals("fraction_along"))
                {
                    fract_col = colNum;
                }
                if (storedInColumn.equals("weight"))
                {
                    weight_col = colNum;
                }
            }
                    
            for(int i = 0;i<data.length;i++)
            {
                printv("Adding point "+i);
                
                int id = id_col>=0 ? (int)data[i][id_col] : i;
                
                int cellId = targ_col>=0 ? (int)data[i][targ_col] : 0;
                
                int segmentId = seg_col>=0 ? (int)data[i][seg_col] : 0;
                float fractionAlong = fract_col>=0 ? data[i][fract_col] : 0.5f;
                              
                Input input = new Input();
                
                if (weight_col>=0)
                {
                    InputW inputw = new InputW();
                    inputw.setWeight(data[i][weight_col]);
                    input = inputw;
                    currentInputList.getInputW().add(inputw);
                }
                else
                {
                    currentInputList.getInput().add(input);
                }
                
                input.setId(id);
                input.setTarget("../"+currentInputList.getPopulation()+"/"+cellId+"/"+populationComponent.get(currentInputList.getPopulation()));
                input.setSegmentId(segmentId);
                input.setFractionAlong(fractionAlong);
                
                
            }
        }
        
    }
        
        
    protected void parseGroup(Group g) throws Hdf5Exception, NeuroMLException
    {
        startGroup(g);
                
        java.util.List members = g.getMemberList();

        // NOTE: parsing contents 3 times to ensure subgroups are handled before datasets
        // and populations are handled before projections/inputs
        // The former is mainly because synapse_props groups will need to be parsed before dataset of connections  
       
        
        for (int j=0; j<members.size(); j++)
        {
            HObject obj = (HObject)members.get(j);
            
            if (obj instanceof Group)
            {
                Group subGroup = (Group)obj;
                
                if(subGroup.getName().startsWith("population_"))
                {
                    printv("---------    Found a sub group (pop): "+subGroup.getName()+" "+subGroup.getName().startsWith("population_"));

                    parseGroup(subGroup);
                }
            }
        }
        for (int j=0; j<members.size(); j++)
        {
            HObject obj = (HObject)members.get(j);
            
            if (obj instanceof Group)
            {
                Group subGroup = (Group)obj;
                
                if(!subGroup.getName().startsWith("population_"))
                {
                    printv("---------    Found a sub group (non pop): "+subGroup.getName()+" "+subGroup.getName().startsWith("population_"));

                    parseGroup(subGroup);
                }
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
            files = new String[]{"src/test/resources/examples/MediumNet.net.nml.h5"};
            files = new String[]{"src/test/resources/examples/complete.nml.h5"};
            files = new String[]{"../git/ca1/NeuroML2/network/PINGNet_0_1.net.nml.h5"};
            //files = new String[]{"/home/padraig/git/osb-model-validation/utilities/local_test/netpyneshowcase/NeuroML2/scaling/Balanced.net.nml.h5"};
            
            for (String file: files)
            {
                File h5File = new File(file);

                NeuroMLHDF5Reader nmlReader = new NeuroMLHDF5Reader();
                nmlReader.setVerbose(true);

                nmlReader.parse(h5File, true);
                
                NeuroMLDocument nml2Doc = nmlReader.getNeuroMLDocument();
                System.out.println("File loaded: "+file+"\n"+NeuroMLConverter.summary(nml2Doc));
                
                NeuroML2Validator nmlv = new NeuroML2Validator();
                nmlv.setBaseDirectory(h5File.getAbsoluteFile().getParentFile());
                nmlv.validateWithTests(nml2Doc);
                System.out.println("Status: "+nmlv.getValidity());
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
