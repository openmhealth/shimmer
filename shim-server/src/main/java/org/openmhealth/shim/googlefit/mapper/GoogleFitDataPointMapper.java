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

package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;
import org.openmhealth.shim.common.mapper.MissingJsonNodeMappingException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Long.parseLong;
import static java.time.ZoneOffset.UTC;
import static java.util.Optional.empty;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalNode;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * A base class for mappers that translate Google Fit API responses into {@link Measure} objects.
 *
 * @author Chris Schaefbauer
 */
public abstract class GoogleFitDataPointMapper<T extends Measure> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Google Fit API";

    private static final String EPOCH_NS_START_DATE_TIME_PATH = "startTimeNanos";
    private static final String EPOCH_NS_END_DATE_TIME_PATH = "endTimeNanos";

    /**
     * Maps a JSON response from the Google Fit API containing a JSON array of data points to a list of {@link
     * DataPoint} objects of the appropriate measure type. Splits individual nodes based on the name of the list node,
     * "point," and then iteratively maps the nodes in the list.
     *
     * @param responseNodes the response body from a Google Fit endpoint, contained in a list of a single JSON node
     * @return a list of DataPoint objects of type T with the appropriate values mapped from the input JSON; because
     * these JSON objects are contained within an array in the input response, each object in that JSON array will map
     * to an item in the returned list
     */
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 1, "Only one response should be input to the mapper");

        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        Optional<JsonNode> listNodes = asOptionalNode(responseNodes.get(0), getListNodeName());
        if (listNodes.isPresent()) {
            for (JsonNode listNode : listNodes.get()) {
                asDataPoint(listNode).ifPresent(dataPoints::add);
            }
        }

        return dataPoints;
    }

    /**
     * Maps a JSON response node from the Google Fit API into a {@link Measure} object of the appropriate type.
     *
     * @param listNode an individual datapoint from the array contained in the Google Fit response
     * @return a {@link DataPoint} object containing the target measure with the appropriate values from the JSON node
     * parameter, wrapped as an {@link Optional}
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode listNode);


    /**
     * Creates a complete {@link DataPoint} object with the measure parameter and the appropriate header and header
     * information.
     *
     * @param measure the {@link Measure} of type T to be wrapped as a {@link DataPoint}
     * @param fitDataSourceId the origin data source from the Google Fit API, contained in the originDataSourceId
     * property; refers to the originating source that brought the data into Google Fit
     */
    public DataPoint<T> newDataPoint(T measure, String fitDataSourceId) {

        DataPointAcquisitionProvenance.Builder acquisitionProvenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        // For data from the Google Fit API that has an origin data source id ending with "user_input" we know that
        // the data point is self-reported from the user through the Google Fit app or web interface
        if (fitDataSourceId != null && fitDataSourceId.endsWith("user_input")) {
            acquisitionProvenanceBuilder.setModality(DataPointModality.SELF_REPORTED);
        }

        // Although there is limited standardization in this information, we decided to pass it through as an
        // additional property to prevent information loss and in case someone were to leverage this information in
        // some way
        DataPointAcquisitionProvenance acquisitionProvenance = acquisitionProvenanceBuilder.build();
        if (fitDataSourceId != null) {
            acquisitionProvenance.setAdditionalProperty("source_origin_id", fitDataSourceId);
        }

        DataPointHeader header = new DataPointHeader.Builder(UUID.randomUUID().toString(), measure.getSchemaId()).
                setAcquisitionProvenance(acquisitionProvenance).build();

        return new DataPoint<>(header, measure);
    }

    /**
     * Converts a nanosecond timestamp from the Google Fit API into an offset date time value.
     *
     * @param epochNanosecondString the timestamp directly from the Google JSON document
     * @return an offset datetime object representing the input timestamp
     */
    private OffsetDateTime asOffsetDateTime(String epochNanosecondString) {

        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(0, parseLong(epochNanosecondString)), UTC);
    }

    /**
     * @param node a JSON node optionally containing time frame properties
     */
    public Optional<TimeFrame> getOptionalTimeFrame(JsonNode node) {

        Optional<String> startTimeNanosString = asOptionalString(node, EPOCH_NS_START_DATE_TIME_PATH);
        Optional<String> endTimeNanosString = asOptionalString(node, EPOCH_NS_END_DATE_TIME_PATH);

        // When the start and end times are identical, such as for a single body weight measure, then we only need to
        // create an effective time frame with a single date time value
        if (startTimeNanosString.isPresent() && endTimeNanosString.isPresent()) {
            if (startTimeNanosString.equals(endTimeNanosString)) {
                return Optional.of(new TimeFrame(asOffsetDateTime(startTimeNanosString.get())));
            }
            else {
                return Optional.of(new TimeFrame(ofStartDateTimeAndEndDateTime(
                        asOffsetDateTime(startTimeNanosString.get()),
                        asOffsetDateTime(endTimeNanosString.get()))
                ));
            }
        }

        return empty();
    }

    public TimeFrame getTimeFrame(JsonNode node) {

        return getOptionalTimeFrame(node)
                .orElseThrow(() -> {
                    if (!asOptionalString(node, EPOCH_NS_START_DATE_TIME_PATH).isPresent()) {
                        return new MissingJsonNodeMappingException(node, EPOCH_NS_START_DATE_TIME_PATH);
                    }
                    else {
                        return new MissingJsonNodeMappingException(node, EPOCH_NS_END_DATE_TIME_PATH);
                    }
                });
    }

    /**
     * The name of the list that contains the datapoints associated with the request.
     */
    protected String getListNodeName() {
        return "point";
    }

    /**
     * The name of the list node contained within each datapoint that contains the target value.
     */
    protected String getValueListNodeName() {
        return "value";
    }
}
