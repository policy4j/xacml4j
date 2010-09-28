package com.artagon.xacml.v3;

/**
 * A well known XACML subject attributes
 * 
 * @author Giedrius Trumpickas
 *
 */
public enum SubjectAttributes 
{
	/**
	 * This identifier indicates the name of the subject.
	 */
	SUBJECT_ID("urn:oasis:names:tc:xacml:1.0:subject:subject-id"),
	
	/**
	 * This identifier indicates the security domain of the subject.  
	 * It identifies the administrator and policy that manages the 
	 * name-space in which the subject id is administered.
	 */
	SUBJECT_ID_QUALIFIER("urn:oasis:names:tc:xacml:1.0:subject:subject-id-qualifier"),
	
	/**
	 * This identifier indicates a public key used to confirm the subject’s identity.
	 */
	KEY_INFO("urn:oasis:names:tc:xacml:1.0:subject:key-info"),
	
	/**
	 * This identifier indicates the time at which the subject was authenticated.
	 */
	AUTHN_TIME("urn:oasis:names:tc:xacml:1.0:subject:authentication-time"),
	
	/**
	 * This identifier indicates the method used to authenticate the subject.
	 */
	AUTHN_METHOD("urn:oasis:names:tc:xacml:1.0:subject:authentication-method"),
	
	/**
	 * This identifier indicates the time at which the subject initiated the 
	 * access request, according to the PEP.
	 */
	REQUEST_TIME("urn:oasis:names:tc:xacml:1.0:subject:request-time"),
	
	/**
	 * This identifier indicates the time at which the subject’s 
	 * current session began, according to the PEP.
	 */
	SESSION_START_TIME("urn:oasis:names:tc:xacml:1.0:subject:session-start-time"),
	
	/**
	 * The following identifiers indicate the location where authentication 
	 * credentials were activated. This identifier indicates that 
	 * the location is expressed as an IP address.
	 */
	AUTHN_LOCALITY_IP_ADDRESS("urn:oasis:names:tc:xacml:1.0:subject:authn-locality:ip-address"),
	
	/**
	 * This identifier indicates that the location is expressed as a DNS name.
	 */
	AUTHN_LOCALITY_DNS_NAME("urn:oasis:names:tc:xacml:1.0:subject:authn-locality:dns-name");
	
		
	private String id;
	
	private SubjectAttributes(String id){
		this.id = id;
	}
	
	@Override
	public String toString(){
		return id;
	}
}
