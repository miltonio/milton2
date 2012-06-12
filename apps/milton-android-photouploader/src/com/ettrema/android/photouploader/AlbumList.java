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

/**
 * This file is part of Picasa Photo Uploader.
 *
 * Picasa Photo Uploader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Picasa Photo Uploader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Picasa Photo Uploader. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ettrema.android.photouploader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Class to get list of Picasa albums for a user
 *
 * @author Jan Peter Hooiveld
 */
public class AlbumList
{
  /**
   * Google authentication string
   */
  private String auth;

  /**
   * Google user e-mail
   */
  private String email;

  /**
   * Picasa album id's
   */
  private CharSequence[] albumIds;

  /**
   * Picasa album names
   */
  private CharSequence[] albumNames;

  /**
   * Constructor
   * 
   * @param auth Google authentication string
   * @param email Google user e-mail
   */
  public AlbumList(String auth, String email)
  {
    this.auth  = auth;
    this.email = email;
  }

  /**
   * Fetches list of albums that user has in Picasa
   * 
   * @return Has albums or not
   */
  public boolean fetchAlbumList()
  {
    try {
      // set http items
      String albumUrl       = "http://picasaweb.google.com/data/feed/api/user/"+email;
      HttpURLConnection con = (HttpURLConnection) new URL(albumUrl).openConnection();

      // request method, timeout and headers
      con.setRequestMethod("GET") ;
      con.setReadTimeout(15000);
      con.setRequestProperty("Authorization", "GoogleLogin auth="+auth);
      con.setRequestProperty("GData-Version", "2");

      // set timeout and that we will process output
      con.setReadTimeout(15000);
      con.setDoOutput(true);

      // connnect to url
      con.connect();

      // read output returned for url
      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      StringBuilder builder = new StringBuilder();
      String line;

      // accumulate response body
      while ((line = reader.readLine()) != null) {
        builder.append(line + "\n");
      }

      // parse response body
      parseAlbums(builder.toString());
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  /**
   * Parse xml to get Picasa album names and id's
   * 
   * @param xml The xml response that was received
   */
  private void parseAlbums(String xml)
  {
    try {
      // set xml vars
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db         = dbf.newDocumentBuilder();
      InputSource is             = new InputSource();

      // import the xml
      is.setCharacterStream(new StringReader(xml));

      // parse xml,get all album entries and create arrays to hold further data
      Document doc     = db.parse(is);
      NodeList entries = doc.getElementsByTagName("entry");
      albumIds         = new CharSequence[entries.getLength()];
      albumNames       = new CharSequence[entries.getLength()];

      // loop all album entries and store album id's and names to arrays
      for (int i = 0; i < entries.getLength(); i++) {
        Element element = (Element) entries.item(i);
        albumIds[i]     = (CharSequence)getCharacterDataFromElement((Element)element.getElementsByTagName("gphoto:id").item(0));
        albumNames[i]   = (CharSequence)getCharacterDataFromElement((Element)element.getElementsByTagName("title").item(0));
      }
    } catch (Exception e) {
      return;
    }
  }

  /**
   * Helper function to get data from an xml element
   *
   * @param element Xml element to get data from
   * @return Data from the xml element
   */
  private String getCharacterDataFromElement(Element element)
  {
    Node child = element.getFirstChild();
    
    if (child instanceof CharacterData) {
       CharacterData cd = (CharacterData) child;
       return cd.getData();
    }
    
    return "";
  }

  /**
   * Get Picasa album id's that were retrieved
   * 
   * @return Picasa album id's
   */
  public CharSequence[] getAlbumIds()
  {
    return albumIds;
  }

  /**
   * Get Picasa album names that were retrieved
   *
   * @return Picasa album names
   */
  public CharSequence[] getAlbumNames()
  {
    return albumNames;
  }
}
