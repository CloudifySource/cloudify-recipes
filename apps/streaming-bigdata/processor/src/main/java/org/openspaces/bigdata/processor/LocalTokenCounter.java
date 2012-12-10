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

import com.gigaspaces.client.WriteModifiers;
import org.openspaces.bigdata.processor.events.TokenCounter;
import org.openspaces.bigdata.processor.events.TokenizedTweet;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEvent;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.openspaces.events.polling.ReceiveHandler;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static com.google.common.collect.Maps.newHashMap;

/**
 * This polling container processor performs token count on bulks of {@link TokenizedTweet}
 * 
 * @author Dotan Horovits
 */
@EventDriven
@Polling(gigaSpace = "gigaSpace", passArrayAsIs = true, concurrentConsumers = 1, maxConcurrentConsumers = 1)
@TransactionalEvent
public class LocalTokenCounter {
    private static final Logger log = Logger.getLogger(LocalTokenCounter.class.getName());
    private static final int BATCH_SIZE = 5;
    private static final int WRITE_TIMEOUT = 5000;
    private static final int LEASE_TTL = 5000;

    @Resource(name = "clusteredGigaSpace")
    GigaSpace clusteredGigaSpace;

    @Resource(name = "gigaSpace")
    GigaSpace gigaSpace;

    @ReceiveHandler
    ReceiveOperationHandler receiveHandler() {
        MultiTakeReceiveOperationHandler receiveHandler = new MultiTakeReceiveOperationHandler();
        receiveHandler.setMaxEntries(BATCH_SIZE);        
        return receiveHandler;
    }

    /**
     * This method returns the template of a filtered {@link TokenizedTweet}.
     * 
     * @return template for the event container
     */
    @EventTemplate
    TokenizedTweet tokenizedFilteredTweet() {
        TokenizedTweet template = new TokenizedTweet();
        template.setFiltered(true);
        return template;
    }

    /**
     * Event handler that takes a bulk of {@link TokenizedTweet}, counts appearances of tokens in the bulk, and generates a corresponding {@link TokenCounter}
     * for each token.
     * 
     * @param tokenizedTweets
     *            array of {@link TokenizedTweet} matching the event template
     */
    @SpaceDataEvent
    public void eventListener(TokenizedTweet[] tokenizedTweets) {
        log.info("local counting of a bulk of " + tokenizedTweets.length + " tweets");
        Map<String, Integer> tokenMap = newHashMap();
        for (TokenizedTweet tokenizedTweet : tokenizedTweets) {
            log.fine("--processing " + tokenizedTweet);
            for (Entry<String, Integer> entry : tokenizedTweet.getTokenMap().entrySet()) {
                String token = entry.getKey();
                Integer count = entry.getValue();
                int newCount = tokenMap.containsKey(token) ? tokenMap.get(token) + count : count;
                log.finest("put token " + token + " with count " + newCount);
                tokenMap.put(token, newCount);
            }
        }

        log.info("writing " + tokenMap.size() + " TokenCounters across the cluster");
        for (Entry<String, Integer> entry : tokenMap.entrySet()) {
            String token = entry.getKey();
            Integer count = entry.getValue();
            log.fine("writing new TokenCounter: token=" + token + ", count=" + count);
            clusteredGigaSpace.write(new TokenCounter(token, count), LEASE_TTL, WRITE_TIMEOUT, WriteModifiers.UPDATE_OR_WRITE);
        }
    }
}
