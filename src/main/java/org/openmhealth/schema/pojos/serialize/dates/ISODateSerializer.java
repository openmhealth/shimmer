package org.openmhealth.schema.pojos.serialize.dates;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

/**
 * Standard date serializer for Joda time in the regular schemas
 *
 * @author Danilo Bonilla
 */
public class ISODateSerializer extends JsonSerializer<DateTime> {

    protected static DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

    @Override
    public void serialize(DateTime value, JsonGenerator gen,
                          SerializerProvider arg2)
        throws IOException {
        gen.writeString(formatter.print(value));
    }

}
