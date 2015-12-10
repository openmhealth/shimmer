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

package org.openmhealth.shimmer.common.controller;

import org.openmhealth.shim.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * @author Danilo Bonilla
 */
@Configuration
@RestController
public class LegacyConfigurationController {

    @Autowired
    private ApplicationAccessParametersRepo applicationAccessParametersRepo;

    @Autowired
    private ShimRegistry shimRegistry;


    /**
     * Return shims available in the registry and all endpoints.
     *
     * @return list of shims + endpoints in a map.
     */
    @RequestMapping(value = "registry", produces = APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> shimList(@RequestParam(value = "available", defaultValue = "") String available)
            throws ShimException {

        List<Map<String, Object>> results = new ArrayList<>();
        List<Shim> shims = "".equals(available) ? shimRegistry.getShims() : shimRegistry.getAvailableShims();

        for (Shim shim : shims) {
            List<String> endpoints = new ArrayList<>();
            for (ShimDataType dataType : shim.getShimDataTypes()) {
                endpoints.add(dataType.name());
            }
            Map<String, Object> row = new HashMap<>();
            row.put("shimKey", shim.getShimKey());
            row.put("label", shim.getLabel());
            row.put("endpoints", endpoints);
            ApplicationAccessParameters parameters = shim.findApplicationAccessParameters();
            if (parameters.getClientId() != null) {
                row.put("clientId", parameters.getClientId());
            }
            if (parameters.getClientSecret() != null) {
                row.put("clientSecret", parameters.getClientSecret());
            }
            results.add(row);
        }
        return results;
    }

    /**
     * Update shim configuration
     *
     * @return list of shims + endpoints in a map.
     */
    @RequestMapping(value = "shim/{shim}/config", method = {GET, PUT, POST}, produces = APPLICATION_JSON_VALUE)
    public List<String> updateShimConfig(
            @PathVariable("shim") String shimKey,
            @RequestParam("clientId") String clientId,
            @RequestParam("clientSecret") String clientSecret)
            throws ShimException {

        ApplicationAccessParameters parameters = applicationAccessParametersRepo.findByShimKey(shimKey);

        if (parameters == null) {
            parameters = new ApplicationAccessParameters();
            parameters.setShimKey(shimKey);
        }
        parameters.setClientId(clientId);
        parameters.setClientSecret(clientSecret);
        applicationAccessParametersRepo.save(parameters);
        shimRegistry.init();

        return singletonList("success");
    }
}
