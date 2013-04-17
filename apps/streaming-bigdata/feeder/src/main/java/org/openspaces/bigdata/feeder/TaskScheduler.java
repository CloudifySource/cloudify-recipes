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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A bean that schedules Runnable tasks periodically
 * 
 * @author Dotan Horovits
 */
public class TaskScheduler {
	
	private static final Logger log = Logger.getLogger(TaskScheduler.class.getSimpleName());

    @Resource
    private Runnable task;
    private int delayInMs = 1000;

    public void setDelayInMs(int delayInMs) {
    	log.info("task scheduler initialized with delay miliseconds of " + delayInMs);
        this.delayInMs = delayInMs;
    }

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        new TaskScheduler().execute();
    }

    
    @PostConstruct
    public void execute() {
    	log.info("Scheduling twitter feeder fetch public tweets interval:" + delayInMs + " milliseconds");
        executorService.scheduleWithFixedDelay(task, 0, delayInMs, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
    }
}
