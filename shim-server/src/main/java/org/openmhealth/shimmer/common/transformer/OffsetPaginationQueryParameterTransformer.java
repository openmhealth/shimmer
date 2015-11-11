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

package org.openmhealth.shimmer.common.transformer;

import org.openmhealth.shimmer.common.configuration.PaginationSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Chris Schaefbauer
 */
public class OffsetPaginationQueryParameterTransformer implements PaginationQueryParameterTransformer {

    @Override
    public Map<String, List<String>> transformPaginationParameters(
            PaginationSettings paginationSettings, Integer inputSkipValue,
            Integer inputLimitValue) {

//        HashMap<String, List<String>> paginationParameters = Maps.newHashMap();
//
//        paginationParameters.put(paginationSettings.getLimitQueryParameterName().get(),
//                singletonList(Integer.toString(inputLimitValue)));
//
//        if (paginationSettings.getRequestPaginationScheme().getRequestSchemeDetail("offset-type") == "page") {
//
//
//            Integer pageNumberFromSkipAndLimit = getDesiredPageNumberFromSkipAndLimit(inputSkipValue,
//                    inputLimitValue, Integer.parseInt(
//                            configurationProperties.getRequestPaginationScheme()
//                                    .getRequestSchemeDetail("offset-page-start")));
//
//            paginationParameters.put(configurationProperties.getOffsetQueryParameterName(),
//                    singletonList(pageNumberFromSkipAndLimit.toString())); // TODO: this needs testing
//
//
//        }
//        else if (configurationProperties.getRequestPaginationScheme().getRequestSchemeDetail("offset-type") == "raw") {
//            paginationParameters
//                    .put(configurationProperties.getOffsetQueryParameterName(),
//                            singletonList(inputSkipValue.toString()));
//        }
//
//        return paginationParameters;
        return new HashMap<>();

    }

    protected static Integer getDesiredPageNumberFromSkipAndLimit(Integer skipValue, Integer limitValue,
            Integer pageStartNumber) {

        Integer calculatedPage = skipValue / limitValue;
        return calculatedPage + pageStartNumber;

    }
}
