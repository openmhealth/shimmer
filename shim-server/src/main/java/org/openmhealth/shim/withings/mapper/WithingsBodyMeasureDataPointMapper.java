package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

import static java.lang.Math.pow;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;


/**
 * An abstract mapper from Withings body measure endpoint responses (/measure?action=getmeas) to data points containing
 * corresponding measure objects.
 *
 * @param <T> the measure to map to
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public abstract class WithingsBodyMeasureDataPointMapper<T> extends WithingsListDataPointMapper<T> {

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
