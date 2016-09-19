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

package org.openmhealth.shim.microsoft.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.LengthUnit.MILE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Microsoft Resource API /Activities responses to {@link PhysicalActivity} objects.
 *
 * @author jjcampa
 */
public class MicrosoftPhysicalActivityDataPointMapper extends MicrosoftDataPointMapper<PhysicalActivity> {
    @Override
    public List<DataPoint<PhysicalActivity>> asDataPoints(List<JsonNode> responseNodes) {

        // all mapped Microsoft responses only require a single endpoint response
        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        // all mapped Microsoft responses contain a single list
        List<DataPoint<PhysicalActivity>> dataPoints = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            Optional<JsonNode> listNodes = asOptionalNode(responseNodes.get(0), getListNodeName(i));

            if (listNodes.isPresent()) {

                for (JsonNode listEntryNode : listNodes.get()) {
                    asDataPoint(listEntryNode).ifPresent(dataPoints::add);
                }
            }
        }
        return dataPoints;
    }

    @Override
    protected String getListNodeName() {


        return "Activities";
    }

    protected String getListNodeName(int act) {

        switch (act) {
            case 0:
                return "bikeActivities";

            case 1:
                return "hikeActivities";

            case 2:
                return "freePlayActivities";

            case 3:
                return "golfActivities";

            case 4:
                return "guidedWorkoutActivities";

            case 5:
                return "runActivities";
            default:
                return "";


        }
    }

    @Override
    public Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode sessionNode) {

        checkNotNull(sessionNode);

        String activityName = asRequiredString(sessionNode, "activityType");

        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);

        Optional<JsonNode> distance = asOptionalNode(sessionNode, "distanceSummary");
        if (distance.isPresent()) {
            JsonNode d = distance.get();
            Optional<Double> dist = asOptionalDouble(d, "totalDistance");
            if (dist.isPresent()) {
                builder.setDistance(new LengthUnitValue(MILE, dist.get()));
            }
        }

        Optional<OffsetDateTime> startDateTime = asOptionalOffsetDateTime(sessionNode, "startTime");

        String sleepDurationString = asRequiredString(sessionNode, "duration");
        Duration duration = java.time.Duration.parse(sleepDurationString);

        if (startDateTime.isPresent()) {
            DurationUnitValue durationUnitValue = new DurationUnitValue(DAY, TimeUnit.DAYS.convert(duration.get(ChronoUnit.SECONDS), TimeUnit.SECONDS));
            builder.setEffectiveTimeFrame(ofStartDateTimeAndDuration(startDateTime.get(), durationUnitValue));
        }
        Optional<JsonNode> caloriesnode = asOptionalNode(sessionNode, "caloriesBurnedSummary");
        if (caloriesnode.isPresent()) {
            JsonNode d = caloriesnode.get();
            Optional<Integer> calories = asOptionalInteger(d, "totalCalories");
            if (calories.isPresent()) {
                builder.setCaloriesBurned(new KcalUnitValue(KILOCALORIE, calories.get()));
            }
        }


        PhysicalActivity measure = builder.build();

        Optional<String> externalId = asOptionalString(sessionNode, "id");

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, externalId.orElse(null), null));
    }


}
