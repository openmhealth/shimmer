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


import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.ClientTokenServices;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;

import java.util.List;

/**
 * @author Danilo Bonilla
 */
public class AccessParameterClientTokenServices implements ClientTokenServices {

    private AccessParametersRepo accessParametersRepo;

    public AccessParameterClientTokenServices(AccessParametersRepo accessParametersRepo) {
        this.accessParametersRepo = accessParametersRepo;
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2ProtectedResourceDetails resource,
                                            Authentication authentication) {
        String username = authentication.getPrincipal().toString();
        String shimKey = authentication.getDetails().toString();

        AccessParameters accessParameters = accessParametersRepo.findByUsernameAndShimKey(
            username, shimKey, new Sort(Sort.Direction.DESC, "dateCreated"));

        if (accessParameters == null || accessParameters.getSerializedToken() == null) {
            return null; //No token was found!
        }

        return SerializationUtils.deserialize(accessParameters.getSerializedToken());
    }

    @Override
    public void saveAccessToken(OAuth2ProtectedResourceDetails resource,
                                Authentication authentication, OAuth2AccessToken accessToken) {
        String username = authentication.getPrincipal().toString();
        String shimKey = authentication.getDetails().toString();
        AccessParameters accessParameters = accessParametersRepo.findByUsernameAndShimKey(
            username, shimKey, new Sort(Sort.Direction.DESC, "dateCreated"));

        if (accessParameters == null) {
            throw new IllegalStateException("Can't save serialized spring oauth2 access token, " +
                "no corresponding access parameters entity was found in which to put it.");
        }

        accessParameters.setSerializedToken(SerializationUtils.serialize(accessToken));
        accessParametersRepo.save(accessParameters);
    }

    @Override
    public void removeAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String username = authentication.getPrincipal().toString();
        String shimKey = authentication.getDetails().toString();

        List<AccessParameters> accessParameters =
            accessParametersRepo.findAllByUsernameAndShimKey(username, shimKey);
        for (AccessParameters accessParameter : accessParameters) {
            accessParametersRepo.delete(accessParameter);
        }
    }
}
