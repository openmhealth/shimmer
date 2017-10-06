/*
 * Copyright 2017 Open mHealth
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

package org.openmhealth.shim.withings.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public enum WithingsDevice {

    ACTIVITY_TRACKER(16, "Activity tracker"),
    AURA(32, "Aura");

    private int magicNumber;
    private String displayName;

    private static Map<Integer, WithingsDevice> map = new HashMap<>();

    static {
        for (WithingsDevice constant : WithingsDevice.values()) {
            map.put(constant.magicNumber, constant);
        }
    }

    WithingsDevice(int magicNumber, String displayName) {
        this.magicNumber = magicNumber;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param magicNumber a magic number
     * @return the constant corresponding to the magic number
     */
    public static Optional<WithingsDevice> findByMagicNumber(Integer magicNumber) {

        return Optional.ofNullable(map.get(magicNumber));
    }
}
