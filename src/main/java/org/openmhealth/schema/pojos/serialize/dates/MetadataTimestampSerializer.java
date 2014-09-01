package org.openmhealth.schema.pojos.serialize.dates;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.Metadata;

@Deprecated
public class MetadataTimestampSerializer extends ISODateSerializer{

    protected static DateTimeFormatter formatter =
        DateTimeFormat.forPattern(Metadata.TIMESTAMP_FORMAT);
}