package com.artagon.xacml.v3.policy.impl.function;

import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.policy.spi.function.XacmlParamVarArg;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;
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
public class ArithmeticFunctions 
{
	/**
	 * Adds two or more integer data-type values
	 * 
	 * @param values a two or more integer data-type value
	 * @return an integer data-type value representing
	 * arithmetic sum of a given values
	 */
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-add")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue addInteger(
			@XacmlParamVarArg(type=DataTypes.INTEGER, min=2)IntegerValue ...values) 
	{
		Long sum = 0L;
		for(IntegerValue v : values){
			sum += v.getValue();
		}
		return DataTypes.INTEGER.create(sum);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-multiply")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue multiplyInteger(
			@XacmlParamVarArg(type=DataTypes.INTEGER, min=2)IntegerValue ...values)
	{
		Long value = 1L;
		for(IntegerValue v : values){
			value *= v.getValue();
		}
		return DataTypes.INTEGER.create(value);
	}	
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-add")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue addDouble(
			@XacmlParamVarArg(type=DataTypes.DOUBLE, min=2)DoubleValue ...values)
	{
		Double sum = 0.0;
		for(DoubleValue v : values){
			sum += v.getValue();
		}
		return DataTypes.DOUBLE.create(sum);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-multiply")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static BooleanValue multiplyDouble(
			@XacmlParamVarArg(type=DataTypes.DOUBLE, min=2)DoubleValue ...values)
	{
		Double value = 1.0;
		for(DoubleValue v : values){
			value *= v.getValue();
		}
		return DataTypes.DOUBLE.create(value);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue abs(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue v)
	{
		return DataTypes.INTEGER.create(Math.abs(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-abs")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue abs(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(Math.abs(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:floor")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue floor(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(Math.floor(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:round")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue round(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(Math.round(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-subtract")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue subtract(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue a,
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		return DataTypes.DOUBLE.create(a.getValue() - b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-divide")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue divideDouble(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue a,
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DataTypes.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-divide")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue divideInteger(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a,
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DataTypes.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-mod")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue modInteger(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a,
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DataTypes.INTEGER.create(a.getValue() % b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-subtract")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue subtract(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a,
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b)
	{
		return DataTypes.INTEGER.create(a.getValue() - b.getValue());
	}
}
