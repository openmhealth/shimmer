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

package org.openmhealth.shim.fitbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.openmhealth.shim.ShimDataResponse;
import org.openmhealth.shim.healthvault.HealthvaultShim;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * @author Danilo Bonilla
 */
public class FitbitShimTest {


    @Test
    @SuppressWarnings("unchecked")
    public void testParse() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("fitbit-activities-1m.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        //todo: fix assertions here!!
    }

    @Test
    @Ignore
    public void testXml() throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class,
            HealthvaultShim.HealthVaultDataType.WEIGHT.getNormalizer());
        xmlMapper.registerModule(module);

        URL url = Thread.currentThread().getContextClassLoader().getResource("data-response-weight.xml");
        assert url != null;
        InputStream inputStream = url.openStream();

        ShimDataResponse response = xmlMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(response.getBody());

        inputStream.close();
    }

    @Test
    @Ignore
    public void testConvert() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class, FitbitShim.FitbitDataType.ACTIVITY.getNormalizer());
        objectMapper.registerModule(module);

        URL url = Thread.currentThread().getContextClassLoader().getResource("data-response-activity.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ShimDataResponse shimDataResponse = objectMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(shimDataResponse.getBody());

        inputStream.close();
    }
}
