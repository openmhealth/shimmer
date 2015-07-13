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
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * Created by Chris Schaefbauer on 7/12/15.
 */
public abstract class GoogleFitDataPointMapper<T extends Measure> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Google Fit API";

    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes){
        checkNotNull(responseNodes);
        checkArgument(responseNodes.size()==1,"Only one response should be input to the mapper");
        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        JsonNode listNodes = asRequiredNode(responseNodes.get(0), getListNodeName());
        for(JsonNode listNode:listNodes){
            asDataPoint(listNode).ifPresent(dataPoints::add);
        }

        return  dataPoints;

    }

    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode listNode);


    public DataPoint<T> newDataPoint(T measure,String fitDataSourceId){


        DataPointAcquisitionProvenance.Builder acquisitionProvenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);
        DataPointAcquisitionProvenance acquisitionProvenance = acquisitionProvenanceBuilder.build();
        DataPointHeader header = new DataPointHeader.Builder(UUID.randomUUID().toString(), measure.getSchemaId()).
                setAcquisitionProvenance(acquisitionProvenance).build();

        return new DataPoint<>(header, measure);
    }

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
