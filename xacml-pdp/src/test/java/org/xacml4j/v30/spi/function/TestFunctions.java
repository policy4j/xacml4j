package org.xacml4j.v30.spi.function;

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

import org.junit.Ignore;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.types.BooleanValue;
import org.xacml4j.v30.types.IntegerValue;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Collection;
import java.util.LinkedList;


@XacmlFunctionProvider(description="TestFunctions")
@Ignore
public final class TestFunctions
{
	/** Private constructor for the utility class */
	private TestFunctions() {}

	@XacmlFuncSpec(id="test1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue test1(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="test2")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue test2(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues bag)
	{
		return XacmlTypes.INTEGER.of(bag.size());
	}

	@XacmlFuncSpec(id="test3", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue and(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2) IntegerValue...values)
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerValue)e.evaluate(context)).value();

		}
		return XacmlTypes.INTEGER.of(v);
	}

	@XacmlFuncSpec(id="test4", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue and(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)Expression ...values)
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerValue)e.evaluate(context)).value();

		}
		return XacmlTypes.INTEGER.of(v);
	}

	@XacmlFuncSpec(id="test5")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfValues test5(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionSpec function,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues bag)
		throws EvaluationException
	{
		Collection<Value> attributes = new LinkedList<Value>();
		for(Value attr : bag.values()){
			Value v = function.invoke(context, attr);
			attributes.add(v);

		}
		return XacmlTypes.INTEGER.bag()
				.attributes(attributes)
				.build();
	}

	@XacmlFuncSpec(id="test5VarArg")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static BooleanValue test5VarArg(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue value,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0) BooleanValue...values)
		throws EvaluationException
	{
		if(values != null){
			System.out.println(values.length);
		}
		return XacmlTypes.BOOLEAN.of(false);
	}

	@XacmlFuncSpec(id="test6VarArg")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static BooleanValue test6(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue b,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0) BooleanValue...values)
		throws EvaluationException
	{
		return XacmlTypes.BOOLEAN.of(false);
	}
}
