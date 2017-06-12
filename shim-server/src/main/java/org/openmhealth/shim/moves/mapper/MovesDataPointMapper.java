package org.openmhealth.shim.moves.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLocalDateTime;

/**
 * The base class for mappers that translate Moves API responses to data points.
 *
 * @author Jared Sieling.
 */
public abstract class MovesDataPointMapper<T extends SchemaSupport> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Moves Resource API";

    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode listNode = responseNodes.get(0);

        List<DataPoint<T>> dataPoints = Lists.newArrayList();

        for (JsonNode listEntryNode : listNode) {
            asDataPoint(listEntryNode).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

     /**
     * Creates a data point.
     *
     * @param measure the measure to set as the body of the data point
     * @param externalId the identifier of the measure as recorded by the data provider
     * @param <T> the measure type
     * @return a data point
     */
    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, @Nullable Long externalId) {

        DataPointAcquisitionProvenance acquisitionProvenance =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME).build();

        if (externalId != null) {
            acquisitionProvenance.setAdditionalProperty("external_id", externalId);
        }

        DataPointHeader header = new DataPointHeader.Builder(UUID.randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance).build();

        return new DataPoint<>(header, measure);
    }

    /**
     * FIXME this is copy-pasted from Fitbit, and it's not clear why this would apply
     * @param node a JSON node containing <code>date</code> and <code>time</code> properties
     * @return the equivalent OffsetDateTime
     */
    protected Optional<OffsetDateTime> combineDateTimeAndTimezone(JsonNode node) {

        Optional<LocalDateTime> dateTime = asOptionalLocalDateTime(node, "date", "time");
        Optional<OffsetDateTime> offsetDateTime = null;

        if (dateTime.isPresent()) {
            offsetDateTime = Optional.of(OffsetDateTime.of(dateTime.get(), ZoneOffset.UTC));
        }

        return offsetDateTime;
    }

    /**
     * FIXME this is copy-pasted from Fitbit, and it's not clear why this would apply
     * Transforms a {@link LocalDateTime} object into an {@link OffsetDateTime} object with a UTC time zone
     *
     * @param dateTime local date and time for the Moves response JSON node
     * @return the date and time based on the input dateTime parameter
     */
    protected OffsetDateTime combineDateTimeAndTimezone(LocalDateTime dateTime) {

        return OffsetDateTime.of(dateTime, ZoneOffset.UTC);
    }

    /**
     * Maps a JSON response node from the Moves API into a data point.
     *
     * @return the data point
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode node);
}
