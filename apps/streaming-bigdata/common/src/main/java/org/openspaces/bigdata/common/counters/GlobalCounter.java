package org.openspaces.bigdata.common.counters;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

public class GlobalCounter {

	private String token;

    private Integer count;

    @SpaceId(autoGenerate = false)
    @SpaceRouting
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/*
	 * this hold the count apearance of the token
	 */
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	
	public GlobalCounter(String token, int counter) {

		this.token = token;
		this.count = counter;
		
	}
	public GlobalCounter(){}
	
	public void incrementCountBy(int incrementBy) {
		this.count+=incrementBy;
	}

	
	
	
}
