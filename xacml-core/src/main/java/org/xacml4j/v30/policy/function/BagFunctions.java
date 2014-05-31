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

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamVarArg;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.Base64BinaryExp;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.DNSNameExp;
import org.xacml4j.v30.types.DateExp;
import org.xacml4j.v30.types.DateTimeExp;
import org.xacml4j.v30.types.DayTimeDurationExp;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.EntityExp;
import org.xacml4j.v30.types.HexBinaryExp;
import org.xacml4j.v30.types.IPAddressExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.RFC822NameExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.TimeExp;
import org.xacml4j.v30.types.X500NameExp;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.types.YearMonthDurationExp;

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
	/** Private constructor for utility class */
	private BagFunctions() {}

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
			BagOfAttributeExp bag){
		Preconditions.checkArgument(!bag.isEmpty(), "Bag is empty");
		Preconditions.checkArgument(!(bag.size() > 1), "Bag has more than one value");
		return bag.value();
	}

	static IntegerExp typeBagSizeImpl(BagOfAttributeExp bag) {
		return IntegerExp.valueOf(bag.size());
	}

	static BooleanExp containsImpl(AttributeExp v,
			BagOfAttributeExp bag){

		return BooleanExp.valueOf(bag.contains(v));
	}

	static BooleanExp isEmpty(BagOfAttributeExp bag){

		return BooleanExp.valueOf(bag.isEmpty());
	}

	@XacmlFuncSpec(id="urn:artagon:names:tc:xacml:1.0:function:string-bag-is-empty")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp stringBagIsEmpty(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
			BagOfAttributeExp bag)
	{
		return isEmpty(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
			BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp stringBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp stringIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfAttributeExp stringBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#string")
			StringExp ...values){
		return XacmlTypes.STRING.bagOf(values);
	}

	// boolean

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp booleanOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp booleanBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp booleanIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfAttributeExp booleanBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#boolean")
			BooleanExp ...values){
		return XacmlTypes.BOOLEAN.bagOf(values);
	}

	// integer

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp integerOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp integerBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp integerIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfAttributeExp integerBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#integer")
			IntegerExp ...values){
		return XacmlTypes.INTEGER.bagOf(values);
	}

	// time

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeExp timeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp timeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp timeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfAttributeExp timeBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#time")
			TimeExp ...values){
		return XacmlTypes.TIME.bagOf(values);
	}

	// double

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp doubleOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp doubleBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp doubleIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfAttributeExp doubleBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#double")
			DoubleExp ...values){
		return XacmlTypes.DOUBLE.bagOf(values);
	}

	// date

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateExp dateOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp dateBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dateIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfAttributeExp dateBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#date")
			DateExp ...values){
		return XacmlTypes.DATE.bagOf(values);
	}

	// dateTime

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeExp dateTimeOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp dateTimeBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dateTimeIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfAttributeExp dateTimeBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dateTime")
			DateTimeExp ...values){
		return XacmlTypes.DATETIME.bagOf(values);
	}

	// anyURI

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIExp anyURIOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp anyURIBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp anyURIIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfAttributeExp anyURIBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#anyURI")
			AnyURIExp ...values){
		return XacmlTypes.ANYURI.bagOf(values);
	}

	// hexBinary

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
	public static HexBinaryExp hexBinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp hexBinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp hexBinaryIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary")HexBinaryExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfAttributeExp hexBinaryBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#hexBinary")
			HexBinaryExp ...values){
		return XacmlTypes.HEXBINARY.bagOf(values);
	}

	// base64Binary

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
	public static Base64BinaryExp base64BinaryOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp base64BinaryBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp base64IsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary")Base64BinaryExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfAttributeExp base64BinaryBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#base64Binary")
			Base64BinaryExp ...values){
		return XacmlTypes.BASE64BINARY.bagOf(values);
	}

	// dayTimeDuration

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationExp dayTimeDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp dayTimeDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dayTimeDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfAttributeExp dayTimeDurationBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
			DayTimeDurationExp ...values){
		return XacmlTypes.DAYTIMEDURATION.bagOf(values);
	}

	// yearMonthDuration

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationExp yearMonthDurationOneAndOnly(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
			BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp yearMonthDurationBagSize(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
			BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp yearMonthDurationIsIn(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp v,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
			BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfAttributeExp yearMonthDurationBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
			YearMonthDurationExp ...values){
		return XacmlTypes.YEARMONTHDURATION.bagOf(values);
	}

	// x500Name

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameExp x500NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
			BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp x500NameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
			BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp x500NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
			BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfAttributeExp x500NameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
			X500NameExp ...values){
		return XacmlTypes.X500NAME.bagOf(values);
	}

	// rfc822Name

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameExp rfc822NameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
			BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp rfc822NamBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
			BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp rfc822NameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameExp v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
			BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfAttributeExp rfc822NameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
			RFC822NameExp ...values){
		return XacmlTypes.RFC822NAME.bagOf(values);
	}

	// ipAddress

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressExp ipAddressOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
			BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp ipAddressBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
			BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp ipAddressIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressExp v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
			BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfAttributeExp ipAddressBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
			IPAddressExp ...values){
		return XacmlTypes.IPADDRESS.bagOf(values);
	}

	// dnsName

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameExp dnsNameOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
			BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp dnsNameBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
			BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dnsNameIsIn(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameExp v,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
			BagOfAttributeExp bag)
	{
		return containsImpl(v, bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName", isBag=true)
	public static BagOfAttributeExp dnsNameBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
			DNSNameExp ...values){
		return XacmlTypes.DNSNAME.bagOf(values);
	}
	// entity

	@XacmlFuncSpec(id="urn:artagon:names:tc:xacml:1.0:function:entity-bag-is-empty")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp entityBagIsEmpty(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data type:entity", isBag=true)
			BagOfAttributeExp bag)
	{
		return isEmpty(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-one-and-only")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp entityOneAndOnly(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data type:entity", isBag=true)
			BagOfAttributeExp bag)
	{
		return oneAndOnlyImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-bag-size")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp entityBagSize(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data type:entity", isBag=true)BagOfAttributeExp bag)
	{
		return typeBagSizeImpl(bag);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-bag")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:3.0:data type:entity", isBag=true)
	public static BagOfAttributeExp entityBag(
			@XacmlFuncParamVarArg(min=0, max=Integer.MAX_VALUE, typeId="urn:oasis:names:tc:xacml:3.0:data type:entity")
			EntityExp ...values){
		return EntityExp.bag().attribute(values).build();
	}
}
