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

package org.openspaces.bigdata.common.counters;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 *  Holds the global count appearance of the token
 */
public class GlobalCounter {

	private String token;
	private Integer counter;

	public GlobalCounter(){}

	public GlobalCounter(String token, Integer counter) {
		this.token = token;
		this.counter = counter;
	}

	@SpaceId(autoGenerate = false)
	@SpaceRouting
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Integer getCounter() {
		return counter;
	}

	public void setCounter(Integer count) {
		this.counter = count;
	}

	public void incrementCountBy(int incrementBy) {
		this.counter += incrementBy;
	}
}
