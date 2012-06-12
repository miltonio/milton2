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

package info.ineighborhood.cardme.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;

/**
 * Copyright (c) 2004, Neighborhood Technologies
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Neighborhood Technologies nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 * @author George El-Haddad
 * <br/>
 * Feb 5, 2010
 * 
 * <p>General Java related utility class to make some things easier.</p>
 */
public final class Util {
	
	private Util() {
		
	}

	/**
	 * <p>Given an array of a particular type, it will return it
	 * as a List of the same type.</p>
	 * 
	 * @param &lt;E&gt;
	 * @param array
	 * @return &lt;E&gt; {@link List}&lt;E&gt;
	 */
	public static <E> List<E> asList(E[] array)
	{
		List<E> list = new ArrayList<E>(array.length);
		for(int i=0; i < array.length; i++) {
			list.add(array[i]);
		}
		
		return list;
	}
	
	/**
	 * <p>Given an indeterminate array of strings; a hashcode is generated and returned.</p>
	 * 
	 * @param args
	 * @return int
	 */
	public static int generateHashCode(String ... args)
	{
		int length = 0;
		char[] cArray = null;
		if(args.length == 1) {
			length = args[0].length();
			cArray = args[0].toCharArray();
		}
		else {
			for(int i = 0; i < args.length; i++) {
				length += args[i].length();
			}
			
			cArray = new char[length];
			int incrementer = 0;
			for(int i = 0; i < args.length; i++) {
				String str = args[i];
				for(int j = 0; j < str.length(); j++) {
					cArray[incrementer] = str.charAt(j);
					++incrementer;
				}
			}
		}
		
		int h = 0;
		for (int i = 0; i < cArray.length; i++) {
			h = 31*h + cArray[i];
		}
		
		return h;
	}
	
	/**
	 * <p>Given a File object it will read and return the entire file
	 * as an array of bytes.</p>
	 *
	 * @param file
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] getFileAsBytes(File file) throws IOException
	{
		byte[] bytes = new byte[(int)file.length()];
		BufferedReader br = new BufferedReader(new FileReader(file));
		int i = 0;
		int b = -1;
		while((b = br.read()) != -1) {
			bytes[i] = (byte)b;
			i++;
		}
		
		br.close();
		return bytes;
	}
	
	/**
	 * <p>Centers an given Window to the user's screen.</p>
	 * 
	 * @param w
	 */
	public static void center(Window w) {
		int screenWidth  = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

		int windowWidth = w.getWidth();
		int windowHeight = w.getHeight();

		if (windowHeight > screenHeight) {
			return;
		}

		if (windowWidth > screenWidth) {
			return;
		}

		int x = (screenWidth - windowWidth) / 2;
		int y = (screenHeight - windowHeight) / 2;

		w.setLocation(x, y);
	}

	/**
	 * <p>Sets the state of a frame to the maximized state.</p>
	 * 
	 * @param frame
	 */
	public static void maximizeFrame(Frame frame) {
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
	}

	/**
	 * <p>Sets the state of a frame to the iconified state (ie, minimized)</p>
	 * 
	 * @param frame
	 */
	public static void iconifyFrame(Frame frame) {
		frame.setExtendedState(Frame.ICONIFIED);
	}

	/**
	 * <p>Restores the frame to its normal state.</p>
	 * 
	 * @param frame
	 */
	public static void restoreFrame(Frame frame) {
		frame.setExtendedState(Frame.NORMAL);
	}

	/**
	 * <p>Returns the current height of the screen.</p>
	 * 
	 * @return int
	 */
	public static int getScreenHeight()
	{
		return Toolkit.getDefaultToolkit().getScreenSize().height;
	}

	/**
	 * <p>Returns the current width of the screen.</p>
	 * 
	 * @return int
	 */
	public static int getScreenWidth()
	{
		return Toolkit.getDefaultToolkit().getScreenSize().width;
	}
	
	/**
	 * <p>Displays a splash window for a specific amount of time with a set of rendering parameters
	 * for the message, font type, window size and frame owner.</p>
	 * 
	 * @param message
	 * 	- the text to display
	 * @param messageFont
	 * 	- the font of text
	 * @param duration
	 * 	- how long to show the window in milliseconds
	 * @param windowSize
	 * 	- the size of the window
	 * @param frameOwner
	 * 	- the parent window or owner, null if none
	 */
	public static void showSplashWindow(String message, Font messageFont, int duration, Dimension windowSize, Window frameOwner) {
		JLabel saved = new JLabel(message);
		saved.setHorizontalAlignment(JLabel.CENTER);
		saved.setOpaque(true);
		saved.setFont(messageFont);

		final JWindow window = new JWindow(frameOwner);
		window.add(saved, BorderLayout.CENTER);
		window.setSize(windowSize);
		saved.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		center(window);
		window.setVisible(true);

		Timer timer = new Timer(duration, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.setVisible(false);
				window.dispose();
			}
		});

		timer.setRepeats(false);
		timer.start();
	}
}
