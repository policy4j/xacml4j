package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.function.XacmlFunc;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.spi.function.XacmlParam;
import com.artagon.xacml.v3.spi.function.XacmlParamVarArg;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.types.Base64BinaryType.Base64BinaryValue;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.DNSNameType.DNSNameValue;
import com.artagon.xacml.v3.types.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.types.DateType.DateValue;
import com.artagon.xacml.v3.types.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.HexBinaryType.HexBinaryValue;
import com.artagon.xacml.v3.types.IPAddressType.IPAddressValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.TimeType.TimeValue;
import com.artagon.xacml.v3.types.X500NameType.X500NameValue;
import com.artagon.xacml.v3.types.YearMonthDurationType.YearMonthDurationValue;
import com.google.common.base.Preconditions;

/**
 * 
 * These functions operate on a bag of ‘type’ values, where type is one of the 
 * primitive data-types, and x.x is a version of XACML where the 
 * function has been defined. Some additional conditions defined for each 
 * function below SHALL cause the expression to evaluate to "Indeterminate".
 * 
 * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
 * This function SHALL take a bag of ‘type’ values as an argument and SHALL return a value 
 * of ‘- type’. It SHALL return the only value in the bag. If the bag does 
 * not have one and only one value, then the expression SHALL evaluate to "Indeterminate".
 * 
 * urn:oasis:names:tc:xacml:x.x:function:type-bag-size This function SHALL take a bag 
 * of ‘type’ values as an argument and SHALL return an
 * “http://www.w3.org/2001/XMLSchema#integer” indicating 
 * the number of values in the bag. urn:oasis:names:tc:xacml:x.x:function:type-is-in
 * This function SHALL take an argument of ‘type’ as the first argument 
 * and a bag of type values as the second argument and SHALL return an 
 * “http://www.w3.org/2001/XMLSchema#boolean”. The function SHALL evaluate to 
 * "True" if and only if the first argument matches by the 
 * "urn:oasis:names:tc:xacml:x.x:function:type-equal" any value in the bag. 
 * Otherwise, it SHALL return “False”.
 * 
 * urn:oasis:names:tc:xacml:x.x:function:type-bag
 * This function SHALL take any number of arguments of ‘type’ and return a 
 * bag of ‘type’ values containing the values of the arguments. 
 * An application of this function to zero arguments SHALL produce an 
 * empty bag of the specified data-type.
 */
@XacmlFunctionProvider
public class BagFunctions 
{
	
	/**
	 * This function takes a bag of {@link AttributeValue} values as an 
	 * argument and returns a value of {@link AttributeValue}. 
	 * It returns the only value in the bag. If the bag does not have 
	 * one and only one value, then this functions throws 
	 * {@link IllegalArgumentException}
	 * 
	 * @param <T> a bag value type
	 * @param bag a bag of values
	 * @return {@link AttributeValue} one and only one value
	 */
	@SuppressWarnings("unchecked")
	static <T extends AttributeValue> T oneAndOnlyImpl(
			BagOfAttributeValues<? extends AttributeValue> bag){
		Preconditions.checkArgument(!bag.isEmpty(), "Bag is empty");
		Preconditions.checkArgument(!(bag.size() > 1), "Bag has more than one value");
		return (T)bag.value();
	}
	
	static IntegerValue typeBagSizeImpl(BagOfAttributeValues<? extends AttributeValue> bag) {
		return XacmlDataTypes.INTEGER.create(bag.size());
	}
	
	static BooleanValue containsImpl(AttributeValue v,
			BagOfAttributeValues<? extends AttributeValue> bag){

		return XacmlDataTypes.BOOLEAN.create(bag.contains(v));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringValue stringOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue stringBagSize(
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue stringIsIn(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue v,
			@XacmlParam(type=XacmlDataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING, isBag=true)
	public static BagOfAttributeValues<StringValue> stringBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.STRING)
			StringValue ...values){
		return XacmlDataTypes.STRING.bag(values);
	}
	
	// boolean
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue booleanOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue booleanBagSize(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue booleanIsIn(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN)BooleanValue v,
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN, isBag=true)
	public static BagOfAttributeValues<BooleanValue> booleanBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.BOOLEAN)
			BooleanValue ...values){
		return XacmlDataTypes.BOOLEAN.bag(values);
	}
	
	// integer
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue integerOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue integerBagSize(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue integerIsIn(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue v,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerValue> integerBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.INTEGER)
			IntegerValue ...values){
		return XacmlDataTypes.INTEGER.bag(values);
	}
	
	// time
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.TIME)
	public static TimeValue timeOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue timeBagSize(
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue timeIsIn(
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue v,
			@XacmlParam(type=XacmlDataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.TIME, isBag=true)
	public static BagOfAttributeValues<IntegerValue> timeBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.TIME)
			TimeValue ...values){
		return XacmlDataTypes.TIME.bag(values);
	}
	
	// double
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleValue doubleOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue doubleBagSize(
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue doubleIsIn(
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue v,
			@XacmlParam(type=XacmlDataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE, isBag=true)
	public static BagOfAttributeValues<DoubleValue> doubleBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.DOUBLE)
			DoubleValue ...values){
		return XacmlDataTypes.DOUBLE.bag(values);
	}
	
	// date
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATE)
	public static DateValue dateOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue dateBagSize(
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateIsIn(
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue v,
			@XacmlParam(type=XacmlDataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATE, isBag=true)
	public static BagOfAttributeValues<DateValue> dateBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.DATE)
			DateValue ...values){
		return XacmlDataTypes.DATE.bag(values);
	}
	
	// dateTime
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME)
	public static DateTimeValue dateTimeOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue dateTimeBagSize(
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dateTimeIsIn(
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue v,
			@XacmlParam(type=XacmlDataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME, isBag=true)
	public static BagOfAttributeValues<DateTimeValue> dateTimeBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.DATETIME)
			DateTimeValue ...values){
		return XacmlDataTypes.DATETIME.bag(values);
	}
	
	// anyURI
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.ANYURI)
	public static AnyURIValue anyURIOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue anyURIBagSize(
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyURIIsIn(
			@XacmlParam(type=XacmlDataTypes.ANYURI)AnyURIValue v,
			@XacmlParam(type=XacmlDataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.ANYURI, isBag=true)
	public static BagOfAttributeValues<AnyURIValue> anyURIBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.ANYURI)
			AnyURIValue ...values){
		return XacmlDataTypes.ANYURI.bag(values);
	}
	
	// hexBinary
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.HEXBINARY)
	public static HexBinaryValue hexBinaryOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue hexBinaryBagSize(
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue hexBinaryIsIn(
			@XacmlParam(type=XacmlDataTypes.HEXBINARY)HexBinaryValue v,
			@XacmlParam(type=XacmlDataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.HEXBINARY, isBag=true)
	public static BagOfAttributeValues<HexBinaryValue> hexBinaryBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.HEXBINARY)
			HexBinaryValue ...values){
		return XacmlDataTypes.HEXBINARY.bag(values);
	}
	
	// base64Binary
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.BASE64BINARY)
	public static Base64BinaryValue base64BinaryOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue base64BinaryBagSize(
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue base64IsIn(
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY)Base64BinaryValue v,
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.BASE64BINARY, isBag=true)
	public static BagOfAttributeValues<Base64BinaryValue> base64BinaryBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.BASE64BINARY)
			Base64BinaryValue ...values){
		return XacmlDataTypes.BASE64BINARY.bag(values);
	}
	
	// dayTimeDuration
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.DAYTIMEDURATION)
	public static DayTimeDurationValue dayTimeDurationOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue dayTimeDurationBagSize(
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dayTimeDurationIsIn(
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION)DayTimeDurationValue v,
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.DAYTIMEDURATION, isBag=true)
	public static BagOfAttributeValues<DayTimeDurationValue> dayTimeDurationBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.DAYTIMEDURATION)
			DayTimeDurationValue ...values){
		return XacmlDataTypes.DAYTIMEDURATION.bag(values);
	}
	
	// yearMonthDuration
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.YEARMONTHDURATION)
	public static YearMonthDurationValue yearMonthDurationOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)
			BagOfAttributeValues<YearMonthDurationValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue yearMonthDurationBagSize(
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)
			BagOfAttributeValues<YearMonthDurationValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue yearMonthDurationIsIn(
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION)YearMonthDurationValue v,
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)
			BagOfAttributeValues<YearMonthDurationValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.YEARMONTHDURATION, isBag=true)
	public static BagOfAttributeValues<YearMonthDurationValue> yearMonthDurationBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.YEARMONTHDURATION)
			YearMonthDurationValue ...values){
		return XacmlDataTypes.YEARMONTHDURATION.bag(values);
	}
	
	// x500Name
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.X500NAME)
	public static X500NameValue x500NameOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)
			BagOfAttributeValues<X500NameValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue x500NameBagSize(
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)
			BagOfAttributeValues<X500NameValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue x500NameIsIn(
			@XacmlParam(type=XacmlDataTypes.X500NAME)X500NameValue v,
			@XacmlParam(type=XacmlDataTypes.X500NAME, isBag=true)
			BagOfAttributeValues<X500NameValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.X500NAME, isBag=true)
	public static BagOfAttributeValues<X500NameValue> x500NameBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.X500NAME)
			X500NameValue ...values){
		return XacmlDataTypes.X500NAME.bag(values);
	}
	
	// rfc822Name
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.RFC822NAME)
	public static RFC822NameValue rfc822NameOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)
			BagOfAttributeValues<RFC822NameValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue rfc822NamBagSize(
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)
			BagOfAttributeValues<RFC822NameValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue rfc822NameIsIn(
			@XacmlParam(type=XacmlDataTypes.RFC822NAME)RFC822NameValue v,
			@XacmlParam(type=XacmlDataTypes.RFC822NAME, isBag=true)
			BagOfAttributeValues<RFC822NameValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.RFC822NAME, isBag=true)
	public static BagOfAttributeValues<RFC822NameValue> rfc822NameBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.RFC822NAME)
			RFC822NameValue ...values){
		return XacmlDataTypes.RFC822NAME.bag(values);
	}
	
	// ipAddress
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.IPADDRESS)
	public static IPAddressValue ipAddressOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.IPADDRESS, isBag=true)
			BagOfAttributeValues<IPAddressValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue ipAddressBagSize(
			@XacmlParam(type=XacmlDataTypes.IPADDRESS, isBag=true)
			BagOfAttributeValues<IPAddressValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue ipAddressIsIn(
			@XacmlParam(type=XacmlDataTypes.IPADDRESS)IPAddressValue v,
			@XacmlParam(type=XacmlDataTypes.IPADDRESS, isBag=true)
			BagOfAttributeValues<IPAddressValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.IPADDRESS, isBag=true)
	public static BagOfAttributeValues<IPAddressValue> ipAddressBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.IPADDRESS)
			IPAddressValue ...values){
		return XacmlDataTypes.IPADDRESS.bag(values);
	}
	
	// dnsName
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only")
	@XacmlFuncReturnType(type=XacmlDataTypes.DNSNAME)
	public static DNSNameValue dnsNameOneAndOnly(
			@XacmlParam(type=XacmlDataTypes.DNSNAME, isBag=true)
			BagOfAttributeValues<DNSNameValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue dnsNameBagSize(
			@XacmlParam(type=XacmlDataTypes.DNSNAME, isBag=true)
			BagOfAttributeValues<DNSNameValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue dnsNameIsIn(
			@XacmlParam(type=XacmlDataTypes.DNSNAME)DNSNameValue v,
			@XacmlParam(type=XacmlDataTypes.DNSNAME, isBag=true)
			BagOfAttributeValues<DNSNameValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag")
	@XacmlFuncReturnType(type=XacmlDataTypes.DNSNAME, isBag=true)
	public static BagOfAttributeValues<DNSNameValue> dnsNameBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=XacmlDataTypes.DNSNAME)
			DNSNameValue ...values){
		return XacmlDataTypes.DNSNAME.bag(values);
	}
}
