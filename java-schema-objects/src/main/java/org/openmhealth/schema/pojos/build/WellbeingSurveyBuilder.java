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
import org.openmhealth.schema.pojos.WellbeingSurvey;
import org.openmhealth.schema.pojos.generic.TimeFrame;

/**
 *
 * @author Fara Kahir @ginsberg
 */
public class WellbeingSurveyBuilder implements SchemaPojoBuilder<WellbeingSurvey> {
    private WellbeingSurvey wellbeingSurvey;
    
    public final String ANSWER_SET_ONE_OPTION_ONE = "Not happy at all";
    public final String ANSWER_SET_ONE_OPTION_TWO = "Not very happy";
    public final String ANSWER_SET_ONE_OPTION_THREE = "Fairly happy";
    public final String ANSWER_SET_ONE_OPTION_FOUR = "Very happy";
    
    public final String ANSWER_SET_TWO_OPTION_ONE = "Some of the time";
    public final String ANSWER_SET_TWO_OPTION_TWO = "Less than half the time";
    public final String ANSWER_SET_TWO_OPTION_THREE = "Less than half the time";
    public final String ANSWER_SET_TWO_OPTION_FOUR = "More than half the time";
    public final String ANSWER_SET_TWO_OPTION_FIVE = "Most of the time";
    public final String ANSWER_SET_TWO_OPTION_SIX = "All of the time";
    
   public WellbeingSurveyBuilder() {
        wellbeingSurvey = new WellbeingSurvey();
        wellbeingSurvey.setEffectiveTimeFrame(new TimeFrame());
    }

    public WellbeingSurveyBuilder setWellbeingSurvey(String satisfaction, String cheerfulness, String calmness, String activeness, String freshness, String interest) {
          wellbeingSurvey.setSatisfaction(GetAnswerSetOneResult(satisfaction));
          wellbeingSurvey.setCheerfulness(GetAnswerSetTwoResult(cheerfulness));
          wellbeingSurvey.setCalmness(GetAnswerSetTwoResult(calmness));
          wellbeingSurvey.setActiveness(GetAnswerSetTwoResult(activeness));
          wellbeingSurvey.setFreshness(GetAnswerSetTwoResult(freshness));
          wellbeingSurvey.setInterest(GetAnswerSetTwoResult(interest));
        return this;
    }

    public WellbeingSurveyBuilder setTimeTaken(DateTime dateTime) {
        wellbeingSurvey.setEffectiveTimeFrame(TimeFrame.withDateTime(dateTime));
        return this;
    }

    @Override
    public WellbeingSurvey build() {
        return wellbeingSurvey;
    }   
    
    private String GetAnswerSetOneResult(String value)
    {
        String answer = "";
        switch(value){
            case "1":
                answer = ANSWER_SET_ONE_OPTION_ONE;
                break;
            case "2":
                answer = ANSWER_SET_ONE_OPTION_TWO;
                break;
            case "3":
                answer = ANSWER_SET_ONE_OPTION_THREE;
                break;
            case "4":
                answer = ANSWER_SET_ONE_OPTION_FOUR;
                break;
        }
        return answer;
    }
    
    private String GetAnswerSetTwoResult(String value)
    {
        String answer = "";
        switch(value){
            case "1":
                answer = ANSWER_SET_TWO_OPTION_ONE;
                break;
            case "2":
                answer = ANSWER_SET_TWO_OPTION_TWO;
                break;
            case "3":
                answer = ANSWER_SET_TWO_OPTION_THREE;
                break;
            case "4":
                answer = ANSWER_SET_TWO_OPTION_FOUR;
                break;
            case "5":
                answer = ANSWER_SET_TWO_OPTION_FIVE;
                break;
             case "6":
                answer = ANSWER_SET_TWO_OPTION_SIX;
                break;
        }
        return answer;
    }
}
