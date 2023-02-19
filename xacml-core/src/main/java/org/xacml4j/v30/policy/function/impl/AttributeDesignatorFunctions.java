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

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.ValueTypeInfo;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionReturnTypeResolver;
import org.xacml4j.v30.policy.function.XacmlEvaluationContextParam;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncParamAnyAttribute;
import org.xacml4j.v30.policy.function.XacmlFuncParamOptional;
import org.xacml4j.v30.policy.function.XacmlFuncReturnTypeResolver;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.AnyURIValue;
import org.xacml4j.v30.types.BooleanValue;
import org.xacml4j.v30.types.EntityValue;
import org.xacml4j.v30.types.PathValue;
import org.xacml4j.v30.types.StringValue;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;

/**
 * XACML 3.0 regular attribute resolution functions
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="Attribute designator functions for XACML Entity type")
public final class AttributeDesignatorFunctions implements FunctionReturnTypeResolver
{
	private static final Logger LOG = LoggerFactory.getLogger(AttributeDesignatorFunctions.class);
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:attribute-designator")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfValues designator(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamAnyAttribute Value categoryOrEntity,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue attributeId,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue dataType,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#boolean", value={"false"}) BooleanValue mustBePresent,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue issuer)
	{
		Preconditions.checkArgument(categoryOrEntity.getType().equals(XacmlTypes.ENTITY) ||
				categoryOrEntity.getType().equals(XacmlTypes.ANYURI));
		LOG.debug("CategoryOrEntity=\"{}\" attributeId=\"{}\" " +
				          "dataType=\"{}\" mustBePresent=\"{}\" issuer=\"{}\"", categoryOrEntity, attributeId, dataType, mustBePresent, issuer);

		AttributeDesignatorKey.Builder designatorKeyBuilder =
				AttributeDesignatorKey
						.builder()
						.dataType(dataType)
						.attributeId(attributeId.value().toString());
		if(issuer != null){
			designatorKeyBuilder.issuer(issuer);
		}
		if(categoryOrEntity.getType().equals(XacmlTypes.ANYURI)){
			designatorKeyBuilder.category(categoryOrEntity).build();
		}
		AttributeDesignatorKey designatorKey = designatorKeyBuilder.build();
		Optional<BagOfValues> v = Optional.empty();
		if(categoryOrEntity.getType().equals(XacmlTypes.ENTITY)){
			v = ((EntityValue)categoryOrEntity).resolve(designatorKey);
		}
		if(categoryOrEntity.getType().equals(XacmlTypes.ANYURI)){
			v = context.resolve(designatorKey);
		}
		if(!v.isPresent()){
			if(mustBePresent.value()){
				throw AttributeReferenceEvaluationException
						.forDesignator(designatorKey,
								()->"Designator.MustBePresent=\"true\", but value is absent");
			}
		}
		return v.orElse(designatorKey
						.getDataType()
						.emptyBag());
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:attribute-selector")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfValues selector(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamAnyAttribute Value categoryOrEntity,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") PathValue xpath,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue dataType,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#boolean", value={"false"}) BooleanValue mustBePresent,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue issuer)
	{
		Preconditions.checkArgument(categoryOrEntity.getType().equals(XacmlTypes.ENTITY) ||
				categoryOrEntity.getType().equals(XacmlTypes.ANYURI));
		AttributeSelectorKey.Builder selectorKeyBuilder =
				AttributeSelectorKey
						.builder()
						.dataType(dataType)
						.path(xpath, true);
		if(categoryOrEntity.getType().equals(XacmlTypes.ANYURI)){
			selectorKeyBuilder
					.category(categoryOrEntity).build();
		}
		Optional<BagOfValues> v = Optional.empty();
		final AttributeSelectorKey selectorKey = selectorKeyBuilder.build();
		if(categoryOrEntity.getType().equals(XacmlTypes.ENTITY)){
			v = ((EntityValue)categoryOrEntity).resolve(selectorKey);

		}
		if(categoryOrEntity.getType().equals(XacmlTypes.ANYURI)){
			v = context.resolve(selectorKey);
		}
		if(!v.isPresent()){
			if(mustBePresent.value()){
				throw AttributeReferenceEvaluationException
						.forSelector(selectorKey, ()->"Selector.MustBePresent=\"true\", but value is absent");
			}
		}
		return v.orElse(selectorKey.getDataType().emptyBag());
	}

	private static ValueType getType(AnyURIValue typeUri){
		return XacmlTypes.getType(typeUri.value().toString())
		                 .orElseThrow(()->SyntaxException.invalidDataTypeId(typeUri.getType().getDataTypeId()));
	}
	
	@Override
	public ValueTypeInfo resolve(FunctionSpec spec, List<Expression> arguments) {
		return getType((AnyURIValue)arguments.get(2));
	}
}
