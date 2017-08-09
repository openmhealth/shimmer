/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;


/**
 * @author Danilo Bonilla
 * @author Emerson Farrugia
 */
@Component
public class ShimRegistryImpl implements ShimRegistry {

    @Autowired
    private List<Shim> allShims;

    private Map<String, Shim> configuredShims = new LinkedHashMap<>();


    @PostConstruct
    public void initializeRegistry() {

        for (Shim shim : allShims) {
            if (shim.isConfigured()) {
                configuredShims.put(shim.getShimKey(), shim);
            }
        }
    }

    @Override
    public Shim getShim(String shimKey) {

        if (!configuredShims.containsKey(shimKey)) {
            throw new RuntimeException(format("A configuration for shim '%s' wasn't found.", shimKey));
        }

        return configuredShims.get(shimKey);
    }
}
