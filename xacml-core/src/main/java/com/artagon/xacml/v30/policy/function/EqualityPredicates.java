package com.artagon.xacml.v30.policy.function;


import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.spi.function.XacmlLegacyFunc;
import com.artagon.xacml.v30.types.AnyURIValue;
import com.artagon.xacml.v30.types.Base64BinaryValue;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.BooleanValue;
import com.artagon.xacml.v30.types.DateTimeValue;
import com.artagon.xacml.v30.types.DateValue;
import com.artagon.xacml.v30.types.DayTimeDurationValue;
import com.artagon.xacml.v30.types.DoubleValue;
import com.artagon.xacml.v30.types.HexBinaryValue;
import com.artagon.xacml.v30.types.IntegerValue;
import com.artagon.xacml.v30.types.RFC822NameValue;
import com.artagon.xacml.v30.types.StringValue;
import com.artagon.xacml.v30.types.TimeValue;
import com.artagon.xacml.v30.types.X500NameValue;
import com.artagon.xacml.v30.types.YearMonthDurationValue;


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
	public static BooleanValue eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue eq(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameValue a, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equalsIgnoreCase(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue b)
	{
		return BooleanType.BOOLEAN.create(a.equalsIgnoreCase(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameValue a, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")HexBinaryValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")HexBinaryValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")Base64BinaryValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")Base64BinaryValue b)
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
}
