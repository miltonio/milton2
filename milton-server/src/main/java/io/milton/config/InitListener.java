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
package io.milton.config;

import io.milton.http.HttpManager;

/**
 * Listener interface to hook into the initialisatio process for HttpManagerBuilder
 *
 * @author brad
 */
public interface InitListener {
	
	/**
	 * Called just before init on HttpManagerBuilder
	 * @param b 
	 */
	void beforeInit(HttpManagerBuilder b);
	
	/**
	 * Called just after init, and before building the HttpManager instance
	 * 
	 * @param b 
	 */
	void afterInit(HttpManagerBuilder b);
	
	/**
	 * Called immediately after bulding the http manager
	 * 
	 * @param b
	 * @param m 
	 */
	void afterBuild(HttpManagerBuilder b, HttpManager m);
}
