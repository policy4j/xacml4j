package org.xacml4j.v30.policy.function.impl;

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

import com.google.common.base.Preconditions;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.policy.function.*;
import org.xacml4j.v30.types.*;

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

	static IntegerVal typeBagSizeImpl(BagOfValues bag) {
		return XacmlTypes.INTEGER.ofAny(bag.size());
	}

	static BooleanVal containsImpl(
			Value v,
			BagOfValues bag){

		return XacmlTypes.BOOLEAN.ofAny(bag.contains(v));
	}

	static BooleanVal isEmpty(BagOfValues bag){

		return XacmlTypes.BOOLEAN.ofAny(bag.isEmpty());
	}

	@XacmlFuncSpec(id="urn:xacml4j:names:tc:xacml:1.0:function:string-bag-is-empty", shortId = "string-bag-is-empty")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal stringBagIsEmpty(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
					BagOfValues bag)
	{
		return isEmpty(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only", shortId = "string-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringVal stringOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag-size", shortId = "string-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal stringBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-is-in", shortId = "string-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal stringIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag", shortId = "string-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfValues stringBag(
			@XacmlFuncParamVarArg(min=0, typeId="http://www.w3.org/2001/XMLSchema#string")
			StringVal...values){
		return XacmlTypes.STRING
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// boolean

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only", shortId = "boolean-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal booleanOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size", shortId = "boolean-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal booleanBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in", shortId = "boolean-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal booleanIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean") BooleanVal v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag", shortId = "boolean-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfValues booleanBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#boolean")
			BooleanVal...values){
		return XacmlTypes.BOOLEAN
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// integer

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only", shortId = "integer-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal integerOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size", shortId = "integer-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal integerBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-is-in", shortId = "integer-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal integerIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag", shortId = "integer-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfValues integerBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#integer")
			IntegerVal...values){
		return XacmlTypes.INTEGER
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// time

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only", shortId = "time-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static ISO8601Time timeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag-size", shortId = "time-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal timeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-is-in", shortId = "time-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal timeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag", shortId = "time-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfValues timeBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#time")
			ISO8601Time...values){
		return XacmlTypes.TIME
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// double

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only", shortId = "double-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleVal doubleOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal doubleBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal doubleIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfValues doubleBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#double")
			DoubleVal...values){
		return XacmlTypes.DOUBLE
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// date

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static ISO8601Date dateOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal dateBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal dateIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfValues dateBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#date")
			ISO8601Date...values){
		return XacmlTypes.DATE
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// dateTime

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static ISO8601DateTime dateTimeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal dateTimeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal dateTimeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfValues dateTimeBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dateTime")
			ISO8601DateTime...values){
		return XacmlTypes.DATETIME
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// anyURI

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURI anyURIOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal anyURIBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal anyURIIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURI v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfValues anyURIBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#anyURI")
			AnyURI...values){
		return XacmlTypes.ANYURI
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// hexBinary

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
	public static HexBinary hexBinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal hexBinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal hexBinaryIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary") HexBinary v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfValues hexBinaryBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
			HexBinary...values){
		return XacmlTypes.HEXBINARY
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// base64Binary

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
	public static Base64Binary base64BinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal base64BinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal base64IsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary") Base64Binary v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfValues base64BinaryBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
			Base64Binary...values){
		return XacmlTypes.BASE64BINARY
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// dayTimeDuration

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static ISO8601DayTimeDuration dayTimeDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal dayTimeDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal dayTimeDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration") ISO8601DayTimeDuration v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfValues dayTimeDurationBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
			ISO8601DayTimeDuration...values){
		return XacmlTypes.DAYTIMEDURATION
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// yearMonthDuration

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static ISO8601YearMonthDuration yearMonthDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal yearMonthDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal yearMonthDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") ISO8601YearMonthDuration v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfValues yearMonthDurationBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
			ISO8601YearMonthDuration...values){
		return XacmlTypes.YEARMONTHDURATION
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// x500Name

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500Name x500NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal x500NameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal x500NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfValues x500NameBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
			X500Name...values){
		return XacmlTypes.X500NAME
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// rfc822Name

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822Name rfc822NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal rfc822NamBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal rfc822NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name") RFC822Name v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfValues rfc822NameBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
			RFC822Name...values){
		return XacmlTypes.RFC822NAME
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// ipAddress

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddress ipAddressOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal ipAddressBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal ipAddressIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress") IPAddress v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfValues ipAddressBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
			IPAddress...values){
		return XacmlTypes.IPADDRESS
				.bagBuilder()
				.attribute(values)
				.build();
	}

	// dnsName

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSName dnsNameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal dnsNameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
					BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal dnsNameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName") DNSName v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
					BagOfValues bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
	public static BagOfValues dnsNameBag(
			@XacmlFuncParamVarArg(min=0, max= java.lang.Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
			DNSName...values){
		return XacmlTypes.DNSNAME
				.bagBuilder()
				.attribute(values)
				.build();
	}
	// entity

	@XacmlFuncSpec(id="urn:xacml4j:names:tc:xacml:1.0:function:entity-bag-is-empty")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal entityBagIsEmpty(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity", isBag=true)
					BagOfValues bag)
	{
		return isEmpty(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringVal entityOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity", isBag=true)
					BagOfValues bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal entityBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity", isBag=true) BagOfValues bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity", isBag=true)
	public static BagOfValues entityBag(
			@XacmlFuncParamVarArg(min=0, typeId="urn:oasis:names:tc:xacml:3.0:data-type:entity")
			Entity...values){
		return XacmlTypes.ENTITY
				.bagBuilder()
				.attribute(values)
				.build();
	}
}
