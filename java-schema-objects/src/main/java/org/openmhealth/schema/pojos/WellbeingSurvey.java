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
package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.TimeFrame;

/**
 *
 * @author Fara Kahir @ginsberg
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = WellbeingSurvey.SCHEMA_WELLBEING_SURVEY, namespace = "omh:normalized")
public class WellbeingSurvey extends BaseDataPoint{
    @JsonProperty(value = "satisfaction", required = true)
    private String satisfaction;

    @JsonProperty(value = "cheerfulness", required = true)
    private String cheerfulness;
    
    @JsonProperty(value = "calmness", required = true)
    private String calmness;
    
    @JsonProperty(value = "activeness", required = true)
    private String activeness;
    
    @JsonProperty(value = "freshness", required = true)
    private String freshness;
    
    @JsonProperty(value = "interest", required = true)
    private String interest;
    
    @JsonProperty(value = "effective_time_frame", required = false)
    private TimeFrame effectiveTimeFrame;
    
    public static final String SCHEMA_WELLBEING_SURVEY = "wellbeing_survey";

    public WellbeingSurvey() {
    } 
    
    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_WELLBEING_SURVEY;
    }

    @Override
    @JsonIgnore
    public DateTime getTimeStamp() {
        return effectiveTimeFrame.getTimestamp();
    }
  
    public String getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(String satisfaction) {
        this.satisfaction = satisfaction;
    }

    public String getCheerfulness() {
        return cheerfulness;
    }

    public void setCheerfulness(String cheerfulness) {
        this.cheerfulness = cheerfulness;
    }
    
    public String getCalmness() {
        return calmness;
    }

    public void setCalmness(String calmness) {
        this.calmness = calmness;
    }
    
    public String getActiveness() {
        return activeness;
    }

    public void setActiveness(String activeness) {
        this.activeness = activeness;
    }
    
    public String getFreshness() {
        return freshness;
    }

    public void setFreshness(String freshness) {
        this.freshness = freshness;
    }
    
    public String getInterest() {
        return interest;
    }
    
    public void setInterest(String interest) {
        this.interest = interest;
    }
    
    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }
    
}
