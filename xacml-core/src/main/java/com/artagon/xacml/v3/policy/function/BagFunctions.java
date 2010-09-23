package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncParamVarArg;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.AnyURIType;
import com.artagon.xacml.v3.types.AnyURIValue;
import com.artagon.xacml.v3.types.Base64BinaryType;
import com.artagon.xacml.v3.types.Base64BinaryValue;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanValue;
import com.artagon.xacml.v3.types.DNSNameType;
import com.artagon.xacml.v3.types.DNSNameValue;
import com.artagon.xacml.v3.types.DateTimeType;
import com.artagon.xacml.v3.types.DateTimeValue;
import com.artagon.xacml.v3.types.DateType;
import com.artagon.xacml.v3.types.DateValue;
import com.artagon.xacml.v3.types.DayTimeDurationType;
import com.artagon.xacml.v3.types.DayTimeDurationValue;
import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.DoubleValue;
import com.artagon.xacml.v3.types.HexBinaryType;
import com.artagon.xacml.v3.types.HexBinaryValue;
import com.artagon.xacml.v3.types.IPAddressType;
import com.artagon.xacml.v3.types.IPAddressValue;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.IntegerValue;
import com.artagon.xacml.v3.types.RFC822NameType;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.StringValue;
import com.artagon.xacml.v3.types.TimeType;
import com.artagon.xacml.v3.types.TimeValue;
import com.artagon.xacml.v3.types.X500NameType;
import com.artagon.xacml.v3.types.X500NameValue;
import com.artagon.xacml.v3.types.YearMonthDurationType;
import com.artagon.xacml.v3.types.YearMonthDurationValue;
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
 * 
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML 3.0 bag functions")
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
	static <T extends AttributeValue> T oneAndOnlyImpl(
			BagOfAttributeValues bag){
		Preconditions.checkArgument(!bag.isEmpty(), "Bag is empty");
		Preconditions.checkArgument(!(bag.size() > 1), "Bag has more than one value");
		return bag.<T>value();
	}
	
	static IntegerValue typeBagSizeImpl(BagOfAttributeValues bag) {
		return IntegerType.INTEGER.create(bag.size());
	}
	
	static BooleanValue containsImpl(AttributeValue v,
			BagOfAttributeValues bag){

		return BooleanType.BOOLEAN.create(bag.contains(v));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue stringBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue stringIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfAttributeValues stringBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#string")
			StringValue ...values){
		return StringType.STRING.bagOf(values);
	}
	
	// boolean
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue booleanBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfAttributeValues booleanBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#boolean")
			BooleanValue ...values){
		return BooleanType.BOOLEAN.bagOf(values);
	}
	
	// integer
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue integerOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue integerBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue integerIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfAttributeValues integerBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#integer")
			IntegerValue ...values){
		return IntegerType.INTEGER.bagOf(values);
	}
	
	// time
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeValue timeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue timeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue timeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfAttributeValues timeBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#time")
			TimeValue ...values){
		return TimeType.TIME.bagOf(values);
	}
	
	// double
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue doubleOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue doubleBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue doubleIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfAttributeValues doubleBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#double")
			DoubleValue ...values){
		return DoubleType.DOUBLE.bagOf(values);
	}
	
	// date
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateValue dateOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue dateBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfAttributeValues dateBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#date")
			DateValue ...values){
		return DateType.DATE.bagOf(values);
	}
	
	// dateTime
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeValue dateTimeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue dateTimeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateTimeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfAttributeValues dateTimeBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dateTime")
			DateTimeValue ...values){
		return DateTimeType.DATETIME.bagOf(values);
	}
	
	// anyURI
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIValue anyURIOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue anyURIBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyURIIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfAttributeValues anyURIBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#anyURI")
			AnyURIValue ...values){
		return AnyURIType.ANYURI.bagOf(values);
	}
	
	// hexBinary
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
	public static HexBinaryValue hexBinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue hexBinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue hexBinaryIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")HexBinaryValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfAttributeValues hexBinaryBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
			HexBinaryValue ...values){
		return HexBinaryType.HEXBINARY.bagOf(values);
	}
	
	// base64Binary
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
	public static Base64BinaryValue base64BinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue base64BinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue base64IsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")Base64BinaryValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfAttributeValues base64BinaryBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
			Base64BinaryValue ...values){
		return Base64BinaryType.BASE64BINARY.bagOf(values);
	}
	
	// dayTimeDuration
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationValue dayTimeDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue dayTimeDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dayTimeDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfAttributeValues dayTimeDurationBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
			DayTimeDurationValue ...values){
		return DayTimeDurationType.DAYTIMEDURATION.bagOf(values);
	}
	
	// yearMonthDuration
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationValue yearMonthDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
			BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue yearMonthDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
			BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue yearMonthDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
			BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfAttributeValues yearMonthDurationBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
			YearMonthDurationValue ...values){
		return YearMonthDurationType.YEARMONTHDURATION.bagOf(values);
	}
	
	// x500Name
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameValue x500NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
			BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue x500NameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
			BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue x500NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameValue v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
			BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfAttributeValues x500NameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
			X500NameValue ...values){
		return X500NameType.X500NAME.bagOf(values);
	}
	
	// rfc822Name
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameValue rfc822NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
			BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue rfc822NamBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
			BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue rfc822NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameValue v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
			BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfAttributeValues rfc822NameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
			RFC822NameValue ...values){
		return RFC822NameType.Factory.bagOf(values);
	}
	
	// ipAddress
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressValue ipAddressOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
			BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue ipAddressBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
			BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue ipAddressIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressValue v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
			BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfAttributeValues ipAddressBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
			IPAddressValue ...values){
		return IPAddressType.IPADDRESS.bagOf(values);
	}
	
	// dnsName
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameValue dnsNameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
			BagOfAttributeValues bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue dnsNameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
			BagOfAttributeValues bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dnsNameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameValue v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
			BagOfAttributeValues bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
	public static BagOfAttributeValues dnsNameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
			DNSNameValue ...values){
		return DNSNameType.DNSNAME.bagOf(values);
	}
}
