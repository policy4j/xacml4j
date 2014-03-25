package org.xacml4j.v30.spi.function;

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
