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

package org.openspaces.bigdata.feeder;

import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.document.SpaceDocument;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceInterruptedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;

/**
 * A {{Runnable}} that generates and feeds simulated tweets to a remote space periodically using scheduled task.
 * <p>
 * The feeder uses tweetTextList, a predefined Spring-injected list of tweet texts, and numberOfUsers for generating user ids.
 *
 */
//@Component
public class ListBasedFeederTask implements Runnable {
    private Logger log = Logger.getLogger(getClass().getSimpleName());

    @Value("${tweet.numberOfUsers:10}")
    private int numberOfUsers = 10;


    @Resource
    private List<String> tweetTextList;

    @Resource
    private GigaSpace gigaSpace;

    private long counter = 1;
    private Random randomGenerator = new Random();

    public void run() {
        try {
            SpaceDocument tweet = buildRandomTweet();
            gigaSpace.write(tweet);
            log.fine("--- FEEDER WROTE " + tweet);
        } catch (SpaceInterruptedException e) {
            log.fine("We are being shutdown " + e.getMessage());
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
    }

    private SpaceDocument buildRandomTweet() {
        String randomTweet = tweetTextList.get(randomGenerator.nextInt(tweetTextList.size()));
        return buildTweet(counter++ //
                , randomTweet //
                , currentTimeMillis() //
                , randomGenerator.nextInt(numberOfUsers) //
                , randomGenerator.nextInt(numberOfUsers));
    }

    public long getCounter() {
        return counter;
    }


    public SpaceDocument buildTweet(long id, String text, long createdAt, long toUserId, long fromUserId) {
        return new SpaceDocument("Tweet", new DocumentProperties() //
                .setProperty("Id", id) //
                .setProperty("Text", text) //
                .setProperty("CreatedAt", new Date(createdAt)) //
                .setProperty("FromUserId", fromUserId) //
                .setProperty("ToUserId", toUserId) //
                .setProperty("Processed", false));
    }
}
