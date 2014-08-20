package org.openmhealth.schema.pojos.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.Metadata;

import java.io.IOException;

public class MetadataTimestampDeserializer extends JsonDeserializer<DateTime> {

    private static DateTimeFormatter formatter =
        DateTimeFormat.forPattern(Metadata.TIMESTAMP_FORMAT);

    public MetadataTimestampDeserializer() {
    }

    @Override
    public DateTime deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return formatter.parseDateTime(node.asText());
    }
}
