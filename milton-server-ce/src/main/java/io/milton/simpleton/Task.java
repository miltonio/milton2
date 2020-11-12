/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.simpleton;

import io.milton.http.HttpManager;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class Task implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(Task.class);
	
	final long enqueueTime;
	long startTime;
	final HttpManager httpManager;
	final Response response;
	final Request request;
	Thread thisThread;
	private SimpleMiltonRequest miltonRequest;
	private SimpleMiltonResponse miltonResponse;

	public Task(HttpManager httpManager, Request request, Response response) {
		this.enqueueTime = System.currentTimeMillis();
		this.httpManager = httpManager;
		this.response = response;
		this.request = request;
	}

	@Override
	public void run() {
		thisThread = Thread.currentThread();
		startTime = System.currentTimeMillis();
		try {
			httpManager.process(getMiltonRequest(), getMiltonResponse());
			//response.commit();
			//response.getOutputStream().flush();
			miltonResponse.close();
		} catch (Exception e) {
			log.error("exception processing request: " + request.getTarget(), e);
			try {
				respondFinalError(this);
			} catch (Exception e2) {
				log.error("exception was thrown in processing, and again an exception was thrown generating error content", e2);
			}
		}
	}

	private void respondFinalError(Task t) {
		try {
			log.warn("setting error status becaue request could not be processed");
			t.response.setCode(500);
			//            t.response.commit();
			t.response.close();
		} catch (Exception e) {
			log.error("error setting last chance error status", e);
		}
	}

	public Request getRequest() {
		return request;
	}

	public Response getResponse() {
		return response;
	}

	@Override
	public String toString() {
		return request.getMethod() + " " + request.getAddress().toString();
	}

	public SimpleMiltonRequest getMiltonRequest() {
		if (miltonRequest == null) {
			miltonRequest = new SimpleMiltonRequest(getRequest());
		}
		return miltonRequest;
	}

	public SimpleMiltonResponse getMiltonResponse() {
		if (miltonResponse == null) {
			miltonResponse = new SimpleMiltonResponse(getResponse());
		}
		return miltonResponse;
	}
    
}
