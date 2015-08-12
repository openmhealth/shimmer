package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.Measure;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.pow;
import static java.time.ZoneId.of;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * An abstract mapper from Withings body measure endpoint responses (/measure?action=getmeas) to data points containing
 * corresponding measure objects.
 *
 * @param <T> the measure to map to
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public abstract class WithingsBodyMeasureDataPointMapper<T extends Measure> extends WithingsListDataPointMapper<T> {

    /**
     * A type of body measure included in a response from the endpoint.
     */
    public enum BodyMeasureType {

        WEIGHT(1),
        HEIGHT(4),
        // FAT_FREE_MASS(5), // TODO confirm what this means
        // FAT_RATIO(6), // TODO confirm what this means
        // FAT_MASS_WEIGHT(8), // TODO confirm what this means
        DIASTOLIC_BLOOD_PRESSURE(9),
        SYSTOLIC_BLOOD_PRESSURE(10),
        HEART_RATE(11),
        OXYGEN_SATURATION(54);

        private int magicNumber;

        BodyMeasureType(int magicNumber) {
            this.magicNumber = magicNumber;
        }

        /**
         * @return the magic number used to refer to this body measure type in responses
         */
        public int getMagicNumber() {
            return magicNumber;
        }
    }

    /**
     * Returns the list name for splitting out individual body measure groups that can then be mapped.
     *
     * @return the name of the array containing the individual body measure group nodes
     */
    String getListNodeName() {
        return "measuregrps";
    }

    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");
        JsonNode responseNodeBody = asRequiredNode(responseNodes.get(0), BODY_NODE_PROPERTY);
        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        JsonNode listNode = asRequiredNode(responseNodeBody, getListNodeName());
        for (JsonNode listEntryNode : listNode) {
            if (isUnattributedSensed(listEntryNode)) {
                //This is a corner case captured by the Withings API where the data point value captured by the scale is
                // similar to multiple users and they were not prompted to specify the data point owner because the new
                // user was created and not synced to the scale before taking a measurement
                //TODO: Log that datapoint was not captured and to revisit since user can assign in the web interface
            }
            else if (!isGoal(
                    listEntryNode)) { // If the measurement point represents a goal (not a measurement) then we skip
                asDataPoint(listEntryNode).ifPresent(dataPoints::add);
            }

        }

        return dataPoints;

    }

    /**
     * Identifies whether a body measures group node was sensed by a device or self-reported by a user
     *
     * @param node a list node from the "measuregrps" array from a body measures endpoint response
     * @return a boolean value indicating true if the data point was sensed by a device or false if the data point was
     * self-reported by a user
     */
    protected Optional<Boolean> isSensed(JsonNode node) {

        Optional<Long> measurementProcess = asOptionalLong(node, "attrib");
        Boolean sensed = null;
        if (measurementProcess.isPresent()) {
            if (measurementProcess.get() == 0 ||
                    measurementProcess.get() == 1) { //TODO: Need to check the semantics of 1
                sensed = true;
            }
            else {
                sensed = false;
            }
        }
        return Optional.ofNullable(sensed);

    }

    /**
     * Calculates the actual value from the value and unit parameters returned by the Withings API for body
     * measurements.
     *
     * @return The value parameter multiplied by 10 to the unit power, in essence shifting the decimal by 'unit'
     * positions
     */
    protected double actualValueOf(double value, long unit) {
        return value * pow(10, unit);
    }

    /**
     * Determines whether a body measure group item is a goal instead of an actual measurement.
     *
     * @param node a list node from the "measuregrps" list in the body measures API response
     * @return whether or not the datapoint is a goal or real measure
     */
    protected Boolean isGoal(JsonNode node) {

        Optional<Long> category = asOptionalLong(node, "category");
        if (category.isPresent()) {
            if (category.get() == 2) {
                return true;
            }
        }

        return false;

    }

    protected void setEffectiveTimeFrame(T.Builder measureBuilder, JsonNode listEntryNode) {
        Optional<Long> dateTimeInUtcSec = asOptionalLong(listEntryNode, "date");
        if (dateTimeInUtcSec.isPresent()) {
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateTimeInUtcSec.get()),
                    of("Z"));

            measureBuilder.setEffectiveTimeFrame(offsetDateTime);
        }
    }

    protected void setUserComment(T.Builder bodyWeightBuilder, JsonNode node) {
        Optional<String> userComment = asOptionalString(node, "comment");
        if (userComment.isPresent()) {
            bodyWeightBuilder.setUserNotes(userComment.get());
        }
    }

    /**
     * Determines whether a body measure group item that was sensed is currently unattributed to a user because the
     * measurement was taken before a new user was synced to the device. Based on Withings feedback, this is only a
     * case
     * with weight measurements.
     *
     * @param node a list node from the "measuregrps" list in the body measures API response
     * @return whether or not a sensed datapoint has been attributed correctly to a user
     */
    protected boolean isUnattributedSensed(JsonNode node) {

        Optional<Long> attrib = asOptionalLong(node, "attrib");
        if (attrib.isPresent()) {
            if (attrib.get() == 1) {
                return true;
            }
        }

        return false;

    }
}
