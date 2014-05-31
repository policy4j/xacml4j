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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.pdp.FunctionReference;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.FunctionParametersValidator;
import org.xacml4j.v30.spi.function.FunctionReturnTypeResolver;
import org.xacml4j.v30.spi.function.XacmlFuncParamAnyAttribute;
import org.xacml4j.v30.spi.function.XacmlFuncParamAnyBag;
import org.xacml4j.v30.spi.function.XacmlFuncParamEvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncParamFunctionReference;
import org.xacml4j.v30.spi.function.XacmlFuncParamValidator;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncReturnTypeResolver;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.BooleanExp;

import com.google.common.base.Preconditions;

@XacmlFunctionProvider(description="XACML higher order functions")
public class HigherOrderFunctions
{
	/** Private constructor for utility class */
	private HigherOrderFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:any-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp anyOf(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyAttribute AttributeExp value,
			@XacmlFuncParamAnyBag BagOfAttributeExp bag)
		throws EvaluationException
	{
		for(AttributeExp valueFromBag : bag.values()){
			BooleanExp r = ref.invoke(context, value, valueFromBag);
			if(r.getValue()){
				return BooleanExp.TRUE;
			}
		}
		return BooleanExp.FALSE;
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp allOf(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyAttribute AttributeExp value,
			@XacmlFuncParamAnyBag BagOfAttributeExp bag)
		throws EvaluationException
	{
		for(AttributeExp valueFromBag : bag.values()){
			BooleanExp r = ref.invoke(context, value, valueFromBag);
			if(!r.getValue()){
				return BooleanExp.FALSE;
			}
		}
		return BooleanExp.TRUE;
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:any-of-any")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp anyOfAny(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfAttributeExp a,
			@XacmlFuncParamAnyBag BagOfAttributeExp b)
		throws EvaluationException
	{
		for(AttributeExp aValue : a.values()){
			for(AttributeExp bValue : b.values()){
				BooleanExp r = ref.invoke(context, aValue, bValue);
				if(r.getValue()){
					return BooleanExp.TRUE;
				}
			}
		}
		return BooleanExp.FALSE;
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of-any")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp allOfAny(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfAttributeExp a,
			@XacmlFuncParamAnyBag BagOfAttributeExp b)
		throws EvaluationException
	{
		boolean result = true;
		for(AttributeExp v : a.values()){
			result  &= anyOf(context, ref, v, b).getValue();
			if(!result){
				break;
			}
		}
		return BooleanExp.create(result);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:any-of-all")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp anyOfAll(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfAttributeExp a,
			@XacmlFuncParamAnyBag BagOfAttributeExp b)
		throws EvaluationException
	{
		for(AttributeExp va : a.values())
		{
			boolean result = allOf(context, ref, va, b).getValue();
			if(result){
				return BooleanExp.TRUE;
			}
		}
		return BooleanExp.FALSE;
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of-all")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp allOfAll(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfAttributeExp a,
			@XacmlFuncParamAnyBag BagOfAttributeExp b)
		throws EvaluationException
	{
		for(AttributeExp aValue : a.values())
		{
			for(AttributeExp bValue : b.values()){
				BooleanExp r = ref.invoke(context, aValue, bValue);
				if(!r.getValue()){
					return BooleanExp.FALSE;
				}
			}
		}
		return BooleanExp.TRUE;
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:map")
	@XacmlFuncReturnTypeResolver(resolverClass=MapFunctionResolverValidator.class)
	@XacmlFuncParamValidator(validatorClass=MapFunctionResolverValidator.class)
	public static  BagOfAttributeExp map(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfAttributeExp bag)
		throws EvaluationException
	{
		Collection<AttributeExp> values = new ArrayList<AttributeExp>(bag.size());
		for(AttributeExp v : bag.values()){
			values.add((AttributeExp)ref.invoke(context, v));
		}
		AttributeExpType type = (AttributeExpType)ref.getEvaluatesTo();
		return type.bagType().create(values);
	}

	/**
	 * An implementation of {@link FunctionReturnTypeResolver} and
	 * {@link FunctionParametersValidator} for XACML map higher
	 * order function
	 *
	 * @author Giedrius Trumpickas
	 */
	public static class MapFunctionResolverValidator implements
		FunctionParametersValidator, FunctionReturnTypeResolver
	{
		private final static Logger log = LoggerFactory.getLogger(MapFunctionResolverValidator.class);

		public MapFunctionResolverValidator(){
		}

		@Override
		public ValueType resolve(FunctionSpec spec,
				List<Expression> arguments)
		{
			Preconditions.checkArgument(arguments != null,
					"Can't resolve function=\"%s\" return type " +
					"dynamically, arguments must be specified", spec.getId());
			Preconditions.checkArgument(arguments.size() == spec.getNumberOfParams(),
					"Can't resolve function=\"%s\" return type " +
					"dynamically, function requires 2 parameters to be specified", spec.getId());
			Expression ref = arguments.get(0);
			Preconditions.checkArgument(ref instanceof FunctionReference,
					"First function argument must be function reference");
			AttributeExpType type = (AttributeExpType)ref.getEvaluatesTo();
			return type.bagType();
		}

		@Override
		public boolean validate(FunctionSpec spec, List<Expression> arguments)
		{
			if(log.isDebugEnabled()){
				log.debug("Validating function=\"{}\" parameters", spec.getId());
			}
			if(arguments == null ||
					arguments.size() != 2){
				return false;
			}
			Expression exp = arguments.get(0);
			if(!(exp instanceof FunctionReference)){
				return false;
			}
			FunctionReference ref = (FunctionReference)exp;
			if(ref.getNumberOfParams() != 1){
				return false;
			}
			return true;
		}
	}
}
