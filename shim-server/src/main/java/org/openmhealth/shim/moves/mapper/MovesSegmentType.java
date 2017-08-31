/*
 * Copyright 2017 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.moves.mapper;

import static java.lang.String.format;


/**
 * A segment type in a Moves API response.
 *
 * @author Emerson Farrugia
 */
public enum MovesSegmentType {

    MOVE,
    PLACE;

    public static MovesSegmentType getByJsonValue(String jsonValue) {

        for (MovesSegmentType constant : values()) {
            if (constant.toString().equalsIgnoreCase(jsonValue)) {
                return constant;
            }
        }

        throw new IllegalArgumentException(format("The value '%s' isn't a recognized Moves segment type.", jsonValue));
    }
}
