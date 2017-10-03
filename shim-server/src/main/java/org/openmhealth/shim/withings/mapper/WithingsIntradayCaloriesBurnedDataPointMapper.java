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

package org.openmhealth.shim.withings.mapper;

import org.openmhealth.schema.domain.omh.CaloriesBurned2;
import org.openmhealth.schema.domain.omh.TimeFrame;

import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;


/**
 * A mapper from Withings Intraday Activity endpoint responses (/measure?action=getactivity) to {@link CaloriesBurned2}
 * objects.
 * <p>
 * <p>This mapper handles responses from an API request that requires special permissions from Withings. This special
 * activation can be requested by filling the form linked from their <a href="http://oauth.withings
 * .com/api/doc#api-Measure-get_intraday_measure">API Documentation website</a></p>
 *
 * @author Emerson Farrugia
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_activity">Activity Measures API documentation</a>
 */
public class WithingsIntradayCaloriesBurnedDataPointMapper extends WithingsIntradayDataPointMapper<CaloriesBurned2> {

    @Override
    public String getMeasureValuePath() {
        return "calories";
    }

    @Override
    public CaloriesBurned2 newMeasure(Long measureValue, TimeFrame effectiveTimeFrame) {

        return new CaloriesBurned2.Builder(KILOCALORIE.newUnitValue(measureValue), effectiveTimeFrame).build();
    }
}
