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


import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.testng.annotations.BeforeClass;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;


/**
 * @author Emerson Farrugia
 */
public abstract class ValidationUnitTests {

    private static Validator validator;


    /**
     * @return the validator
     */
    protected static Validator getValidator() {
        return validator;
    }


    @BeforeClass
    public static void prepareValidator() {

        HibernateValidatorConfiguration configuration = Validation.byProvider(HibernateValidator.class).configure();

        // enable fail-fast mode for unit tests to simplify debugging
        ValidatorFactory factory = configuration.failFast(true).buildValidatorFactory();

        validator = factory.getValidator();
    }

    protected void assertThatBeanIsValid(Object bean) {

        assertThat(getValidator().validate(bean), is(empty()));

    }

    protected void assertThatBeanIsNotValid(Object bean) {

        assertThat(getValidator().validate(bean), is(not(empty())));
    }
}