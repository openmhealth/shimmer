package org.openmhealth.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import org.junit.Test;
import org.openmhealth.schema.pojos.*;

import java.util.Arrays;
import java.util.List;

public class SchemaGeneratorTest {

    @Test
    public void test() throws JsonProcessingException {

        List<Class<? extends DataPoint>> standardSchemaPojos = Arrays.asList(Activity.class, BloodGlucose.class,
            BloodPressure.class, BodyHeight.class, BodyWeight.class, HeartRate.class, NumberOfSteps.class,
            SleepDuration.class);

        for (Class<?> clazz : standardSchemaPojos) {
            ObjectMapper mapper = new ObjectMapper();
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
            mapper.acceptJsonFormatVisitor(clazz, visitor);
            JsonSchema schema = visitor.finalSchema();
            System.out.println("===========================================================================");
            System.out.println(clazz.getSimpleName() + ":");
            System.out.println("===========================================================================");
            System.out.println(
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
        }

        /**
         * Mongo commands for installing schema types to the registry
         *
         * db.registry.insert({"schema_id":"omh:normalized:activity","schema_version":1,"schema":{"type":"object","fields":[]}});
         * db.registry.insert({"schema_id":"omh:normalized:number-of-steps","schema_version":1,"schema":{"type":"object","fields":[]}});
         */
    }
}
