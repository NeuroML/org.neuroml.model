package org.neuroml.model.util;

public class NeuroMLElements {

    public static final String ORG_NEUROML_MODEL_VERSION = "1.1.0";
    
    public static final String NAMESPACE_URI_VERSION_2 = "http://www.neuroml.org/schema/neuroml2";

    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_ALPHA = "https://raw.github.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2alpha.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA =  "https://raw.github.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2beta.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA1 = "https://raw.github.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2beta1.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA2 = "https://raw.github.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2beta2.xsd";
    

    public static final String TARGET_SCHEMA = "/Schemas/NeuroML2/NeuroML_v2beta2.xsd";
    public static final String TARGET_SCHEMA_LOCATION = DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA2;
	

    
    // Ion channels
    public static final String ION_CHANNEL_COMP_TYPE = "baseIonChannel";
    
    // Cells
    public static final String BASE_CELL_COMP_TYPE = "baseCell";
    public static final String BASE_CELL_CAP_COMP_TYPE = "baseCellMembPotCap";
    public static final String CELL_COMP_TYPE = "cell";
    
    public static final String BASE_GATE_COMP_TYPE = "baseGate";
    public static final String BASE_CONC_DEP_RATE_COMP_TYPE = "baseVoltageConcDepRate";
    public static final String BASE_CONC_DEP_VAR_COMP_TYPE = "baseVoltageConcDepVariable";
   
    public static final String BASE_CELL_COMP_TYPE_CAP__I_MEMB = "iMemb";

    public static final String CONC_MODEL_COMP_TYPE = "concentrationModel";
    public static final String CONC_MODEL_SURF_AREA = "surfaceArea";
    public static final String CONC_MODEL_CA_TOT_CURR = "iCa";
    public static final String CONC_MODEL_INIT_CONC = "initialConcentration";
    public static final String CONC_MODEL_INIT_EXT_CONC = "initialExtConcentration";
    public static final String CONC_MODEL_CONC_STATE_VAR = "concentration";

    // External props
    public static final String TEMPERATURE = "temperature";
    public static final String TEMPERATURE_DIM = "temperature";

    // Currents
    public static final String POINT_CURR_CURRENT = "i";
    
    // Synapses
    public static final String BASE_POINT_CURR_COMP_TYPE = "basePointCurrent";
    public static final String BASE_SYNAPSE_COMP_TYPE = "baseSynapse";
    public static final String BASE_PLASTICITY_MECHANISM_COMP_TYPE = "basePlasticityMechanism"; 
    
    public static final String SYNAPSE_PORT_IN = "in";
    
    // Networks
    public static final String NETWORK = "network";
    
    public static final String POPULATION = "population";
    public static final String POPULATION_COMPONENT = "component";
    public static final String POPULATION_SIZE = "size";
    public static final String INSTANCE = "instance";
    
    public static final String POPULATION_LIST = "populationList";
    public static final String PROJECTION = "projection";
    public static final String CONNECTION = "connection";

}
