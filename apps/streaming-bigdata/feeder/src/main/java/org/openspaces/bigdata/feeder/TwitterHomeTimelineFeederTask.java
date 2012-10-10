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
import org.springframework.dao.DataAccessException;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * A {{Runnable}} implementation that connects to Twitter, retrieves the public timeline tweets, converts them to standard Tweet
 * SpaceDocument format, and writes them to a remote space.
 *
 */
@Component
public class TwitterHomeTimelineFeederTask implements Runnable {
    private Logger log = Logger.getLogger(getClass().getSimpleName());


    @Resource
    private GigaSpace gigaSpace;

    public void run() {
        try {
            for (Tweet publicTweet : getPublicTimeline()) {
                logTweet(publicTweet);
                
            	//make sure tweets are not null and sleep if they are  
            	if ( publicTweet == null )
            	{

					Thread.sleep(5000);
            		log.info("No tweets sleeping ..... ");
            		continue;
            	}
                logTweet(publicTweet);
                gigaSpace.write(buildTweet(publicTweet));
                Thread.sleep(2000);

                gigaSpace.write(buildTweet(publicTweet));
            }
        } catch (DataAccessException e) {
            log.log(Level.SEVERE, "error feeding tweets",e);
        } catch (InterruptedException e) {}
    }

    public SpaceDocument buildTweet(Tweet tweet) {
        return new SpaceDocument("Tweet", new DocumentProperties() //
                .setProperty("Id", tweet.getId()) //
                .setProperty("Text", tweet.getText()) //
                .setProperty("CreatedAt", tweet.getCreatedAt()) //
                .setProperty("FromUserId", tweet.getFromUserId()) //
                .setProperty("ToUserId", tweet.getToUserId()) //
                .setProperty("Processed", Boolean.FALSE));
    }

    /**
     * Return all the tweets from the Twitter API
     */
    private List<Tweet> getPublicTimeline() {
        return new TwitterTemplate() //
                .timelineOperations() //
                .getPublicTimeline();
    }

    private void logTweet(Tweet tweet) {
        log.fine(String.format("Tweet id=%d\tfromUser=%s\ttext=%s \n", tweet.getId(), tweet.getFromUser(), tweet.getText()));
    }


}
