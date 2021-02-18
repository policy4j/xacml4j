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

import com.google.common.base.Preconditions;
import org.xacml4j.v30.*;
import org.xacml4j.v30.pdp.AttributeReferenceEvaluationException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.*;
import org.xacml4j.v30.types.*;

import java.util.List;
import java.util.Optional;

/**
 * XACML 3.0 regular attribute resolution functions
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="Attribute designator functions for XACML Entity type")
final class AttributeDesignatorFunctions implements FunctionReturnTypeResolver
{
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:attribute-designator")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfAttributeValues designator(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamAnyAttribute AttributeValue categoryOrEntity,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue attributeId,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue dataType,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#boolean", value={"false"}) BooleanValue mustBePresent,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue issuer)
	{
		Preconditions.checkArgument(categoryOrEntity.getType().equals(XacmlTypes.ENTITY) ||
				categoryOrEntity.getType().equals(XacmlTypes.ANYURI));
		AttributeDesignatorKey.Builder designatorKeyBuilder =
				AttributeDesignatorKey
						.builder()
						.issuer(issuer)
						.dataType(dataType)
						.attributeId(attributeId);
		if(categoryOrEntity.getType().equals(XacmlTypes.ANYURI)){
			designatorKeyBuilder.category(categoryOrEntity).build();
		}
		AttributeDesignatorKey designatorKey = designatorKeyBuilder.build();
		Optional<BagOfAttributeValues> v = Optional.empty();
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
		return v.orElse(
				designatorKey
						.getDataType()
						.emptyBag());
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:attribute-selector")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfAttributeValues selector(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamAnyAttribute AttributeValue categoryOrEntity,
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
		Optional<BagOfAttributeValues> v = Optional.empty();
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

	private static AttributeValueType getType(AnyURIValue typeUri){
		AttributeValueType typeId = typeUri.getEvaluatesTo();
		Optional<AttributeValueType> resolvedType = XacmlTypes.getType(typeUri.value().toString());
		if(!resolvedType.isPresent()){
			throw new SyntaxException("Unknown XACML typeId=\"%s\"",
						typeId);
		}
		return resolvedType.get();
	}
	
	@Override
	public ValueType resolve(FunctionSpec spec, List<Expression> arguments) {
		return getType((AnyURIValue)arguments.get(2));
	}
}
