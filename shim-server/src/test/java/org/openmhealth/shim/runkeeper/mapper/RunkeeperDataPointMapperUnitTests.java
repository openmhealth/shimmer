package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * TODO refactor
 * @author Chris Schaefbauer
 */
public class RunkeeperDataPointMapperUnitTests extends DataPointMapperUnitTests {

    RunkeeperDataPointMapper mapper = new RunkeeperDataPointMapper() {
        @Override
        protected Optional<DataPoint> asDataPoint(JsonNode listEntryNode) {
            return null;
        }
    };

    @Test
    public void getModalityShouldReturnSensedOnlyWhenSourceIsRunkeeperAndModeIsApiAndHasPath() throws IOException {

        JsonNode expectedSensedNode = objectMapper.readTree("{\"entry_mode\": \"API\",\n" +
                "            \"has_path\": true,\n" +
                "            \"source\": \"RunKeeper\"}");

        assertThat(mapper.getModality(expectedSensedNode).get(), equalTo(SENSED));

        JsonNode expectedEmptyNodeMissingMode = objectMapper.readTree("{\"has_path\": true,\n" +
                "            \"source\": \"RunKeeper\"}");

        assertThat(mapper.getModality(expectedEmptyNodeMissingMode).isPresent(), equalTo(false));

        JsonNode expectedEmptyNodeMissingSource = objectMapper.readTree("{\"has_path\": true,\n" +
                "            \"source\": \"RunKeeper\"}");

        assertThat(mapper.getModality(expectedEmptyNodeMissingSource).isPresent(), equalTo(false));

        JsonNode expectedEmptyNodeMissingHasPath = objectMapper.readTree("{\"entry_mode\": \"API\",\n" +
                "            \"source\": \"RunKeeper\"}");

        assertThat(mapper.getModality(expectedEmptyNodeMissingHasPath).isPresent(), equalTo(false));

        JsonNode expectedEmptyNodeHasPathFalse = objectMapper.readTree("{\"entry_mode\": \"API\",\n" +
                "            \"has_path\": false,\n" +
                "            \"source\": \"RunKeeper\"}");

        assertThat(mapper.getModality(expectedEmptyNodeHasPathFalse).isPresent(), equalTo(false));
    }

    @Test
    public void getModalityShouldReturnSelfReportedModeIsWeb() throws IOException {

        JsonNode expectedSensedNode = objectMapper.readTree("{\"entry_mode\": \"Web\",\n" +
                "            \"has_path\": true,\n" +
                "            \"source\": \"RunKeeper\"}");

        assertThat(mapper.getModality(expectedSensedNode).get(), equalTo(SELF_REPORTED));
    }

    @Test
    public void getModalityShouldReturnEmptyWhenSourceIsNotRunkeeper() throws IOException {

        JsonNode expectedEmptyNode = objectMapper.readTree("{\"entry_mode\": \"API\",\n" +
                "            \"has_path\": true,\n" +
                "            \"source\": \"Linq (QA)\"}");

        assertThat(mapper.getModality(expectedEmptyNode).isPresent(), equalTo(false));
    }
}
