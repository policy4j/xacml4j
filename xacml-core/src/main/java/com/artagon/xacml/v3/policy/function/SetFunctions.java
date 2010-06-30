package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.function.XacmlFunc;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.spi.function.XacmlParam;
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

@XacmlFunctionProvider
public class SetFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerValue> integerIntersection(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerValue> integerUnion(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue integerSubest(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue integerAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue integerSetEquals(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN, isBag=true)
	public static BagOfAttributeValues<BooleanValue> booleanIntersection(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN, isBag=true)
	public static BagOfAttributeValues<BooleanValue> booleanUnion(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue booleanSubset(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue booleanAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue booleanSetEquals(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING, isBag=true)
	public static BagOfAttributeValues<IntegerValue> stringIntersection(
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING, isBag=true)
	public static BagOfAttributeValues<StringValue> stringUnion(
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> a,
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue stringSubest(
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> a,
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue stringAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> a,
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue stringSetEquals(
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> a,
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE, isBag=true)
	public static BagOfAttributeValues<DoubleValue> doubleIntersection(
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE, isBag=true)
	public static BagOfAttributeValues<DoubleValue> doubleUnion(
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue doubleSubest(
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue doubleAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue doubleSetEquals(
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> a,
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATE, isBag=true)
	public static BagOfAttributeValues<DateValue> dateIntersection(
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATE, isBag=true)
	public static BagOfAttributeValues<DateValue> dateUnion(
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateSubest(
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateSetEquals(
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> a,
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.TIME, isBag=true)
	public static BagOfAttributeValues<TimeValue> timeIntersection(
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.TIME, isBag=true)
	public static BagOfAttributeValues<TimeValue> timeUnion(
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue timeSubest(
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue timeAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue timeSetEquals(
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> a,
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME, isBag=true)
	public static BagOfAttributeValues<DateTimeValue> dateTimeIntersection(
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME, isBag=true)
	public static BagOfAttributeValues<DateTimeValue> dateTimeUnion(
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateTimeSubest(
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateTimeAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateTimeSetEquals(
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> a,
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.ANYURI, isBag=true)
	public static BagOfAttributeValues<AnyURIValue> anyURIIntersection(
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.ANYURI, isBag=true)
	public static BagOfAttributeValues<AnyURIValue> anyURITimeUnion(
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyURISubest(
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyURIAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyURISetEquals(
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> a,
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.X500NAME, isBag=true)
	public static BagOfAttributeValues<X500NameValue> x500NameIntersection(
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.X500NAME, isBag=true)
	public static BagOfAttributeValues<X500NameValue> x500NameUnion(
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue x500NameSubest(
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue x500NameAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue x500NameSetEquals(
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> a,
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)BagOfAttributeValues<X500NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.RFC822NAME, isBag=true)
	public static BagOfAttributeValues<RFC822NameValue> rfc822NameIntersection(
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.RFC822NAME, isBag=true)
	public static BagOfAttributeValues<RFC822NameValue> rfc822NameUnion(
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue rfc822NameSubest(
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue rfc822NameAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue rfc822NameSetEquals(
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> a,
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)BagOfAttributeValues<RFC822NameValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.HEXBINARY, isBag=true)
	public static BagOfAttributeValues<HexBinaryValue> hexBinaryIntersection(
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.HEXBINARY, isBag=true)
	public static BagOfAttributeValues<HexBinaryValue> hexBinaryUnion(
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue hexBinarySubest(
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue hexBinaryAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue hexBinarySetEquals(
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.BASE64BINARY, isBag=true)
	public static BagOfAttributeValues<Base64BinaryValue> base64BinaryIntersection(
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.BASE64BINARY, isBag=true)
	public static BagOfAttributeValues<Base64BinaryValue> base64BinaryUnion(
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue base64BinarySubest(
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue base64BinaryAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue base64BinarySetEquals(
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> a,
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)
	public static BagOfAttributeValues<DayTimeDurationValue> dayTimeDurationIntersection(
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)
	public static BagOfAttributeValues<DayTimeDurationValue> dayTimeDurationUnion(
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dayTimeDurationSubest(
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dayTimeDurationAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dayTimeDurationSetEquals(
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)
	public static BagOfAttributeValues<YearMonthDurationValue> yearMonthDurationIntersection(
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)
	public static BagOfAttributeValues<YearMonthDurationValue> yearMonthDurationUnion(
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue yearMonthDurationSubest(
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-member-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue yearMonthDurationAtLeastOneMemberOf(
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue yearMonthDurationSetEquals(
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> a,
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)BagOfAttributeValues<YearMonthDurationValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
}
