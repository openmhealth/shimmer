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
 * A mapper from Withings Intraday Activity endpoint responses (/measure?action=getactivity) to {@link CaloriesBurned}
 * objects
 * <p>
 * <p>This mapper handles responses from an API request that requires special permissions from Withings. This special
 * activation can be requested from their <a href="http://oauth.withings
 * .com/api/doc#api-Measure-get_intraday_measure">API
 * Documentation website</a></p>
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_intraday_measure">Intrday Activity Measures API documentation</a>
 */
public class WithingsIntradayCaloriesBurnedDataPointMapper extends WithingsDataPointMapper<CaloriesBurned> {

    /**
     * Maps JSON response nodes from the intraday activities endpoint (measure?action=getintradayactivity) in the
     * Withings API into a list of {@link CaloriesBurned} {@link DataPoint} objects
     *
     * @param responseNodes a list of a single JSON node containing the entire response from the intraday activities
     * endpoint
     * @return a list of DataPoint objects of type {@link CaloriesBurned} with the appropriate values mapped from the
     * input
     * JSON
     */
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

    /**
     * Maps an individual list node from the array in the Withings activity measure endpoint response into a {@link
     * CaloriesBurned} data point
     *
     * @param nodeWithCalorie activity node from the array "activites" contained in the "body" of the endpoint response
     * that has a calories field
     * @return a {@link DataPoint} object containing a {@link CaloriesBurned} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
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
        return Optional.of(newDataPoint(calorieBurned, WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME, null, true,
                null));
    }

    /**
     * Creates a hashmap that contains only the entries from the intraday activities dictionary that have calories
     * burned counts
     *
     * @param fieldsIterator an iterator of map entries containing the key-value pairs related to each intraday
     * activity event
     * @return a hashmap with keys as the start datetime (in unix epoch seconds) of each activity event, and values as
     * the information related to the activity event starting at the key datetime
     */
    private HashMap<Long, JsonNode> nodesWithCalories(Iterator<Map.Entry<String, JsonNode>> fieldsIterator) {
        HashMap<Long, JsonNode> nodesWithCalories = Maps.newHashMap();
        fieldsIterator.forEachRemaining(n -> addNodesIfHasCalories(nodesWithCalories, n));
        return nodesWithCalories;
    }

    /**
     * Adds a key-value entry into the nodesWithCalories hashmap if it has a calories value
     *
     * @param nodesWithCalories pass by reference hashmap to which the key-value pair should be added if a calories
     * value exists
     * @param intradayActivityEventEntry an entry from the intraday activity series dictionary, the key is a string
     * representing the state datetime for the acivity period (in unix epoch seconds) and the value is the JSON object
     * holding data related to that activity
     */
    private void addNodesIfHasCalories(HashMap<Long, JsonNode> nodesWithCalories,
            Map.Entry<String, JsonNode> intradayActivityEventEntry) {
        if (intradayActivityEventEntry.getValue().has("calories")) {
            nodesWithCalories
                    .put(Long.parseLong(intradayActivityEventEntry.getKey()), intradayActivityEventEntry.getValue());
        }
    }


}
