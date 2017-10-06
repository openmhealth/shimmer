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

package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.OffsetDateTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.LengthUnit.MILE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Misfit Resource API /activity/sessions responses to {@link PhysicalActivity} objects.
 *
 * @author Emerson Farrugia
 * @author Eric Jain
 * @see <a href="https://build.misfit.com/docs/cloudapi/api_references#session">API documentation</a>
 */
public class MisfitPhysicalActivityDataPointMapper extends MisfitDataPointMapper<PhysicalActivity> {

    @Override
    protected String getListNodeName() {
        return "sessions";
    }

    @Override
    public Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode sessionNode) {

        checkNotNull(sessionNode);

        String activityName = asRequiredString(sessionNode, "activityType");

        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);

        asOptionalDouble(sessionNode, "distance")
                .ifPresent(distanceInMi -> builder.setDistance(new LengthUnitValue(MILE, distanceInMi)));

        Optional<OffsetDateTime> startDateTime = asOptionalOffsetDateTime(sessionNode, "startTime");
        Optional<Double> durationInSec = asOptionalDouble(sessionNode, "duration");

        if (startDateTime.isPresent() && durationInSec.isPresent()) {
            DurationUnitValue durationUnitValue = new DurationUnitValue(SECOND, durationInSec.get());
            builder.setEffectiveTimeFrame(ofStartDateTimeAndDuration(startDateTime.get(), durationUnitValue));
        }

        asOptionalBigDecimal(sessionNode, "calories")
                .ifPresent(calories -> builder.setCaloriesBurned(new KcalUnitValue(KILOCALORIE, 96.8)));

        PhysicalActivity measure = builder.build();

        Optional<String> externalId = asOptionalString(sessionNode, "id");

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, externalId.orElse(null), null));
    }
}
