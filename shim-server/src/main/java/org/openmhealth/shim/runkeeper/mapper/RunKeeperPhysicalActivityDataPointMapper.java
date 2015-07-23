package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from RunKeeper HealthGraph API application/vnd.com.runkeeper.FitnessActivityFeed+json responses to {@link
 * PhysicalActivity} objects.
 *
 * @author Emerson Farrugia
 * @author Danilo Bonilla
 * @see <a href="http://runkeeper.com/developer/healthgraph/fitness-activities#past">API documentation</a>
 */
public class RunKeeperPhysicalActivityDataPointMapper extends RunKeeperDataPointMapper<PhysicalActivity> {

    @Override
    protected String getListNodeName() {
        return "items";
    }

    @Override
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode itemNode) {

        PhysicalActivity measure = getMeasure(itemNode);
        DataPointHeader header = getDataPointHeader(itemNode, measure);

        return Optional.of(new DataPoint<>(header, measure));
    }

    private PhysicalActivity getMeasure(JsonNode itemNode) {

        String activityName = asRequiredString(itemNode, "type");

        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);

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

        asOptionalDouble(itemNode, "total_distance")
                .ifPresent(distanceInM -> builder.setDistance(new LengthUnitValue(METER, distanceInM)));

        return builder.build();
    }

    private DataPointHeader getDataPointHeader(JsonNode itemNode, PhysicalActivity measure) {

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
}
