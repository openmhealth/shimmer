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

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.Wellbeing;
import org.openmhealth.schema.pojos.WellbeingMeasure;
import org.openmhealth.schema.pojos.generic.TimeFrame;

/**
 *
 * @author Fara Kahir @ginsberg
 */
public class WellbeingBuilder implements SchemaPojoBuilder<Wellbeing>{
    private Wellbeing wellbeing;
    
    public final String ANSWER_ONE = "Strongly Disagree";
    public final String ANSWER_TWO = "Disagree";
    public final String ANSWER_THREE = "Undecided";
    public final String ANSWER_FOUR = "Agree";
    public final String ANSWER_FIVE = "Strongly Agree";
    
   public WellbeingBuilder() {
        wellbeing = new Wellbeing();
        wellbeing.setEffectiveTimeFrame(new TimeFrame());
    }

    public WellbeingBuilder setWellbeing(String value, String measure) {
        WellbeingMeasure wellbeingMeasure = new WellbeingMeasure();
        wellbeingMeasure.setMeasure(measure);
        wellbeingMeasure.setValue(setWellBeingText(value));
        wellbeing.setWellbeingMeasure(wellbeingMeasure);
        return this;
    }

    public WellbeingBuilder setTimeTaken(DateTime dateTime) {
        wellbeing.setEffectiveTimeFrame(TimeFrame.withDateTime(dateTime));
        return this;
    }

    @Override
    public Wellbeing build() {
        return wellbeing;
    }
    
    private String setWellBeingText(String value)
    {
        String answer = "";
        switch(value)
        {
            case "1":
                answer = ANSWER_ONE;
                break;
            case "2":
                answer = ANSWER_TWO;
                break;
            case "3":
                answer = ANSWER_THREE;
                break;
            case "4":
                answer = ANSWER_FOUR;
                break;
            case "5":
                answer = ANSWER_FIVE;
                break;
        }
        
        return answer;
    }
}
