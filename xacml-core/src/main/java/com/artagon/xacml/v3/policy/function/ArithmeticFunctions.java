package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncParamVarArg;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.DoubleValue;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.IntegerValue;
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
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue addInteger(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)IntegerValue ...values) 
	{
		Long sum = 0L;
		for(IntegerValue v : values){
			sum += v.getValue();
		}
		return IntegerType.INTEGER.create(sum);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-multiply")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue multiplyInteger(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)IntegerValue ...values)
	{
		Long value = 1L;
		for(IntegerValue v : values){
			value *= v.getValue();
		}
		return IntegerType.INTEGER.create(value);
	}	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-add")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue addDouble(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#double", min=2)DoubleValue ...values)
	{
		Double sum = 0.0;
		for(DoubleValue v : values){
			sum += v.getValue();
		}
		return DoubleType.DOUBLE.create(sum);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-multiply")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue multiplyDouble(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#double", min=2)DoubleValue ...values)
	{
		Double value = 1.0;
		for(DoubleValue v : values){
			value *= v.getValue();
		}
		return DoubleType.DOUBLE.create(value);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue abs(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue v)
	{
		return IntegerType.INTEGER.create(Math.abs(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-abs")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue abs(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue v)
	{
		return DoubleType.DOUBLE.create(Math.abs(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:floor")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue floor(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue v)
	{
		return DoubleType.DOUBLE.create(Math.floor(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:round")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue round(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue v)
	{
		return DoubleType.DOUBLE.create(Math.round(v.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-subtract")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue b)
	{
		return DoubleType.DOUBLE.create(a.getValue() - b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-divide")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue divideDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue b)
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DoubleType.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-divide")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue divideInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DoubleType.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-mod")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue modInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return IntegerType.INTEGER.create(a.getValue() % b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-subtract")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b)
	{
		return IntegerType.INTEGER.create(a.getValue() - b.getValue());
	}
}
