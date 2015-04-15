/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmhealth.schema.pojos.build;

import org.openmhealth.schema.pojos.Alcohol;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.TimeFrame;

/**
 *
 * @author Fara Kahir
 */
public class AlcoholBuilder implements SchemaPojoBuilder<Alcohol>{
    private Alcohol alcohol;
    
   public AlcoholBuilder() {
        alcohol = new Alcohol();
        alcohol.setEffectiveTimeFrame(new TimeFrame());
    }

    public AlcoholBuilder setAlcohol(Double units) {
        alcohol.setAlcohol(units);
        return this;
    }

    public AlcoholBuilder setTimeTaken(DateTime dateTime) {
        alcohol.setEffectiveTimeFrame(TimeFrame.withDateTime(dateTime));
        return this;
    }

    @Override
    public Alcohol build() {
        return alcohol;
    }
}
