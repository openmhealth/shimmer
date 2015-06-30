package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureTypes.BLOOD_PRESSURE_DIASTOLIC;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureTypes.BLOOD_PRESSURE_SYSTOLIC;


/**
 * Created by Chris Schaefbauer on 6/30/15.
 */
public class WithingsBloodPressureDataPointMapper extends WithingsBodyMeasureDataPointMapper<BloodPressure>{

    @Override
    Optional<DataPoint<BloodPressure>> asDataPoint(JsonNode node, String timeZoneFullName) {
        JsonNode measuresNode = asRequiredNode(node, "measures");

        Double diastolicValue=null,systolicValue = null;
        Long diastolicUnit=null, systolicUnit = null;

        for(JsonNode measureNode : measuresNode){
            //We assume that there is only one value and unit for each measure type in the measures array
            //This implementation, in essence, grabs the value and unit for the last measure of that type in the list
            Long type = asRequiredLong(measureNode, "type");
            if(type== BLOOD_PRESSURE_DIASTOLIC.getIntVal()){
                diastolicValue = asRequiredDouble(measureNode,"value");
                diastolicUnit = asRequiredLong(measureNode,"unit");
            }
            else if(type==BLOOD_PRESSURE_SYSTOLIC.getIntVal()){
                systolicValue = asRequiredDouble(measureNode,"value");
                systolicUnit = asRequiredLong(measureNode,"unit");
            }
        }

        if(diastolicValue==null||diastolicUnit==null||systolicValue==null||systolicUnit==null){
            //We are missing a unit or value from one of the bp measurements, and therefore unable to create a datapoint
            if(diastolicValue!=null||diastolicUnit!=null||systolicValue!=null||systolicUnit!=null){
                //TODO: log or record that we were unable to map data point because we only had some of the values
                //In this case, there was a value or unit for at least one of these, however there was not complete
                //information so we are unable to map the datapoint and we should let them know
                //If we skip this step, it implies that there was not any blood pressure related information in the measrgrp
                //which is one expected outcome, so we do not need to document that situation
            }
            return Optional.empty();
        }

        SystolicBloodPressure systolicBloodPressure = new SystolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY,trueValueOf(systolicValue,systolicUnit));
        DiastolicBloodPressure diastolicBloodPressure = new DiastolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY,trueValueOf(diastolicValue,diastolicUnit));
        BloodPressure.Builder bloodPressureBuilder = new BloodPressure.Builder(systolicBloodPressure,diastolicBloodPressure);


        Optional<Long> dateInEpochSeconds = asOptionalLong(node, "date");
        if(dateInEpochSeconds.isPresent()){
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateInEpochSeconds.get()),
                    ZoneId.of(timeZoneFullName));
            bloodPressureBuilder.setEffectiveTimeFrame(offsetDateTime);
        }

        Optional<String> userComment = asOptionalString(node,"comment");
        if(userComment.isPresent()){
            bloodPressureBuilder.setUserNotes(userComment.get());
        }

        BloodPressure bloodPressureMeasure = bloodPressureBuilder.build();
        Optional<Long> externalId = asOptionalLong(node,"grpid");
        DataPoint<BloodPressure> bloodPressureDataPoint =
                newDataPoint(bloodPressureMeasure, RESOURCE_API_SOURCE_NAME,externalId.orElse(null),isSensed(node).orElse(null));
        return Optional.of(bloodPressureDataPoint);
    }

    /**
     * DO NOT USE THIS METHOD
     * @param node
     * @return
     */
    @Override
    Optional<DataPoint<BloodPressure>> asDataPoint(JsonNode node) {
        return null;
    }
}
