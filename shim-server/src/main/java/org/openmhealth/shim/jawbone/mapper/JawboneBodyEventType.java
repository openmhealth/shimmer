package org.openmhealth.shim.jawbone.mapper;

/**
 * Represents the different body event types in Jawbone. The enum maps each type to the property name in the endpoint
 * response that contains its value.
 *
 * @author Chris Schaefbauer
 */
public enum JawboneBodyEventType {

    BODY_WEIGHT("weight"),
    BODY_MASS_INDEX("bmi");

    private String propertyName;

    JawboneBodyEventType(String propertyName) {

        this.propertyName = propertyName;
    }

    public String getPropertyName() {

        return propertyName;
    }


}
