/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * 
 */
package io.milton.sso;

/**
 *
 * @author brad
 */
public interface SsoSessionProvider {

    public Object getUserTag(String firstComp);
}
