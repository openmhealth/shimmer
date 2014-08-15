package org.openmhealth.schema.pojos.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.io.IOException;

/**
 * Standard date serializer for Joda time in the regular schemas
 */
public class JodaTimeDateSerializer extends JsonSerializer<DateTime> {

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern(TimeFrame.DATE_TIME_FORMAT);

    @Override
    public void serialize(DateTime value, JsonGenerator gen,
                          SerializerProvider arg2)
        throws IOException {
        gen.writeString(formatter.print(value));
    }

}
