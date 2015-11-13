/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * A mapper from Withings Intraday Activity endpoint responses (/measure?action=getactivity) to {@link StepCount}
 * objects.
 * <p>
 * <p>This mapper handles responses from an API request that requires special permissions from Withings. This special
 * activation can be requested by filling the form linked from their <a href="http://oauth.withings
 * .com/api/doc#api-Measure-get_intraday_measure">API Documentation website</a></p>
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_activity">Activity Measures API documentation</a>
 */
public class WithingsIntradayStepCountDataPointMapper extends WithingsDataPointMapper<StepCount> {

    /**
     * Maps JSON response nodes from the intraday activities endpoint (measure?action=getintradayactivity) in the
     * Withings API into a list of {@link StepCount} {@link DataPoint} objects.
     *
     * @param responseNodes a list of a single JSON node containing the entire response from the intraday activities
     * endpoint
     * @return a list of DataPoint objects of type {@link StepCount} with the appropriate values mapped from the input
     * JSON
     */
    @Override
    public List<DataPoint<StepCount>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode bodyNode = asRequiredNode(responseNodes.get(0), "body");
        JsonNode seriesNode = asRequiredNode(bodyNode, "series");

        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = seriesNode.fields();
        Map<Long, JsonNode> nodesWithSteps = getNodesWithSteps(fieldsIterator);
        List<Long> startDateTimesInUnixEpochSeconds = Lists.newArrayList(nodesWithSteps.keySet());

        //ensure the datapoints are in order of passing time (data points that are earlier in time come before data
        // points that are later)
        Collections.sort(startDateTimesInUnixEpochSeconds);
        ArrayList<DataPoint<StepCount>> dataPoints = Lists.newArrayList();
        for (Long startDateTime : startDateTimesInUnixEpochSeconds) {
            asDataPoint(nodesWithSteps.get(startDateTime), startDateTime).ifPresent(dataPoints::add);
        }

        return dataPoints;

    }

    /**
     * Maps an individual list node from the array in the Withings activity measure endpoint response into a {@link
     * StepCount} data point.
     *
     * @param nodeWithSteps activity node from the array "activites" contained in the "body" of the endpoint response
     * @return a {@link DataPoint} object containing a {@link StepCount} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    private Optional<DataPoint<StepCount>> asDataPoint(JsonNode nodeWithSteps, Long startDateTimeInUnixEpochSeconds) {
        Long stepCountValue = asRequiredLong(nodeWithSteps, "steps");
        StepCount.Builder stepCountBuilder = new StepCount.Builder(stepCountValue);

        Optional<Long> duration = asOptionalLong(nodeWithSteps, "duration");
        if (duration.isPresent()) {
            OffsetDateTime offsetDateTime =
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(startDateTimeInUnixEpochSeconds), ZoneId.of("Z"));
            stepCountBuilder.setEffectiveTimeFrame(
                    TimeInterval.ofStartDateTimeAndDuration(offsetDateTime, new DurationUnitValue(
                            DurationUnit.SECOND, duration.get())));
        }

        Optional<String> userComment = asOptionalString(nodeWithSteps, "comment");
        if (userComment.isPresent()) {
            stepCountBuilder.setUserNotes(userComment.get());
        }

        StepCount stepCount = stepCountBuilder.build();
        return Optional.of(newDataPoint(stepCount, null, true, null));
    }

    /**
     * Creates a map that contains only the entries from the intraday activities dictionary that have step counts.
     *
     * @param fieldsIterator an iterator of map entries containing the key-value pairs related to each intraday
     * activity
     * event
     * @return a map with keys as the start datetime (in unix epoch seconds) of each activity event, and values as
     * the information related to the activity event starting at the key datetime
     */
    private Map<Long, JsonNode> getNodesWithSteps(Iterator<Map.Entry<String, JsonNode>> fieldsIterator) {
        HashMap<Long, JsonNode> nodesWithSteps = Maps.newHashMap();
        fieldsIterator.forEachRemaining(n -> addNodeIfHasSteps(nodesWithSteps, n));
        return nodesWithSteps;
    }

    /**
     * Adds a key-value entry into the nodesWithStepValue hashmap if it has a steps value.
     *
     * @param nodesWithStepValue pass by reference hashmap to which the key-value pair should be added if a step count
     * value exists
     * @param intradayActivityEventEntry an entry from the intraday activity series dictionary, the key is a string
     * representing the state datetime for the acivity period (in unix epoch seconds) and the value is the JSON object
     * holding data related to that activity
     */
    private void addNodeIfHasSteps(HashMap<Long, JsonNode> nodesWithStepValue,
            Map.Entry<String, JsonNode> intradayActivityEventEntry) {
        if (intradayActivityEventEntry.getValue().has("steps")) {
            nodesWithStepValue
                    .put(Long.parseLong(intradayActivityEventEntry.getKey()), intradayActivityEventEntry.getValue());
        }
    }


}
