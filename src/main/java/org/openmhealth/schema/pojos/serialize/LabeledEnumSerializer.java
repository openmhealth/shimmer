package org.openmhealth.schema.pojos.serialize;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.openmhealth.schema.pojos.LabeledEnum;

import java.io.IOException;

public class LabeledEnumSerializer extends JsonSerializer<LabeledEnum> {

    @Override
    public void serialize(LabeledEnum labeledEnum,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(labeledEnum.getLabel());
    }
}
