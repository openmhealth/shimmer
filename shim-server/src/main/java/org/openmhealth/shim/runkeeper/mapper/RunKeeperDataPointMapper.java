package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.UUID.randomUUID;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * An abstract mapper for building RunKeeper data points.
 *
 * @author Emerson Farrugia
 */
public abstract class RunKeeperDataPointMapper<T> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Runkeeper HealthGraph API";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss");


    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        // all mapped RunKeeper responses only require a single endpoint response
        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        // all mapped RunKeeper responses contain a single list
        JsonNode listNode = asRequiredNode(responseNodes.get(0), getListNodeName());

        List<DataPoint<T>> dataPoints = new ArrayList<>();

        for (JsonNode listEntryNode : listNode) {
            asDataPoint(listEntryNode).ifPresent(dataPoints::add);
        }

        return dataPoints;

    }

    /**
     * @return the name of the list node used by this mapper
     */
    protected String getListNodeName() {
        return "items";
    }

    /**
     * @return a {@link DataPointHeader} for data points created from Runkeeper Healthgraph API responses
     */
    protected DataPointHeader getDataPointHeader(JsonNode itemNode, Measure measure) {

        DataPointAcquisitionProvenance.Builder provenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        getModality(itemNode).ifPresent(provenanceBuilder::setModality);

        DataPointAcquisitionProvenance provenance = provenanceBuilder.build();

        asOptionalString(itemNode, "uri")
                .ifPresent(externalId -> provenance.setAdditionalProperty("external_id", externalId));

        DataPointHeader.Builder headerBuilder =
                new DataPointHeader.Builder(randomUUID().toString(), measure.getSchemaId())
                        .setAcquisitionProvenance(provenance);

        asOptionalInteger(itemNode, "userId").ifPresent(userId -> headerBuilder.setUserId(userId.toString()));

        return headerBuilder.build();

    }

    /**
     * @see <a href="http://billday.com/2013/04/09/validating-tracked-versus-manual-fitness-activities-using-the
     * -health-graph-api/">article on modality</a>
     */
    public Optional<DataPointModality> getModality(JsonNode itemNode) {

        String source = asOptionalString(itemNode, "source").orElse(null);
        String entryMode = asOptionalString(itemNode, "entry_mode").orElse(null);
        Boolean hasPath = asOptionalBoolean(itemNode, "has_path").orElse(null);

        if (entryMode != null && entryMode.equals("Web")) {
            return Optional.of(DataPointModality.SELF_REPORTED);
        }

        if (source != null && source.equals("RunKeeper")
                && entryMode != null && entryMode.equals("API")
                && hasPath != null && hasPath) {

            return Optional.of(DataPointModality.SENSED);
        }

        return Optional.empty();

    }

    /**
     * Sets the effective time frame property for a measure builder
     * @param itemNode an individual datapoint from the list of datapoints returned in the API response
     * @param builder the measure builder to have the effective date property set
     */
    protected void setEffectiveTimeframeIfPresent(JsonNode itemNode, Measure.Builder builder) {

        Optional<LocalDateTime> localStartDateTime =
                asOptionalLocalDateTime(itemNode, "start_time", DATE_TIME_FORMATTER);

        // RunKeeper doesn't support fractional time zones
        Optional<Integer> utcOffset = asOptionalInteger(itemNode, "utc_offset");
        Optional<Double> durationInS = asOptionalDouble(itemNode, "duration");

        if (localStartDateTime.isPresent() && utcOffset.isPresent() && durationInS.isPresent()) {

            OffsetDateTime startDateTime = localStartDateTime.get().atOffset(ZoneOffset.ofHours(utcOffset.get()));
            DurationUnitValue duration = new DurationUnitValue(SECOND, durationInS.get());

            builder.setEffectiveTimeFrame(ofStartDateTimeAndDuration(startDateTime, duration));
        }

    }

    /**
     * @param listEntryNode the list entry node
     * @return the data point mapped to from that entry, unless skipped
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode listEntryNode);
}
