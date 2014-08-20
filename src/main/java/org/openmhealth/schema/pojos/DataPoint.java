package org.openmhealth.schema.pojos;

import org.joda.time.DateTime;

public interface DataPoint {

    public static final String NAMESPACE = "omh:normalized";

    String getSchemaName();

    DateTime getTimeStamp();

    Metadata getMetadata();

}
