package org.openmhealth.schema.pojos.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.Metadata;

import java.io.IOException;

public class MetadataTimestampSerializer extends JsonSerializer<DateTime> {

    private static DateTimeFormatter formatter =
        DateTimeFormat.forPattern(Metadata.TIMESTAMP_FORMAT);

    public MetadataTimestampSerializer() {
    }

    @Override
    public void serialize(DateTime value, JsonGenerator gen,
                          SerializerProvider arg2)
        throws IOException {
        gen.writeString(formatter.print(value));
    }
}