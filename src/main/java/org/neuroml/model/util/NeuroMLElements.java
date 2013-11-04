package org.neuroml.model.util;

public class NeuroMLElements {

    public static String ORG_NEUROML_MODEL_VERSION = "1.1.0";
    
    public static String NAMESPACE_URI_VERSION_2 = "http://www.neuroml.org/schema/neuroml2";

    public static String DEFAULT_SCHEMA_LOCATION_VERSION_2_ALPHA = "https://raw.github.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2alpha.xsd";
    public static String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA =  "https://raw.github.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2beta.xsd";
    public static String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA1 = "https://raw.github.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2beta1.xsd";
    public static String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA2 = "https://raw.github.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2beta2.xsd";
    

    public static String TARGET_SCHEMA = "/Schemas/NeuroML2/NeuroML_v2beta2.xsd";
    public static String TARGET_SCHEMA_LOCATION = DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA2;
	

    
    // Ion channels
    public final static String ION_CHANNEL_COMP_TYPE = "baseIonChannel";
    
    // Cells
    public final static String BASE_CELL_COMP_TYPE = "baseCell";
    public final static String BASE_CELL_CAP_COMP_TYPE = "baseCellMembPotCap";
    public final static String CELL_COMP_TYPE = "cell";
    
    public final static String BASE_GATE_COMP_TYPE = "baseGate";
    public final static String BASE_CONC_DEP_RATE_COMP_TYPE = "baseVoltageConcDepRate";
    public final static String BASE_CONC_DEP_VAR_COMP_TYPE = "baseVoltageConcDepVariable";
   
    public final static String BASE_CELL_COMP_TYPE_CAP__I_MEMB = "iMemb";

    public final static String CONC_MODEL_COMP_TYPE = "concentrationModel";
    public final static String CONC_MODEL_SURF_AREA = "surfaceArea";
    public final static String CONC_MODEL_CA_CURR_DENS = "iCa";
    public final static String CONC_MODEL_INIT_CONC = "initialConcentration";
    public final static String CONC_MODEL_INIT_EXT_CONC = "initialExtConcentration";
    public final static String CONC_MODEL_CONC_STATE_VAR = "concentration";

    // External props
    public final static String TEMPERATURE = "temperature";
    public final static String TEMPERATURE_DIM = "temperature";

    // Currents
    public final static String POINT_CURR_CURRENT = "i";
    
    // Synapses
    public final static String BASE_POINT_CURR_COMP_TYPE = "basePointCurrent";
    public final static String BASE_SYNAPSE_COMP_TYPE = "baseSynapse";
    public final static String BASE_PLASTICITY_MECHANISM_COMP_TYPE = "basePlasticityMechanism"; 
    
    public final static String SYNAPSE_PORT_IN = "in";
    
    // Networks
    public final static String NETWORK = "network";
    public final static String POPULATION = "population";
    public final static String INSTANCE = "instance";
    
    public final static String POPULATION_LIST = "populationList";
    public final static String PROJECTION = "projection";

}
