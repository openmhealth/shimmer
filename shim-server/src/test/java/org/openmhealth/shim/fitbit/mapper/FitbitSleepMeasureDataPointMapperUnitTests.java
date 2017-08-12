package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.SchemaSupport;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;


public abstract class FitbitSleepMeasureDataPointMapperUnitTests<T extends SchemaSupport>
        extends DataPointMapperUnitTests {

    protected JsonNode sleepDateResponseNode;
    protected JsonNode sleepDateEmptySleepListResponseNode;
    protected JsonNode sleepDateRangeResponseNode;
    protected JsonNode sleepDateRangeEmptySleepListResponseNode;

    @BeforeMethod
    public void initializeResponseNode() throws IOException {

        sleepDateResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date.json");
        sleepDateEmptySleepListResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-empty-sleep-list.json");
        sleepDateRangeResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-range.json");
        sleepDateRangeEmptySleepListResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-range-empty-sleep-list.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(getMapper().asDataPoints(sleepDateResponseNode).size(), equalTo(2));
        assertThat(getMapper().asDataPoints(sleepDateEmptySleepListResponseNode), is(empty()));
        assertThat(getMapper().asDataPoints(sleepDateRangeResponseNode).size(), equalTo(2));
        assertThat(getMapper().asDataPoints(sleepDateRangeEmptySleepListResponseNode), is(empty()));
    }

    protected abstract FitbitSleepMeasureDataPointMapper<T> getMapper();
}
