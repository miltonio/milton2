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

    Object getUserTag(String firstComp);
}
