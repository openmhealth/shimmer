package org.openmhealth.shim;

import org.openmhealth.shim.fatsecret.FatsecretShim;
import org.openmhealth.shim.fitbit.FitbitShim;
import org.openmhealth.shim.healthvault.HealthvaultShim;
import org.openmhealth.shim.jawbone.JawboneShim;
import org.openmhealth.shim.runkeeper.RunkeeperShim;
import org.openmhealth.shim.withings.WithingsShim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
public class ShimRegistryImpl implements ShimRegistry {

    @Autowired
    private AccessParametersRepo accessParametersRepo;

    @Autowired
    private AuthorizationRequestParametersRepo authParametersRepo;

    private LinkedHashMap<String, Shim> registryMap;

    public ShimRegistryImpl() {
    }

    private void init() {
        registryMap = new LinkedHashMap<String, Shim>();
        registryMap.put(JawboneShim.SHIM_KEY, new JawboneShim(authParametersRepo, accessParametersRepo));
        registryMap.put(RunkeeperShim.SHIM_KEY, new RunkeeperShim(authParametersRepo, accessParametersRepo));
        registryMap.put(FatsecretShim.SHIM_KEY, new FatsecretShim(authParametersRepo));
        registryMap.put(WithingsShim.SHIM_KEY, new WithingsShim(authParametersRepo));
        registryMap.put(FitbitShim.SHIM_KEY, new FitbitShim(authParametersRepo));
        registryMap.put(HealthvaultShim.SHIM_KEY, new HealthvaultShim(authParametersRepo));
    }

    @Override
    public Shim getShim(String shimKey) {
        if (registryMap == null) {
            init();
        }
        return registryMap.get(shimKey);
    }
}
