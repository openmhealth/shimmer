package org.openmhealth.shim.withings.domain;


/**
 * A body measure type included in responses from the Withings body measure endpoint, specifically in a 'meastype'
 * property.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see {@link org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper}
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public enum WithingsBodyMeasureType {

    BODY_WEIGHT(1),
    BODY_HEIGHT(4),
    // FAT_FREE_MASS(5), // TODO confirm what this means
    // FAT_RATIO(6), // TODO confirm what this means
    // FAT_MASS_WEIGHT(8), // TODO confirm what this means
    DIASTOLIC_BLOOD_PRESSURE(9),
    SYSTOLIC_BLOOD_PRESSURE(10),
    HEART_RATE(11),
    OXYGEN_SATURATION(54);

    private int magicNumber;

    WithingsBodyMeasureType(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    /**
     * @return the magic number used to refer to this body measure type in responses
     */
    public int getMagicNumber() {
        return magicNumber;
    }
}
