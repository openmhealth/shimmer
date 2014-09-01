package org.openmhealth.schema.pojos.serialize.dates;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.generic.TimeInterval;

public class SimpleDateDeserializer extends ISODateDeserializer {
    protected static DateTimeFormatter formatter =
        DateTimeFormat.forPattern(TimeInterval.FULLDATE_FORMAT);
}
