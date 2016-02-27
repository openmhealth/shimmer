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

package org.openmhealth.shimmer.common.decoder;

/**
 * A decoder that passes through the value input because no encoding is present. This decoder in essence implies the
 * absence of a decoder, however it supports the case where we pass all pagination values through a decoder for
 * simplicity sake. Since not every value is encoded, we would need a decoder that does nothing.
 * <p>
 * todo: Decide if this is needed based on whether all values go through a decoder
 *
 * @author Chris Schaefbauer
 */
public class PassthroughPaginationResponseDecoder implements PaginationResponseDecoder {

    @Override
    public String decodePaginationResponseValue(String paginationResponseValue) {
        return paginationResponseValue;
    }
}
