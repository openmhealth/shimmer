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
 * A mapper from Google Fit "merged calories expended" endpoint responses
 * (derived:com.google.calories.expended:com.google.android.gms:merge_calories_expended) to {@link CaloriesBurned}
 * objects
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitCaloriesBurnedDataPointMapper extends GoogleFitDataPointMapper<CaloriesBurned> {

    /**
     * Maps a JSON response node from the Google Fit API to a {@link CaloriesBurned} measure
     * @param listNode an individual datapoint from the array from the Google Fit response
     * @return a {@link DataPoint} object containing a {@link CaloriesBurned} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
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
