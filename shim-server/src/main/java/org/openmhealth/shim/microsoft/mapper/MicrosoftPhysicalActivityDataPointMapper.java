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
import com.mongodb.util.JSON;
import org.openmhealth.schema.domain.omh.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.LengthUnit.MILE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Microsoft Resource API /activity/sessions responses to {@link PhysicalActivity} objects.
 *
 * @author Emerson Farrugia
 * @author Eric Jain
 * @see <a href="https://build.misfit.com/docs/references#APIReferences-Session">API documentation</a>
 */
public class MicrosoftPhysicalActivityDataPointMapper extends MicrosoftDataPointMapper<PhysicalActivity> {
    @Override
    public List<DataPoint<PhysicalActivity>> asDataPoints(List<JsonNode> responseNodes) {

        // all mapped Microsoft responses only require a single endpoint response
        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        // all mapped Microsoft responses contain a single list
        List<DataPoint<PhysicalActivity>> dataPoints = new ArrayList<>();

        for(int i=0;i<6;i++){
        Optional<JsonNode> listNodes = asOptionalNode(responseNodes.get(0), getListNodeName(i));

        if(listNodes.isPresent()) {

            for (JsonNode listEntryNode : listNodes.get()) {
                asDataPoint(listEntryNode).ifPresent(dataPoints::add);
            }
        }}
        return dataPoints;
    }

    @Override
    protected String getListNodeName() {


        return "sleepActivities";
    }
    protected String getListNodeName(int act) {

        switch(act){
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
        if(distance.isPresent()) {
            JsonNode d = distance.get();
            Optional<Double> dist = asOptionalDouble(d, "totalDistance");
            if (dist.isPresent()) {
                builder.setDistance(new LengthUnitValue(MILE, dist.get()));
            }
        }
        Optional<OffsetDateTime> startDateTime = asOptionalOffsetDateTime(sessionNode, "startTime");


        String sleepDurationString = asRequiredString(sessionNode, "duration");
        long h=0;
        long m=0;
        long s=0;
        try {
            if(sleepDurationString.indexOf("H")!=-1)
            h = Long.parseLong((sleepDurationString.substring(sleepDurationString.indexOf("H") - 2, sleepDurationString.indexOf("H"))));

        }
        catch(NumberFormatException ex){
            h = Long.parseLong((sleepDurationString.substring(sleepDurationString.indexOf("H") - 1, sleepDurationString.indexOf("H"))));

        }
        try {
            if(sleepDurationString.indexOf("M")!=-1)

                m = Long.parseLong((sleepDurationString.substring(sleepDurationString.indexOf("M") - 2, sleepDurationString.indexOf("M"))));
        }
        catch(NumberFormatException ex) {
            m = Long.parseLong((sleepDurationString.substring(sleepDurationString.indexOf("M") - 1, sleepDurationString.indexOf("M"))));

        }

        try {
            if(sleepDurationString.indexOf("S")!=-1)

                s = Long.parseLong((sleepDurationString.substring(sleepDurationString.indexOf("S") - 2, sleepDurationString.indexOf("S"))));
        }
        catch(NumberFormatException ex){
            s = Long.parseLong((sleepDurationString.substring(sleepDurationString.indexOf("S") - 1, sleepDurationString.indexOf("S"))));

        }
        Long durationInSec = h * 3600 + m * 60 + s;
        if (durationInSec == 0) {
            return Optional.empty();
        }




        if (startDateTime.isPresent()) {
            DurationUnitValue durationUnitValue = new DurationUnitValue(SECOND, durationInSec);
            builder.setEffectiveTimeFrame(ofStartDateTimeAndDuration(startDateTime.get(), durationUnitValue));
        }
        Optional<JsonNode> caloriesnode = asOptionalNode(sessionNode, "caloriesSummary");
        if(caloriesnode.isPresent()) {
            JsonNode d = caloriesnode.get();
            Optional<Double> calories = asOptionalDouble(d, "totalcalories");
            if (calories.isPresent()) {
                builder.setCaloriesBurned(new KcalUnitValue(KILOCALORIE, calories.get()));
            }
        }


        PhysicalActivity measure = builder.build();

        Optional<String> externalId = asOptionalString(sessionNode, "id");

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, externalId.orElse(null), null));
    }


}
