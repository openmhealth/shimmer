/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.withings;

import org.junit.Test;
import org.openmhealth.shim.ShimDataType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Danilo Bonilla
 */
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
