package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * Created by Chris Schaefbauer on 7/2/15.
 */
public class WithingsIntradayStepCountDataPointMapper extends WithingsDataPointMapper<StepCount>{

    @Override
    public List<DataPoint<StepCount>> asDataPoints(List<JsonNode> responseNode) {


        JsonNode bodyNode = asRequiredNode(responseNode.get(0), "body");
        JsonNode seriesNode = asRequiredNode(bodyNode, "series");

        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = seriesNode.fields();
        HashMap<Long, JsonNode> nodesWithSteps = nodesWithSteps(fieldsIterator);
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

    private HashMap<Long,JsonNode> nodesWithSteps(Iterator<Map.Entry<String, JsonNode>> fieldsIterator) {
        HashMap<Long,JsonNode> nodesWithSteps = Maps.newHashMap();
        ArrayList<Long> startTimesWithSteps = Lists.newArrayList();
        fieldsIterator.forEachRemaining(n->addNodeIfHasSteps(nodesWithSteps, n));
        return nodesWithSteps;
    }

    private void addNodeIfHasSteps(HashMap<Long, JsonNode> nodesWithStepValue, Map.Entry<String, JsonNode> n) {
        if(n.getValue().has("steps")) {
            nodesWithStepValue.put(Long.parseLong(n.getKey()), n.getValue());
        }
    }

    private Optional<DataPoint<StepCount>> asDataPoint(JsonNode nodeWithSteps, Long startDateTimeInUnixEpochSeconds){
        Long stepCountValue = asRequiredLong(nodeWithSteps, "steps");
        StepCount.Builder stepCountBuilder = new StepCount.Builder(stepCountValue);

        Optional<Long> duration = asOptionalLong(nodeWithSteps, "duration");
        if(duration.isPresent()){
            OffsetDateTime offsetDateTime =
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(startDateTimeInUnixEpochSeconds), ZoneId.of("Z"));
            stepCountBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetDateTime,new DurationUnitValue(
                    DurationUnit.SECOND,duration.get())));
        }

        Optional<String> userComment = asOptionalString(nodeWithSteps, "comment");
        if(userComment.isPresent()){
            stepCountBuilder.setUserNotes(userComment.get());
        }

        StepCount stepCount = stepCountBuilder.build();
        return Optional.of(newDataPoint(stepCount,WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME,null,true));
    }
}
