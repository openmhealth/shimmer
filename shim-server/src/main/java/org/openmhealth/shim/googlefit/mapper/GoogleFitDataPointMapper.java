package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalNode;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * The base class for mappers that translate Google Fit API responses into to {@link
 * Measure} objects
 *
 * @author Chris Schaefbauer
 */
public abstract class GoogleFitDataPointMapper<T extends Measure> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Google Fit API";

    /**
     * Maps a JSON response from the Google Fit API containing a JSON array of data points to a list of {@link DataPoint} objects
     * of the appropriate measure type. Splits individual nodes based on the name of the list node, "point," and then iteratively
     * maps the nodes in the list.
     *
     * @param responseNodes the response body from a Google Fit endpoint, contained in a list of a single JSON node
     * @return a list of DataPoint objects of type T with the appropriate values mapped from the input JSON; because
     * these JSON objects are contained within an array in the input response, each object in that JSON array will map to
     * an item in the returned list
     */
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes){
        checkNotNull(responseNodes);
        checkArgument(responseNodes.size()==1,"Only one response should be input to the mapper");

        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        Optional<JsonNode> listNodes = asOptionalNode(responseNodes.get(0), getListNodeName());
        if(listNodes.isPresent()){
            for(JsonNode listNode:listNodes.get()){
                asDataPoint(listNode).ifPresent(dataPoints::add);
            }
        }

        return  dataPoints;

    }

    /**
     * Abstract method to be implemented by subclasses mapping a JSON response node from the Google Fit API into a {@link
     * Measure} object of the appropriate type
     *
     * @param listNode an individual datapoint from the array from the Google Fit response
     * @return a {@link DataPoint} object containing the target measure with the appropriate values from the JSON node
     * parameter, wrapped as an {@link Optional}
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode listNode);


    public DataPoint<T> newDataPoint(T measure, String fitDataSourceId){

        DataPointAcquisitionProvenance.Builder acquisitionProvenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);
        if(fitDataSourceId!=null&&fitDataSourceId.endsWith("user_input")){
            acquisitionProvenanceBuilder.setModality(DataPointModality.SELF_REPORTED);
        }

        DataPointAcquisitionProvenance acquisitionProvenance = acquisitionProvenanceBuilder.build();
        if(fitDataSourceId!=null){
            acquisitionProvenance.setAdditionalProperty("source_origin_id",fitDataSourceId);
        }

        DataPointHeader header = new DataPointHeader.Builder(UUID.randomUUID().toString(), measure.getSchemaId()).
                setAcquisitionProvenance(acquisitionProvenance).build();

        return new DataPoint<>(header, measure);
    }

    /**
     * Converts a nanosecond timestamp from the Google Fit API into an offset datetime value
     * @param unixEpochNanosString the timestamp directly from the Google JSON document
     * @return an offset datetime object representing the input timestamp
     */
    public OffsetDateTime convertGoogleNanosToOffsetDateTime(String unixEpochNanosString){
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(0, Long.parseLong(unixEpochNanosString)), ZoneId.of("Z"));
    }

    public void setEffectiveTimeFrameIfPresent(T.Builder builder, JsonNode listNode){
        Optional<String> startTimeNanosString = asOptionalString(listNode, "startTimeNanos");
        Optional<String> endTimeNanosString = asOptionalString(listNode, "endTimeNanos");
        if(startTimeNanosString.isPresent()&&endTimeNanosString.isPresent()){
            if(startTimeNanosString.equals(endTimeNanosString)){
                builder.setEffectiveTimeFrame(convertGoogleNanosToOffsetDateTime(startTimeNanosString.get()));

            }
            else{
                builder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(
                        convertGoogleNanosToOffsetDateTime(startTimeNanosString.get()),
                        convertGoogleNanosToOffsetDateTime(endTimeNanosString.get())));
            }

        }
    }

    /**
     * The name of the list that contains the datapoints associated with the request
     */
    protected String getListNodeName(){
        return "point";
    }

    /**
     * The name of the list node contained within each datapoint that contains the target value
     */
    protected String getValueListNodeName(){
        return "value";
    }

}
