package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
	 * This identifier indicates a public key used to confirm the subject's identity.
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
	 * This identifier indicates the time at which the subject's
	 * current session began, according to the PEP.
	 */
	SESSION_START_TIME("urn:oasis:names:tc:xacml:1.0:subject:session-start-time"),

	/**
	 * This identifier indicates an identifier for subject's
	 * current session.
	 */
	SESSION_IDENTIFIER("urn:artagon:names:tc:xacml:1.0:subject:session-id"),

	/**
	 * The following identifiers indicate the location where authentication
	 * credentials were activated. This identifier indicates that
	 * the location is expressed as an IP address.
	 */
	AUTHN_LOCALITY_IP_ADDRESS("urn:oasis:names:tc:xacml:1.0:subject:authn-locality:ip-address"),

	/**
	 * This identifier indicates that the location is expressed as a DNS name.
	 */
	AUTHN_LOCALITY_DNS_NAME("urn:oasis:names:tc:xacml:1.0:subject:authn-locality:dns-name"),

	USER_PASSWORD("http://www.ietf.org/rfc/rfc2256.txt#userPassword");


	private String id;

	private SubjectAttributes(String id){
		this.id = id;
	}

	@Override
	public String toString(){
		return id;
	}
}
