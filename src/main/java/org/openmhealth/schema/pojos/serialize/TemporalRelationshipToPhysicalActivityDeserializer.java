package org.openmhealth.schema.pojos.serialize;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.pojos.TemporalRelationshipToPhysicalActivity;

import java.io.IOException;

public class TemporalRelationshipToPhysicalActivityDeserializer
    extends JsonDeserializer<TemporalRelationshipToPhysicalActivity> {

    @Override
    public TemporalRelationshipToPhysicalActivity deserialize(
        JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return TemporalRelationshipToPhysicalActivity.valueForLabel(node.asText());
    }
}
