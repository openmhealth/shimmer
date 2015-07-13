package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * Created by Chris Schaefbauer on 7/12/15.
 */
public class GoogleFitBodyWeightDataPointMapper extends GoogleFitDataPointMapper<BodyWeight> {

    @Override
    public Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node) {
        JsonNode valueList = asRequiredNode(node, getValueListNodeName());
        Double bodyWeightValue = asRequiredDouble(valueList.get(0), "fpVal");
        BodyWeight.Builder bodyWeightBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM,bodyWeightValue));
        setEffectiveTimeFrameIfPresent(bodyWeightBuilder, node);
//        Optional<String> startTimeNanosString = asOptionalString(node, "startTimeNanos");
//        Optional<String> endTimeNanosString = asOptionalString(node, "endTimeNanos");
//        if(startTimeNanosString.isPresent()&&endTimeNanosString.isPresent()){
//            if(startTimeNanosString.equals(endTimeNanosString)){
//                bodyWeightBuilder.setEffectiveTimeFrameIfPresent(convertGoogleNanosToOffsetDateTime(startTimeNanosString.get()));
//
//            }
//            else{
//                bodyWeightBuilder.setEffectiveTimeFrameIfPresent(TimeInterval.ofStartDateTimeAndEndDateTime(
//                        convertGoogleNanosToOffsetDateTime(startTimeNanosString.get()),
//                        convertGoogleNanosToOffsetDateTime(endTimeNanosString.get())));
//            }
//
//        }

        BodyWeight bodyWeight = bodyWeightBuilder.build();
        return Optional.of(newDataPoint(bodyWeight,null));
    }
}
