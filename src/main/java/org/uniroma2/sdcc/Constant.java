package org.uniroma2.sdcc;

public class Constant {

    public final static String STREET_LAMP_MSG = "streetLampMessage";
    public final static String JSON_STRING = "jsonString";
    public final static String ID = "id";
    public final static String ADDRESS = "address";
    public final static String ON = "on";
    public final static String LAMP_MODEL = "model";
    public final static String CONSUMPTION = "consumption";
    public final static String INTENSITY = "intensity";
    public final static String LIFETIME = "lifetime";
    public final static String NATURAL_LIGHT_LEVEL = "naturalLightLevel";
    public final static String TIMESTAMP = "timestamp";


    public static String GAP_TO_INCREASE = "gat_to_encrease";
    public static String GAP_TO_DECREASE = "gap_to_decrease";
    public static String TRAFFIC_BY_ADDRESS = "traffic_by_address";
    public static String PARKING_BY_CELLID = "parking_by_cellid";

    public static final Float TRAFFIC_THRESHOLD = 0.2f;    // under this value street traffic is considered not relevant
    public static final Float PARKING_THRESHOLD = 0.2f;    // under this value cell parking availability is considered not relevant

    public static String ADAPTED_INTENSITY = "adapted_intensity";

}
