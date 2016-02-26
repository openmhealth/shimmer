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

package org.openmhealth.shimmer.common.domain.pagination;

import java.util.Map;


/**
 * @author Chris Schaefbauer
 */
@Deprecated
public class RequestPaginationScheme {

    // Injected from config file
    protected String requestSchemeName;

    public String getRequestSchemeName(){
        return this.requestSchemeName;
    }

    public void setRequestSchemeName(String requestSchemeName){
        this.requestSchemeName = requestSchemeName;
    }

    // To be injected from config file, but how do we figure out which set of details to inject?
    Map<String, String> requestSchemeDetails;

    public Map<String, String> getRequestSchemeDetails() {

        return requestSchemeDetails;
    }

    public void setRequestSchemeDetails(Map<String, String> schemeDetails) {
        this.requestSchemeDetails = schemeDetails;
    }

    public String getRequestSchemeDetail(String schemeDetailKey) {

        if (requestSchemeDetails.containsKey(schemeDetailKey)) {
            return requestSchemeDetails.get(schemeDetailKey);
        }

        throw new UnsupportedOperationException();
    }

}
