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

import io.milton.http.Cookie;



/**
 *
 * @author brad
 */
public class SimpletonCookie implements Cookie{

    private final org.simpleframework.http.Cookie c;

    public SimpletonCookie( org.simpleframework.http.Cookie wrapped ) {
        this.c = wrapped;
    }

    public org.simpleframework.http.Cookie getWrapped() {
        return c;
    }

	@Override
    public int getVersion() {
        return c.getVersion();
    }

	@Override
    public void setVersion( int version ) {
        c.setVersion( version );
    }

	@Override
    public String getName() {
        return c.getName();
    }

	@Override
    public String getValue() {
        return c.getValue();
    }

	@Override
    public void setValue( String value ) {
        c.setValue( value );
    }

	@Override
    public boolean getSecure() {
        return c.getSecure();
    }

	@Override
    public void setSecure( boolean secure ) {
        c.setSecure( secure );
    }

	@Override
    public int getExpiry() {
        return c.getExpiry();
    }

	@Override
    public void setExpiry( int expiry ) {
        c.setExpiry( expiry );
    }

	@Override
    public String getPath() {
        return c.getPath();
    }

	@Override
    public void setPath( String path ) {
        c.setPath( path );
    }

	@Override
    public String getDomain() {
        return c.getDomain();
    }

	@Override
    public void setDomain( String domain ) {
        c.setDomain( domain );
    }

	@Override
	public boolean isHttpOnly() {
		return false;
	}

	@Override
	public void setHttpOnly(boolean b) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
