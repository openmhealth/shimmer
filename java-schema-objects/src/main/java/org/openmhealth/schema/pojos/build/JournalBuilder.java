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

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.Journal;
import org.openmhealth.schema.pojos.generic.TimeFrame;

/**
 *
 * @author Fara Kahir @Ginsberg
 */
public class JournalBuilder implements SchemaPojoBuilder<Journal> {
   private Journal journal;
    
   public JournalBuilder() {
        journal = new Journal();
        journal.setEffectiveTimeFrame(new TimeFrame());
    }

    public JournalBuilder setEntry(String entry) {
        journal.setEntry(entry);
        return this;
    }

    public JournalBuilder setTimeTaken(DateTime dateTime) {
        journal.setEffectiveTimeFrame(TimeFrame.withDateTime(dateTime));
        return this;
    }

    @Override
    public Journal build() {
        return journal;
    }    
}
