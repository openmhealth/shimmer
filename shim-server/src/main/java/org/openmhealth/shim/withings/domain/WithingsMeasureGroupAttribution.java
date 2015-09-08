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

import java.util.Optional;


/**
 * A measure group attribution included in responses from the Withings body measure endpoint, specifically in an
 * 'attrib' property. The attribution defines whether the user that created the measure group is ambiguous, and whether
 * the measure group was sensed or self-reported.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see {@link org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper}
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public enum WithingsMeasureGroupAttribution {

    SENSED_AND_UNAMBIGUOUS(0, true, false),
    SENSED_BUT_AMBIGUOUS(1, true, true),
    SELF_REPORTED(2, false, false),
    SELF_REPORTED_DURING_CREATION(4, false, false); // TODO confirm

    private int magicNumber;
    private boolean sensed;
    private boolean ambiguous;

    WithingsMeasureGroupAttribution(int magicNumber, boolean sensed, boolean ambiguous) {

        this.magicNumber = magicNumber;
        this.sensed = sensed;
        this.ambiguous = ambiguous;
    }

    /**
     * @return the magic number used to refer to this attribution in responses
     */
    public int getMagicNumber() {
        return magicNumber;
    }

    /**
     * @return true if the measure was sensed, false if it was self-reported
     */
    public boolean isSensed() {
        return sensed;
    }

    /**
     * @return true if the measure may belong to more than one user, false if it is known to belong to one user.
     * According to Withings, this can happen for weight measure groups when a measurement is taken before a new
     * user is synced to the device.
     */
    public boolean isAmbiguous() {
        return ambiguous;
    }

    /**
     * @param magicNumber a magic number
     * @return the constant corresponding to the magic number
     */
    public static Optional<WithingsMeasureGroupAttribution> findByMagicNumber(Integer magicNumber) {

        for (WithingsMeasureGroupAttribution constant : values()) {
            if (constant.getMagicNumber() == magicNumber) {
                return Optional.of(constant);
            }
        }

        return Optional.empty();
    }
}
