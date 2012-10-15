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

package org.openspaces.bigdata.processor.events;

import java.util.Map;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

/**
 * This class represents a tokenized version of a tweet.
 * 
 * @author Dotan Horovits
 */
@SpaceClass
public class TokenizedTweet {
	
    private Long id;
    private Map<String, Integer> tokenMap;
    private boolean filtered;

    public TokenizedTweet(Long id, Map<String, Integer> tokenMap) {
        this.id = id;
        this.tokenMap = tokenMap;
        this.filtered = false;
    }

    public TokenizedTweet() {
    }

    public boolean getFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    @SpaceId(autoGenerate = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Integer> getTokenMap() {
        return tokenMap;
    }

    public void setTokenMap(Map<String, Integer> tokenMap) {
        this.tokenMap = tokenMap;
    }

    @Override
    public String toString() {
        return "TokenizedTweet [id=" + id + ", tokenMap=" + tokenMap + "]";
    }
}
