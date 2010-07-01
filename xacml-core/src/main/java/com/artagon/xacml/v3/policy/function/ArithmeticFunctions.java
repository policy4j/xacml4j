package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncParamVarArg;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;
import com.google.common.base.Preconditions;


/**
 * All of the following functions SHALL take two arguments of the specified data-type, 
 * integer, or double, and SHALL return an element of integer or double data-type, 
 * respectively. However, the “add” functions MAY take more than two arguments. 
 * Each function evaluation operating on doubles SHALL proceed as specified by their 
 * logical counterparts in IEEE 754 [IEEE754]. For all of these functions, 
 * if any argument is "Indeterminate", then the function SHALL evaluate to 
 * "Indeterminate". In the case of the divide functions, if the divisor is zero,
 *  then the function SHALL evaluate to “Indeterminate”.
 * 
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML arithmetic functions")
public class ArithmeticFunctions 
{
	/**
	 * Adds two or more integer data-type values
	 * 
	 * @param values a two or more integer data-type value
	 * @return an integer data-type value representing
	 * arithmetic sum of a given values
	 */
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-add")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue addInteger(
			@XacmlFuncParamVarArg(type=XacmlDataTypes.INTEGER, min=2)IntegerValue ...values) 
	{
		Long sum = 0L;
		for(IntegerValue v : values){
			sum += v.getValue();
		}
		return XacmlDataTypes.INTEGER.create(sum);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-multiply")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue multiplyInteger(
			@XacmlFuncParamVarArg(type=XacmlDataTypes.INTEGER, min=2)IntegerValue ...values)
	{
		Long value = 1L;
		for(IntegerValue v : values){
			value *= v.getValue();
		}
		return XacmlDataTypes.INTEGER.create(value);
	}	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-add")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleValue addDouble(
			@XacmlFuncParamVarArg(type=XacmlDataTypes.DOUBLE, min=2)DoubleValue ...values)
	{
		Double sum = 0.0;
		for(DoubleValue v : values){
			sum += v.getValue();
		}
		return XacmlDataTypes.DOUBLE.create(sum);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-multiply")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleValue multiplyDouble(
			@XacmlFuncParamVarArg(type=XacmlDataTypes.DOUBLE, min=2)DoubleValue ...values)
	{
		Double value = 1.0;
		for(DoubleValue v : values){
			value *= v.getValue();
		}
		return XacmlDataTypes.DOUBLE.create(value);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue abs(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue v)
	{
		return XacmlDataTypes.INTEGER.create(Math.abs(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-abs")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleValue abs(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue v)
	{
		return XacmlDataTypes.DOUBLE.create(Math.abs(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:floor")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleValue floor(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue v)
	{
		return XacmlDataTypes.DOUBLE.create(Math.floor(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:round")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleValue round(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue v)
	{
		return XacmlDataTypes.DOUBLE.create(Math.round(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-subtract")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleValue subtract(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue a,
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.DOUBLE.create(a.getValue() - b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-divide")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleValue divideDouble(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue a,
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return XacmlDataTypes.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-divide")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleValue divideInteger(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue a,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return XacmlDataTypes.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-mod")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue modInteger(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue a,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return XacmlDataTypes.INTEGER.create(a.getValue() % b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-subtract")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue subtract(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue a,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.INTEGER.create(a.getValue() - b.getValue());
	}
}
