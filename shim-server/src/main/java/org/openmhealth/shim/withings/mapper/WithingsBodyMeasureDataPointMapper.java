package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.JsonNodeMappingException;
import org.openmhealth.shim.withings.domain.WithingsBodyMeasureType;
import org.openmhealth.shim.withings.domain.WithingsMeasureGroupAttribution;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * An abstract mapper from Withings body measure endpoint responses (/measure?action=getmeas) to data points containing
 * corresponding measure objects.
 *
 * @param <T> the measure to map to
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public abstract class WithingsBodyMeasureDataPointMapper<T extends Measure> extends WithingsDataPointMapper<T> {

    private static final Logger logger = getLogger(WithingsBodyMeasureDataPointMapper.class);

    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode responseNodeBody = asRequiredNode(responseNodes.get(0), BODY_NODE_PROPERTY);
        List<DataPoint<T>> dataPoints = Lists.newArrayList();

        for (JsonNode measureGroupNode : asRequiredNode(responseNodeBody, "measuregrps")) {

            if (isGoal(measureGroupNode)) {
                continue;
            }

            if (isOwnerAmbiguous(measureGroupNode)) {
                logger.warn("The following Withings measure group is being ignored because its owner is ambiguous.\n{}",
                        measureGroupNode);
                continue;
            }

            JsonNode measuresNode = asRequiredNode(measureGroupNode, "measures");

            Measure.Builder<T, ?> measureBuilder = newMeasureBuilder(measuresNode).orElse(null);
            if (measureBuilder == null) {
                continue;
            }

            Optional<Long> dateTimeInEpochSeconds = asOptionalLong(measureGroupNode, "date");
            if (dateTimeInEpochSeconds.isPresent()) {

                Instant dateTimeInstant = Instant.ofEpochSecond(dateTimeInEpochSeconds.get());
                measureBuilder.setEffectiveTimeFrame(OffsetDateTime.ofInstant(dateTimeInstant, UTC));
            }

            Optional<String> userComment = asOptionalString(measureGroupNode, "comment");
            if (userComment.isPresent()) {
                measureBuilder.setUserNotes(userComment.get());
            }

            T measure = measureBuilder.build();

            Optional<Long> externalId = asOptionalLong(measureGroupNode, "grpid");

            dataPoints.add(newDataPoint(measure, externalId.orElse(null), isSensed(measureGroupNode), null));
        }

        return dataPoints;
    }

    /**
     * @return true if the measure group is a goal, or false otherwise
     */
    protected boolean isGoal(JsonNode measureGroupNode) {

        int categoryValue = asRequiredInteger(measureGroupNode, "category");

        if (categoryValue == 1) {
            return false;
        }
        else if (categoryValue == 2) {
            return true;
        }

        throw new JsonNodeMappingException(format(
                "The following Withings measure group node has an unrecognized category value.\n%s", measureGroupNode));
    }

    /**
     * @return true if the measure group can't be attributed to a single user, or false if it can
     * @see {@link WithingsMeasureGroupAttribution#isAmbiguous()}
     */
    protected boolean isOwnerAmbiguous(JsonNode measureGroupNode) {

        return getMeasureGroupAttribution(measureGroupNode)
                .orElseThrow(() -> new JsonNodeMappingException(format(
                        "The following Withings measure group node doesn't contain an attribution property.\n%s",
                        measureGroupNode)))
                .isAmbiguous();
    }

    /**
     * @see {@link WithingsMeasureGroupAttribution}
     */
    protected Optional<WithingsMeasureGroupAttribution> getMeasureGroupAttribution(JsonNode measureGroupNode) {

        Optional<Integer> attributionValue = asOptionalInteger(measureGroupNode, "attrib");

        if (attributionValue.isPresent()) {
            Optional<WithingsMeasureGroupAttribution> attribution =
                    WithingsMeasureGroupAttribution.findByMagicNumber(attributionValue.get().intValue());

            if (attribution.isPresent()) {
                return attribution;
            }

            throw new JsonNodeMappingException(format(
                    "The following Withings measure group node has an unrecognized attribution value.\n%s",
                    measureGroupNode));
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * @return true if the measure group was sensed, or false if it was self-reported
     */
    protected boolean isSensed(JsonNode measureGroupNode) {

        return getMeasureGroupAttribution(measureGroupNode)
                .orElseThrow(() -> new JsonNodeMappingException(format(
                        "The following Withings measure group node doesn't contain an attribution property.\n%s",
                        measureGroupNode)))
                .isSensed();
    }

    /**
     * @param measuresNode the list of measures in a measure group node
     * @return a measure builder initialised with the data in the measures list
     */
    abstract Optional<Measure.Builder<T, ?>> newMeasureBuilder(JsonNode measuresNode);

    /**
     * @return a {@link BigDecimal} corresponding to the specified measure node
     */
    protected BigDecimal getValue(JsonNode measureNode) {

        long unscaledValue = asRequiredLong(measureNode, "value");
        int scale = asRequiredInteger(measureNode, "unit");

        return BigDecimal.valueOf(unscaledValue, -1 * scale);

    }

    /**
     * @param bodyMeasureType the body measure type of interest
     * @return the
     */
    protected Optional<BigDecimal> getValueForType(JsonNode measuresNode, WithingsBodyMeasureType bodyMeasureType) {

        List<BigDecimal> values = StreamSupport.stream(measuresNode.spliterator(), false)
                .filter((measureNode) -> asRequiredLong(measureNode, "type") == bodyMeasureType.getMagicNumber())
                .map(this::getValue)
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            return Optional.empty();
        }

        if (values.size() > 1) {
            logger.warn("The following Withings measures node contains multiple measures of type {}\n{}.",
                    bodyMeasureType, measuresNode);
        }

        return Optional.of(values.get(0));
    }
}
