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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.BodyMassIndexUnit2.KILOGRAMS_PER_SQUARE_METER;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLocalDateTime;


/**
 * A mapper that translates responses from the Fitbit Resource API <code>body/log/weight</code> endpoint into {@link
 * BodyMassIndex2} data points.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="https://dev.fitbit.com/docs/body/#weight">API documentation</a>
 */
public class FitbitBodyMassIndexDataPointMapper extends FitbitDataPointMapper<BodyMassIndex2> {

    @Override
    protected String getListNodeName() {
        return "weight";
    }

    @Override
    protected Optional<DataPoint<BodyMassIndex2>> asDataPoint(JsonNode node) {

        TypedUnitValue<BodyMassIndexUnit2> bmiValue =
                new TypedUnitValue<>(KILOGRAMS_PER_SQUARE_METER, asRequiredDouble(node, "bmi"));

        LocalDateTime effectiveLocalDateTime = asRequiredLocalDateTime(node, "date", "time");
        OffsetDateTime effectiveDateTime = asOffsetDateTimeWithFakeUtcTimeZone(effectiveLocalDateTime);

        BodyMassIndex2.Builder builder = new BodyMassIndex2.Builder(bmiValue, effectiveDateTime);

        Optional<Long> externalId = asOptionalLong(node, "logId");

        return Optional.of(newDataPoint(builder.build(), externalId.orElse(null)));
    }
}
