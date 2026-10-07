package org.xacml4j.v30.policy.function.impl;

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

import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.*;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;


@XacmlFunctionProvider(description="XACML special match functions")
public final class SpecialMatchFunctions
{
	/** Private constructor for utility class */
	private SpecialMatchFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal rfc822NameMatch(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal pattern,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name") RFC822Name rfc822Name)
	{
		 return BooleanVal.of(rfc822Name.matches(pattern.get()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal x500NameMatch(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name b)
	{
		List<String> first = canonicalRdns(a);
		List<String> second = canonicalRdns(b);
		int offset = second.size() - first.size();
		if(offset < 0){
			return XacmlTypes.BOOLEAN.ofAny(false);
		}
		// The first RDN of a name is the most specific one, so a terminal
		// sequence of the second name is the end of its list.
		return XacmlTypes.BOOLEAN.ofAny(
				first.equals(second.subList(offset, second.size())));
	}

	/**
	 * Splits the canonical form of a name into its RDNs. x500Name-equal
	 * compares the canonical forms of two names, and the canonical form of a
	 * name is the list of the canonical forms of its RDNs joined by commas.
	 * Two RDNs are therefore equal under x500Name-equal exactly when their
	 * parts of the canonical form are equal: a multi-valued RDN stays one
	 * part, and a value keeps the encoding that the canonical form keeps.
	 */
	private static List<String> canonicalRdns(X500Name name)
	{
		String canonical = name.get().getName(X500Principal.CANONICAL);
		List<String> rdns = new ArrayList<>();
		if(canonical.isEmpty()){
			return rdns;
		}
		StringBuilder rdn = new StringBuilder();
		boolean escaped = false;
		for(int i = 0; i < canonical.length(); i++){
			char c = canonical.charAt(i);
			if(!escaped && c == ','){
				rdns.add(rdn.toString());
				rdn.setLength(0);
				continue;
			}
			escaped = !escaped && c == '\\';
			rdn.append(c);
		}
		rdns.add(rdn.toString());
		return rdns;
	}
}
