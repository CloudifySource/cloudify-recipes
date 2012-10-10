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

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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

/**
 * Event polling container processor filters out non-informative tokens, such as prepositions, from non-filtered {@link TokenizedTweet} instances.
 * 
 * @author Dotan Horovits
 */
@EventDriven
@Polling(gigaSpace = "gigaSpace", concurrentConsumers = 2, maxConcurrentConsumers = 2, receiveTimeout = 5000)
@TransactionalEvent
public class TokenFilter {
    private static final Logger log = Logger.getLogger(TokenFilter.class.getName());
    private static final int BATCH_SIZE = 100;

    @Resource(name = "gigaSpace")
    GigaSpace gigaSpace;

    @PostConstruct
    void postConstruct() {
        log.info(this.getClass().getName() + " initialized");
    }

    @ReceiveHandler
    ReceiveOperationHandler receiveHandler() {
        MultiTakeReceiveOperationHandler receiveHandler = new MultiTakeReceiveOperationHandler();
        receiveHandler.setMaxEntries(BATCH_SIZE);
        receiveHandler.setNonBlocking(true);
        receiveHandler.setNonBlockingFactor(1);
        return receiveHandler;
    }

    /**
     * This method returns the template of a non-filtered {@link TokenizedTweet}.
     * 
     * @return template for the event container
     */
    @EventTemplate
    TokenizedTweet tokenizedNonFilteredTweet() {
        TokenizedTweet template = new TokenizedTweet();
        template.setFiltered(false);
        return template;
    }

    /**
     * Event handler that receives a {@link TokenizedTweet} and filters out non-informative tokens. Filtering is performed using
     * {@link #isTokenRequireFilter(String)}
     * 
     * @param tokenizedTweet
     * @return the input tokenizedTweet after modifications
     */
    @SpaceDataEvent
    public TokenizedTweet eventListener(TokenizedTweet tokenizedTweet) {
        log.info("filtering tweet " + tokenizedTweet.getId());
        Map<String, Integer> tokenMap = newHashMap(tokenizedTweet.getTokenMap());
        int numTokensBefore = tokenMap.size();
        Iterator<Entry<String, Integer>> it = tokenMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Integer> entry = it.next();
            if (isTokenRequireFilter(entry.getKey())) {
                it.remove();
            }
        }
        int numTokensAfter = tokenMap.size();
        tokenizedTweet.setTokenMap(tokenMap);
        tokenizedTweet.setFiltered(true);
        log.fine("filtered out " + (numTokensBefore - numTokensAfter) + " tokens from tweet " + tokenizedTweet.getId());
        return tokenizedTweet;
    }

    private boolean isTokenRequireFilter(final String token) {
        return filterTokensSet.contains(token);
    }

    private static final Set<String> filterTokensSet = newHashSet("aboard", "about", "above", "across", "after", "against", "along", "amid", "among", "anti",
            "around", "as", "at", "before", "behind", "below", "beneath", "beside", "besides", "between", "beyond", "but", "by", "concerning", "considering",
            "despite", "down", "during", "except", "excepting", "excluding", "following", "for", "from", "in", "inside", "into", "like", "minus", "near", "of",
            "off", "on", "onto", "opposite", "outside", "over", "past", "per", "plus", "regarding", "round", "save", "since", "than", "through", "to",
            "toward", "under", "underneath", "unlike", "until", "up", "upon", "versus", "via", "with", "within", "without");
}
