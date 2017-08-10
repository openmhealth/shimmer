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
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnitValue;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLocalDateTime;


/**
 * A mapper that translates responses from the Fitbit Resource API <code>body/log/weight</code> endpoint into {@link
 * BodyWeight} data points.
 *
 * @author Chris Schaefbauer
 * @see <a href="https://dev.fitbit.com/docs/body/#weight">API documentation</a>
 */
public class FitbitBodyWeightDataPointMapper extends FitbitDataPointMapper<BodyWeight> {

    @Override
    protected String getListNodeName() {
        return "weight";
    }

    @Override
    protected Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node) {

        MassUnitValue bodyWeight = new MassUnitValue(KILOGRAM, asRequiredDouble(node, "weight"));

        LocalDateTime effectiveLocalDateTime = asRequiredLocalDateTime(node, "date", "time");
        OffsetDateTime effectiveDateTime = asOffsetDateTimeWithFakeUtcTimeZone(effectiveLocalDateTime);

        BodyWeight.Builder builder = new BodyWeight.Builder(bodyWeight)
                .setEffectiveTimeFrame(effectiveDateTime);

        Optional<Long> externalId = asOptionalLong(node, "logId");

        return Optional.of(newDataPoint(builder.build(), externalId.orElse(null)));
    }
}
