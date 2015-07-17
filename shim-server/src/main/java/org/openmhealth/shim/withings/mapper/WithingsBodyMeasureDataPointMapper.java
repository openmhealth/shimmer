package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.Measure;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.pow;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * Base class for mappers that map body measures responses to specific {@link Measure} data points.
 * @author Chris Schaefbauer
 */
public abstract class WithingsBodyMeasureDataPointMapper<T> extends WithingsDataPointMapper<T> {

    /**
     * Enumeration representing the different body measure types contained in Body Measure endpoint responses. Calling
     * getIntVal on a body measure type returns the integer value that corresponds to the value in the Withings API.
     */
    public enum BodyMeasureTypes {
        WEIGHT(1),
        HEIGHT(4),
        FAT_FREE_MASS(5),
        FAT_RATIO(6),
        FAT_MASS_WEIGHT(8),
        BLOOD_PRESSURE_DIASTOLIC(9),
        BLOOD_PRESSURE_SYSTOLIC(10),
        HEART_PULSE(11),
        SP02(54);

        private int intVal;

        BodyMeasureTypes(int measureType) {
            this.intVal = measureType;
        }

        public int getIntVal() {
            return intVal;
        }
    }

    /**
     * Maps JSON response nodes from the body measures endpoint (/measure?action=getmeas) in the Withings API into a
     * list of {@link DataPoint} objects with the appropriate measure
     *
     * @param responseNodes a list of a single JSON node containing the entire response from the body measures endpoint
     * @return a list of DataPoint objects of type T with the appropriate values mapped from the input JSON; because
     * JSON objects are contained within an array in the input response, each measuregrp item in that array will map
     * into an item in the list
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode responseNodeBody = asRequiredNode(responseNodes.get(0), BODY_NODE_PROPERTY);
        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        JsonNode listNode = asRequiredNode(responseNodeBody, getListNodeName());
        Optional<String> timeZoneFullName = asOptionalString(responseNodeBody,
                TIME_ZONE_PROPERTY); //assumes that time zone is available in all data points
        for (JsonNode listEntryNode : listNode) {
            if (timeZoneFullName.isPresent() && !timeZoneFullName.get().isEmpty()) {
                asDataPoint(listEntryNode, timeZoneFullName.get()).ifPresent(dataPoints::add);
            }
            else {
                //TODO: log that we have not captured this data point because it is missing timezone
            }

        }

        return dataPoints;
    }

    /**
     * Abstract method to be implemented by classes that map a specific body measure into a {@link DataPoint} object of
     * the appropriate {@link Measure} type
     */
    abstract Optional<DataPoint<T>> asDataPoint(JsonNode node, String timeZoneFullName);

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
     * measurements
     *
     * @return The value parameter multiplied by 10 to the unit power, in essence shifting the decimal by 'unit'
     * positions
     */
    protected double actualValueOf(double value, long unit) {
        return value * pow(10, unit);
    }

    /**
     * Determines whether a body measure group item is a goal instead of an actual measurement
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
     * with weight measurements
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
