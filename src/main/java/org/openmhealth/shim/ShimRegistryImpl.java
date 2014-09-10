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

import org.openmhealth.shim.fatsecret.FatsecretConfig;
import org.openmhealth.shim.fatsecret.FatsecretShim;
import org.openmhealth.shim.fitbit.FitbitConfig;
import org.openmhealth.shim.fitbit.FitbitShim;
import org.openmhealth.shim.healthvault.HealthvaultConfig;
import org.openmhealth.shim.healthvault.HealthvaultShim;
import org.openmhealth.shim.jawbone.JawboneConfig;
import org.openmhealth.shim.jawbone.JawboneShim;
import org.openmhealth.shim.runkeeper.RunkeeperConfig;
import org.openmhealth.shim.runkeeper.RunkeeperShim;
import org.openmhealth.shim.withings.WithingsConfig;
import org.openmhealth.shim.withings.WithingsShim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

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
    private FitbitConfig fitbitConfig;

    @Autowired
    private FatsecretConfig fatsecretConfig;

    @Autowired
    private HealthvaultConfig healthvaultConfig;

    @Autowired
    private JawboneConfig jawboneConfig;

    @Autowired
    private RunkeeperConfig runkeeperConfig;

    @Autowired
    private WithingsConfig withingsConfig;

    private LinkedHashMap<String, Shim> registryMap;

    public ShimRegistryImpl() {
    }

    private void init() {
        registryMap = new LinkedHashMap<>();

        if (jawboneConfig.getClientId() != null && jawboneConfig.getClientSecret() != null) {
            registryMap.put(JawboneShim.SHIM_KEY,
                new JawboneShim(
                    authParametersRepo, accessParametersRepo, shimServerConfig, jawboneConfig));
        }

        if (runkeeperConfig.getClientId() != null && runkeeperConfig.getClientSecret() != null) {
            registryMap.put(RunkeeperShim.SHIM_KEY,
                new RunkeeperShim(
                    authParametersRepo, accessParametersRepo, shimServerConfig, runkeeperConfig));
        }

        if (fatsecretConfig.getClientId() != null && fatsecretConfig.getClientSecret() != null) {
            registryMap.put(FatsecretShim.SHIM_KEY,
                new FatsecretShim(authParametersRepo, shimServerConfig, fatsecretConfig));
        }

        if (withingsConfig.getClientId() != null && withingsConfig.getClientSecret() != null) {
            registryMap.put(WithingsShim.SHIM_KEY,
                new WithingsShim(
                    authParametersRepo, shimServerConfig, withingsConfig));
        }

        if (fitbitConfig.getClientId() != null && fitbitConfig.getClientSecret() != null) {
            registryMap.put(FitbitShim.SHIM_KEY,
                new FitbitShim(authParametersRepo, shimServerConfig, fitbitConfig));
        }

        if (healthvaultConfig.getClientId() != null) {
            registryMap.put(HealthvaultShim.SHIM_KEY,
                new HealthvaultShim(
                    authParametersRepo, shimServerConfig, healthvaultConfig));
        }
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
}
