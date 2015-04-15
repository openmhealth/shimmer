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
package org.openmhealth.schema.pojos.build;

import org.openmhealth.schema.pojos.Tag;

/**
 *
 * @author Fara Kahir
 */
public class TagBuilder implements SchemaPojoBuilder<Tag>{
   private Tag tag;
    
   public TagBuilder() {
        tag = new Tag();
    }

    public TagBuilder setTag(String value, String count) {
        tag.setTagName(value);
        tag.setTagCount(Integer.parseInt(count));
        return this;
    }

    @Override
    public Tag build() {
        return tag;
    }
}
