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

package info.ineighborhood.cardme.engine;

import info.ineighborhood.cardme.io.CompatibilityMode;
import info.ineighborhood.cardme.io.VCardWriter;
import info.ineighborhood.cardme.util.StringUtil;
import info.ineighborhood.cardme.vcard.VCard;
import info.ineighborhood.cardme.vcard.VCardImpl;
import info.ineighborhood.cardme.vcard.VCardVersion;
import info.ineighborhood.cardme.vcard.errors.VCardError;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

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
 * Mar 10, 2010
 * 
 * <p>A Test class to see how to use the VCardEngine.</p>
 */
public class TestParser {

	private File[] vcardFiles = null;
	private VCardEngine vcardEngine = null;
	
	/**
	 * <p>Creates a new TestParser.</p>
	 */
	public TestParser() {
		vcardEngine = new VCardEngine();
	}
	
	/**
	 * <p>Sets the compatibility mode.</p>
	 *
	 * @param compatMode
	 */
	public void setCompatibilityMode(CompatibilityMode compatMode) {
		vcardEngine.setCompatibilityMode(compatMode);
	}
	
	/**
	 * <p>Retrieve all VCard files and then parse them.</p>
	 *
	 * @return {@link List}&lt;VCard&gt;
	 */
	public List<VCard> importVCards() {
		List<VCard> vcards = new ArrayList<VCard>();
		vcardFiles = getFiles();
		for(int i = 0; i < vcardFiles.length; i++) {
			try {
				VCard vcard = vcardEngine.parse(vcardFiles[i]);
				vcards.add(vcard);
			}
			catch(IOException ioe) {
				System.err.println("Could not read vcard file: "+vcardFiles[i].getAbsolutePath());
				ioe.printStackTrace();
			}
		}
		
		return vcards;
	}

	/**
	 * <p>Opens a file chooser dialog to select VCard files.</p>
	 * 
	 * @return {@link File}[]
	 */
	private File[] getFiles()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select VCards");
		chooser.setCurrentDirectory(new File(System.getProperties().getProperty("user.home")));
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".vcf") || f.isDirectory();
			}

			public @Override
			String getDescription() {

				return "VCard Files";
			}
		});

		int result = chooser.showOpenDialog(null);
		if(result == JFileChooser.CANCEL_OPTION) {
			return null;
		}

		try {
			File[] files = chooser.getSelectedFiles(); // get the file
			return files;
		}
		catch(Exception ex) {
			JOptionPane.showMessageDialog(null, "Warning! Could not load the file(s)!", "Warning!", JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}

	/**
	 * <p>This is the main method. Here the TestParses in created and initialized.
	 * A VCardWriter is created to write the imported vcards to the System.out so
	 * we can see if everything got imported and written correctly.</p>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		TestParser testParser = new TestParser();
		testParser.setCompatibilityMode(CompatibilityMode.RFC2426);
//		testParser.setCompatibilityMode(CompatibilityMode.I_PHONE);
//		testParser.setCompatibilityMode(CompatibilityMode.KDE_ADDRESS_BOOK);
//		testParser.setCompatibilityMode(CompatibilityMode.MAC_ADDRESS_BOOK);
//		testParser.setCompatibilityMode(CompatibilityMode.MS_OUTLOOK);
		List<VCard> vcards = testParser.importVCards();
		
		VCardWriter writer = new VCardWriter();
		writer.setOutputVersion(VCardVersion.V3_0);
//		writer.setCompatibilityMode(CompatibilityMode.MAC_ADDRESS_BOOK);
//		writer.setFoldingScheme(FoldingScheme.MAC_ADDRESS_BOOK);
		
		for(int i = 0; i < vcards.size(); i++) {
			VCardImpl vcard = (VCardImpl)vcards.get(i);
			
			if(vcard.hasErrors()) {
				System.out.println("VCard "+i+" has some errors ...");
				List<VCardError> errors = vcard.getErrors();
				for(int j = 0; j < errors.size(); j++) {
					System.out.println(errors.get(j).getErrorMessage());
					System.out.println(errors.get(j).getSeverity());
					System.out.println(StringUtil.formatException(errors.get(j).getError()));
				}
			}
			
//			//Uncomment to change the output style of the Telephone numbers
//			
//			Iterator<TelephoneFeature> iter = vcard.getTelephoneNumbers();
//			while(iter.hasNext()) {
//				iter.next().setParameterTypeStyle(ParameterTypeStyle.PARAMETER_LIST);
//			}
			
			writer.setVCard(vcard);
			String vstring = writer.buildVCardString();
			
			if(writer.hasErrors()) {
				List<VCardError> errors = vcard.getErrors();
				for(int j = 0; j < errors.size(); j++) {
					System.out.println(errors.get(j).getErrorMessage());
					System.out.println(errors.get(j).getSeverity());
					System.out.println(StringUtil.formatException(errors.get(j).getError()));
				}
			}
			
			System.out.println(vstring);
		}
		
		System.out.println("\n-- END --");
	}
}
