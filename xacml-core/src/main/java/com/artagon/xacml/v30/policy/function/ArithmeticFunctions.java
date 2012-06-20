package com.artagon.xacml.v30.policy.function;

import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncParamVarArg;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.types.DoubleExp;
import com.artagon.xacml.v30.types.DoubleType;
import com.artagon.xacml.v30.types.IntegerExp;
import com.artagon.xacml.v30.types.IntegerType;
import com.google.common.base.Preconditions;


/**
 * All of the following functions SHALL take two arguments of the specified data-type, 
 * integer, or double, and SHALL return an element of integer or double data-type, 
 * respectively. However, the "add" functions MAY take more than two arguments. 
 * Each function evaluation operating on doubles SHALL proceed as specified by their 
 * logical counterparts in IEEE 754 [IEEE754]. For all of these functions, 
 * if any argument is "Indeterminate", then the function SHALL evaluate to 
 * "Indeterminate". In the case of the divide functions, if the divisor is zero,
 *  then the function SHALL evaluate to "Indeterminate".
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
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp addInteger(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)
			IntegerExp ...values) 
	{
		Long sum = 0L;
		for(IntegerExp v : values){
			sum += v.getValue();
		}
		return IntegerType.INTEGER.create(sum);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-multiply")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp multiplyInteger(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)
			IntegerExp ...values)
	{
		Long value = 1L;
		for(IntegerExp v : values){
			value *= v.getValue();
		}
		return IntegerType.INTEGER.create(value);
	}	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-add")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp addDouble(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#double", min=2)
			DoubleExp ...values)
	{
		Double sum = 0.0;
		for(DoubleExp v : values){
			sum += v.getValue();
		}
		return DoubleType.DOUBLE.create(sum);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-multiply")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp multiplyDouble(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#double", min=2)
			DoubleExp ...values)
	{
		Double value = 1.0;
		for(DoubleExp v : values){
			value *= v.getValue();
		}
		return DoubleType.DOUBLE.create(value);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp abs(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp v)
	{
		return IntegerType.INTEGER.create(Math.abs(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-abs")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp abs(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return DoubleType.DOUBLE.create(Math.abs(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:floor")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp floor(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return DoubleType.DOUBLE.create(Math.floor(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:round")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp round(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return DoubleType.DOUBLE.create(Math.round(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-subtract")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		return DoubleType.DOUBLE.create(a.getValue() - b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-divide")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp divideDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DoubleType.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-divide")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp divideInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DoubleType.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-mod")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp modInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return IntegerType.INTEGER.create(a.getValue() % b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-subtract")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return IntegerType.INTEGER.create(a.getValue() - b.getValue());
	}
}
