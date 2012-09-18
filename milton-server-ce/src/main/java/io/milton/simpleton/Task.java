/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	
	long enqueueTime;
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
			httpManager.process(miltonRequest, miltonResponse);
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
