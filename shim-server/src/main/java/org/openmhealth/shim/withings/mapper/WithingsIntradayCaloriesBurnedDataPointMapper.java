package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * Created by Chris Schaefbauer on 7/5/15.
 */
public class WithingsIntradayCaloriesBurnedDataPointMapper extends WithingsDataPointMapper<CaloriesBurned> {

    @Override
    public List<DataPoint<CaloriesBurned>> asDataPoints(List<JsonNode> responseNodes) {
        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode bodyNode = asRequiredNode(responseNodes.get(0), "body");
        JsonNode seriesNode = asRequiredNode(bodyNode, "series");

        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = seriesNode.fields();
        HashMap<Long, JsonNode> nodesWithCalories = nodesWithCalories(fieldsIterator);
        List<Long> startDateTimesInUnixEpochSeconds = Lists.newArrayList(nodesWithCalories.keySet());

        //ensure the datapoints are in order of passing time (data points that are earlier in time come before data
        // points that are later)
        Collections.sort(startDateTimesInUnixEpochSeconds);
        ArrayList<DataPoint<CaloriesBurned>> dataPoints = Lists.newArrayList();
        for (Long startDateTime : startDateTimesInUnixEpochSeconds) {
            asDataPoint(nodesWithCalories.get(startDateTime), startDateTime).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    private Optional<DataPoint<CaloriesBurned>> asDataPoint(JsonNode nodeWithCalorie,
            Long startDateTimeInUnixEpochSeconds) {
        Long caloriesBurnedValue = asRequiredLong(nodeWithCalorie, "calories");
        CaloriesBurned.Builder caloriesBurnedBuilder =
                new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE, caloriesBurnedValue));

        Optional<Long> duration = asOptionalLong(nodeWithCalorie, "duration");
        if (duration.isPresent()) {
            OffsetDateTime offsetDateTime =
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(startDateTimeInUnixEpochSeconds), ZoneId.of("Z"));
            caloriesBurnedBuilder.setEffectiveTimeFrame(
                    TimeInterval.ofStartDateTimeAndDuration(offsetDateTime, new DurationUnitValue(
                            DurationUnit.SECOND, duration.get())));
        }

        Optional<String> userComment = asOptionalString(nodeWithCalorie, "comment");
        if (userComment.isPresent()) {
            caloriesBurnedBuilder.setUserNotes(userComment.get());
        }

        CaloriesBurned calorieBurned = caloriesBurnedBuilder.build();
        return Optional.of(newDataPoint(calorieBurned, WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME, null, true));
    }

    private HashMap<Long, JsonNode> nodesWithCalories(Iterator<Map.Entry<String, JsonNode>> fieldsIterator) {
        HashMap<Long, JsonNode> nodesWithCalories = Maps.newHashMap();
        fieldsIterator.forEachRemaining(n -> addNodesIfHasCalories(nodesWithCalories, n));
        return nodesWithCalories;
    }

    private void addNodesIfHasCalories(HashMap<Long, JsonNode> nodesWithCalories,
            Map.Entry<String, JsonNode> intradayActivityEventEntry) {
        if (intradayActivityEventEntry.getValue().has("calories")) {
            nodesWithCalories
                    .put(Long.parseLong(intradayActivityEventEntry.getKey()), intradayActivityEventEntry.getValue());
        }
    }


}
