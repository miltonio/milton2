/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
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
