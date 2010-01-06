package com.artagon.xacml.policy.function;

import org.junit.Before;

import com.artagon.xacml.policy.DataTypeFactory;
import com.artagon.xacml.policy.XacmlPolicyTestCase;
import com.artagon.xacml.policy.type.DefaultDataTypeFactory;

public class DefaultFunctionFactoryTestCase extends XacmlPolicyTestCase
{
	protected DataTypeFactory types;
	
	@Before
	public void init__(){
		this.types = new DefaultDataTypeFactory();
	}
}
