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

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.mutation.Mutator;

import org.openspaces.archive.ArchiveOperationHandler;
import org.springframework.beans.factory.annotation.Value;

import com.gigaspaces.document.SpaceDocument;

/**
 * This implementation persists space documents into Cassandra.
 * It assumes that each 
 * @author Itai Frenkel
 *
 */
public class CassandraNaiveArchiveOperationHandler implements ArchiveOperationHandler {
    private static final Random RANDOM = new Random();
	private static final Logger log = Logger.getLogger(CassandraNaiveArchiveOperationHandler.class.getName());
    private static final StringSerializer stringSerializer = StringSerializer.get();

    @Value("${cassandra.keyspace}")
    private String keyspaceName;
    @Value("${cassandra.cf}")
    private String columnFamily;

    private String host = "localhost";
    @Value("${cassandra.port}")
    private int port = 9160;
   
    private Cluster cluster;
    private Keyspace keyspace;
    
    @Value("${cassandra.exception.percentage}")
	private int exceptionPercentage = 0;
    
    @PostConstruct
    public void init() throws Exception {    	
        log.info(format("initializing connection to Cassandra DB: host=%s port=%d keyspace=%s column-family=%s\n" //
                , host, port, keyspaceName, columnFamily));
        cluster = getOrCreateCluster(keyspaceName, host + ":" + port);
        keyspace = createKeyspace(keyspaceName, cluster);
    }

    private void write(Object data) {
    	if (RANDOM.nextInt(100) < exceptionPercentage) {
    		throw new RuntimeException("injected exception");
    	}
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

    /**
	 * @see org.openspaces.archive.ArchiveOperationHandler#archive()
	 */
    @Override
    public void archive(Object... dataArray) {
    	log.info("Writing " + dataArray.length + " object(s) to Cassandra");
        for (Object o : dataArray) {
            write(o);
        }
    }

	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @see org.openspaces.archive.ArchiveOperationHandler#supportsAtomicBatchArchiving()
	 */
	@Override
	public boolean supportsBatchArchiving() {
		return true;
	}   
}
