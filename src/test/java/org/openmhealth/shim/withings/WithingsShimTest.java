package org.openmhealth.shim.withings;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.openmhealth.shim.ShimDataResponse;
import org.openmhealth.shim.ShimDataType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WithingsShimTest {

    @Test
    public void testNormalize() throws IOException {

        URL url = Thread.currentThread().getContextClassLoader().getResource("withings-body.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ShimDataType dataType = WithingsShim.WithingsDataType.BODY;

       /* SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class, withingsDataType.getNormalizer());
        objectMapper.registerModule(module);
        return objectMapper.readValue(responseEntity.getContent(), ShimDataResponse.class);
        */

        //todo: add more assertions and unit tests.

    }

}
