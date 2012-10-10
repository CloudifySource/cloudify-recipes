/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openspaces.bigdata.processor;

import static java.lang.System.currentTimeMillis;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gigaspaces.document.SpaceDocument;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Ignore
public class TestCassandraPersistence {

    @Autowired
    private CassandraExternalPersistence persister;

    @Test
    public void testPersistence() {
        Long id = currentTimeMillis();
        persister.write(new SpaceDocument("Tweet") //
                .setProperty("Id", id) //
                .setProperty("Text", "text") //
                .setProperty("CreatedAt", "now") //
                .setProperty("FromUserId", "uri1803") //
                .setProperty("ToUserId", "natishalom"));
    }
}
