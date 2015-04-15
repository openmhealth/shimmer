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
import org.openmhealth.schema.pojos.SleepQuality;
import org.openmhealth.schema.pojos.generic.TimeFrame;

/**
 *
 * @author Fara Kahir @ginsberg
 */
public class SleepQualityBuilder implements SchemaPojoBuilder<SleepQuality> {
    
   private SleepQuality sleepQuality;
    
   public SleepQualityBuilder() {
        sleepQuality = new SleepQuality();
        sleepQuality.setEffectiveTimeFrame(new TimeFrame());
    }

    public SleepQualityBuilder setSleepQuality(String value) {
        sleepQuality.setValue(GetSleepQuality(value));
        return this;
    }

    public SleepQualityBuilder setTimeTaken(DateTime dateTime) {
        sleepQuality.setEffectiveTimeFrame(TimeFrame.withDateTime(dateTime));
        return this;
    }

    @Override
    public SleepQuality build() {
        return sleepQuality;
    }
    
    public String GetSleepQuality(String value)
    {
        String answer = "";
        switch(value)
        {
            case "1":
                answer = "terrible";
                break;
            case "2":
                answer = "bad";
                break;
            case "3":
                answer = "ok";
                break;
           case "4":
                answer = "good";
                break;
            case "5":
                answer = "great";
                break;
        }
        
        return answer;
    }
}
