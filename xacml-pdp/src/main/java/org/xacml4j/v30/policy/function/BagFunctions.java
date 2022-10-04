package org.xacml4j.v30.policy.function;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamVarArg;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.AnyURIValue;
import org.xacml4j.v30.types.Base64BinaryValue;
import org.xacml4j.v30.types.BooleanValue;
import org.xacml4j.v30.types.DNSNameValue;
import org.xacml4j.v30.types.DateTimeValue;
import org.xacml4j.v30.types.DateValue;
import org.xacml4j.v30.types.DayTimeDurationValue;
import org.xacml4j.v30.types.DoubleValue;
import org.xacml4j.v30.types.EntityValue;
import org.xacml4j.v30.types.HexBinaryValue;
import org.xacml4j.v30.types.IPAddressValue;
import org.xacml4j.v30.types.IntegerValue;
import org.xacml4j.v30.types.RFC822NameValue;
import org.xacml4j.v30.types.StringValue;
import org.xacml4j.v30.types.TimeValue;
import org.xacml4j.v30.types.X500NameValue;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.types.YearMonthDurationValue;

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
public final class BagFunctions
{
	/** Private constructor for utility class */
	private BagFunctions() {}

	/**
	 * This function takes a bag of {@link Value} values as an
	 * argument and returns a value of {@link Value}.
	 * It returns the only value in the bag. If the bag does not have
	 * one and only one value, then this functions throws
	 * {@link IllegalArgumentException}
	 *
	 * @param <T> a bag value type
	 * @param bag a bag of values
	 * @return {@link Value} one and only one value
	 */
	static <T extends Value> T oneAndOnlyImpl(
			BagOfValues bag){
		Preconditions.checkArgument(!bag.isEmpty(), "Bag is empty");
		Preconditions.checkArgument(!(bag.size() > 1), "Bag has more than one value");
		return bag.value();
	}

	static IntegerValue typeBagSizeImpl(BagOfValues bag) {
		return XacmlTypes.INTEGER.of(bag.size());
	}

	static BooleanValue containsImpl(
			Value v,
			BagOfValues bag){

		return XacmlTypes.BOOLEAN.of(bag.contains(v));
	}

	static BooleanValue isEmpty(BagOfValues bag){

		return XacmlTypes.BOOLEAN.of(bag.isEmpty());
	}

	@XacmlFuncSpec(id="urn:xacml4j:names:tc:xacml:1.0:function:string-bag-is-empty")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue stringBagIsEmpty(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
					BagOfValues bag)
	{
		return isEmpty(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue stringBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue stringIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfValues stringBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#string")
			StringValue...values){
		return XacmlTypes.STRING
				.bag()
				.attribute(values)
				.build();
	}

	// boolean

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue booleanBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean") BooleanValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfValues booleanBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#boolean")
			BooleanValue...values){
		return XacmlTypes.BOOLEAN
				.bag()
				.attribute(values)
				.build();
	}

	// integer

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue integerOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue integerBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue integerIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfValues integerBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#integer")
			IntegerValue...values){
		return XacmlTypes.INTEGER
				.bag()
				.attribute(values)
				.build();
	}

	// time

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeValue timeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue timeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue timeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfValues timeBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#time")
			TimeValue...values){
		return XacmlTypes.TIME
				.bag()
				.attribute(values)
				.build();
	}

	// double

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue doubleOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue doubleBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue doubleIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfValues doubleBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#double")
			DoubleValue...values){
		return XacmlTypes.DOUBLE
				.bag()
				.attribute(values)
				.build();
	}

	// date

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateValue dateOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue dateBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfValues dateBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#date")
			DateValue...values){
		return XacmlTypes.DATE
				.bag()
				.attribute(values)
				.build();
	}

	// dateTime

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeValue dateTimeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue dateTimeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateTimeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfValues dateTimeBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dateTime")
			DateTimeValue...values){
		return XacmlTypes.DATETIME
				.bag()
				.attribute(values)
				.build();
	}

	// anyURI

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIValue anyURIOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue anyURIBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyURIIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfValues anyURIBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#anyURI")
			AnyURIValue...values){
		return XacmlTypes.ANYURI
				.bag()
				.attribute(values)
				.build();
	}

	// hexBinary

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
	public static HexBinaryValue hexBinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue hexBinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue hexBinaryIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary") HexBinaryValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfValues hexBinaryBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
			HexBinaryValue...values){
		return XacmlTypes.HEXBINARY
				.bag()
				.attribute(values)
				.build();
	}

	// base64Binary

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
	public static Base64BinaryValue base64BinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue base64BinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue base64IsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary") Base64BinaryValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfValues base64BinaryBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
			Base64BinaryValue...values){
		return XacmlTypes.BASE64BINARY
				.bag()
				.attribute(values)
				.build();
	}

	// dayTimeDuration

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationValue dayTimeDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue dayTimeDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dayTimeDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration") DayTimeDurationValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfValues dayTimeDurationBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
			DayTimeDurationValue...values){
		return XacmlTypes.DAYTIMEDURATION
				.bag()
				.attribute(values)
				.build();
	}

	// yearMonthDuration

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationValue yearMonthDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue yearMonthDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue yearMonthDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") YearMonthDurationValue v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfValues yearMonthDurationBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
			YearMonthDurationValue...values){
		return XacmlTypes.YEARMONTHDURATION
				.bag()
				.attribute(values)
				.build();
	}

	// x500Name

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameValue x500NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue x500NameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue x500NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500NameValue v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfValues x500NameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
			X500NameValue...values){
		return XacmlTypes.X500NAME
				.bag()
				.attribute(values)
				.build();
	}

	// rfc822Name

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameValue rfc822NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue rfc822NamBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue rfc822NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name") RFC822NameValue v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfValues rfc822NameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
			RFC822NameValue...values){
		return XacmlTypes.RFC822NAME
				.bag()
				.attribute(values)
				.build();
	}

	// ipAddress

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressValue ipAddressOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue ipAddressBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue ipAddressIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress") IPAddressValue v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfValues ipAddressBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
			IPAddressValue...values){
		return XacmlTypes.IPADDRESS
				.bag()
				.attribute(values)
				.build();
	}

	// dnsName

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameValue dnsNameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue dnsNameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dnsNameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName") DNSNameValue v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
	public static BagOfValues dnsNameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
			DNSNameValue...values){
		return XacmlTypes.DNSNAME
				.bag()
				.attribute(values)
				.build();
	}
	// entity

	@XacmlFuncSpec(id="urn:xacml4j:names:tc:xacml:1.0:function:entity-bag-is-empty")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue entityBagIsEmpty(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity", isBag=true)
					BagOfValues bag)
	{
		return isEmpty(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue entityOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue entityBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity", isBag=true)
	public static BagOfValues entityBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity")
			EntityValue...values){
		return XacmlTypes.ENTITY
				.bag()
				.attribute(values)
				.build();
	}
}
