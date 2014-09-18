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

import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * A wrapper for encapsulating data requests sent
 * to shims. Prevents from having long method signatures.
 *
 * @author Danilo Bonilla
 */
public class ShimDataRequest {

    /**
     * Identifier for the type of data being retrieved
     */
    private String dataTypeKey;
    /**
     * parameters required for acessing data, this
     * will likely be oauth token + any extras or some
     * kind of trusted access.
     */
    private AccessParameters accessParameters;

    /**
     * The start date for the data being retrieved
     */

    private DateTime startDate;

    /**
     * The end date for the data being retrieved
     */
    private DateTime endDate;

    /**
     * List of columns required
     */
    private List<String> columnList;

    /**
     * The starting row for the data (for pagination purposes)
     */
    private Long numToSkip;

    /**
     * Number of rows to return
     */
    private Long numToReturn;

    /**
     * If true, returns normalized results
     * from the external data provider, otherwise
     * returns raw data.
     */
    private boolean normalize = false;

    public ShimDataRequest() {
    }

    public ShimDataRequest(String dataTypeKey,
                           AccessParameters accessParameters,
                           DateTime startDate,
                           DateTime endDate,
                           List<String> columnList,
                           Long numToSkip,
                           Long numToReturn,
                           boolean normalize) {
        this.dataTypeKey = dataTypeKey;
        this.accessParameters = accessParameters;
        this.startDate = startDate;
        this.endDate = endDate;
        this.columnList = columnList;
        this.numToSkip = numToSkip;
        this.numToReturn = numToReturn;
        this.normalize = false;
    }

    public void setDataTypeKey(String dataTypeKey) {
        this.dataTypeKey = dataTypeKey;
    }

    public void setAccessParameters(AccessParameters accessParameters) {
        this.accessParameters = accessParameters;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public void setColumnList(List<String> columnList) {
        this.columnList = columnList;
    }

    public void setNumToSkip(Long numToSkip) {
        this.numToSkip = numToSkip;
    }

    public void setNumToReturn(Long numToReturn) {
        this.numToReturn = numToReturn;
    }

    public String getDataTypeKey() {
        return dataTypeKey;
    }

    public AccessParameters getAccessParameters() {
        return accessParameters;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public List<String> getColumnList() {
        return columnList;
    }

    public Long getNumToSkip() {
        return numToSkip;
    }

    public Long getNumToReturn() {
        return numToReturn;
    }

    public boolean getNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public static ShimDataRequest fromHttpRequest(HttpServletRequest request) {
        ShimDataRequest dataRequest = new ShimDataRequest();
        return dataRequest;
    }
}
