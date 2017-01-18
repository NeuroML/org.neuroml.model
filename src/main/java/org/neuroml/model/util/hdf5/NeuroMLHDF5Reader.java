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
    /*
    boolean inPopulations = false;
    boolean inProjections = false;
    boolean inInputs = false;*/
    
    Network currentNetwork = null;
    
    Population currentPopulation = null;
    Projection currentProjection = null;
    String currentInput = null;
   
    NeuroMLConverter neuromlConverter;
    NeuroMLDocument neuroMLDocument;
    boolean verbose = false;

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
    
    public void parse(File hdf5File, boolean includeConnections) throws Hdf5Exception, NeuroMLException 
    {
        if (includeConnections) 
        {
            throw new NeuroMLException("Not yet implemented!!");
        }
        H5File h5File = Hdf5Utils.openForRead(hdf5File);
        
        Group root = Hdf5Utils.getRootGroup(h5File);
        printv("root: "+root);
        
        parseGroup(root);
        
        Hdf5Utils.close(h5File);
    }
        
        
    public void startGroup(Group g) throws Hdf5Exception, NeuroMLException
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
            
           
        }/*
        else if (g.getName().startsWith(NetworkMLConstants.SYN_PROPS_ELEMENT+"_") && inProjections)
        {
            String name = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.SYN_TYPE_ATTR);
            
            ConnSpecificProps cp = new ConnSpecificProps(name);
            
            
            String internalDelay = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INTERNAL_DELAY_ATTR);
            if (internalDelay!=null)
                cp.internalDelay = (float)UnitConverter.getTime(Float.parseFloat(internalDelay), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            
            // Lump them in to the internal delay...
            String preDelay = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.PRE_DELAY_ATTR);
            if (preDelay!=null)
                cp.internalDelay = cp.internalDelay + (float)UnitConverter.getTime(Float.parseFloat(preDelay), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            String postDelay = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.POST_DELAY_ATTR);
            if (postDelay!=null)
                cp.internalDelay = cp.internalDelay + (float)UnitConverter.getTime(Float.parseFloat(postDelay), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            
            cp.weight = Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.WEIGHT_ATTR));
            
            String propDelay = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.PROP_DELAY_ATTR);
            if (propDelay!=null)
                globAPDelay = (float)UnitConverter.getTime(Float.parseFloat(propDelay), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            printv("Found: "+ cp);
            
            globConnProps.add(cp);
        }
        else if (g.getName().equals(NetworkMLConstants.INPUTS_ELEMENT))
        {
            printv("Found the Inputs group");
            inInputs = true;
            
            String units = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.UNITS_ATTR);
            
            inputUnitSystem = UnitConverter.getUnitSystemIndex(units);
        }
        else if (g.getName().startsWith(NetworkMLConstants.INPUT_ELEMENT) && inInputs)
        {
            // The table of input sites is within the input group so get sites from here
            
            String inputName = g.getName().substring(6);
            
            //String inputName = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_ELEMENT);
            
            printv("Found an Input: "+ inputName);
            //inInput = true;
            
            if (project.elecInputInfo.getStim(inputName) == null)
            {
                throw new Hdf5Exception("Error: there is an electrical input with name: "+ inputName+" specified in " +
                        "that file, but no such electrical input exists in the project. Add one to allow import of this file");
            }
            // Get the atributes of the Input and compare them with the attributes within the project
            // Test to find out what type of input this is

        }
        else if (g.getName().startsWith("IClamp") && inInputs)
        {
            String inputName = g.getParent().getName().substring(6);
            // Get the input sites from the table

            String cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR);
            if (cellGroup==null)
            {
                cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_CELLGROUP_OLD_ATTR); // check old name
            }

            float readDelay = (float)UnitConverter.getTime(Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_DELAY_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            float readDuration = (float)UnitConverter.getTime(Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_DUR_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            float readAmp = (float)UnitConverter.getCurrent(Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_AMP_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            StimulationSettings nextStim = project.elecInputInfo.getStim(inputName);
            ElectricalInput myElectricalInput = nextStim.getElectricalInput();
            IClamp ic = (IClamp)myElectricalInput;
            
            printv("Found an IClamp Input"); 
            
            float currDelay=-1, currDur=-1, currAmp=-1;
            
            
            
            currDelay = ic.getDel().getNominalNumber();
            currDur = ic.getDur().getNominalNumber();     
            currAmp = ic.getAmp().getNominalNumber();
            
            
            if ((!project.elecInputInfo.getStim(inputName).getCellGroup().equals(cellGroup))
                   ||(readDelay!= currDelay)
                   ||(readDuration != currDur)
                   ||(readAmp != currAmp))                    
            {
                throw new Hdf5Exception("Error: the input properties of the file do not match those in the project for input "+ inputName+"" +
                        "\nreadDelay: "+readDelay+", currDelay: "+currDelay+
                        "\nreadDuration: "+readDuration+", currDur: "+currDur+
                        "\nreadAmp: "+readAmp+", currAmp: "+currAmp+", str: "+Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_AMP_ATTR));
            }
            currentInput = inputName;
        }
        else if (g.getName().startsWith("RandomSpikeTrain") && inInputs)
        {
            String inputName = g.getParent().getName().substring(6);
            // Get the input sites from the table
            String cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR);
            if (cellGroup==null)
            {
                cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_CELLGROUP_OLD_ATTR); // check old name
            }

            float frequency = (float)UnitConverter.getRate(Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.RND_STIM_FREQ_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            String mechanism = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.RND_STIM_MECH_ATTR);
            
            StimulationSettings nextStim = project.elecInputInfo.getStim(inputName);
            ElectricalInput myElectricalInput = nextStim.getElectricalInput();
            RandomSpikeTrain rs = (RandomSpikeTrain)myElectricalInput;
            
            printv("Found an Random Spike Train Input");
            
            if ((!project.elecInputInfo.getStim(inputName).getCellGroup().equals(cellGroup))||
                    frequency != rs.getRate().getFixedNum()||
                    !rs.getSynapseType().equals(mechanism))                    
            {
                throw new Hdf5Exception("Error: the input properties of the file do not match those in the project for input "+ inputName);
            }
            currentInput = inputName;
        }        */
        
    }
    
    
    
    
    public void endGroup(Group g) throws Hdf5Exception
    {
        printv("-----   Going out of a group: "+g.getFullName());
        
        if (g.getName().equals(NeuroMLElements.INPUT_LIST))
        {
            currentInput = null;
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
        else if (g.getName().startsWith(NeuroMLElements.CONNECTION))
        {
            //localAPDelay = 0;
        }
        
    }
    /*
    private ArrayList<String> getConnectionSynTypes()
    {
        ArrayList<String> a = new ArrayList<String>();
        
        for(ConnSpecificProps c: globConnProps)
        {
            a.add(c.synapseType);
        }
        return a;
    }*/
    
    
    public void dataSet(Dataset d) throws Hdf5Exception
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
                /*
                PositionRecord posRec = new PositionRecord(id,x,y,z);
                if (data[0].length==5)
                {
                posRec.setNodeId((int)data[i][4]);
                }
                this.project.generatedCellPositions.addPosition(currentPopulation, posRec);*/
            }
        }
        if (currentProjection!=null)
        {
            printv("Adding info for Projection: "+ currentProjection);
            
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
            
            for(int i = 0;i<data.length;i++)
            {
                int pre_seg_id = 0;
                float pre_fract_along = 0.5f;
                int post_seg_id = 0;
                float post_fract_along = 0.5f;
                
                int id = (int)data[i][id_col];
                int pre_cell_id = (int)data[i][pre_cell_id_col];
                int post_cell_id = (int)data[i][post_cell_id_col];
                
                
                if (pre_segment_id_col>=0) 
                    pre_seg_id = (int)data[i][pre_segment_id_col];
                if (pre_fraction_along_col>=0) 
                    pre_fract_along = data[i][pre_fraction_along_col];
                
                if (post_segment_id_col>=0) 
                    post_seg_id = (int)data[i][post_segment_id_col];
                if (post_fraction_along_col>=0) 
                    post_fract_along = data[i][post_fraction_along_col];
                
                
                    //(float)UnitConverter.getTime(XXXXXXXXX, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
                //if (prop_delay_col>=0) 
                //    prop_delay = (float)UnitConverter.getTime(data[i][prop_delay_col], projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                
                Connection conn = new Connection();
                conn.setId(id);
                conn.setPreCellId("../"+currentProjection.getPresynapticPopulation()+"/"+pre_cell_id+"/???");
                conn.setPostCellId("../"+currentProjection.getPostsynapticPopulation()+"/"+post_cell_id+"/???");
                conn.setPreSegmentId(pre_seg_id);
                conn.setPostSegmentId(post_seg_id);
                conn.setPreFractionAlong(pre_fract_along);
                conn.setPostFractionAlong(post_fract_along);
                currentProjection.getConnection().add(conn);
                /*
                this.project.generatedNetworkConnections.addSynapticConnection(currentProjection,
                                                                               GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                                               pre_cell_id, 
                                                                               pre_seg_id,
                                                                               pre_fract_along,
                                                                               post_cell_id,
                                                                               post_seg_id,
                                                                               post_fract_along,
                                                                               prop_delay,
                                                                               props);*/
            }
            
        }/*
        if (inInputs && currentInput !=null)
        {
            printv("Adding info for: "+ currentInput);
            StimulationSettings nextStim = project.elecInputInfo.getStim(currentInput);
            ElectricalInput myElectricalInput = nextStim.getElectricalInput();
            String electricalInputType = myElectricalInput.getType();
            String cellGroup = nextStim.getCellGroup();
                    
            for(int i = 0;i<data.length;i++)
            {
                Float fileCellId = data[i][0];
                Float fileSegmentId = data[i][1];
                Float fractionAlong = data[i][2];
                int cellId = fileCellId.intValue();
                int segmentId = fileSegmentId.intValue();                
                
                SingleElectricalInput singleElectricalInputFromFile 
                        = new SingleElectricalInput(electricalInputType,
                                                    cellGroup,
                                                    cellId,
                                                    segmentId,
                                                    fractionAlong,
                                                    null);
               
                this.project.generatedElecInputs.addSingleInput(currentInput,singleElectricalInputFromFile);
            }
        }
        */
        
    }
        
        
    public void parseGroup(Group g) throws Hdf5Exception, NeuroMLException
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
            files = new String[]{"src/test/resources/tmp/MediumNet.net.nml.h5"};
            
            for (String file: files)
            {
                File h5File = new File(file);

                NeuroMLHDF5Reader nmlReader = new NeuroMLHDF5Reader();
                nmlReader.setVerbose(true);

                nmlReader.parse(h5File, false);

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
