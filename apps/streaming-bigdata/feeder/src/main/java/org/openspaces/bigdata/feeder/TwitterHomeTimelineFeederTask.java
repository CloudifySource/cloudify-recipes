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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.social.ApiException;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.document.SpaceDocument;

/**
 *
 * A {{Runnable}} implementation that connects to Twitter, retrieves the public timeline tweets, converts them to standard Tweet
 * SpaceDocument format, and writes them to a remote space.
 *
 */
public class TwitterHomeTimelineFeederTask implements Runnable {

	private static final Logger log = Logger.getLogger(TwitterHomeTimelineFeederTask.class.getSimpleName());
	
	@Resource
	private GigaSpace gigaSpace;
	private final Set<Long> previousTimeLineTweets = new HashSet<Long>();	
	
	@Value("${twitter.screenName}")
	private String screenName;
	
    @Resource
    private TwitterTemplate twitterTemplate;

	@Override
	public void run() {
    	List<Tweet> userTimeline;
    	try {
    		log.info("Getting latest tweets from public timeline and feeding them into processing grid");
    		// Return all the tweets from the Twitter API
    		userTimeline = twitterTemplate.timelineOperations().getUserTimeline(screenName);
    	}
        catch(ApiException e){
        	log.log(Level.SEVERE, "Error getting tweets from public timeline from twitter", e);
        	return;
        }
    	try {
    		//according to the API we may get duplicate tweets if invoked with frequency of lower than 60 seconds.
    		//We will filter tweets which are duplicates
    		for (Tweet publicTweet : userTimeline) {
    			if (previousTimeLineTweets.contains(publicTweet.getId())){
    				continue;
    			}
    			logTweet(publicTweet);
    			gigaSpace.write(buildTweet(publicTweet));
    		}
    	} catch (DataAccessException e) {
    		log.log(Level.SEVERE, "error feeding tweets",e);
    	}
    	finally {
    		previousTimeLineTweets.clear();
    		for (Tweet publicTweet : userTimeline) {
    			previousTimeLineTweets.add(publicTweet.getId());
    		}
    	}
    }

	public SpaceDocument buildTweet(Tweet tweet) {
		return new SpaceDocument("Tweet", new DocumentProperties() 
		.setProperty("Id", tweet.getId()) 
		.setProperty("Text", tweet.getText()) 
		.setProperty("CreatedAt", tweet.getCreatedAt()) 
		.setProperty("FromUserId", tweet.getFromUserId()) 
		.setProperty("ToUserId", tweet.getToUserId()) 
		.setProperty("Processed", Boolean.FALSE)
		.setProperty("Archived", 0));
	}

	private void logTweet(Tweet tweet) {
		log.fine(String.format("Tweet id=%d\tfromUser=%s\ttext=%s \n", tweet.getId(), tweet.getFromUser(), tweet.getText()));
	}

}
