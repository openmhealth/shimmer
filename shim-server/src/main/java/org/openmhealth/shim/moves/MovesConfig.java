package org.openmhealth.shim.moves;

import org.openmhealth.shim.ApplicationAccessParameters;
import org.openmhealth.shim.ApplicationAccessParametersRepo;
import org.openmhealth.shim.ShimConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Cheng-Kang Hsieh on 3/3/15.
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.moves")


    public class MovesConfig implements ShimConfig {

        private String clientId;

        private String clientSecret;

        @Autowired
        private ApplicationAccessParametersRepo applicationParametersRepo;

        public String getClientId() {
            ApplicationAccessParameters parameters =
                    applicationParametersRepo.findByShimKey(MovesShim.SHIM_KEY);
            return parameters != null ? parameters.getClientId() : clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            ApplicationAccessParameters parameters =
                    applicationParametersRepo.findByShimKey(MovesShim.SHIM_KEY);
            return parameters != null ? parameters.getClientSecret() : clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }


