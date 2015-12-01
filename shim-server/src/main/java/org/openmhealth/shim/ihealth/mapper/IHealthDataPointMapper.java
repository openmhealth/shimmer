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

package org.openmhealth.shim.ihealth.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.DataPointMapper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.Instant.ofEpochSecond;
import static java.time.OffsetDateTime.ofInstant;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.Measure.Builder;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * The base class for mappers that translate iHealth API responses to {@link DataPoint} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public abstract class IHealthDataPointMapper<T> implements DataPointMapper<T, JsonNode> {

    public static final String RESOURCE_API_SOURCE_NAME = "iHealth Resource API";
    public static final String DATA_SOURCE_MANUAL = "Manual";
    public static final String DATA_SOURCE_FROM_DEVICE = "FromDevice";

    /**
     * Maps a JSON response with individual data points contained in a JSON array to a list of {@link  DataPoint}
     * objects with the appropriate measure. Splits individual nodes and then iteratively maps the nodes in the list.
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        // all mapped iHealth responses only require a single endpoint response
        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode responseNode = responseNodes.get(0);

        Integer measureUnitMagicNumber = null;

        if (getMeasureUnitNodeName().isPresent()) {
            measureUnitMagicNumber = asRequiredInteger(responseNode, getMeasureUnitNodeName().get());
        }

        List<DataPoint<T>> dataPoints = Lists.newArrayList();

        for (JsonNode listEntryNode : asRequiredNode(responseNode, getListNodeName())) {

            asDataPoint(listEntryNode, measureUnitMagicNumber).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    /**
     * Creates a data point header with information describing the data point created around the measure.
     * <p>
     * Note: Additional properties within the header come from the iHealth API and are not defined by the data point
     * header schema. Additional properties are subject to change.
     */
    protected DataPointHeader createDataPointHeader(JsonNode listEntryNode, Measure measure) {

        DataPointAcquisitionProvenance.Builder acquisitionProvenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        asOptionalString(listEntryNode, "DataSource").ifPresent(
                dataSource -> setAppropriateModality(dataSource, acquisitionProvenanceBuilder));

        DataPointAcquisitionProvenance acquisitionProvenance = acquisitionProvenanceBuilder.build();

        asOptionalString(listEntryNode, "DataID")
                .ifPresent(externalId -> acquisitionProvenance.setAdditionalProperty("external_id",
                        externalId));

        asOptionalLong(listEntryNode, "LastChangeTime").ifPresent(
                lastUpdatedInUnixSecs -> acquisitionProvenance.setAdditionalProperty("source_updated_date_time",
                        ofInstant(ofEpochSecond(lastUpdatedInUnixSecs), ZoneId.of("Z"))));

        return new DataPointHeader.Builder(UUID.randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance)
                .build();

    }

    /**
     * Sets the effective time frame of a measure builder as a single point in time using a date_time. This method does
     * not set time intervals.
     *
     * @param listEntryNode A single node from the response result array that contains the MDate field that needs to get
     * mapped as a date_time in the timeframe.
     * @param builder The measure builder to set the effective time frame.
     */
    protected static void setEffectiveTimeFrameWithDateTimeIfExists(JsonNode listEntryNode, Builder builder) {

        Optional<Long> optionalOffsetDateTime = asOptionalLong(listEntryNode, "MDate");

        if (!optionalOffsetDateTime.isPresent()) {
            return;
        }

        Optional<String> timeZone = asOptionalString(listEntryNode, "TimeZone");

        if (timeZone.isPresent() && !timeZone.get().isEmpty()) {

            OffsetDateTime offsetDateTimeCorrectOffset =
                    getDateTimeWithCorrectOffset(optionalOffsetDateTime.get(), timeZone.get());
            builder.setEffectiveTimeFrame(offsetDateTimeCorrectOffset);
        }

        else if (asOptionalLong(listEntryNode, "TimeZone").isPresent()) {

            Long timeZoneOffsetValue = asOptionalLong(listEntryNode, "TimeZone").get();
            String timeZoneString = timeZoneOffsetValue.toString();


            // Zone offset cannot parse a positive string offset that's missing a '+' sign (i.e., "0200" vs "+0200")
            if (timeZoneOffsetValue >= 0) {
                timeZoneString = "+" + timeZoneOffsetValue.toString();
            }

            OffsetDateTime offsetDateTimeCorrectOffset =
                    getDateTimeWithCorrectOffset(optionalOffsetDateTime.get(),
                            timeZoneString);

            builder.setEffectiveTimeFrame(offsetDateTimeCorrectOffset);
        }
    }

    /**
     * This method transforms the unix epoch second timestamps in iHealth responses into an {@link OffsetDateTime} with
     * the correct date/time and offset. The timestamps provided in iHealth responses are not in UTC but instead offset
     * to the local time zone of the data point.
     */
    protected static OffsetDateTime getDateTimeWithCorrectOffset(Long dateTimeInUnixSecondsWithLocalTimeOffset,
            String timeZoneString) {

        // Since the timestamps are in local time, we can use the local date time provided by rendering the timestamp
        // in UTC, then translating that local time to the appropriate offset.
        OffsetDateTime offsetDateTimeFromOffsetInstant = ofInstant(
                ofEpochSecond(dateTimeInUnixSecondsWithLocalTimeOffset),
                ZoneId.of("Z"));

        return offsetDateTimeFromOffsetInstant.toLocalDateTime().atOffset(ZoneOffset.of(timeZoneString));
    }

    /**
     * @param dateTimeInUnixSecondsWithLocalTimeOffset A unix epoch timestamp in local time.
     * @param timeZoneString The time zone offset as a String (e.g., "+0200","-2").
     * @return The date time with the correct offset.
     */
    protected static OffsetDateTime getDateTimeAtStartOfDayWithCorrectOffset(
            Long dateTimeInUnixSecondsWithLocalTimeOffset, String timeZoneString) {

        // Since the timestamps are in local time, we can use the local date time provided by rendering the timestamp
        // in UTC, then translating that local time to the appropriate offset.
        OffsetDateTime dateTimeFromOffsetInstant =
                ofInstant(ofEpochSecond(dateTimeInUnixSecondsWithLocalTimeOffset),
                        ZoneId.of("Z"));

        return dateTimeFromOffsetInstant.toLocalDate().atStartOfDay().atOffset(ZoneOffset.of(timeZoneString));
    }

    /**
     * Sets the user note in a measure builder with the value of the note property in the list entry node if that
     * property exists.
     *
     * @param listEntryNode A single entry from the response result array.
     * @param builder The measure builder to set the user note.
     */
    protected static void setUserNoteIfExists(JsonNode listEntryNode, Builder builder) {

        Optional<String> note = asOptionalString(listEntryNode, "Note");

        if (note.isPresent() && !note.get().isEmpty()) {

            builder.setUserNotes(note.get());
        }
    }

    /**
     * Sets the correct DataPointModality based on the iHealth value indicating the source of the DataPoint.
     *
     * @param dataSourceValue The iHealth value in the list entry node indicating the source of the DataPoint.
     * @param builder The DataPointAcquisitionProvenance builder to set the modality.
     */
    private void setAppropriateModality(String dataSourceValue, DataPointAcquisitionProvenance.Builder builder) {

        if (dataSourceValue.equals(DATA_SOURCE_FROM_DEVICE)) {
            builder.setModality(SENSED);
        }
        else if (dataSourceValue.equals(DATA_SOURCE_MANUAL)) {
            builder.setModality(SELF_REPORTED);
        }
    }

    /**
     * @return The name of the JSON array that contains the individual data points. This is different per endpoint.
     */
    protected abstract String getListNodeName();

    /**
     * @return The name of the JSON property whose value indicates the unit of measure used to render the values in the
     * response. This is different per endpoint and some endpoints do not provide any units, in which case, the value
     * should be an empty Optional.
     */
    protected abstract Optional<String> getMeasureUnitNodeName();

    /**
     * @param listEntryNode A single entry from the response result array.
     * @param measureUnitMagicNumber The number representing the units used to render the response, according to
     * iHealth. This is retrieved from the main body of the response node. If the measure type does not use units, then
     * this value is null.
     * @return The data point mapped from the listEntryNode, unless it is skipped.
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber);
}
