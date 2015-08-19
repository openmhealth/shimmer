package org.openmhealth.shim.jawbone.mapper;

/**
 * @author Chris Schaefbauer
 */
public enum JawboneBodyEventType {

    BODY_WEIGHT("weight"),
    BODY_MASS_INDEX("bmi");

    private String propertyName;

    JawboneBodyEventType(String propertyName){

        this.propertyName = propertyName;
    }

    public String getPropertyName(){

        return propertyName;
    }



}
