package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.types.Base64BinaryType.Base64BinaryValue;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.types.DateType.DateValue;
import com.artagon.xacml.v3.types.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.HexBinaryType.HexBinaryValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.TimeType.TimeValue;
import com.artagon.xacml.v3.types.X500NameType.X500NameValue;
import com.artagon.xacml.v3.types.YearMonthDurationType.YearMonthDurationValue;

@XacmlFunctionProvider(description="XACML set functions")
public class SetFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerValue> integerIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerValue> integerUnion(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue integerSubest(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue integerAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue integerSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN, isBag=true)
	public static BagOfAttributeValues<BooleanValue> booleanIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN, isBag=true)
	public static BagOfAttributeValues<BooleanValue> booleanUnion(
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue booleanSubset(
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue booleanAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue booleanSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING, isBag=true)
	public static BagOfAttributeValues<IntegerValue> stringIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING, isBag=true)
	public static BagOfAttributeValues<StringValue> stringUnion(
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue stringSubest(
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue stringAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue stringSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE, isBag=true)
	public static BagOfAttributeValues<DoubleValue> doubleIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE, isBag=true)
	public static BagOfAttributeValues<DoubleValue> doubleUnion(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue doubleSubest(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue doubleAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue doubleSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATE, isBag=true)
	public static BagOfAttributeValues<DateValue> dateIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATE, isBag=true)
	public static BagOfAttributeValues<DateValue> dateUnion(
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateSubest(
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.TIME, isBag=true)
	public static BagOfAttributeValues<TimeValue> timeIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.TIME, isBag=true)
	public static BagOfAttributeValues<TimeValue> timeUnion(
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue timeSubest(
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue timeAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue timeSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME, isBag=true)
	public static BagOfAttributeValues<DateTimeValue> dateTimeIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME, isBag=true)
	public static BagOfAttributeValues<DateTimeValue> dateTimeUnion(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateTimeSubest(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateTimeAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateTimeSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.ANYURI, isBag=true)
	public static BagOfAttributeValues<AnyURIValue> anyURIIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.ANYURI, isBag=true)
	public static BagOfAttributeValues<AnyURIValue> anyURITimeUnion(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyURISubest(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyURIAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyURISetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.X500NAME, isBag=true)
	public static BagOfAttributeValues<X500NameValue> x500NameIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.X500NAME, isBag=true)
	public static BagOfAttributeValues<X500NameValue> x500NameUnion(
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue x500NameSubest(
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue x500NameAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue x500NameSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.RFC822NAME, isBag=true)
	public static BagOfAttributeValues<RFC822NameValue> rfc822NameIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.RFC822NAME, isBag=true)
	public static BagOfAttributeValues<RFC822NameValue> rfc822NameUnion(
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue rfc822NameSubest(
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue rfc822NameAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue rfc822NameSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.HEXBINARY, isBag=true)
	public static BagOfAttributeValues<HexBinaryValue> hexBinaryIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.HEXBINARY, isBag=true)
	public static BagOfAttributeValues<HexBinaryValue> hexBinaryUnion(
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue hexBinarySubest(
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue hexBinaryAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue hexBinarySetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.BASE64BINARY, isBag=true)
	public static BagOfAttributeValues<Base64BinaryValue> base64BinaryIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.BASE64BINARY, isBag=true)
	public static BagOfAttributeValues<Base64BinaryValue> base64BinaryUnion(
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue base64BinarySubest(
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue base64BinaryAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue base64BinarySetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)
	public static BagOfAttributeValues<DayTimeDurationValue> dayTimeDurationIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)
	public static BagOfAttributeValues<DayTimeDurationValue> dayTimeDurationUnion(
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dayTimeDurationSubest(
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dayTimeDurationAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dayTimeDurationSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)
	public static BagOfAttributeValues<YearMonthDurationValue> yearMonthDurationIntersection(
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)
	public static BagOfAttributeValues<YearMonthDurationValue> yearMonthDurationUnion(
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue yearMonthDurationSubest(
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue yearMonthDurationAtLeastOneMemberOf(
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue yearMonthDurationSetEquals(
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
}
