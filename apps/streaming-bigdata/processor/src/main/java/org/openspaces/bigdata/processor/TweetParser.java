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

import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.openspaces.bigdata.processor.events.TokenizedTweet;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEvent;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;

import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.client.SQLQuery;

/**
 * This polling container processor parses raw tweets, generating TokenizedTweets.
 * <p>
 * A raw tweet is represented by an instance of SpaceDocument of type "Tweet"
 * 
 * @author Dotan Horovits
 */
@EventDriven
@Polling(gigaSpace = "gigaSpace", concurrentConsumers = 2, maxConcurrentConsumers = 2, receiveTimeout = 60)
@TransactionalEvent(timeout = 100)
public class TweetParser {
    private static final Logger log = Logger.getLogger(TweetParser.class.getName());
    private static final int MIN_TOKEN_LENGTH = 3;

    @Resource(name = "clusteredGigaSpace")
    GigaSpace clusteredGigaSpace;

    @Resource(name = "gigaSpace")
    GigaSpace gigaSpace;

    /**
     * This method returns a SQL query defining an unprocessed Tweet.
     * 
     * @return {@link SQLQuery} of a {@link SpaceDocument} of type "Tweet"
     */
    @EventTemplate
    SQLQuery<SpaceDocument> unprocessedTweet() {
        return new SQLQuery<SpaceDocument>("Tweet", "Processed = " + false);
    }

    /**
     * Event handler that receives a Tweet instance, processes its text and generates a listing of the tokens appearing in the text and their respective count
     * of appearance in the text, instantiates an instance of {@link TokenizedTweet} with this data, and writes it to the space.
     * 
     * @param tweet
     * @return {@link TokenizedTweet} containing a mapping of {token->count}
     */
    @SpaceDataEvent
    public SpaceDocument eventListener(SpaceDocument tweet) {
        log.info("parsing tweet " + tweet);

        Long id = (Long) tweet.getProperty("Id");
        String text = tweet.getProperty("Text");
        if (text != null) {
            gigaSpace.write(new TokenizedTweet(id, tokenize(text)));
        }

        tweet.setProperty("Processed", true);
        return tweet;
    }

    protected Map<String, Integer> tokenize(String text) {
        Map<String, Integer> tokenMap = newHashMap();
        StringTokenizer st = new StringTokenizer(text, "\"{}[]:;|<>?`'.,/~!@#$%^&*()_-+= \t\n\r\f\\");

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() < MIN_TOKEN_LENGTH) {
                continue;
            }
            Integer count = tokenMap.containsKey(token) ? tokenMap.get(token) + 1 : 1;
            tokenMap.put(token, count);
        }
        return tokenMap;
    }
}
