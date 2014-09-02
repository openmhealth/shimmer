package org.openmhealth.schema.pojos.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.pojos.HeartRateUnitValue;

import java.io.IOException;

public class HeartRateUnitDeserializer extends JsonDeserializer<HeartRateUnitValue.Unit> {

    @Override
    public HeartRateUnitValue.Unit deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return HeartRateUnitValue.Unit.valueForLabel(node.asText());
    }
}
