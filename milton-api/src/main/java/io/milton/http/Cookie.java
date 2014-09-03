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

package io.milton.http;

/**
 *
 * @author brad
 */
public interface Cookie {


   /**
    * This returns the version for this cookie. The version is
    * not optional and so will always return the version this
    * cookie uses. If no version number is specified this will
    * return a version of 1, to comply with RFC 2109.
    *
    * @return the version value from this cookie instance
    */
   int getVersion();

   /**
    * This enables the version of the <code>Cookie</code> to be
    * set. By default the version of the <code>Cookie</code> is
    * set to 1. It is not advisable to set the version higher
    * than 1, unless it is known that the client will accept it.
    * <p>
    * Some old browsers can only handle cookie version 0. This
    * can be used to comply with the original Netscape cookie
    * specification. Version 1 complies with RFC 2109.
    *
    * @param version this is the version number for the cookie
    */
   void setVersion(int version);

   /**
    * This returns the name for this cookie. The name and value
    * attributes of a cookie define what the <code>Cookie</code>
    * is for, these values will always be present. These are
    * mandatory for both the Cookie and Set-Cookie headers.
    * <p>
    * Because the cookie may be stored by name, the cookie name
    * cannot be modified after the creation of the cookie object.
    *
    * @return the name from this cookie instance object
    */
   String getName();

   /**
    * This returns the value for this cookie. The name and value
    * attributes of a cookie define what the <code>Cookie</code>
    * is for, these values will always be present. These are
    * mandatory for both the Cookie and Set-Cookie headers.
    *
    * @return the value from this cookie instance object
    */
   String getValue();

   /**
    * This enables the value of the cookie to be changed. This
    * can be set to any value the server wishes to send. Cookie
    * values can contain space characters as they are transmitted
    * in quotes. For example a value of <code>some value</code>
    * is perfectly legal. However for maximum compatibility
    * across the different plaforms such as PHP, JavaScript and
    * others, quotations should be avoided. If quotations are
    * required they must be added to the string. For example a
    * quoted value could be created as <code>"some value"</code>.
    *
    * @param value this is the new value of this cookie object
    */
   void setValue(String value);

   /**
    * This determines whether the cookie is secure. The cookie
    * is secure if it has the "secure" token set, as defined
    * by RFC 2109. If this token is set then the cookie is only
    * sent over secure channels such as SSL and TLS and ensures
    * that a third party cannot intercept and spoof the cookie.
    *
    * @return this returns true if the "secure" token is set
    */
   boolean getSecure();

   /**
    * This is used to determine if the client browser should send
    * this cookie over a secure protocol. If this is true then
    * the client browser should only send the cookie over secure
    * channels such as SSL and TLS. This ensures that the value
    * of the cookie cannot be intercepted by a third party.
    *
    * @param secure if true then the cookie should be protected
    */
   void setSecure(boolean secure);

   /**
    * This returns the number of seconds a cookie lives for. This
    * determines how long the cookie will live on the client side.
    * If the expiry is less than zero the cookie lifetime is the
    * duration of the client browser session, if it is zero then
    * the cookie will be deleted from the client browser.
    *
    * @return returns the duration in seconds the cookie lives
    */
   int getExpiry();

   /**
    * This allows a lifetime to be specified for the cookie. This
    * will make use of the "max-age" token specified by RFC 2109
    * the specifies the number of seconds a browser should keep
    * a cookie for. This is useful if the cookie is to be kept
    * beyond the lifetime of the client session. If the valie of
    * this is zero then this will remove the client cookie, if
    * it is less than zero then the "max-age" field is ignored.
    *
    * @param expiry the duration in seconds the cookie lives
    */
   void setExpiry(int expiry);

   /**
    * This returns the path for this cookie. The path is in both
    * the Cookie and Set-Cookie headers and so may return null
    * if there is no domain value. If the <code>toString</code>
    * or <code>toClientString</code> is invoked the path will
    * not be present if the path attribute is null.
    *
    * @return this returns the path value from this cookie
    */
   String getPath();

   /**
    * This is used to set the cookie path for this cookie. This
    * is set so that the cookie can specify the directories that
    * the cookie is sent with. For example if the path attribute
    * is set to <code>/pub/bin</code>, then requests for the
    * resource <code>http://hostname:port/pub/bin/README</code>
    * will be issued with this cookie. The cookie is issued for
    * all resources in the path and all subdirectories.
    *
    * @param path this is the path value for this cookie object
    */
   void setPath(String path);

   /**
    * This returns the domain for this cookie. The domain is in
    * both the Cookie and Set-Cookie headers and so may return
    * null if there is no domain value. If either the
    * <code>toString</code> or <code>toClientString</code> is
    * invoked the domain will not be present if this is null.
    *
    * @return this returns the domain value from this cookie
    */
   String getDomain();

   /**
    * This enables the domain for this <code>Cookie</code> to be
    * set. The form of the domain is specified by RFC 2109. The
    * value can begin with a dot, like <code>.host.com</code>.
    * This means that the cookie is visible within a specific
    * DNS zone like <code>www.host.com</code>. By default this
    * value is null which means it is sent back to its origin.
    *
    * @param domain this is the domain value for this cookie
    */
   void setDomain(String domain);

   boolean isHttpOnly();
   
   void setHttpOnly(boolean  b);
}
