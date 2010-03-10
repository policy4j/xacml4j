package com.artagon.xacml.v3.policy.impl.function;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;

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
 
 * urn:oasis:names:tc:xacml:1.0:function:string-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:string-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:string-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:string-bag 
 * urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:boolean-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:boolean-bag 
 * urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only
 * urn:oasis:names:tc:xacml:1.0:function:integer-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:integer-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:integer-bag 
 * urn:oasis:names:tc:xacml:1.0:function:double-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:double-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:double-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:double-bag 
 * urn:oasis:names:tc:xacml:1.0:function:time-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:time-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:time-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:time-bag 
 * urn:oasis:names:tc:xacml:1.0:function:date-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:date-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:date-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:date-bag 
 * urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:dateTime-bag 
 * urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:anyURI-bag 
 * urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag 
 * urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag 
 * urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag 
 * urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag 
 * urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:x500Name-bag 
 * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only 
 * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size 
 * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in 
 * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag 
 * urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only 
 * urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size 
 * urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in 
 * urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag 
 * urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only 
 * urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size 
 * urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in 
 * urn:oasis:names:tc:xacml:2.0:function:dnsName-bag
 */
public class BagFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringValue oneAndOnly(
			@XacmlParam(type=DataTypes.STRING, isBag=true)BagOfAttributeValues<StringValue> bag) 
	{
		Preconditions.checkArgument(!bag.isEmpty(), "Bag is empty");
		Preconditions.checkArgument(!(bag.size() > 1), "Bag has more than one value");
		return bag.values().iterator().next();
	}
}
