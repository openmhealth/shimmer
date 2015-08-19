package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.Measure;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * @author Chris Schaefbauer
 */
public abstract class JawboneBodyEventsDataPointMapper<T extends Measure> extends JawboneDataPointMapper<T> {

    @Override
    protected Optional<T> getMeasure(JsonNode listEntryNode) {
        if(!containsType(listEntryNode,getBodyEventType())){
            return Optional.empty();
        }
        Optional<Measure.Builder<T, ?>> builderOptional = newMeasureBuilder(listEntryNode);
        if(builderOptional.isPresent()){

            Measure.Builder<T,?> builder = builderOptional.get();

            Optional<Long> dateTimeInEpochSeconds = asOptionalLong(listEntryNode, "time_created");
            if(dateTimeInEpochSeconds.isPresent()){
                Optional<String> timezoneStringOptional = asOptionalString(listEntryNode, "details.tz");
                if(timezoneStringOptional.isPresent()){
                    ZoneId timeZoneId;

                    try{
                        timeZoneId = ZoneId.of(timezoneStringOptional.get());
                        builder.setEffectiveTimeFrame(
                                OffsetDateTime
                                        .ofInstant(Instant.ofEpochSecond(dateTimeInEpochSeconds.get()), timeZoneId));
                    }
                    catch(DateTimeException e){
                        e.printStackTrace();
                    }
                }
                else{
                    builder.setEffectiveTimeFrame(OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(dateTimeInEpochSeconds.get()), ZoneId.of("Z")));
                }
            }

            Optional<String> optionalUserNote = asOptionalString(listEntryNode, "note");
            optionalUserNote.ifPresent(userNote->builder.setUserNotes(userNote));

            Optional<String> externalId = asOptionalString(listEntryNode, "xid");

            return Optional.of(builder.build());
        }

        return Optional.empty();
    }


    private boolean containsType(JsonNode listEntryNode,JawboneBodyEventType bodyEventType) {
        if(bodyEventType == JawboneBodyEventType.BODY_WEIGHT || bodyEventType == JawboneBodyEventType.BODY_MASS_INDEX){
            Optional<Double> optionalPropertyValue = asOptionalDouble(listEntryNode, bodyEventType.getPropertyName());
            if(optionalPropertyValue.isPresent()){
                if(optionalPropertyValue.get()!=null){
                    return true;
                }
            }
        }
        return false;
    }

    abstract Optional<Measure.Builder<T, ?>> newMeasureBuilder(JsonNode measuresNode);



    abstract protected JawboneBodyEventType getBodyEventType();

    //    @Override
    //    protected Optional<DataPoint<T>> asDataPoint(JsonNode listEntryNode) {
    //        if(!containsType(listEntryNode,getBodyEventType())){
    //            return Optional.empty();
    //        }
    //        Optional<Measure.Builder<T, ?>> builderOptional = newMeasureBuilder(listEntryNode);
    //        if(builderOptional.isPresent()){
    //
    //            Measure.Builder<T,?> builder = builderOptional.get();
    //
    //            Optional<Long> dateTimeInEpochSeconds = asOptionalLong(listEntryNode, "time_created");
    //            if(dateTimeInEpochSeconds.isPresent()){
    //                Optional<String> timezoneStringOptional = asOptionalString(listEntryNode, "details.tz");
    //                if(timezoneStringOptional.isPresent()){
    //                    ZoneId timeZoneId;
    //
    //                    try{
    //                        timeZoneId = ZoneId.of(timezoneStringOptional.get());
    //                        builder.setEffectiveTimeFrame(
    //                                OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateTimeInEpochSeconds.get()), timeZoneId));
    //                    }
    //                    catch(DateTimeException e){
    //                        e.printStackTrace();
    //                    }
    //                }
    //                else{
    //                    builder.setEffectiveTimeFrame(OffsetDateTime.ofInstant(
    //                            Instant.ofEpochSecond(dateTimeInEpochSeconds.get()), ZoneId.of("Z")));
    //                }
    //            }
    //
    //            Optional<String> optionalUserNote = asOptionalString(listEntryNode, "note");
    //            optionalUserNote.ifPresent(userNote->builder.setUserNotes(userNote));
    //
    //            Optional<String> externalId = asOptionalString(listEntryNode, "xid");
    //
    //            return Optional.of(newDataPoint(builder.build(), RESOURCE_API_SOURCE_NAME, externalId.orElse(null),null));
    //        }
    //
    //        return Optional.empty();
    //    }

}
