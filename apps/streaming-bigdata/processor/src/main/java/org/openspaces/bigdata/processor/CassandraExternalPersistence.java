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

import static java.lang.String.format;
import static me.prettyprint.hector.api.factory.HFactory.createColumn;
import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.createMutator;
import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.mutation.Mutator;

import org.springframework.beans.factory.annotation.Value;

import com.gigaspaces.document.SpaceDocument;

public class CassandraExternalPersistence implements ExternalPersistence {
    private static final Logger log = Logger.getLogger(CassandraExternalPersistence.class.getName());
    private static final StringSerializer stringSerializer = StringSerializer.get();

    @Value("${cassandra.keyspace}")
    private String keyspaceName;
    @Value("${cassandra.cf}")
    private String columnFamily;
    @Value("${cassandra.host}")
    private String host = "localhost";
    @Value("${cassandra.port}")
    private int port = 9160;

    private Cluster cluster;
    private Keyspace keyspace;

    @PostConstruct
    public void init() throws Exception {
        log.info(format("initializing connection to Cassandra DB: host=%s port=%d keyspace=%s column-family=%s\n" //
                , host, port, keyspaceName, columnFamily));
        cluster = getOrCreateCluster(keyspaceName, host + ":" + port);
        keyspace = createKeyspace(keyspaceName, cluster);
    }

    @Override
    public void write(Object data) {
        if (!(data instanceof SpaceDocument)) {
            log.log(Level.WARNING, "Received non document event");
            return;
        }
        SpaceDocument document = (SpaceDocument) data;
        Long id = document.getProperty("Id");
        log.info("persisting data with id=" + id);
        Mutator<String> mutator = createMutator(keyspace, stringSerializer);
        for (String key : document.getProperties().keySet()) {
            Object value = document.getProperty(key);
            if (value != null) {
                mutator.addInsertion(String.valueOf(id), //
                        columnFamily, //
                        createColumn(key, value.toString(), stringSerializer, stringSerializer));
            }
        }
        mutator.execute();
    }

    @Override
    public void writeBulk(Object[] dataArray) {
        for (Object o : dataArray) {
            write(o);
        }
    }
}
