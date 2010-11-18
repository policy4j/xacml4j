package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.sdk.function.XacmlFuncParam;
import com.artagon.xacml.v3.sdk.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.sdk.function.XacmlFuncSpec;
import com.artagon.xacml.v3.sdk.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanValue;

@XacmlFunctionProvider(description="XACML Set Functions")
public class SetFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfAttributeValues integerIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfAttributeValues integerUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue integerSubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue integerAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue integerSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfAttributeValues booleanIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfAttributeValues booleanUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanSubset(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfAttributeValues stringIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfAttributeValues stringUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue stringSubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue stringAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue stringSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfAttributeValues doubleIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfAttributeValues doubleUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue doubleSubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue doubleAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue doubleSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfAttributeValues dateIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfAttributeValues dateUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateSubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfAttributeValues timeIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfAttributeValues timeUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue timeSubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue timeAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue timeSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfAttributeValues dateTimeIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfAttributeValues dateTimeUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateTimeSubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateTimeAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateTimeSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfAttributeValues anyURIIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfAttributeValues anyURITimeUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyURISubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyURIAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyURISetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfAttributeValues x500NameIntersection(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-union")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfAttributeValues x500NameUnion(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue x500NameSubest(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue x500NameAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue x500NameSetEquals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfAttributeValues rfc822NameIntersection(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfAttributeValues rfc822NameUnion(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue rfc822NameSubest(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue rfc822NameAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue rfc822NameSetEquals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfAttributeValues hexBinaryIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfAttributeValues hexBinaryUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue hexBinarySubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue hexBinaryAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue hexBinarySetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfAttributeValues base64BinaryIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfAttributeValues base64BinaryUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue base64BinarySubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue base64BinaryAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue base64BinarySetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfAttributeValues dayTimeDurationIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfAttributeValues dayTimeDurationUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dayTimeDurationSubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dayTimeDurationAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dayTimeDurationSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfAttributeValues yearMonthDurationIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfAttributeValues yearMonthDurationUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue yearMonthDurationSubest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue yearMonthDurationAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue yearMonthDurationSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-intersection")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfAttributeValues ipAddressIntersection(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-union")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfAttributeValues ipAddressDurationUnion(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues b) 
	{
		return a.union(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue ipAddressDurationSubest(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue ipAddressAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(b.containsAtLeastOneOf(a));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue ipAddressSetEquals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeValues b) 
	{
		return BooleanType.BOOLEAN.create(a.containsAll(b) && b.containsAll(a));
	}
}
