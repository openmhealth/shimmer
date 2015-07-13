package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * Created by Chris Schaefbauer on 7/12/15.
 */
public class GoogleFitBodyWeightDataPointMapper extends GoogleFitDataPointMapper<BodyWeight> {

    @Override
    protected Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node) {
        JsonNode valueList = asRequiredNode(node, getValueListNodeName());
        Double bodyWeightValue = asRequiredDouble(valueList.get(0), "fpVal");
        BodyWeight.Builder bodyWeightBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM,bodyWeightValue));

        Optional<String> startTimeNanosString = asOptionalString(node, "startTimeNanos");
        Optional<String> endTimeNanosString = asOptionalString(node, "endTimeNanos");
        if(startTimeNanosString.isPresent()&&endTimeNanosString.isPresent()){
            if(startTimeNanosString.equals(endTimeNanosString)){
                long measureTimeNanos = Long.parseLong(startTimeNanosString.get());
                bodyWeightBuilder.setEffectiveTimeFrame(OffsetDateTime.ofInstant(Instant.ofEpochSecond(0, measureTimeNanos), ZoneId.of("Z")));

            }
            else{
                long startTimeNanos = Long.parseLong(startTimeNanosString.get());
                long endTimeNanos = Long.parseLong(endTimeNanosString.get());
                bodyWeightBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.ofInstant(Instant.ofEpochSecond(0, startTimeNanos), ZoneId.of("Z")),
                        OffsetDateTime.ofInstant(Instant.ofEpochSecond(0, endTimeNanos), ZoneId.of("Z"))));
            }

        }

        BodyWeight bodyWeight = bodyWeightBuilder.build();
        return Optional.of(newDataPoint(bodyWeight,null));
    }
}
