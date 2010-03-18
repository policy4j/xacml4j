package com.artagon.xacml.v3.policy.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.DoubleType;
import com.artagon.xacml.v3.policy.type.StringType;

public class ParamSingleTypeSpecTest extends XacmlPolicyTestCase
{
	private DoubleType t1;
	private StringType t2;
	private BagOfAttributeValuesType<?> b1;
	
	@Before
	public void init(){
		this.t1 = DataTypes.DOUBLE.getType();
		this.t2 = DataTypes.STRING.getType();
		this.b1 = t1.bagOf();
	}
	
	@Test
	public void testValidateWithAttributeType() throws Exception
	{
		ParamSpec spec = new ParamValueTypeSpec(t1);
		List<Expression> good = Collections.<Expression>singletonList(t1.create(new Double(0.1)));
		List<Expression> bad = Collections.<Expression>singletonList(t2.create("AAAA"));
		assertTrue(spec.validate(good.listIterator()));		
		assertFalse(spec.validate(bad.listIterator()));
	}
	
	@Test
	public void testValidateWithBagType() throws Exception
	{		
		ParamSpec spec = new ParamValueTypeSpec(t1);
		AttributeValue v = t1.create(new Double(0.1));
		BagOfAttributeValues<?> bag = b1.create(Collections.<AttributeValue>singletonList(v));
		List<Expression> good = Collections.<Expression>singletonList(bag);
		List<Expression> bad = Collections.<Expression>singletonList(t2.create("AAAA"));
		assertFalse(spec.validate(good.listIterator()));		
		assertFalse(spec.validate(bad.listIterator()));
	}

}
