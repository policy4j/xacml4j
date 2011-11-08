package com.artagon.xacml.v30.spi.function;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.FunctionParamSpec;
import com.artagon.xacml.v30.types.DoubleType;
import com.artagon.xacml.v30.types.StringType;

public class ParamSingleTypeSpecTest
{
	private DoubleType t1;
	private StringType t2;
	private BagOfAttributesExpType b1;
	
	@Before
	public void init(){
		this.t1 = DoubleType.DOUBLE;
		this.t2 = StringType.STRING;
		this.b1 = t1.bagType();
	}
	
	@Test
	public void testValidateWithAttributeType() throws Exception
	{
		FunctionParamSpec spec = new FunctionParamValueTypeSpec(t1);
		List<Expression> good = Collections.<Expression>singletonList(t1.create(new Double(0.1)));
		List<Expression> bad = Collections.<Expression>singletonList(t2.create("AAAA"));
		assertTrue(spec.validate(good.listIterator()));		
		assertFalse(spec.validate(bad.listIterator()));
	}
	
	@Test
	public void testValidateWithBagType() throws Exception
	{		
		FunctionParamSpec spec = new FunctionParamValueTypeSpec(t1);
		AttributeExp v = t1.create(new Double(0.1));
		BagOfAttributesExp bag = b1.create(Collections.<AttributeExp>singletonList(v));
		List<Expression> good = Collections.<Expression>singletonList(bag);
		List<Expression> bad = Collections.<Expression>singletonList(t2.create("AAAA"));
		assertFalse(spec.validate(good.listIterator()));		
		assertFalse(spec.validate(bad.listIterator()));
	}

}
