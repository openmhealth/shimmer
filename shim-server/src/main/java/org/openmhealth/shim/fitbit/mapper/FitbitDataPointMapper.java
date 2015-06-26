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
    //TODO: Add heart rate mapper and hook the mappers up to the shim itself

    public static final String RESOURCE_API_SOURCE_NAME = "Fitbit Resource API";

    /**
     * Maps JSON response nodes from the Fitbit API into a list of {@link DataPoint} objects with the appropriate type
     *
     * <p>Data points from the Fitbit API do not have an associated time zone, so this mapper uses the
     * time zone from the user's profile. The user's current time zone may have changed since the data point was
     * created, so the original offset for that date point may differ from the offset used in this mapper. There is
     * currently no way to determine the appropriate time zone for a datapoint given the Fitbit API.</p>
     *
     * @param responseNodes the list of two json nodes - the first being the get-user-info response (from user/<user-id>/profile) and the second being the specific data point of interest for the mapper
     * @return a list of DataPoint objects of type T with the appropriate values mapped from the input JSON; if JSON objects are contained within an array in the input response, each item in that array will map into an item in the lit
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {
        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 2, "FitbitDataPointMapper requires two response nodes - a response node from get-user-info and from the target datatype endpoint.");
        JsonNode firstNode = responseNodes.get(0);
        checkNotNull(firstNode.get("user"), "The first response node in the input list for the FitbitDataPointMapper (index 0) should be the response from get-user-info");

        //TODO: Look at a more robust approach to parsing the user-info and target-type nodes
        JsonNode userInfoNode = responseNodes.get(0);

        // FIXME fix the time zone offset to use the appropriate offset for the data point once it is fixed by Fitbit
        int utcOffsetMillis = asRequiredInteger(userInfoNode, "user.offsetFromUTCMillis");
        checkNotNull(responseNodes.get(1).get(getListNodeName()),"The second response node in the input list for the FitbitDataPointMapper (index 1) should be the response from the target type endpoint");
        JsonNode targetTypeNodeList = asRequiredNode(responseNodes.get(1), getListNodeName());
        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        for(JsonNode targetTypeNode:targetTypeNodeList){
            asDataPoint(targetTypeNode,utcOffsetMillis).ifPresent(dataPoints::add);
        }

        return dataPoints;


    }

    /**
     * Adds a {@link DataPointHeader} to a {@link Measure} object and wraps it as a {@link DataPoint}
     * @param measure the body of the data point
     * @param externalId the identifier of the measure as recorded by the data provider in the JSON data-point (optional)
     * @param <T> the measure type (e.g., StepCount, BodyMassIndex)
     * @return a {@link DataPoint} object containing the body and header that map values from Fitbit API response nodes to schema objects
     */
    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, Long externalId) {
        DataPointAcquisitionProvenance acquisitionProvenance = new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME).build();
        if (externalId != null) {
            acquisitionProvenance.setAdditionalProperty("external_id", externalId);
        }
        DataPointHeader header = new Builder(UUID.randomUUID().toString(), measure.getSchemaId()).setAcquisitionProvenance(acquisitionProvenance).build();
        return new DataPoint<>(header,measure);
    }

    /**
     * Takes a Fitbit response JSON node, which contains a date and time property, and then maps them into an {@link OffsetDateTime} object with an offset given by the second parameter
     * @param node
     * @param offsetFromUTCInMilliseconds
     * @return the date and time based on the "date" and "time" properties of the JsonNode parameter with the appropriate UTC offset, wrapped as an {@link Optional}
     */
    protected Optional<OffsetDateTime> combineDateTimeAndTimezone(JsonNode node, int offsetFromUTCInMilliseconds){
        Optional<LocalDateTime> dateTime = asOptionalLocalDateTime(node,"date","time");
        Optional<OffsetDateTime> offsetDateTime = null;
        if(dateTime.isPresent()){
            offsetDateTime = Optional.ofNullable(OffsetDateTime.of(dateTime.get(), ZoneOffset.ofTotalSeconds(offsetFromUTCInMilliseconds / 1000)));

        }
        return offsetDateTime;
    }

    /**
     * Transforms a {@link LocalDateTime} object into an {@link OffsetDateTime} object with a zone offset given by the offsetFromUTCInMilliseconds parameter
     * @param dateTime local date and time for the Fitbit response JSON node
     * @param offsetFromUTCInMilliseconds the offset from UTC in milliseconds
     * @return the date and time based on the input dateTime parameter with the appropriate UTC offset
     */
    protected OffsetDateTime combineDateTimeAndTimezone(LocalDateTime dateTime, int offsetFromUTCInMilliseconds){
        return OffsetDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(offsetFromUTCInMilliseconds / 1000));
    }

    /**
     * Implemented by subclasses to map a JSON response node from the Fitbit API into a {@link Measure} object of the appropriate type
     * @param node
     * @param offsetFromUTCInMilliseconds
     * @return a {@link DataPoint} object containing the target measure with the appropriate values from the JSON node parameter, wrapped as an {@link Optional}
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode node, int offsetFromUTCInMilliseconds);

    /**
     * @return the name of the list node used by this mapper
     */
    protected abstract String getListNodeName();
}
