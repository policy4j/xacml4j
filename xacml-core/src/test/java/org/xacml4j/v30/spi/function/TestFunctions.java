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

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Ignore;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;


@XacmlFunctionProvider(description="TestFunctions")
@Ignore
public final class TestFunctions
{
	/** Private constructor for the utility class */
	private TestFunctions() {}

	@XacmlFuncSpec(id="test1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp test1(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return BooleanExp.valueOf(a.equals(b));
	}

	@XacmlFuncSpec(id="test2")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp test2(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp bag)
	{
		return IntegerExp.valueOf(bag.size());
	}

	@XacmlFuncSpec(id="test3", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp and(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)IntegerExp ...values)
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerExp)e.evaluate(context)).getValue();

		}
		return IntegerExp.valueOf(v);
	}

	@XacmlFuncSpec(id="test4", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp and(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)Expression ...values)
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerExp)e.evaluate(context)).getValue();

		}
		return IntegerExp.valueOf(v);
	}

	@XacmlFuncSpec(id="test5")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfAttributeExp test5(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionSpec function,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp bag)
		throws EvaluationException
	{
		Collection<AttributeExp> attributes = new LinkedList<AttributeExp>();
		for(AttributeExp attr : bag.values()){
			AttributeExp v = function.invoke(context, attr);
			attributes.add(v);

		}
		return IntegerExp.bag().attributes(attributes).build();
	}

	@XacmlFuncSpec(id="test5VarArg")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static BooleanExp test5VarArg(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp value,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)BooleanExp ...values)
		throws EvaluationException
	{
		if(values != null){
			System.out.println(values.length);
		}
		return BooleanExp.valueOf(false);
	}

	@XacmlFuncSpec(id="test6VarArg")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static BooleanExp test6(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)BooleanExp ...values)
		throws EvaluationException
	{
		return BooleanExp.valueOf(false);
	}
}
