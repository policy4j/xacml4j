package org.xacml4j.v30.policy.function.impl;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;

/**
 * A provider class that is not public, in the package of {@link DefaultFunctionInvocation}: the
 * reflective call from {@code FunctionInvocationFactory} cannot access it, so a direct binding must
 * not either (review RVI1, design D15 of optimize-pdp-evaluation).
 */
@XacmlFunctionProvider(description = "A provider class that is not public")
final class PackagePrivateFunctions
{
	static final String EQUAL = "urn:xacml4j:test:function:package-private-equal";

	private PackagePrivateFunctions() {}

	@XacmlFuncSpec(id = EQUAL)
	@XacmlFuncReturnType(typeId = "http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equal(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringVal a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringVal b)
	{
		return BooleanVal.of(a.equals(b));
	}
}
