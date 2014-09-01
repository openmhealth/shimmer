package org.openmhealth.schema.pojos.serialize.dates;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

public class ISODateDeserializer extends JsonDeserializer<DateTime> {

    protected static DateTimeFormatter formatter = ISODateTimeFormat.dateParser();

    @Override
    public DateTime deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return formatter.parseDateTime(node.asText());
    }
}
