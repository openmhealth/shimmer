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

package org.openmhealth.shim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Danilo Bonilla
 */
@Component
public class ShimRegistryImpl implements ShimRegistry {

    @Autowired
    private AccessParametersRepo accessParametersRepo;

    @Autowired
    private AuthorizationRequestParametersRepo authParametersRepo;

    @Autowired
    private ShimServerConfig shimServerConfig;

    @Autowired
    private List<Shim> shims;

    private Map<String, Shim> registryMap;

    public ShimRegistryImpl() {
    }

    public void init() {
        Map<String, Shim> registryMap = new LinkedHashMap<>();
        for (Shim shim : shims) {
            if (shim.isConfigured()) {
                registryMap.put(shim.getShimKey(), shim);
            }
        }
        this.registryMap = registryMap;
    }

    @Override
    public Shim getShim(String shimKey) {
        if (registryMap == null) {
            init();
        }

        if (!registryMap.containsKey(shimKey)) {
            throw new RuntimeException(format("A configuration for shim '%s' wasn't found.", shimKey));
        }

        return registryMap.get(shimKey);
    }

    @Override
    public List<Shim> getAvailableShims() {
        return shims;
    }

    @Override
    public List<Shim> getShims() {
        if (registryMap == null) {
            init();
        }
        return new ArrayList<>(registryMap.values());
    }
}
