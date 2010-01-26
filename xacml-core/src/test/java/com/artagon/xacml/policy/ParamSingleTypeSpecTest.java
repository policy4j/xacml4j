package com.artagon.xacml.policy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.function.ParamAnyAttributeSpec;
import com.artagon.xacml.policy.function.ParamAnyBagSpec;
import com.artagon.xacml.policy.function.ParamValueTypeSpec;
import com.artagon.xacml.policy.type.DoubleType;
import com.artagon.xacml.policy.type.StringType;
import com.artagon.xacml.policy.type.XacmlDataType;

public class ParamSingleTypeSpecTest extends XacmlPolicyTestCase
{
	private DoubleType t1;
	private StringType t2;
	private BagOfAttributesType<?> b1;
	
	@Before
	public void init(){
		this.t1 = XacmlDataType.BOOLEAN.getType();
		this.t2 = XacmlDataType.STRING.getType();
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
		Attribute v = t1.create(new Double(0.1));
		BagOfAttributes<?> bag = b1.createFromAttributes(Collections.<Attribute>singletonList(v));
		List<Expression> good = Collections.<Expression>singletonList(bag);
		List<Expression> bad = Collections.<Expression>singletonList(t2.create("AAAA"));
		assertFalse(spec.validate(good.listIterator()));		
		assertFalse(spec.validate(bad.listIterator()));
	}
	
	@Test
	public void testWithAnyAttributeDataTypeSpec() throws Exception
	{
		ParamSpec spec = new ParamAnyAttributeSpec();
		assertTrue(spec.isValidParamType(t1));
		assertTrue(spec.isValidParamType(t2));
		assertFalse(spec.isValidParamType(b1));
	}
	
	@Test
	public void testWithAnyBagOfAttributesTypeSpec() throws Exception
	{
		ParamSpec spec = new ParamAnyBagSpec();
		assertFalse(spec.isValidParamType(t1));
		assertFalse(spec.isValidParamType(t2));
		assertTrue(spec.isValidParamType(t1.bagOf()));
		assertTrue(spec.isValidParamType(t2.bagOf()));
	}
}
