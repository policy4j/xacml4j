package org.xacml4j.v30.policy.function;

import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;


@XacmlFunctionProvider(description="XACML numeric comparision functions")
public class NumericComparisionFunctions
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp greatherThanInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return BooleanExp.valueOf(a.getValue() > b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp lessThanInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return BooleanExp.valueOf(a.getValue() < b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp greatherThanOrEqualInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return BooleanExp.valueOf(a.getValue().equals(b.getValue()) || a.getValue() > b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp lessThanOrEqualBoolean(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return BooleanExp.valueOf(a.getValue().equals(b.getValue()) || a.getValue() < b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp greatherThanDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		return BooleanExp.valueOf(a.getValue() > b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp lessThanDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		return BooleanExp.valueOf(a.getValue() < b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp greatherThanOrEqualDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		return BooleanExp.valueOf(a.getValue().equals(b.getValue()) || a.getValue() > b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp lessThanOrEqualDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		return BooleanExp.valueOf(a.getValue().equals(b.getValue()) || a.getValue() < b.getValue());
	}
}
