package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DataPointHeader.*;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;

/**
 * The base class for mappers that translate Fitbit API responses to {@link Measure} objects
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 *
 */
public abstract class FitbitDataPointMapper<T> implements JsonNodeDataPointMapper<T> {
    public static final String RESOURCE_API_SOURCE_NAME = "Fitbit Resource API";

    /**
     *
     * @param responseNodes the list of two json nodes - the first being the get-user-info response and the second being the specific data point of interest for the mapper
     * @return
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {
        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 2, "FitbitDataPointMapper requires two response nodes - a response node from get-user-info and from the target datatype endpoint.");
        JsonNode firstNode = responseNodes.get(0);
        checkNotNull(firstNode.get("user"), "The first response node in the input list for the FitbitDataPointMapper (index 0) should be the response from get-user-info");

        //TODO: Look at a more robust approach to parsing the user-info and target-type nodes
        JsonNode userInfoNode = responseNodes.get(0);
        int utcOffsetMillis = asRequiredInteger(userInfoNode, "user.offsetFromUTCMillis");
        checkNotNull(responseNodes.get(1).get(getListNodeName()),"The second response node in the input list for the FitbitDataPointMapper (index 1) should be the response from the target type endpoint");
        JsonNode targetTypeNodeList = asRequiredNode(responseNodes.get(1), getListNodeName());
        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        for(JsonNode targetTypeNode:targetTypeNodeList){
            asDataPoint(targetTypeNode,utcOffsetMillis).ifPresent(dataPoints::add);
        }

        return dataPoints;

    }

    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, Long externalId) {


        DataPointAcquisitionProvenance acquisitionProvenance = new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME).build();
        if (externalId != null) {
            acquisitionProvenance.setAdditionalProperty("external_id", externalId);
        }
        DataPointHeader header = new Builder(UUID.randomUUID().toString(), measure.getSchemaId()).setAcquisitionProvenance(acquisitionProvenance).build();
        return new DataPoint<>(header,measure);
    }

    protected Optional<OffsetDateTime> combineDateTimeAndTimezone(JsonNode node, int UTCOffsetInMilliseconds){
        Optional<LocalDateTime> dateTime = asOptionalLocalDateTime(node,"date","time");
        Optional<OffsetDateTime> offsetDateTime = null;
        if(dateTime.isPresent()){
            offsetDateTime = Optional.ofNullable(OffsetDateTime.of(dateTime.get(), ZoneOffset.ofTotalSeconds(UTCOffsetInMilliseconds / 1000)));

        }
        return offsetDateTime;
    }

    protected OffsetDateTime combineDateTimeAndTimezone(LocalDateTime dateTime, int UTCOffsetInMilliseconds){
        return OffsetDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(UTCOffsetInMilliseconds / 1000));
    }

    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode node, int offsetInMilliseconds);

    /**
     * @return the name of the list node used by this mapper
     */
    protected abstract String getListNodeName();
}
