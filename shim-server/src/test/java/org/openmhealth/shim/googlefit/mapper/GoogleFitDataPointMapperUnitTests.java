package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.DataPointModality;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.shim.googlefit.mapper.GoogleFitDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * Created by Chris Schaefbauer on 7/12/15.
 */
public abstract class GoogleFitDataPointMapperUnitTests<T extends Measure> extends DataPointMapperUnitTests {

    protected JsonNode responseNode;

    protected abstract String getDataPointResourcePath();

    @Test
    public abstract void asDataPointsShouldReturnCorrectNumberOfDataPoints();

    @Test
    public abstract void asDataPointsShouldReturnCorrectDataPoints();

    public abstract void testGoogleFitMeasureFromDataPoint(T testMeasure, Map<String,Object> properties);

    public void testGoogleFitDataPoint(DataPoint<T> dataPoint,Map<String,Object> properties){
        testGoogleFitMeasureFromDataPoint(dataPoint.getBody(),properties);
        DataPointHeader dataPointHeader = dataPoint.getHeader();
        assertThat(dataPointHeader.getAcquisitionProvenance().getSourceName(),equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(dataPointHeader.getAcquisitionProvenance().getAdditionalProperties().get(
                "source_origin_id"),equalTo(properties.get("sourceOriginId")));
        if(properties.containsKey("modality")){
            assertThat(dataPointHeader.getAcquisitionProvenance().getModality(),equalTo(properties.get("modality")));
        }

    }

    public Map<String,Object> createFloatingPointTestProperties(double fpValue, String startDateTime,
            String endDateTime, String sourceOriginId){
        Map<String,Object> properties = createTestProperties(startDateTime,endDateTime,sourceOriginId);
        properties.put("fpValue",fpValue);
        return properties;
    }

    public Map<String,Object> createIntegerTestProperties(long intValue,String startDateTime,String endDateTime,String sourceOriginId){
        Map<String, Object> properties = createTestProperties(startDateTime, endDateTime,sourceOriginId);
        properties.put("intValue",intValue);
        return properties;
    }

    private Map<String, Object> createTestProperties(String startDateTimeString, String endDateTimeString, String sourceOriginId) {
        HashMap<String, Object> properties = Maps.newHashMap();
        if(startDateTimeString!=null){
            properties.put("startDateTimeString",startDateTimeString);
        }
        if(endDateTimeString!=null){
            properties.put("endDateTimeString",endDateTimeString);
        }
        if(sourceOriginId!=null){
            properties.put("sourceOriginId",sourceOriginId);
            if(sourceOriginId.endsWith("user_input")){
                properties.put("modality", DataPointModality.SELF_REPORTED);
            }
        }

        return properties;
    }
}
