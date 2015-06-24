/*
 *
 * Copyright 2014 McEvoy Software Ltd.
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
package io.milton.simpleton;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.fs.FileSystemResourceFactory;
import io.milton.http.fs.NullSecurityManager;
import java.io.File;

/**
 *
 * @author brad
 */
public class SimpletonStarter {

	public static void main(String[] args) {
		String homeFolder = "/tmp";
		int port = 8080;
		FileSystemResourceFactory resourceFactory = new FileSystemResourceFactory(new File(homeFolder),
				new NullSecurityManager(), "/");
		resourceFactory.setAllowDirectoryBrowsing(true);
		HttpManagerBuilder b = new HttpManagerBuilder();
		b.setEnableFormAuth(false);
		b.setResourceFactory(resourceFactory);
		HttpManager httpManager = b.buildHttpManager();
		SimpletonServer ss = new SimpletonServer(httpManager, b.getOuterWebdavResponseHandler(), 100, 10);
		ss.setHttpPort(port);
		ss.start();

	}
}
