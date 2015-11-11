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

package org.openmhealth.shimmer.common.validation;

import org.openmhealth.shimmer.common.domain.DataPointSearchCriteria;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.time.OffsetDateTime.now;


/**
 * @author Emerson Farrugia
 */
public class DataPointSearchCriteriaValidationUnitTests extends ValidationUnitTests {

    private DataPointSearchCriteria searchCriteria;


    @BeforeMethod
    public void initializeFixture() {

        searchCriteria = new DataPointSearchCriteria();

        searchCriteria.setUserId("someUserId"); // Tesla
        searchCriteria.setSchemaNamespace("someNamespace");
        searchCriteria.setSchemaName("someName");
        searchCriteria.setAcquisitionSourceId("someSourceId");
        searchCriteria.setOnOrAfterEffectiveTimestamp(now().minusDays(1));
        searchCriteria.setOnOrAfterEffectiveTimestamp(now().minusHours(23));
    }

    @Test
    public void validateShouldPassOnValidCriteria() {

        assertThatBeanIsValid(searchCriteria);
    }

    @Test
    public void validateShouldPassOnUndefinedAcquisitionSourceId() {

        searchCriteria.setAcquisitionSourceId(null);

        assertThatBeanIsValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnUndefinedUserId() {

        searchCriteria.setUserId(null);

        assertThatBeanIsNotValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnEmptyUserId() {

        searchCriteria.setUserId("");

        assertThatBeanIsNotValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnUndefinedSchemaNamespace() {

        searchCriteria.setSchemaNamespace(null);

        assertThatBeanIsNotValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnEmptySchemaNamespace() {

        searchCriteria.setSchemaNamespace("");

        assertThatBeanIsNotValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnInvalidSchemaNamespace() {

        searchCriteria.setSchemaNamespace("foo*bar");

        assertThatBeanIsNotValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnUndefinedSchemaName() {

        searchCriteria.setSchemaName(null);

        assertThatBeanIsNotValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnEmptySchemaName() {

        searchCriteria.setSchemaName("");

        assertThatBeanIsNotValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnInvalidSchemaName() {

        searchCriteria.setSchemaName("foo.bar");

        assertThatBeanIsNotValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnEmptyAcquisitionSourceId() {

        searchCriteria.setAcquisitionSourceId("");

        assertThatBeanIsNotValid(searchCriteria);
    }

    // TODO add tests for reversed time range bounds

    @Test
    public void validateShouldPassOnUnrestrictedEffectiveRangeAndRestrictedCreationRange() {

        searchCriteria.setOnOrAfterEffectiveTimestamp(null);
        searchCriteria.setBeforeEffectiveTimestamp(null);
        searchCriteria.setOnOrAfterCreationTimestamp(now().minusDays(1));
        searchCriteria.setBeforeCreationTimestamp(now().minusHours(23));

        assertThatBeanIsValid(searchCriteria);
    }

    @Test
    public void validateShouldPassOnRestrictedEffectiveRangeAndUnrestrictedCreationRange() {

        searchCriteria.setOnOrAfterEffectiveTimestamp(now().minusDays(1));
        searchCriteria.setBeforeEffectiveTimestamp(now().minusHours(23));
        searchCriteria.setOnOrAfterCreationTimestamp(null);
        searchCriteria.setBeforeCreationTimestamp(null);

        assertThatBeanIsValid(searchCriteria);
    }

    @Test
    public void validateShouldFailOnUnrestrictedTimestampRange() {

        searchCriteria.setOnOrAfterCreationTimestamp(null);
        searchCriteria.setBeforeCreationTimestamp(null);
        searchCriteria.setOnOrAfterEffectiveTimestamp(null);
        searchCriteria.setBeforeEffectiveTimestamp(null);

        assertThatBeanIsNotValid(searchCriteria);
    }
}
