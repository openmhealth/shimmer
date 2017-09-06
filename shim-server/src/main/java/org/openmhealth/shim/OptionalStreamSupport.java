/*
 * Copyright 2017 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim;

import java.util.Optional;
import java.util.stream.Stream;


/**
 * A set of utility methods to help with {@link Optional} {@link Stream} objects.
 *
 * @author Emerson Farrugia
 */
public class OptionalStreamSupport {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <O> Stream<O> asStream(Optional<O> optional) {

        return optional.map(Stream::of).orElseGet(Stream::empty);
    }
}
