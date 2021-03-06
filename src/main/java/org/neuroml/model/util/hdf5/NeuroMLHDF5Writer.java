/*
   Originally based on code from neuroConstruct: https://github.com/NeuralEnsemble/neuroConstruct

   @ Author: p.gleeson 
*/

package org.neuroml.model.util.hdf5;


import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import ncsa.hdf.utils.SetNatives;
import org.neuroml.model.Connection;
import org.neuroml.model.InputList;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;
import org.neuroml.model.Property;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLElements;
import org.neuroml.model.util.NeuroMLException;


public class NeuroMLHDF5Writer
{

    public static String NEUROML_TOP_LEVEL_CONTENT = "neuroml_top_level";
    
    public NeuroMLHDF5Writer()
    {
        super();
    }

    public static File createNeuroMLH5file(NeuroMLDocument neuroml, File file) throws Hdf5Exception, IOException
    {

        SetNatives.getInstance().setHDF5Native(System.getProperty("user.dir"));

        H5File h5File = Hdf5Utils.createH5file(file);

        Hdf5Utils.open(h5File);

        Group root = Hdf5Utils.getRootGroup(h5File);
        Group nmlGroup;   

        try
        {
            nmlGroup = h5File.createGroup(NeuroMLElements.NEUROML_ROOT, root);
            
            Hdf5Utils.addStringAttribute(nmlGroup, "id", neuroml.getId(), h5File);
            Hdf5Utils.addStringAttribute(nmlGroup, "notes", neuroml.getNotes(), h5File);
            

            for (Network network: neuroml.getNetwork()) {
                Group netGroup = h5File.createGroup(NeuroMLElements.NETWORK, nmlGroup);
                Hdf5Utils.addStringAttribute(netGroup, "id", network.getId(), h5File);
                Hdf5Utils.addStringAttribute(netGroup, "notes", network.getNotes(), h5File);
                if (network.getTemperature()!=null)
                {
                    Hdf5Utils.addStringAttribute(netGroup, "temperature", network.getTemperature(), h5File);
                }

                for (Population population: network.getPopulation())
                {
                    Group popGroup = h5File.createGroup(NeuroMLElements.POPULATION+"_"+population.getId(), netGroup);
                    Hdf5Utils.addStringAttribute(popGroup, "id", population.getId(), h5File);
                    Hdf5Utils.addStringAttribute(popGroup, "component", population.getComponent(), h5File);
                    Hdf5Utils.addStringAttribute(popGroup, "type", population.getType()!= null ? population.getType().value() : "population", h5File);
                    int size;
                    
                    if (!population.getInstance().isEmpty())
                    {
                        size = population.getInstance().size();
                    }
                    else
                    {
                        size = population.getSize();
                    }
                    
                    
                    Hdf5Utils.addStringAttribute(popGroup, "size", size+"", h5File);
                    
                    for (Property p: population.getProperty())
                        Hdf5Utils.addStringAttribute(popGroup, "property:"+p.getTag(), p.getValue(), h5File);
                    
                    if (!population.getInstance().isEmpty())
                    {
                        Datatype dtype = getPopDatatype(h5File);
                        int numColumns = 4; // id, x, y, z
                        long[] dims2D = {population.getInstance().size(), numColumns};

                        float[] posArray = new float[population.getInstance().size() * numColumns];


                        for (int i=0; i<population.getInstance().size(); i++)
                        {
                            Instance inst = population.getInstance().get(i);
                            Location p = inst.getLocation();
                            posArray[i * numColumns + 0] = inst.getId().intValue();
                            posArray[i * numColumns + 1] = p.getX();
                            posArray[i * numColumns + 2] = p.getY();
                            posArray[i * numColumns + 3] = p.getZ();
                        }


                        Dataset dataset = h5File.createScalarDS
                           (population.getId(), popGroup, dtype, dims2D, null, null, 0, posArray);
                        /*
                        Attribute attr0 = Hdf5Utils.getSimpleAttr("column_0", NetworkMLConstants.INSTANCE_ID_ATTR, h5File);
                        dataset.writeMetadata(attr0);
                        Attribute attr1 = Hdf5Utils.getSimpleAttr("column_1", NetworkMLConstants.LOC_X_ATTR, h5File);
                        dataset.writeMetadata(attr1);
                        Attribute attr2 = Hdf5Utils.getSimpleAttr("column_2", NetworkMLConstants.LOC_Y_ATTR, h5File);
                        dataset.writeMetadata(attr2);
                        Attribute attr3 = Hdf5Utils.getSimpleAttr("column_3", NetworkMLConstants.LOC_Z_ATTR, h5File);
                        dataset.writeMetadata(attr3);*/
                    }
                }
                
                if (network.getElectricalProjection().size()>0)
                    throw new NeuroMLException("Electrical projections can't yet be exported to HDF5...");
                if (network.getContinuousProjection().size()>0)
                    throw new NeuroMLException("Continuous projections can't yet be exported to HDF5...");
                
                for (Projection proj: network.getProjection())
                {
                    Group projGroup = h5File.createGroup(NeuroMLElements.PROJECTION+"_"+proj.getId(), netGroup);
                    Hdf5Utils.addStringAttribute(projGroup, "id", proj.getId(), h5File);
                    Hdf5Utils.addStringAttribute(projGroup, "presynapticPopulation", proj.getPresynapticPopulation(), h5File);
                    Hdf5Utils.addStringAttribute(projGroup, "postsynapticPopulation", proj.getPostsynapticPopulation(), h5File);
                    Hdf5Utils.addStringAttribute(projGroup, "synapse", proj.getSynapse(), h5File);
                    Hdf5Utils.addStringAttribute(projGroup, "type", "projection", h5File);
                    
                    if (!proj.getConnectionWD().isEmpty())
                        throw new NeuroMLException("ConnectionWDs can't yet be exported to HDF5...");
                        
                    
                    if (!proj.getConnection().isEmpty())
                    {
                        
                        Datatype dtype = getProjDatatype(h5File);
                        

                        ArrayList<String> columnsNeeded = new ArrayList<String>();

                        columnsNeeded.add("id");
                        columnsNeeded.add("pre_cell_id");
                        columnsNeeded.add("post_cell_id");
                        
                        //TODO: optimise!!!
                        columnsNeeded.add("pre_segment_id");
                        columnsNeeded.add("post_segment_id");
                        columnsNeeded.add("pre_fraction_along");
                        columnsNeeded.add("post_fraction_along");

                        long[] dims2D = {proj.getConnection().size(), columnsNeeded.size()};

                        float[] projArray = new float[proj.getConnection().size() * columnsNeeded.size()];

                        for (int i = 0; i < proj.getConnection().size(); i++)
                        {
                            Connection conn = proj.getConnection().get(i);

                            int row = 0;
                            projArray[i * columnsNeeded.size() +row] = i;
                            row++;

                            projArray[i * columnsNeeded.size() + row] = NeuroMLConverter.getPreCellId(conn);
                            row++;

                            projArray[i * columnsNeeded.size() + row] = NeuroMLConverter.getPostCellId(conn);
                            row++;
                            
                            projArray[i * columnsNeeded.size() + row] = conn.getPreSegmentId();
                            row++;
                            
                            projArray[i * columnsNeeded.size() + row] = conn.getPostSegmentId();
                            row++;
                            
                            projArray[i * columnsNeeded.size() + row] = conn.getPreFractionAlong();
                            row++;
                            
                            projArray[i * columnsNeeded.size() + row] = conn.getPostFractionAlong();
                            row++;

                        }

                        Dataset projDataset = h5File.createScalarDS(proj.getId(), projGroup, dtype, dims2D, null, null, 0, projArray);

                        for(int i=0;i<columnsNeeded.size();i++)
                        {
                            Attribute attr = Hdf5Utils.getSimpleAttr("column_"+i, columnsNeeded.get(i), h5File);
                            projDataset.writeMetadata(attr);
                        }


                        System.out.println("Dataset compression: " + projDataset.getCompression());
                    }
                }
                
                for (InputList il: network.getInputList())
                {
                    Group ilGroup = h5File.createGroup(NeuroMLElements.INPUT_LIST+"_"+il.getId(), netGroup);
                    Hdf5Utils.addStringAttribute(ilGroup, "id", il.getId(), h5File);
                    Hdf5Utils.addStringAttribute(ilGroup, "component", il.getComponent(), h5File);
                    Hdf5Utils.addStringAttribute(ilGroup, "population", il.getPopulation(), h5File);

                    int inputsNumCols = 3; // Cell ID, Segment ID, Fraction Along Segment

                    int inputNumber = il.getInput().size();

                    long[] dims2D = {inputNumber, inputsNumCols};

                    float[] sitesArray = new float[inputNumber * inputsNumCols];

                    Datatype dtype = getInputDatatype(h5File);
                    
                    for (int i=0; i<inputNumber; i++)
                    {
                        sitesArray[i * inputsNumCols + 0] = NeuroMLConverter.getTargetCellId(il.getInput().get(i));
                        sitesArray[i * inputsNumCols + 1] = NeuroMLConverter.getSegmentId(il.getInput().get(i));
                        sitesArray[i * inputsNumCols + 2] = NeuroMLConverter.getFractionAlong(il.getInput().get(i));
                    }   
                    
                    Dataset sitesDataset = h5File.createScalarDS(il.getId(), ilGroup, dtype, dims2D, null, null, 0, sitesArray);
                    
                    Attribute attr0 = Hdf5Utils.getSimpleAttr("column_0", "target_cell_id", h5File);
                    sitesDataset.writeMetadata(attr0);
                    Attribute attr1 = Hdf5Utils.getSimpleAttr("column_1", "segment_id", h5File);
                    sitesDataset.writeMetadata(attr1);
                    Attribute attr2 = Hdf5Utils.getSimpleAttr("column_2", "fraction_along", h5File);
                    sitesDataset.writeMetadata(attr2);
                }
            }

            neuroml.getNetwork().clear();

            NeuroMLConverter neuromlConverter = new NeuroMLConverter();
            String xml = neuromlConverter.neuroml2ToXml(neuroml);
            
            Hdf5Utils.addStringAttribute(nmlGroup, NEUROML_TOP_LEVEL_CONTENT, xml, h5File);


        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to create group in HDF5 file: "+ h5File.getFilePath(), ex);
        }


        /*


        Iterator<String> nCs = gnc.getNamesNetConnsIter();

        while(nCs.hasNext())
        {
            String nc = nCs.next();

            ArrayList<SingleSynapticConnection> conns = gnc.getSynapticConnections(nc);

            try
            {
                Group projGroup = h5File.createGroup(NetworkMLConstants.PROJECTION_ELEMENT +"_" + nc, projsGroup);


                Attribute nameAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.PROJ_NAME_ATTR, nc, h5File);
                projGroup.writeMetadata(nameAttr);

                String src = null;
                String tgt = null;
                Vector<SynapticProperties>  globalSynPropList = null;

                if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(nc))
                {
                    src = project.morphNetworkConnectionsInfo.getSourceCellGroup(nc);
                    tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(nc);
                    globalSynPropList = project.morphNetworkConnectionsInfo.getSynapseList(nc);
                }

                else if (project.volBasedConnsInfo.isValidVolBasedConn(nc))
                {
                    src = project.volBasedConnsInfo.getSourceCellGroup(nc);
                    src = project.volBasedConnsInfo.getTargetCellGroup(nc);
                    globalSynPropList = project.volBasedConnsInfo.getSynapseList(nc);
                }

                Attribute srcAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.SOURCE_ATTR, src, h5File);
                projGroup.writeMetadata(srcAttr);
                Attribute tgtAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.TARGET_ATTR, tgt, h5File);
                projGroup.writeMetadata(tgtAttr);

                float globWeight = 1;
                float globDelay = 0;

                for(SynapticProperties sp:  globalSynPropList)
                {
                    Group synPropGroup = h5File.createGroup(NetworkMLConstants.SYN_PROPS_ELEMENT +"_" + sp.getSynapseType(), projGroup);

                    Attribute synTypeAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.SYN_TYPE_ATTR, sp.getSynapseType(), h5File);
                    synPropGroup.writeMetadata(synTypeAttr);

                    globDelay = (float)UnitConverter.getTime(sp.getDelayGenerator().getNominalNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                    Attribute synTypeDelay = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INTERNAL_DELAY_ATTR, globDelay+"", h5File);
                    synPropGroup.writeMetadata(synTypeDelay);

                    globWeight = sp.getWeightsGenerator().getNominalNumber();
                    Attribute synTypeWeight = Hdf5Utils.getSimpleAttr(NetworkMLConstants.WEIGHT_ATTR, globWeight+"", h5File);
                    synPropGroup.writeMetadata(synTypeWeight);

                    Attribute synTypeThreshold = Hdf5Utils.getSimpleAttr(NetworkMLConstants.THRESHOLD_ATTR, 
                            (float)UnitConverter.getVoltage(sp.getThreshold(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"", h5File);

                    synPropGroup.writeMetadata(synTypeThreshold);
                }

                ArrayList<String> columnsNeeded = new ArrayList<String>();

                columnsNeeded.add(NetworkMLConstants.CONNECTION_ID_ATTR);
                columnsNeeded.add(NetworkMLConstants.PRE_CELL_ID_ATTR);
                columnsNeeded.add(NetworkMLConstants.POST_CELL_ID_ATTR);

                for (int i = 0; i < conns.size(); i++)
                {
                    SingleSynapticConnection conn = conns.get(i);

                    if (conn.sourceEndPoint.location.getSegmentId()!=0 && !columnsNeeded.contains(NetworkMLConstants.PRE_SEGMENT_ID_ATTR))
                        columnsNeeded.add(NetworkMLConstants.PRE_SEGMENT_ID_ATTR);

                    if (conn.sourceEndPoint.location.getFractAlong()!=SegmentLocation.DEFAULT_FRACT_CONN && 
                            !columnsNeeded.contains(NetworkMLConstants.PRE_FRACT_ALONG_ATTR))
                        columnsNeeded.add(NetworkMLConstants.PRE_FRACT_ALONG_ATTR);

                    if (conn.targetEndPoint.location.getSegmentId()!=0 && !columnsNeeded.contains(NetworkMLConstants.POST_SEGMENT_ID_ATTR))
                        columnsNeeded.add(NetworkMLConstants.POST_SEGMENT_ID_ATTR);

                    if (conn.targetEndPoint.location.getFractAlong()!=SegmentLocation.DEFAULT_FRACT_CONN && 
                            !columnsNeeded.contains(NetworkMLConstants.POST_FRACT_ALONG_ATTR))
                        columnsNeeded.add(NetworkMLConstants.POST_FRACT_ALONG_ATTR);

                    if (conn.apPropDelay!=0)
                    {
                        for(SynapticProperties sp:  globalSynPropList)
                        {
                            String colName = NetworkMLConstants.PROP_DELAY_ATTR +"_"+sp.getSynapseType();
                            if (!columnsNeeded.contains(colName))
                            {
                                columnsNeeded.add(colName);
                            }
                        }
                    }

                    if (conn.props!=null)
                    {
                        for(ConnSpecificProps prop: conn.props)
                        {
                            if(prop.weight!=1 && !columnsNeeded.contains(NetworkMLConstants.WEIGHT_ATTR+"_"+prop.synapseType))
                                columnsNeeded.add(NetworkMLConstants.WEIGHT_ATTR+"_"+prop.synapseType);

                            if(prop.internalDelay!=0 && !columnsNeeded.contains(NetworkMLConstants.INTERNAL_DELAY_ATTR+"_"+prop.synapseType))
                                columnsNeeded.add(NetworkMLConstants.INTERNAL_DELAY_ATTR+"_"+prop.synapseType);
                        }
                    }
                }


                Datatype dtype = getProjDatatype(h5File);

                long[] dims2D = {conns.size(), columnsNeeded.size()};

                float[] projArray = new float[conns.size() * columnsNeeded.size()];

                for (int i = 0; i < conns.size(); i++)
                {
                    SingleSynapticConnection conn = conns.get(i);

                    int row = 0;
                    projArray[i * columnsNeeded.size() +row] = i;
                    row++;

                    projArray[i * columnsNeeded.size() + row] = conn.sourceEndPoint.cellNumber;
                    row++;

                    projArray[i * columnsNeeded.size() + row] = conn.targetEndPoint.cellNumber;
                    row++;

                    if (columnsNeeded.contains(NetworkMLConstants.PRE_SEGMENT_ID_ATTR))
                    {
                        projArray[i * columnsNeeded.size() + row] = conn.sourceEndPoint.location.getSegmentId();
                        row++;
                    }

                    if (columnsNeeded.contains(NetworkMLConstants.PRE_FRACT_ALONG_ATTR))
                    {
                        projArray[i * columnsNeeded.size() + row] = conn.sourceEndPoint.location.getFractAlong();
                        row++;
                    }


                    if (columnsNeeded.contains(NetworkMLConstants.POST_SEGMENT_ID_ATTR))
                    {
                        projArray[i * columnsNeeded.size() + row] = conn.targetEndPoint.location.getSegmentId();
                        row++;
                    }

                    if (columnsNeeded.contains(NetworkMLConstants.POST_FRACT_ALONG_ATTR))
                    {
                        projArray[i * columnsNeeded.size() + row] = conn.targetEndPoint.location.getFractAlong();
                        row++;
                    }
                    for(SynapticProperties sp:  globalSynPropList)
                    {
                        String colName = NetworkMLConstants.PROP_DELAY_ATTR +"_"+sp.getSynapseType();
                        if (columnsNeeded.contains(colName))
                        {
                            projArray[i * columnsNeeded.size() + row] = 
                                    (float)UnitConverter.getTime(conn.apPropDelay, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                            row++;
                        }
                    }


                    if (conn.props!=null)
                    {
                        for(ConnSpecificProps prop: conn.props)
                        {
                            if(columnsNeeded.contains(NetworkMLConstants.WEIGHT_ATTR+"_"+prop.synapseType))
                            {
                                projArray[i * columnsNeeded.size() + row] = prop.weight;
                                row++;
                            }
                            if(columnsNeeded.contains(NetworkMLConstants.INTERNAL_DELAY_ATTR+"_"+prop.synapseType))
                            {
                                projArray[i * columnsNeeded.size() + row] = 
                                        (float)UnitConverter.getTime(prop.internalDelay, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                                row++;
                            }

                        }
                    }

                }

                Dataset projDataset = h5File.createScalarDS(nc, projGroup, dtype, dims2D, null, null, 0, projArray);

                for(int i=0;i<columnsNeeded.size();i++)
                {
                    Attribute attr = Hdf5Utils.getSimpleAttr("column_"+i, columnsNeeded.get(i), h5File);
                    projDataset.writeMetadata(attr);
                }


                System.out.println("Dataset compression: " + projDataset.getCompression());

            }
            catch (Exception ex)
            {
                throw new Hdf5Exception("Failed to create group in HDF5 file: " + h5File.getFilePath(), ex);
            }
        }

        // Start of writing the Electrical Inputs into Hdf5 format

        // Create Record Inputs
        //                 - Input either IClamp or Random Spike
        //                      - IClamp Type add attributes Delay, Duration, Amplitude)
        //                      - Random Spike add attributes Frequency, Mechanism) 
        //                              - Sites Group Table of 4 Columns (Cell Group ID, Cell ID, Segment ID, Fraction Along)

        // Create an iterator to navigate through all the input names

        Iterator<String> nEi = gei.getElecInputsItr(); 
        try
        { 
            // Add units for the Inputs

                Attribute unitsAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.UNITS_ATTR, units, h5File);
                inputsGroup.writeMetadata(unitsAttr);


            // loop around all the inputs
            while(nEi.hasNext())        
            {

                String ei = nEi.next();

                // Get the stimulation settings for the input referenc
                StimulationSettings nextStim = project.elecInputInfo.getStim(ei);

                // Get the electrical input for the stim settings
                ElectricalInput myElectricalInput = nextStim.getElectricalInput();

                // Get Input Locations

                ArrayList<SingleElectricalInput> inputsHere =  project.generatedElecInputs.getInputLocations(ei);


                    Group inputGroup = h5File.createGroup(NetworkMLConstants.INPUT_ELEMENT+"_"+ei, inputsGroup);


                    // Build Site Table for both IClamp and RandomSpikeTrain Inputs

                    int inputsNumCols = 3; // Cell ID, Segment ID, Fraction Along Segment

                    int inputNumber = inputsHere.size();

                    long[] dims2D = {inputNumber, inputsNumCols};

                    float[] sitesArray = new float[inputNumber * inputsNumCols];

                    Datatype dtype = getInputDatatype(h5File);

                    // Build array of sites as stim setting

                    for (int i=0; i<inputNumber; i++)
                    {
                        sitesArray[i * inputsNumCols + 0] = inputsHere.get(i).getCellNumber();
                        sitesArray[i * inputsNumCols + 1] = inputsHere.get(i).getSegmentId();
                        sitesArray[i * inputsNumCols + 2] = inputsHere.get(i).getFractionAlong();
                    }               

                    Dataset sitesDataset = h5File.createScalarDS (ei+"_"+"input_sites", inputGroup, dtype, dims2D, null, null, 0, sitesArray);

                    Attribute attr0 = Hdf5Utils.getSimpleAttr("column_0", NetworkMLConstants.INPUT_SITE_CELLID_ATTR, h5File);
                    sitesDataset.writeMetadata(attr0);
                    Attribute attr1 = Hdf5Utils.getSimpleAttr("column_1", NetworkMLConstants.INPUT_SITE_SEGID_ATTR, h5File);
                    sitesDataset.writeMetadata(attr1);
                    Attribute attr2 = Hdf5Utils.getSimpleAttr("column_2", NetworkMLConstants.INPUT_SITE_FRAC_ATTR, h5File);
                    sitesDataset.writeMetadata(attr2);

                    String cellGroup = nextStim.getCellGroup();

                    if (myElectricalInput instanceof IClamp)
                    {
                        IClamp ic = (IClamp)myElectricalInput;

                        Group inputTypeGroup = h5File.createGroup(myElectricalInput.getType()+"_"+"properties", inputGroup);

                        // Get Details of the IClamp attributes


                        String delay = (float)UnitConverter.getTime(ic.getDel().getNominalNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
                        String duration = (float)UnitConverter.getTime(ic.getDur().getNominalNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";     
                        String amp = (float)UnitConverter.getCurrent(ic.getAmp().getNominalNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";


//////////                        ic.getDelay().reset();
//////////                        String delay = (float)UnitConverter.getTime(ic.getDelay().getNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
//////////                        ic.getDuration().reset();
//////////                        String duration = (float)UnitConverter.getTime(ic.getDuration().getNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
//////////                        ic.getAmplitude().reset();   
//////////                        String amp = (float)UnitConverter.getCurrent(ic.getAmplitude().getNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";

                        //Assign them to the attibutes of the group

                        Attribute cellGroupAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR, cellGroup, h5File);
                        Attribute delayAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_DELAY_ATTR, delay, h5File);                    
                        Attribute durationAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_DUR_ATTR, duration, h5File);                    
                        Attribute ampAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_AMP_ATTR, amp, h5File);

                         //add them as attributes to the group

                        inputTypeGroup.writeMetadata(cellGroupAttr);
                        inputTypeGroup.writeMetadata(delayAttr);                    
                        inputTypeGroup.writeMetadata(durationAttr);
                        inputTypeGroup.writeMetadata(ampAttr);                    
                    }
                    else if (myElectricalInput instanceof RandomSpikeTrain)
                    {
                        RandomSpikeTrain rst = (RandomSpikeTrain)myElectricalInput;

                        Group inputTypeGroup = h5File.createGroup(myElectricalInput.getType()+"_"+"properties", inputGroup);

                        // Get details of Random Spike Train Attributes

                        String stimFreq = (float)UnitConverter.getRate(rst.getRate().getFixedNum(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
                        String stimMech = rst.getSynapseType();

                        //Assign them to the attibutes of the group

                        Attribute cellGroupAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR, cellGroup, h5File);
                        Attribute stimFreqAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.RND_STIM_FREQ_ATTR, stimFreq, h5File);                    
                        Attribute stimMechAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.RND_STIM_MECH_ATTR, stimMech, h5File);                    

                        // add them as attributes to the group

                        inputTypeGroup.writeMetadata(cellGroupAttr);                    
                        inputTypeGroup.writeMetadata(stimFreqAttr);                    
                        inputTypeGroup.writeMetadata(stimMechAttr);

                        }
               }
            }
            catch (Exception ex)
            {
                throw new Hdf5Exception("Failed to create group in HDF5 file: " + h5File.getFilePath(), ex);
            }

        */

        //h5File.

        Hdf5Utils.close(h5File);

        System.out.println("Created file: " + file);
        System.out.println("Size: " + file.length()+" bytes");

        return file;
    }

    public static Datatype getPopDatatype(H5File h5File) throws Hdf5Exception
    {
        try
        {

            Datatype popDataType = h5File.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, -1);
            return popDataType;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to get pop datatype in HDF5 file: " + h5File.getFilePath(), ex);

        }

    }

    public static Datatype getProjDatatype(H5File h5File) throws Hdf5Exception
    {
        try
        {
            Datatype projDataType = h5File.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, -1);
            return projDataType;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to get projection datatype in HDF5 file: " + h5File.getFilePath(),ex);

        }

    }

    public static Datatype getInputDatatype(H5File h5File) throws Hdf5Exception
    {
        try
        {
            Datatype projDataType = h5File.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, -1);
            return projDataType;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to get input datatype in HDF5 file: " + h5File.getFilePath(),ex);
        }
    }

    public static void main(String[] args)
    {

        File h5File = new File("../temp/net.h5");
        //File nmlFile = new File("src/test/resources/examples/testnet.nml");
        File nmlFile = new File("src/test/resources/examples/MediumNet.net.nml");
        h5File = new File("src/test/resources/examples/MediumNet.net.nml.h5");
        try
        {
            NeuroMLConverter neuromlConverter = new NeuroMLConverter();
            NeuroMLDocument nmlDoc = neuromlConverter.loadNeuroML(nmlFile);
            
            String summary0 = NeuroMLConverter.summary(nmlDoc);
            System.out.println("nmlDoc loaded: \n"+summary0);

            NeuroMLHDF5Writer.createNeuroMLH5file(nmlDoc, h5File);

            NeuroMLHDF5Reader nmlReader = new NeuroMLHDF5Reader();
            nmlReader.setVerbose(true);

            nmlReader.parse(h5File, true);
            String summary1 = NeuroMLConverter.summary(nmlReader.getNeuroMLDocument());

            System.out.println("File loaded: "+h5File+"\n"+summary1);

            compare(summary0,summary1);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }


    }
    
    
    public static void compare(String s1, String s2) throws Exception
    {
        //System.out.println("Comparing\n"+"\n----------------------------\n"+s1+"\n----------------------------\n"+s2+"\n----------------------------\n");
        String[] s1a = s1.split("\n");
        String[] s2a = s2.split("\n");
        for (int i=0;i< Math.min(s2a.length, s1a.length);i++) {
            if (!s1a[i].equals(s2a[i]))
                System.out.println("Mismatch, line "+i+":\n"+"\n----------------------------\n"+s1a[i]+"\n----------------------------\n"+s2a[i]+"\n----------------------------\n");
            //Assert.assertEquals(s1, s2);
        }
        
        if (!s1.equals(s2)) throw new Exception("Strings not equal!!");
        
        System.out.println("Strings are equal!!"+s1.equals(s2));
    }
}
