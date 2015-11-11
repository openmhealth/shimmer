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

import org.testng.annotations.Test;


/**
 * @author Emerson Farrugia
 */
public class SchemaNameValidationUnitTests extends ValidationUnitTests {

    class Wrapper {

        private String name;

        public Wrapper(String name) {
            this.name = name;
        }

        @ValidSchemaName
        public String getName() {
            return name;
        }
    }


    @Test
    public void validateShouldFailOnEmptyString() {

        assertThatBeanIsNotValid(new Wrapper(""));
    }

    @Test
    public void validateShouldFailOnIllegalCharacter() {

        assertThatBeanIsNotValid(new Wrapper("abc."));
    }

    @Test
    public void validateShouldPassOnValidString() {

        assertThatBeanIsValid(new Wrapper("abc"));
    }
}
