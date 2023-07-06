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
		 return BooleanVal.of(RegExpFunctions.matches(pattern.get(), rfc822Name.getFqName()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal x500NameMatch(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name b)
	{
		 java.lang.String n0 = a.get().getName(X500Principal.CANONICAL);
		 java.lang.String n1 = b.get().getName(X500Principal.CANONICAL);
		 return XacmlTypes.BOOLEAN.ofAny(n1.endsWith(n0));
	}
}
