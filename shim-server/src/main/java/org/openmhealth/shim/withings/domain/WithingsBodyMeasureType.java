/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    DIASTOLIC_BLOOD_PRESSURE(9),
    SYSTOLIC_BLOOD_PRESSURE(10),
    HEART_RATE(11),
    OXYGEN_SATURATION(54),
    BODY_TEMPERATURE(71);

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
