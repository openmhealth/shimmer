package org.openmhealth.schema.pojos;

/**
 * An enumeration with a non code-compliant value
 * which requires special handling.
 * <p/>
 * Example: enum value is 'after_exercise', label is 'After Exercise'
 */
public interface LabeledEnum {

    String getLabel();
}
