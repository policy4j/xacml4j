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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Which provider methods get a direct binding (design D15 of optimize-pdp-evaluation). Review RVI3: a
 * generated lambda class is strongly linked to its defining loader, so binding a method of a hidden class
 * would keep that class from unloading; hidden classes stay on the reflective path.
 */
public class DirectBindingEligibilityTest
{
	private static DefaultFunctionInvocation create(Method m)
	{
		return DefaultFunctionInvocation.create(FunctionInvocationFactory.defaultFactory(), null, m, false, null);
	}

	@Test
	public void methodOfAHiddenClassIsNotBoundAndStillWorks() throws Exception
	{
		byte[] bytes;
		try(InputStream in = DirectBindingTemplate.class.getResourceAsStream("DirectBindingTemplate.class")){
			bytes = in.readAllBytes();
		}
		Class<?> hidden = MethodHandles.lookup().defineHiddenClass(bytes, true).lookupClass();
		Method same = hidden.getMethod("same", StringVal.class, StringVal.class);
		StringVal a = XacmlTypes.STRING.ofAny("a");

		FunctionSpec spec = mock(FunctionSpec.class);
		when(spec.getNumberOfParams()).thenReturn(2);

		DefaultFunctionInvocation invocation = create(same);
		ValueExp result = invocation.invokeDirect(spec, mock(EvaluationContext.class), a, a);

		assertThat(result).isEqualTo(XacmlTypes.BOOLEAN.ofAny(true));
	}
}
