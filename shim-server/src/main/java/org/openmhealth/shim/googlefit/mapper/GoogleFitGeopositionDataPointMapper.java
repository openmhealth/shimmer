/*
 * Copyright 2017 Open mHealth
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

package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.Geoposition;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.schema.domain.omh.PlaneAngleUnit.DEGREE_OF_ARC;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit "merged location samples" endpoint responses (derived:com.google.location.sample:com.google.android.gms:merge_location_samples)
 * to {@link Geoposition} objects.
 *
 * @author Emerson Farrugia
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitGeopositionDataPointMapper extends GoogleFitDataPointMapper<Geoposition> {

    @Override
    protected Optional<DataPoint<Geoposition>> asDataPoint(JsonNode listNode) {

        JsonNode listValueNode = asRequiredNode(listNode, "value");
        double latitude = asRequiredDouble(listValueNode.get(0), "fpVal");
        double longitude = asRequiredDouble(listValueNode.get(1), "fpVal");
        // TODO add accuracy to geoposition
         Optional<Double> accuracyInM = asOptionalDouble(listValueNode.get(2), "fpVal");

        Geoposition.Builder measureBuilder =
                new Geoposition.Builder(
                        DEGREE_OF_ARC.newUnitValue(latitude),
                        DEGREE_OF_ARC.newUnitValue(longitude),
                        getTimeFrame(listNode));

        if (listValueNode.size() >= 4) {
            measureBuilder.setElevation(METER.newUnitValue(asRequiredDouble(listValueNode.get(3), "fpVal")));
        }

        Geoposition geoposition = measureBuilder.build();

        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");

        return Optional.of(newDataPoint(geoposition, originDataSourceId.orElse(null)));
    }
}
