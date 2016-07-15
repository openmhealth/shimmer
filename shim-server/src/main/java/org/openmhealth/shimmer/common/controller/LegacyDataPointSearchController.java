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
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * @author Danilo Bonilla
 */
@Configuration
@RestController
public class LegacyDataPointSearchController {

    @Autowired
    private AccessParametersRepo accessParametersRepo;

    @Autowired
    private ShimRegistry shimRegistry;


    /**
     * Endpoint for retrieving data from shims.
     *
     * @param username User ID record for which to retrieve data, if not approved this will throw ShimException.
     * <p>
     * TODO: finish javadoc!
     * @return The shim data response wrapper with data from the shim.
     */
    @RequestMapping(value = "/data/{shim}/{dataType}", produces = APPLICATION_JSON_VALUE)
    public ShimDataResponse data(
            @RequestParam(value = "username") String username,
            @PathVariable("shim") String shim,
            @PathVariable("dataType") String dataTypeKey,
            @RequestParam(value = "normalize", defaultValue = "true") boolean normalize,
            @RequestParam(value = "dateStart", defaultValue = "") String dateStart,
            @RequestParam(value = "dateEnd", defaultValue = "") String dateEnd)
            throws ShimException {

        setPassThroughAuthentication(username, shim);

        ShimDataRequest shimDataRequest = new ShimDataRequest();

        shimDataRequest.setDataTypeKey(dataTypeKey);
        shimDataRequest.setNormalize(normalize);

        if (!dateStart.isEmpty()) {
            shimDataRequest.setStartDateTime(LocalDateTime.parse(dateStart).atOffset(UTC));
        }
        if (!dateEnd.isEmpty()) {
            shimDataRequest.setEndDateTime(LocalDateTime.parse(dateEnd).atOffset(UTC));
        }

        AccessParameters accessParameters = accessParametersRepo.findByUsernameAndShimKey(
                username, shim, new Sort(Sort.Direction.DESC, "dateCreated"));

        if (accessParameters == null) {
            throw new ShimException("User '" + username + "' has not authorized shim: '" + shim + "'");
        }
        shimDataRequest.setAccessParameters(accessParameters);

        return shimRegistry.getShim(shim).getData(shimDataRequest);
    }

    /**
     * Sets pass through authentication required by spring.
     */
    private void setPassThroughAuthentication(String username, String shim) {
        SecurityContextHolder.getContext().setAuthentication(new ShimAuthentication(username, shim));
    }
}
