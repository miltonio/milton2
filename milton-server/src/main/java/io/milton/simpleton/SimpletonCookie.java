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

    public int getVersion() {
        return c.getVersion();
    }

    public void setVersion( int version ) {
        c.setVersion( version );
    }

    public String getName() {
        return c.getName();
    }

    public String getValue() {
        return c.getValue();
    }

    public void setValue( String value ) {
        c.setValue( value );
    }

    public boolean getSecure() {
        return c.getSecure();
    }

    public void setSecure( boolean secure ) {
        c.setSecure( secure );
    }

    public int getExpiry() {
        return c.getExpiry();
    }

    public void setExpiry( int expiry ) {
        c.setExpiry( expiry );
    }

    public String getPath() {
        return c.getPath();
    }

    public void setPath( String path ) {
        c.setPath( path );
    }

    public String getDomain() {
        return c.getDomain();
    }

    public void setDomain( String domain ) {
        c.setDomain( domain );
    }

}
