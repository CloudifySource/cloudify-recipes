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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventExceptionHandler;
import org.openspaces.events.EventTemplateProvider;
import org.openspaces.events.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.TransactionStatus;

import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.client.SQLQuery;

/**
 * This tweet archive filter is injected into the archiveContainer (See pu.xml file)
 * It decides which tweets to persist, and handles archive operation retries.
 * 
 * @author Dotan Horovits
 * @author Itai Frenkel
 */
public class TweetArchiveFilter implements EventExceptionHandler<SpaceDocument[]>, EventTemplateProvider {

	private static final Logger log = Logger.getLogger(TweetArchiveFilter.class.getName());

	@Value("${archiver.retries}")
	int maxRetries = 2;
	
	@Override
	public SQLQuery<SpaceDocument> getTemplate() {
		return new SQLQuery<SpaceDocument>("Tweet", "Processed = ?", true);
	}

	@Override
	public void onSuccess(SpaceDocument[] tweets, GigaSpace gigaSpace,
			TransactionStatus txStatus, Object source) throws RuntimeException {

		for (SpaceDocument tweet: tweets) {
			log.log(Level.INFO, "Archived tweet " + tweet.getProperty("Id"));
		}
	}

	@Override
	public void onException(ListenerExecutionFailedException exception,
			SpaceDocument[] tweets, GigaSpace gigaSpace, TransactionStatus txStatus,
			Object source) throws RuntimeException {
		
		List<SpaceDocument> retryTweets = new ArrayList<SpaceDocument>();
		
		for (SpaceDocument tweet : tweets) {
			Integer tries = tweet.getProperty("Archived"); 
			if (tries == null) { 
				tries = 0; 
			}
			if (tries < maxRetries) {
				tweet.setProperty("Archived", tries+1);
				retryTweets.add(tweet);
			}
			else {
				log.log(Level.WARNING, "Error archiving tweet " + tweet.getProperty("Id"), exception.getCause());
			}
		}
		
		if (!retryTweets.isEmpty()) {
			//retry
			SpaceDocument[] spaceDocuments = retryTweets.toArray(new SpaceDocument[retryTweets.size()]);
			gigaSpace.writeMultiple(spaceDocuments);
		}
	}
}
