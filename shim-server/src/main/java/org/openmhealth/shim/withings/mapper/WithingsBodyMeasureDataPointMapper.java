package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.pow;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * Created by Chris Schaefbauer on 6/29/15.
 */
public abstract class WithingsBodyMeasureDataPointMapper<T> extends WithingsDataPointMapper<T> {

    public enum BodyMeasureTypes{
        WEIGHT(1),
        HEIGHT(4),
        FAT_FREE_MASS(5),
        FAT_RATIO(6),
        FAT_MASS_WEIGHT(8),
        BLOOD_PRESSURE_DIASTOLIC(9),
        BLOOD_PRESSURE_SYSTOLIC(10),
        HEART_PULSE(11),
        SP02(54);

        private int intVal;

        BodyMeasureTypes(int measureType) {
            this.intVal = measureType;
        }

        public int getIntVal() {
            return intVal;
        }
    }

    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        JsonNode responseNodeBody = asRequiredNode(responseNodes.get(0), BODY_NODE_PROPERTY);
        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        JsonNode listNode = asRequiredNode(responseNodeBody, getListNodeName());
        Optional<String> timeZoneFullName = asOptionalString(responseNodeBody, TIME_ZONE_PROPERTY); //assumes that time zone is available in all data points
        for (JsonNode listEntryNode : listNode) {
            if(timeZoneFullName.isPresent()&&!timeZoneFullName.get().isEmpty()){
                asDataPoint(listEntryNode,timeZoneFullName.get()).ifPresent(dataPoints::add);
            }
            else{
                //log that we have not captured this data point because it is missing timezone
            }

        }

        return dataPoints;
    }

    abstract Optional<DataPoint<T>> asDataPoint(JsonNode node,String timeZoneFullName);

    String getListNodeName() {
        return "measuregrps";
    }

    protected Optional<Boolean> isSensed(JsonNode node){
        Optional<Long> measurementProcess = asOptionalLong(node, "attrib");
        Boolean sensed=null;
        if(measurementProcess.isPresent()){
            if (measurementProcess.get()==0 || measurementProcess.get()==1){ //TODO: Need to check the semantics of 1
                sensed = true;
            }
            else{
                sensed = false;
            }
        }
        return Optional.ofNullable(sensed);
    }

    /**
     * Calculates the actual value from the value and unit parameters returned by the Withings API for body measurements
     * @param value
     * @param unit
     * @return The value parameter multiplied by 10 to the unit power, in essence shifting the decimal by 'unit' positions
     */
    protected double actualValueOf(double value, long unit){
        return value * pow(10,unit);
    }
}
