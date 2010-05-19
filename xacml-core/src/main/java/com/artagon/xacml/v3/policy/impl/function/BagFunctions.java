package com.artagon.xacml.v3.policy.impl.function;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.policy.spi.function.XacmlParamVarArg;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.policy.type.Base64BinaryType.Base64BinaryValue;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.DNSNameType.DNSNameValue;
import com.artagon.xacml.v3.policy.type.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.policy.type.DateType.DateValue;
import com.artagon.xacml.v3.policy.type.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.v3.policy.type.HexBinaryType.HexBinaryValue;
import com.artagon.xacml.v3.policy.type.IPAddressType.IPAddressValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;
import com.artagon.xacml.v3.policy.type.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;
import com.artagon.xacml.v3.policy.type.TimeType.TimeValue;
import com.artagon.xacml.v3.policy.type.X500NameType.X500NameValue;
import com.artagon.xacml.v3.policy.type.YearMonthDurationType.YearMonthDurationValue;
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
		return DataTypes.INTEGER.create(bag.size());
	}
	
	static BooleanValue containsImpl(AttributeValue v,
			BagOfAttributeValues<? extends AttributeValue> bag){

		return DataTypes.BOOLEAN.create(bag.contains(v));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringValue stringOneAndOnly(
			@XacmlParam(type=DataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue stringBagSize(
			@XacmlParam(type=DataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue stringIsIn(
			@XacmlParam(type=DataTypes.STRING)StringValue v,
			@XacmlParam(type=DataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-bag")
	@XacmlFuncReturnType(type=DataTypes.STRING, isBag=true)
	public static BagOfAttributeValues<StringValue> stringBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.STRING)
			StringValue ...values){
		return DataTypes.STRING.bag(values);
	}
	
	// boolean
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static BooleanValue booleanOneAndOnly(
			@XacmlParam(type=DataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue booleanBagSize(
			@XacmlParam(type=DataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue booleanIsIn(
			@XacmlParam(type=DataTypes.BOOLEAN)BooleanValue v,
			@XacmlParam(type=DataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN, isBag=true)
	public static BagOfAttributeValues<BooleanValue> booleanBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.BOOLEAN)
			BooleanValue ...values){
		return DataTypes.BOOLEAN.bag(values);
	}
	
	// integer
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue integerOneAndOnly(
			@XacmlParam(type=DataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue integerBagSize(
			@XacmlParam(type=DataTypes.DOUBLE, isBag=true)BagOfAttributeValues<IntegerValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue integerIsIn(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue v,
			@XacmlParam(type=DataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag")
	@XacmlFuncReturnType(type=DataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerValue> integerBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.INTEGER)
			IntegerValue ...values){
		return DataTypes.INTEGER.bag(values);
	}
	
	// time
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.TIME)
	public static TimeValue timeOneAndOnly(
			@XacmlParam(type=DataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue timeBagSize(
			@XacmlParam(type=DataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue timeIsIn(
			@XacmlParam(type=DataTypes.TIME)TimeValue v,
			@XacmlParam(type=DataTypes.TIME, isBag=true)BagOfAttributeValues<TimeValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-bag")
	@XacmlFuncReturnType(type=DataTypes.TIME, isBag=true)
	public static BagOfAttributeValues<IntegerValue> timeBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.TIME)
			TimeValue ...values){
		return DataTypes.TIME.bag(values);
	}
	
	// double
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue doubleOneAndOnly(
			@XacmlParam(type=DataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue doubleBagSize(
			@XacmlParam(type=DataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue doubleIsIn(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue v,
			@XacmlParam(type=DataTypes.DOUBLE, isBag=true)BagOfAttributeValues<DoubleValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-bag")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE, isBag=true)
	public static BagOfAttributeValues<DoubleValue> doubleBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.DOUBLE)
			DoubleValue ...values){
		return DataTypes.DOUBLE.bag(values);
	}
	
	// date
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.DATE)
	public static DateValue dateOneAndOnly(
			@XacmlParam(type=DataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue dateBagSize(
			@XacmlParam(type=DataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue dateIsIn(
			@XacmlParam(type=DataTypes.DATE)DateValue v,
			@XacmlParam(type=DataTypes.DATE, isBag=true)BagOfAttributeValues<DateValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-bag")
	@XacmlFuncReturnType(type=DataTypes.DATE, isBag=true)
	public static BagOfAttributeValues<DateValue> dateBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.DATE)
			DateValue ...values){
		return DataTypes.DATE.bag(values);
	}
	
	// dateTime
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.DATETIME)
	public static DateTimeValue dateTimeOneAndOnly(
			@XacmlParam(type=DataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue dateTimeBagSize(
			@XacmlParam(type=DataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue dateTimeIsIn(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue v,
			@XacmlParam(type=DataTypes.DATETIME, isBag=true)BagOfAttributeValues<DateTimeValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag")
	@XacmlFuncReturnType(type=DataTypes.DATETIME, isBag=true)
	public static BagOfAttributeValues<DateTimeValue> dateTimeBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.DATETIME)
			DateTimeValue ...values){
		return DataTypes.DATETIME.bag(values);
	}
	
	// anyURI
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.ANYURI)
	public static AnyURIValue anyURIOneAndOnly(
			@XacmlParam(type=DataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue anyURIBagSize(
			@XacmlParam(type=DataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in")
	@XacmlFuncReturnType(type=DataTypes.ANYURI)
	public static BooleanValue anyURIIsIn(
			@XacmlParam(type=DataTypes.ANYURI)AnyURIValue v,
			@XacmlParam(type=DataTypes.ANYURI, isBag=true)BagOfAttributeValues<AnyURIValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag")
	@XacmlFuncReturnType(type=DataTypes.ANYURI, isBag=true)
	public static BagOfAttributeValues<AnyURIValue> anyURIBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.ANYURI)
			AnyURIValue ...values){
		return DataTypes.ANYURI.bag(values);
	}
	
	// hexBinary
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.HEXBINARY)
	public static HexBinaryValue hexBinaryOneAndOnly(
			@XacmlParam(type=DataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue hexBinaryBagSize(
			@XacmlParam(type=DataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue hexBinaryIsIn(
			@XacmlParam(type=DataTypes.HEXBINARY)HexBinaryValue v,
			@XacmlParam(type=DataTypes.HEXBINARY, isBag=true)BagOfAttributeValues<HexBinaryValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag")
	@XacmlFuncReturnType(type=DataTypes.HEXBINARY, isBag=true)
	public static BagOfAttributeValues<HexBinaryValue> hexBinaryBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.HEXBINARY)
			HexBinaryValue ...values){
		return DataTypes.HEXBINARY.bag(values);
	}
	
	// base64Binary
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.BASE64BINARY)
	public static Base64BinaryValue base64BinaryOneAndOnly(
			@XacmlParam(type=DataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue base64BinaryBagSize(
			@XacmlParam(type=DataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue base64IsIn(
			@XacmlParam(type=DataTypes.BASE64BINARY)Base64BinaryValue v,
			@XacmlParam(type=DataTypes.BASE64BINARY, isBag=true)BagOfAttributeValues<Base64BinaryValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag")
	@XacmlFuncReturnType(type=DataTypes.BASE64BINARY, isBag=true)
	public static BagOfAttributeValues<Base64BinaryValue> base64BinaryBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.BASE64BINARY)
			Base64BinaryValue ...values){
		return DataTypes.BASE64BINARY.bag(values);
	}
	
	// dayTimeDuration
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.DAYTIMEDURATION)
	public static DayTimeDurationValue dayTimeDurationOneAndOnly(
			@XacmlParam(type=DataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue dayTimeDurationBagSize(
			@XacmlParam(type=DataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue dayTimeDurationIsIn(
			@XacmlParam(type=DataTypes.DAYTIMEDURATION)DayTimeDurationValue v,
			@XacmlParam(type=DataTypes.DAYTIMEDURATION, isBag=true)BagOfAttributeValues<DayTimeDurationValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag")
	@XacmlFuncReturnType(type=DataTypes.DAYTIMEDURATION, isBag=true)
	public static BagOfAttributeValues<DayTimeDurationValue> dayTimeDurationBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.DAYTIMEDURATION)
			DayTimeDurationValue ...values){
		return DataTypes.DAYTIMEDURATION.bag(values);
	}
	
	// yearMonthDuration
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.YEARMONTHDURATION)
	public static YearMonthDurationValue yearMonthDurationOneAndOnly(
			@XacmlParam(type=DataTypes.YEARMONTHDURATION, isBag=true)
			BagOfAttributeValues<YearMonthDurationValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue yearMonthDurationBagSize(
			@XacmlParam(type=DataTypes.YEARMONTHDURATION, isBag=true)
			BagOfAttributeValues<YearMonthDurationValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue yearMonthDurationIsIn(
			@XacmlParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationValue v,
			@XacmlParam(type=DataTypes.YEARMONTHDURATION, isBag=true)
			BagOfAttributeValues<YearMonthDurationValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag")
	@XacmlFuncReturnType(type=DataTypes.YEARMONTHDURATION, isBag=true)
	public static BagOfAttributeValues<YearMonthDurationValue> yearMonthDurationBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.YEARMONTHDURATION)
			YearMonthDurationValue ...values){
		return DataTypes.YEARMONTHDURATION.bag(values);
	}
	
	// x500Name
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.X500NAME)
	public static X500NameValue x500NameOneAndOnly(
			@XacmlParam(type=DataTypes.X500NAME, isBag=true)
			BagOfAttributeValues<X500NameValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue x500NameBagSize(
			@XacmlParam(type=DataTypes.X500NAME, isBag=true)
			BagOfAttributeValues<X500NameValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue x500NameIsIn(
			@XacmlParam(type=DataTypes.X500NAME)X500NameValue v,
			@XacmlParam(type=DataTypes.X500NAME, isBag=true)
			BagOfAttributeValues<X500NameValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag")
	@XacmlFuncReturnType(type=DataTypes.X500NAME, isBag=true)
	public static BagOfAttributeValues<X500NameValue> x500NameBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.X500NAME)
			X500NameValue ...values){
		return DataTypes.X500NAME.bag(values);
	}
	
	// rfc822Name
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.RFC822NAME)
	public static RFC822NameValue rfc822NameOneAndOnly(
			@XacmlParam(type=DataTypes.RFC822NAME, isBag=true)
			BagOfAttributeValues<RFC822NameValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue rfc822NamBagSize(
			@XacmlParam(type=DataTypes.RFC822NAME, isBag=true)
			BagOfAttributeValues<RFC822NameValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue rfc822NameIsIn(
			@XacmlParam(type=DataTypes.RFC822NAME)RFC822NameValue v,
			@XacmlParam(type=DataTypes.RFC822NAME, isBag=true)
			BagOfAttributeValues<RFC822NameValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag")
	@XacmlFuncReturnType(type=DataTypes.RFC822NAME, isBag=true)
	public static BagOfAttributeValues<RFC822NameValue> rfc822NameBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.RFC822NAME)
			RFC822NameValue ...values){
		return DataTypes.RFC822NAME.bag(values);
	}
	
	// ipAddress
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.IPADDRESS)
	public static IPAddressValue ipAddressOneAndOnly(
			@XacmlParam(type=DataTypes.IPADDRESS, isBag=true)
			BagOfAttributeValues<IPAddressValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue ipAddressBagSize(
			@XacmlParam(type=DataTypes.IPADDRESS, isBag=true)
			BagOfAttributeValues<IPAddressValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue ipAddressIsIn(
			@XacmlParam(type=DataTypes.IPADDRESS)IPAddressValue v,
			@XacmlParam(type=DataTypes.IPADDRESS, isBag=true)
			BagOfAttributeValues<IPAddressValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag")
	@XacmlFuncReturnType(type=DataTypes.IPADDRESS, isBag=true)
	public static BagOfAttributeValues<IPAddressValue> ipAddressBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.IPADDRESS)
			IPAddressValue ...values){
		return DataTypes.IPADDRESS.bag(values);
	}
	
	// dnsName
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.DNSNAME)
	public static DNSNameValue dnsNameOneAndOnly(
			@XacmlParam(type=DataTypes.DNSNAME, isBag=true)
			BagOfAttributeValues<DNSNameValue> bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue dnsNameBagSize(
			@XacmlParam(type=DataTypes.DNSNAME, isBag=true)
			BagOfAttributeValues<DNSNameValue> bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue dnsNameIsIn(
			@XacmlParam(type=DataTypes.DNSNAME)DNSNameValue v,
			@XacmlParam(type=DataTypes.DNSNAME, isBag=true)
			BagOfAttributeValues<DNSNameValue> bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag")
	@XacmlFuncReturnType(type=DataTypes.DNSNAME, isBag=true)
	public static BagOfAttributeValues<DNSNameValue> dnsNameBag(
			@XacmlParamVarArg(min=0, max=Integer.MAX_VALUE, type=DataTypes.DNSNAME)
			DNSNameValue ...values){
		return DataTypes.DNSNAME.bag(values);
	}
}
