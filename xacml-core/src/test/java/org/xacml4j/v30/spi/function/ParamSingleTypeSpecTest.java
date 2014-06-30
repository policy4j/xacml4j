package org.xacml4j.v30.spi.function;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.FunctionParamSpec;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;


public class ParamSingleTypeSpecTest
{

	@Test
	public void testValidateWithAttributeType() throws Exception
	{
		FunctionParamSpec spec = new FunctionParamValueTypeSpec(XacmlTypes.DOUBLE);
		List<Expression> good = Collections.<Expression>singletonList(DoubleExp.valueOf(0.1));
		List<Expression> bad = Collections.<Expression>singletonList(StringExp.valueOf("AAAA"));
		assertTrue(spec.validate(good.listIterator()));
		assertFalse(spec.validate(bad.listIterator()));
	}
	
	@Test
	public void testValidateWithBagType() throws Exception
	{
		FunctionParamSpec spec = new FunctionParamValueTypeSpec(XacmlTypes.DOUBLE);
		List<Expression> good = Collections.<Expression>singletonList(DoubleExp.valueOf(0.1).toBag());
		List<Expression> bad = Collections.<Expression>singletonList(StringExp.valueOf("AAAA").toBag());
		assertFalse(spec.validate(good.listIterator()));
		assertFalse(spec.validate(bad.listIterator()));
	}

}
