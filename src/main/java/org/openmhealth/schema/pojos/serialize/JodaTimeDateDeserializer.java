package org.openmhealth.schema.pojos.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.io.IOException;

public class JodaTimeDateDeserializer extends JsonDeserializer<DateTime> {

    private static DateTimeFormatter formatter =
        DateTimeFormat.forPattern(TimeFrame.DATE_TIME_FORMAT);

    @Override
    public DateTime deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return formatter.parseDateTime(node.asText());
    }
}
