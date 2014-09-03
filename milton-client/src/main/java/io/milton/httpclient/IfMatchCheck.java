package io.milton.httpclient;

/**
 * Just a wrapper for an etag. If this is present, but the etag is null it means
 *  to do a if-none-match: * (ie ensure there is no resource)
 * If etag is present it will be if-match: (etag), ie ensure the resource is the same
 * 
 * If you pass a null instead of this, then it means do no match checks
 *
 * @author brad
 */
public class IfMatchCheck {
    private final String etag;

    public IfMatchCheck(String etag) {
        this.etag = etag;
    }

    public String getEtag() {
        return etag;
    }        
}
