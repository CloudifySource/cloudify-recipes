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

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEvent;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.openspaces.events.polling.ReceiveHandler;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;
import org.springframework.beans.factory.annotation.Value;

import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.client.SQLQuery;

/**
 * This polling container processor removes processed tweets and persists it to an external data store
 * 
 * @author Dotan Horovits
 */
@EventDriven
@Polling(gigaSpace = "gigaSpace", passArrayAsIs = true, concurrentConsumers = 2, maxConcurrentConsumers = 2, receiveTimeout = 10000)
@TransactionalEvent
public class TweetPersister {
    private static final Logger log = Logger.getLogger(TweetPersister.class.getName());

    @Value("${batch.size}")
    private int BATCH_SIZE = 100;

    @Resource
    private ExternalPersistence persister;

    @ReceiveHandler
    ReceiveOperationHandler receiveHandler() {
        MultiTakeReceiveOperationHandler receiveHandler = new MultiTakeReceiveOperationHandler();
        receiveHandler.setMaxEntries(BATCH_SIZE);
        receiveHandler.setNonBlocking(true);
        receiveHandler.setNonBlockingFactor(1);
        return receiveHandler;
    }

    @EventTemplate
    SQLQuery<SpaceDocument> processedTweet() {
        return new SQLQuery<SpaceDocument>("Tweet", "Processed = ?", true);
    }

    @SpaceDataEvent
    public void eventListener(SpaceDocument[] tweetArray) {
        log.info("writing behind a bulk of " + tweetArray.length + " tweets to backend persistence store");
        try {
            persister.writeBulk(tweetArray);
        } catch (IOException e) {
            log.severe("error persisting tweet bulk: " + e.getMessage());
        }
    }
}
