package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.CaloriesBurned;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.KcalUnit;
import org.openmhealth.schema.domain.omh.KcalUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * Created by Chris Schaefbauer on 7/15/15.
 */
public class GoogleFitCaloriesBurnedDataPointMapper extends GoogleFitDataPointMapper<CaloriesBurned> {

    @Override
    protected Optional<DataPoint<CaloriesBurned>> asDataPoint(JsonNode listNode) {

        JsonNode listValueNode = asRequiredNode(listNode, "value");
        double caloriesBurnedValue = asRequiredDouble(listValueNode.get(0), "fpVal");
        CaloriesBurned.Builder caloriesBurnedBuilder =
                new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE, caloriesBurnedValue));
        setEffectiveTimeFrameIfPresent(caloriesBurnedBuilder, listNode);
        CaloriesBurned caloriesBurned = caloriesBurnedBuilder.build();
        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");

        // Google Fit calories burned endpoint returns calories burned by basal metabolic rate (BMR), however these
        // are not activity related calories burned so we do not create a datapoint for values from this source
        if (originDataSourceId.isPresent()) {
            if (originDataSourceId.get().contains("bmr")) {
                return Optional.empty();
            }
        }
        return Optional.of(newDataPoint(caloriesBurned, originDataSourceId.orElse(null)));
    }
}
