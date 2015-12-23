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

import com.google.common.collect.Range;
import org.openmhealth.shimmer.common.domain.DataPointSearchCriteria;
import org.openmhealth.shimmer.common.domain.DataPointSearchResult;
import org.openmhealth.shimmer.common.service.DataPointSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;
import java.time.OffsetDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


/**
 * A controller that finds and retrieves data points.
 *
 * @author Emerson Farrugia
 */
@RestController
public class DataPointSearchController {

    private static final Logger logger = LoggerFactory.getLogger(DataPointSearchController.class);

    /*
     * These filtering parameters are temporary. They will likely change when a more generic filtering approach is
     * implemented.
     */
    public static final String CREATED_ON_OR_AFTER_PARAMETER = "created_on_or_after";
    public static final String CREATED_BEFORE_PARAMETER = "created_before";
    public static final String EFFECTIVE_ON_OR_AFTER_PARAMETER = "effective_on_or_after";
    public static final String EFFECTIVE_BEFORE_PARAMETER = "effective_before";
    public static final String SCHEMA_NAMESPACE_PARAMETER = "schema_namespace";
    public static final String SCHEMA_NAME_PARAMETER = "schema_name";
    // TODO searching by schema version should support wildcards, which requires more thought
    // public static final String SCHEMA_VERSION_PARAMETER = "schema_version";
    public static final String ACQUISITION_SOURCE_ID_PARAMETER = "acquisition_source_id"; // TODO confirm name
    public static final String END_USER_ID_PARAMETER = "end_user_id"; // TODO confirm name and implementation

    @Autowired
    private Validator validator;

    @Autowired
    private DataPointSearchService dataPointSearchService;


    /**
     * Finds and retrieves data points.
     *
     * @param schemaNamespace the namespace of the schema the data points conform to
     * @param schemaName the name of the schema the data points conform to
     * @param createdOnOrAfter the earliest creation timestamp of the data points to return, inclusive
     * @param createdBefore the latest creation timestamp of the data points to return, exclusive
     * @return a list of matching data points
     */
    @RequestMapping(value = "/dataPoints", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DataPointSearchResult> findDataPoints(
            @RequestParam(value = SCHEMA_NAMESPACE_PARAMETER) final String schemaNamespace,
            @RequestParam(value = SCHEMA_NAME_PARAMETER) final String schemaName,
            @RequestParam(value = CREATED_ON_OR_AFTER_PARAMETER, required = false)
            @DateTimeFormat(iso = DATE_TIME)
            final OffsetDateTime createdOnOrAfter,
            @RequestParam(value = CREATED_BEFORE_PARAMETER, required = false)
            @DateTimeFormat(iso = DATE_TIME)
            final OffsetDateTime createdBefore,
            @RequestParam(value = EFFECTIVE_ON_OR_AFTER_PARAMETER, required = false)
            @DateTimeFormat(iso = DATE_TIME)
            final OffsetDateTime effectiveOnOrAfter,
            @RequestParam(value = EFFECTIVE_BEFORE_PARAMETER, required = false)
            @DateTimeFormat(iso = DATE_TIME)
            final OffsetDateTime effectiveBefore,
            @RequestParam(value = ACQUISITION_SOURCE_ID_PARAMETER, required = false) final String acquisitionSourceId,
            @RequestParam(value = END_USER_ID_PARAMETER, required = false) final String specifiedEndUserId,
            Authentication authentication) { // FIXME revise authentication

        // TODO this provides feature parity, but no security
        // FIXME revise authentication
        String endUserId = specifiedEndUserId;
        if (specifiedEndUserId == null || specifiedEndUserId.isEmpty()) {
            // determine the user associated with the access token to restrict the search accordingly
            endUserId = getEndUserId(authentication);
        }

        DataPointSearchCriteria searchCriteria = new DataPointSearchCriteria();

        searchCriteria.setUserId(specifiedEndUserId);
        searchCriteria.setSchemaNamespace(schemaNamespace);
        searchCriteria.setSchemaName(schemaName);
        searchCriteria.setCreatedOnOrAfter(createdOnOrAfter);
        searchCriteria.setCreatedBefore(createdBefore);
        searchCriteria.setEffectiveOnOrAfter(effectiveOnOrAfter);
        searchCriteria.setEffectiveBefore(effectiveBefore);
        searchCriteria.setAcquisitionSourceId(acquisitionSourceId);

        if (!validator.validate(searchCriteria).isEmpty()) {
            // TODO add feedback
            return badRequest().body(null);
        }

        DataPointSearchResult searchResult = dataPointSearchService.findDataPoints(searchCriteria);

        return ok().body(searchResult);
    }

    // FIXME revise authentication
    public String getEndUserId(Authentication authentication) {
        return "foo";
    }

    public Range<OffsetDateTime> asRange(OffsetDateTime onOrAfterDateTime, OffsetDateTime beforeDateTime) {

        if (onOrAfterDateTime != null && beforeDateTime != null) {
            return Range.closedOpen(onOrAfterDateTime, beforeDateTime);
        }

        if (onOrAfterDateTime != null) {
            return Range.atLeast(onOrAfterDateTime);
        }

        else if (beforeDateTime != null) {
            return Range.lessThan(beforeDateTime);
        }

        return Range.all();
    }
}
