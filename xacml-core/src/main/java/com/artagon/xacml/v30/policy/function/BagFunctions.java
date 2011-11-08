package com.artagon.xacml.v30.policy.function;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncParamVarArg;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.types.AnyURIType;
import com.artagon.xacml.v30.types.AnyURIValueExp;
import com.artagon.xacml.v30.types.Base64BinaryType;
import com.artagon.xacml.v30.types.Base64BinaryValueExp;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.BooleanValueExp;
import com.artagon.xacml.v30.types.DNSNameType;
import com.artagon.xacml.v30.types.DNSNameValueExp;
import com.artagon.xacml.v30.types.DateTimeType;
import com.artagon.xacml.v30.types.DateTimeValueExp;
import com.artagon.xacml.v30.types.DateType;
import com.artagon.xacml.v30.types.DateValueExp;
import com.artagon.xacml.v30.types.DayTimeDurationType;
import com.artagon.xacml.v30.types.DayTimeDurationValueExp;
import com.artagon.xacml.v30.types.DoubleType;
import com.artagon.xacml.v30.types.DoubleValueExp;
import com.artagon.xacml.v30.types.HexBinaryType;
import com.artagon.xacml.v30.types.HexBinaryValueExp;
import com.artagon.xacml.v30.types.IPAddressType;
import com.artagon.xacml.v30.types.IPAddressValueExp;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.IntegerValueExp;
import com.artagon.xacml.v30.types.RFC822NameType;
import com.artagon.xacml.v30.types.RFC822NameValueExp;
import com.artagon.xacml.v30.types.StringType;
import com.artagon.xacml.v30.types.StringValueExp;
import com.artagon.xacml.v30.types.TimeType;
import com.artagon.xacml.v30.types.TimeValueExp;
import com.artagon.xacml.v30.types.X500NameType;
import com.artagon.xacml.v30.types.X500NameValueExp;
import com.artagon.xacml.v30.types.YearMonthDurationType;
import com.artagon.xacml.v30.types.YearMonthDurationValueExp;
import com.google.common.base.Preconditions;

/**
 * 
 * These functions operate on a bag of 'type' values, where type is one of the 
 * primitive data-types, and x.x is a version of XACML where the 
 * function has been defined. Some additional conditions defined for each 
 * function below SHALL cause the expression to evaluate to "Indeterminate".
 * 
 * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
 * This function SHALL take a bag of 'type' values as an argument and SHALL return a value 
 * of '- type'. It SHALL return the only value in the bag. If the bag does 
 * not have one and only one value, then the expression SHALL evaluate to "Indeterminate".
 * 
 * urn:oasis:names:tc:xacml:x.x:function:type-bag-size This function SHALL take a bag 
 * of 'type' values as an argument and SHALL return an
 * "http://www.w3.org/2001/XMLSchema#integer" indicating 
 * the number of values in the bag. urn:oasis:names:tc:xacml:x.x:function:type-is-in
 * This function SHALL take an argument of 'type' as the first argument 
 * and a bag of type values as the second argument and SHALL return an 
 * "http://www.w3.org/2001/XMLSchema#boolean". The function SHALL evaluate to 
 * "True" if and only if the first argument matches by the 
 * "urn:oasis:names:tc:xacml:x.x:function:type-equal" any value in the bag. 
 * Otherwise, it SHALL return "False".
 * 
 * urn:oasis:names:tc:xacml:x.x:function:type-bag
 * This function SHALL take any number of arguments of 'type' and return a 
 * bag of 'type' values containing the values of the arguments. 
 * An application of this function to zero arguments SHALL produce an 
 * empty bag of the specified data-type.
 * 
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML 3.0 bag functions")
public class BagFunctions 
{
	
	/**
	 * This function takes a bag of {@link AttributeExp} values as an 
	 * argument and returns a value of {@link AttributeExp}. 
	 * It returns the only value in the bag. If the bag does not have 
	 * one and only one value, then this functions throws 
	 * {@link IllegalArgumentException}
	 * 
	 * @param <T> a bag value type
	 * @param bag a bag of values
	 * @return {@link AttributeExp} one and only one value
	 */
	static <T extends AttributeExp> T oneAndOnlyImpl(
			BagOfAttributesExp bag){
		Preconditions.checkArgument(!bag.isEmpty(), "Bag is empty");
		Preconditions.checkArgument(!(bag.size() > 1), "Bag has more than one value");
		return bag.<T>value();
	}
	
	static IntegerValueExp typeBagSizeImpl(BagOfAttributesExp bag) {
		return IntegerType.INTEGER.create(bag.size());
	}
	
	static BooleanValueExp containsImpl(AttributeExp v,
			BagOfAttributesExp bag){

		return BooleanType.BOOLEAN.create(bag.contains(v));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
			BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp stringBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp stringIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfAttributesExp stringBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#string")
			StringValueExp ...values){
		return StringType.STRING.bagOf(values);
	}
	
	// boolean
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp booleanOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp booleanBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp booleanIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfAttributesExp booleanBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#boolean")
			BooleanValueExp ...values){
		return BooleanType.BOOLEAN.bagOf(values);
	}
	
	// integer
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp integerOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp integerBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp integerIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfAttributesExp integerBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#integer")
			IntegerValueExp ...values){
		return IntegerType.INTEGER.bagOf(values);
	}
	
	// time
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeValueExp timeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp timeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp timeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfAttributesExp timeBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#time")
			TimeValueExp ...values){
		return TimeType.TIME.bagOf(values);
	}
	
	// double
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValueExp doubleOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp doubleBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp doubleIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfAttributesExp doubleBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#double")
			DoubleValueExp ...values){
		return DoubleType.DOUBLE.bagOf(values);
	}
	
	// date
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateValueExp dateOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp dateBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp dateIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfAttributesExp dateBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#date")
			DateValueExp ...values){
		return DateType.DATE.bagOf(values);
	}
	
	// dateTime
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeValueExp dateTimeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp dateTimeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp dateTimeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfAttributesExp dateTimeBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dateTime")
			DateTimeValueExp ...values){
		return DateTimeType.DATETIME.bagOf(values);
	}
	
	// anyURI
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIValueExp anyURIOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp anyURIBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp anyURIIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfAttributesExp anyURIBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#anyURI")
			AnyURIValueExp ...values){
		return AnyURIType.ANYURI.bagOf(values);
	}
	
	// hexBinary
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
	public static HexBinaryValueExp hexBinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp hexBinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp hexBinaryIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")HexBinaryValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfAttributesExp hexBinaryBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
			HexBinaryValueExp ...values){
		return HexBinaryType.HEXBINARY.bagOf(values);
	}
	
	// base64Binary
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
	public static Base64BinaryValueExp base64BinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp base64BinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp base64IsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")Base64BinaryValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfAttributesExp base64BinaryBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
			Base64BinaryValueExp ...values){
		return Base64BinaryType.BASE64BINARY.bagOf(values);
	}
	
	// dayTimeDuration
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationValueExp dayTimeDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp dayTimeDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp dayTimeDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfAttributesExp dayTimeDurationBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
			DayTimeDurationValueExp ...values){
		return DayTimeDurationType.DAYTIMEDURATION.bagOf(values);
	}
	
	// yearMonthDuration
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationValueExp yearMonthDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
			BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp yearMonthDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
			BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp yearMonthDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationValueExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
			BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfAttributesExp yearMonthDurationBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
			YearMonthDurationValueExp ...values){
		return YearMonthDurationType.YEARMONTHDURATION.bagOf(values);
	}
	
	// x500Name
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameValueExp x500NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
			BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp x500NameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
			BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp x500NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameValueExp v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
			BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfAttributesExp x500NameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
			X500NameValueExp ...values){
		return X500NameType.X500NAME.bagOf(values);
	}
	
	// rfc822Name
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameValueExp rfc822NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
			BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp rfc822NamBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
			BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp rfc822NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameValueExp v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
			BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfAttributesExp rfc822NameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
			RFC822NameValueExp ...values){
		return RFC822NameType.RFC822NAME.bagOf(values);
	}
	
	// ipAddress
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressValueExp ipAddressOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
			BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp ipAddressBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
			BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp ipAddressIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressValueExp v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
			BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfAttributesExp ipAddressBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
			IPAddressValueExp ...values){
		return IPAddressType.IPADDRESS.bagOf(values);
	}
	
	// dnsName
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameValueExp dnsNameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
			BagOfAttributesExp bag) 
	{
		return oneAndOnlyImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp dnsNameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
			BagOfAttributesExp bag) 
	{
		return typeBagSizeImpl(bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp dnsNameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameValueExp v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
			BagOfAttributesExp bag) 
	{
		return containsImpl(v, bag);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
	public static BagOfAttributesExp dnsNameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
			DNSNameValueExp ...values){
		return DNSNameType.DNSNAME.bagOf(values);
	}
}
