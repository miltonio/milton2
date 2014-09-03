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
