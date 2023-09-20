package org.neuroml.model.util;

public class NeuroMLElements {

    public static final String ORG_NEUROML_MODEL_VERSION = "1.9.1";

    public static final String NAMESPACE_URI_VERSION_2 = "http://www.neuroml.org/schema/neuroml2";

    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_ALPHA = "https://raw.githubusercontent.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2alpha.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA =  "https://raw.githubusercontent.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2beta.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA1 = "https://raw.githubusercontent.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2beta1.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA2 = "https://raw.githubusercontent.com/NeuroML/NeuroML2/master/Schemas/NeuroML2/NeuroML_v2beta2.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA3 = "https://raw.githubusercontent.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2beta3.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA4 = "https://raw.githubusercontent.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2beta4.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_BETA5 = "https://raw.githubusercontent.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2beta5.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_0 = "https://raw.githubusercontent.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2.0.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_1 = "https://raw.githubusercontent.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2.1.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_2 = "https://raw.githubusercontent.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2.2.xsd";
    public static final String DEFAULT_SCHEMA_LOCATION_VERSION_2_3 = "https://raw.githubusercontent.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2.3.xsd";


    public static final String LATEST_SCHEMA_VERSION = "2.3";
    public static final String LATEST_SCHEMA = "/Schemas/NeuroML2/NeuroML_v"+LATEST_SCHEMA_VERSION+".xsd";
    public static final String LATEST_SCHEMA_LOCATION = DEFAULT_SCHEMA_LOCATION_VERSION_2_3;

    // Top level...
    public static final String NEUROML_ROOT = "neuroml";

    // Ion channels
    public static final String BASE_ION_CHANNEL_COMP_TYPE = "baseIonChannel";
    public static final String ION_CHANNEL_HH_COMP_TYPE = "ionChannelHH";
    public static final String ION_CHANNEL_KS_COMP_TYPE = "ionChannelKS";
    public static final String ION_CHANNEL_V_SHIFT_TYPE = "ionChannelVShift";
    public static final String KS_STATE_COMP_TYPE = "KSState";

    // Cells
    public static final String BASE_CELL_COMP_TYPE = "baseCell";
    public static final String BASE_CELL_CAP_COMP_TYPE = "baseCellMembPotCap";
    public static final String BASE_CELL_CAP_POINT_COND_BASED = "pointCellCondBased";
    public static final String BASE_CELL_CAP_POINT_COND_BASED_CA = "pointCellCondBasedCa";
    public static final String BASE_IAF_CELL = "baseIaf";
    public static final String BASE_IAF_CAP_CELL = "baseIafCapCell";

    public static final String BASE_PYNN_CELL = "basePyNNCell";

    public static final String BASE_RATE_UNIT = "baseRateUnit";

    public static final String CELL_COMP_TYPE = "cell";


    public static final String BASE_COND_SCALING_CA = "baseConductanceScalingCaDependent";
    public static final String BASE_GATE_COMP_TYPE = "baseGate";
    public static final String BASE_CONC_DEP_RATE_COMP_TYPE = "baseVoltageConcDepRate";
    public static final String BASE_CONC_DEP_VAR_COMP_TYPE = "baseVoltageConcDepVariable";

    public static final String BASE_CELL_COMP_TYPE_CAP__I_MEMB = "iMemb";

    public static final String CONC_MODEL_COMP_TYPE = "concentrationModel";
    public static final String CONC_MODEL_SURF_AREA = "surfaceArea";
    //public static final String CONC_MODEL_CA_TOT_CURR = "iCa";
    public static final String CONC_MODEL_INIT_CONC = "initialConcentration";
    public static final String CONC_MODEL_INIT_EXT_CONC = "initialExtConcentration";
    public static final String CONC_MODEL_CONC_STATE_VAR = "concentration";


    public static final String SEGMENT_GROUP_ALL = "all";

    // External props
    public static final String TEMPERATURE = "temperature";
    public static final String TEMPERATURE_DIM = "temperature";

    // Currents
    public static final String POINT_CURR_CURRENT = "i";
    public static final String PULSE_GENERATOR_CURRENT = "pulseGenerator";
    public static final String PULSE_GENERATOR_CURRENT_DL = "pulseGeneratorDL";

    // Spike sources
    public static final String BASE_SPIKE_SOURCE_COMP_TYPE = "baseSpikeSource";
    public static final String BASE_VOLT_DEP_CURR_SRC_SPIKING_COMP_TYPE = "baseVoltageDepPointCurrentSpiking";
    public static final String SPIKE_ARRAY = "spikeArray";
    public static final String SPIKE_GENERATOR = "spikeGenerator";

    // Synapses
    public static final String BASE_POINT_CURR_COMP_TYPE = "basePointCurrent";
    public static final String BASE_POINT_CURR_DL_COMP_TYPE = "basePointCurrentDL";
    public static final String BASE_SYNAPSE_COMP_TYPE = "baseSynapse";
    public static final String DOUBLE_SYNAPSE_COMP_TYPE = "doubleSynapse";
    public static final String BASE_PLASTICITY_MECHANISM_COMP_TYPE = "basePlasticityMechanism";
    public static final String BASE_GRADED_SYNAPSE = "baseGradedSynapse";
    public static final String GAP_JUNCTION = "gapJunction";

    public static final String SYNAPSE_PORT_IN = "in";

    public static final String BASE_RATE_SYNAPSE = "baseRateSynapse";

    // Networks
    public static final String NETWORK = "network";

    public static final String POPULATION = "population";
    public static final String POPULATION_COMPONENT = "component";
    public static final String POPULATION_SIZE = "size";
    public static final String INSTANCE = "instance";
    public static final String LOCATION = "location";
    public static final String LOCATION_X = "x";
    public static final String LOCATION_Y = "y";
    public static final String LOCATION_Z = "z";

    public static final String POPULATION_LIST = "populationList";

    public static final String PROJECTION = "projection";
    public static final String CONNECTION = "connection";
    public static final String CONNECTION_WEIGHT_DELAY = "connectionWD";

    public static final String ELECTRICAL_PROJECTION = "electricalProjection";
    public static final String ELECTRICAL_CONNECTION = "electricalConnection";
    public static final String ELECTRICAL_CONNECTION_INSTANCE = "electricalConnectionInstance";
    public static final String ELECTRICAL_CONNECTION_INSTANCE_WEIGHT = "electricalConnectionInstanceW";

    public static final String CONTINUOUS_PROJECTION = "continuousProjection";
    public static final String CONTINUOUS_CONNECTION = "continuousConnection";
    public static final String CONTINUOUS_CONNECTION_INSTANCE = "continuousConnectionInstance";
    public static final String CONTINUOUS_CONNECTION_INSTANCE_WEIGHT = "continuousConnectionInstanceW";


    public static final String INPUT_LIST = "inputList";

}
