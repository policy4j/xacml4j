package com.artagon.xacml.v30.policy.function;


import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.spi.function.XacmlLegacyFunc;
import com.artagon.xacml.v30.types.AnyURIExp;
import com.artagon.xacml.v30.types.Base64BinaryExp;
import com.artagon.xacml.v30.types.BooleanExp;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.DateExp;
import com.artagon.xacml.v30.types.DateTimeExp;
import com.artagon.xacml.v30.types.DayTimeDurationExp;
import com.artagon.xacml.v30.types.DoubleExp;
import com.artagon.xacml.v30.types.HexBinaryExp;
import com.artagon.xacml.v30.types.IntegerExp;
import com.artagon.xacml.v30.types.RFC822NameExp;
import com.artagon.xacml.v30.types.StringExp;
import com.artagon.xacml.v30.types.TimeExp;
import com.artagon.xacml.v30.types.X500NameExp;
import com.artagon.xacml.v30.types.YearMonthDurationExp;


/**
 * An implementation of XACML equality predicates
 * 
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML equality predicates")
public class EqualityPredicates 
{
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp eq(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp a, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equalsIgnoreCase(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanType.BOOLEAN.create(a.equalsIgnoreCase(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameExp a, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")HexBinaryExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")HexBinaryExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")Base64BinaryExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")Base64BinaryExp b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
}
