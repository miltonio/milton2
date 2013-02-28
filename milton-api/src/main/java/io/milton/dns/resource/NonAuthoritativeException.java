package io.milton.dns.resource;

public class NonAuthoritativeException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3054727106748339165L;

	private String domainName;
	
	public NonAuthoritativeException(String domainName) {
		this(domainName, "No authoritative data for " + domainName);
	}
	public NonAuthoritativeException(String domainName, String msg) {
		super(msg);
		this.domainName = domainName;
	}
	public String getDomainName() {
		return domainName;
	}
}
