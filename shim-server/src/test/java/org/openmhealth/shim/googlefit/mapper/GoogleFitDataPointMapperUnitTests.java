package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.shim.googlefit.mapper.GoogleFitDataPointMapper.*;


/**
 * Created by Chris Schaefbauer on 7/12/15.
 */
public abstract class GoogleFitDataPointMapperUnitTests<T extends Measure> extends DataPointMapperUnitTests {

    protected JsonNode responseNode;

    @BeforeTest
    public abstract void initializeResponseNode() throws IOException;

    @Test
    public abstract void asDataPointsShouldReturnCorrectNumberOfDataPoints();

    @Test
    public abstract void asDataPointsShouldReturnCorrectDataPoints();

    public abstract void testGoogleFitMeasureFromDataPoint(T testMeasure, Map<String,Object> properties);

    public void testGoogleFitDataPoint(DataPoint<T> dataPoint,Map<String,Object> properties){

        testGoogleFitMeasureFromDataPoint(dataPoint.getBody(),properties);
        DataPointHeader dataPointHeader = dataPoint.getHeader();
        assertThat(dataPointHeader.getAcquisitionProvenance().getSourceName(),equalTo(RESOURCE_API_SOURCE_NAME));

    }

    public Map<String,Object> createFloatingPointTestProperties(double fpValue,String startDateTime,String endDateTime){
        Map<String,Object> properties = createTestProperties(startDateTime,endDateTime);
        properties.put("fpValue",fpValue);
        return properties;
    }

    public Map<String,Object> createIntegerTestProperties(long intValue,String startDateTime,String endDateTime){
        Map<String, Object> properties = createTestProperties(startDateTime, endDateTime);
        properties.put("intValue",intValue);
        return properties;
    }

    private Map<String, Object> createTestProperties(String startDateTimeString, String endDateTimeString) {
        HashMap<String, Object> properties = Maps.newHashMap();
        if(startDateTimeString!=null){
            properties.put("startDateTimeString",startDateTimeString);
        }
        if(endDateTimeString!=null){
            properties.put("endDateTimeString",endDateTimeString);
        }


        return properties;
    }
}
