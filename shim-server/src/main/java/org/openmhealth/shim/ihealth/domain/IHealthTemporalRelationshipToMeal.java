package org.openmhealth.shim.ihealth.domain;

import org.openmhealth.schema.domain.omh.TemporalRelationshipToMeal;

import java.util.Optional;


/**
 * An enumeration of iHealth response values representing the temporal relationship between a blood glucose measure and
 * a meal.
 *
 * @author Emerson Farrugia
 */
public enum IHealthTemporalRelationshipToMeal {

    BEFORE_BREAKFAST(TemporalRelationshipToMeal.BEFORE_BREAKFAST),
    AFTER_BREAKFAST(TemporalRelationshipToMeal.AFTER_BREAKFAST),
    BEFORE_LUNCH(TemporalRelationshipToMeal.BEFORE_LUNCH),
    AFTER_LUNCH(TemporalRelationshipToMeal.AFTER_LUNCH),
    BEFORE_DINNER(TemporalRelationshipToMeal.BEFORE_DINNER),
    AFTER_DINNER(TemporalRelationshipToMeal.AFTER_DINNER),
    AT_MIDNIGHT(TemporalRelationshipToMeal.AFTER_DINNER);

    private TemporalRelationshipToMeal standardConstant;

    IHealthTemporalRelationshipToMeal(TemporalRelationshipToMeal standardConstant) {
        this.standardConstant = standardConstant;
    }

    /**
     * @return the standard constant used to refer to this temporal relationship
     */
    public TemporalRelationshipToMeal getStandardConstant() {
        return standardConstant;
    }


    public static Optional<IHealthTemporalRelationshipToMeal> findByResponseValue(String responseValue) {

        return Optional.of(IHealthTemporalRelationshipToMeal.valueOf(responseValue.toUpperCase()));
    }
}
