package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.sdk.XacmlFuncParam;
import com.artagon.xacml.v3.sdk.XacmlFuncReturnType;
import com.artagon.xacml.v3.sdk.XacmlFuncSpec;
import com.artagon.xacml.v3.sdk.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanValue;
import com.artagon.xacml.v3.types.DoubleValue;
import com.artagon.xacml.v3.types.IntegerValue;

@XacmlFunctionProvider(description="XACML numeric comparision functions")
public class NumericComparisionFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greatherThanInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue() > b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue() < b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greatherThanOrEqualInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().equals(b.getValue()) || a.getValue() > b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanOrEqualBoolean(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().equals(b.getValue()) || a.getValue() < b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greatherThanDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue() > b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue() < b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greatherThanOrEqualDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().equals(b.getValue()) || a.getValue() > b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanOrEqualDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().equals(b.getValue()) || a.getValue() < b.getValue());
	}
}
