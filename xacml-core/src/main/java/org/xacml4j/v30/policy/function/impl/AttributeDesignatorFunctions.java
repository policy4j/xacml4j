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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.*;
import org.xacml4j.v30.types.*;

import java.util.List;
import java.util.Optional;

/**
 * XACML 3.0 regular attribute resolution functions
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="Attribute designator functions for XACML Entity type")
public final class AttributeDesignatorFunctions implements FunctionReturnTypeResolver
{
	private static final Logger LOG = LoggerFactory.getLogger(AttributeDesignatorFunctions.class);
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:attribute-designator", shortId = "attribute-designator")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfValues designator(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamAnyValue Value categoryOrEntity,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURI attributeId,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURI dataType,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#boolean", value={"false"}) BooleanVal mustBePresent,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal issuer)
	{
		Preconditions.checkArgument(categoryOrEntity.getEvaluatesTo().equals(XacmlTypes.ENTITY) ||
				categoryOrEntity.getEvaluatesTo().equals(XacmlTypes.ANYURI));
		LOG.debug("CategoryOrEntity=\"{}\" attributeId=\"{}\" " +
				          "dataType=\"{}\" mustBePresent=\"{}\" issuer=\"{}\"", categoryOrEntity, attributeId, dataType, mustBePresent, issuer);
		ValueType valueType = XacmlTypes.getType(dataType).orElseThrow(()->SyntaxException.invalidDataTypeId(dataType));
		Entity entity = categoryOrEntity.getEvaluatesTo().equals(XacmlTypes.ENTITY)?(Entity)
				categoryOrEntity:context.resolveEntity( CategoryId.of(categoryOrEntity.get().toString())).orElse(null);
		LOG.debug("Resolved entity={}", entity);
		if(entity == null){
			if(mustBePresent.get()){
				String missingAttributeId = attributeId.toXacmlString();
				throw new EvaluationException(Status.missingAttribute(missingAttributeId)
						.build(), missingAttributeId);
			}
			return valueType.emptyBag();
		}
		String issuerString = Optional.ofNullable(issuer)
				.map(StringVal::get)
				.orElse(null);
		Optional<BagOfValues> v = entity.resolve(attributeId.toXacmlString(), valueType, issuerString);
		if(!v.isPresent()){
			if(mustBePresent.get()){
				String missingAttributeId = attributeId.toXacmlString();
				throw new EvaluationException(Status.missingAttribute(missingAttributeId)
						.build(), missingAttributeId);
			}
		}
		return v.orElse(valueType.emptyBag());
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:attribute-selector", shortId = "attribute-selector")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfValues selector(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamAnyValue Value categoryOrEntity,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") Path xpath,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURI dataType,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#boolean", value={"false"}) BooleanVal mustBePresent,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal issuer)
	{
		Preconditions.checkArgument(categoryOrEntity.getEvaluatesTo().equals(XacmlTypes.ENTITY) ||
				categoryOrEntity.getEvaluatesTo().equals(XacmlTypes.ANYURI));
		AttributeSelectorKey.Builder selectorKeyBuilder =
				AttributeSelectorKey
						.builder()
						.dataType(dataType)
						.path(xpath, true);
		if(categoryOrEntity.getEvaluatesTo().equals(XacmlTypes.ANYURI)){
			selectorKeyBuilder
					.category(categoryOrEntity).build();
		}
		Optional<BagOfValues> v = Optional.empty();
		final AttributeSelectorKey selectorKey = selectorKeyBuilder.build();
		if(categoryOrEntity.getEvaluatesTo().equals(XacmlTypes.ENTITY)){
			v = ((Entity)categoryOrEntity).resolve(selectorKey);

		}
		if(categoryOrEntity.getEvaluatesTo().equals(XacmlTypes.ANYURI)){
			v = context.resolve(selectorKey);
		}
		if(!v.isPresent()){
			if(mustBePresent.get()){
				throw AttributeReferenceEvaluationException
						.forSelector(selectorKey, ()->"Selector.MustBePresent=\"true\", but value is absent");
			}
		}
		return v.orElse(selectorKey.getDataType().emptyBag());
	}

	private static ValueType getType(AnyURI typeUri){
		return XacmlTypes.getType(typeUri.get().toString())
		                 .orElseThrow(()->SyntaxException.invalidDataTypeId(typeUri.getEvaluatesTo().getTypeId()));
	}
	
	/**
	 * Both functions return a bag of the data type that the third argument
	 * names. The type is known before the evaluation only when that
	 * argument is a literal.
	 */
	@Override
	public ValueExpTypeInfo resolve(FunctionSpec spec, List<Expression> arguments) {
		return getType((AnyURI)arguments.get(2)).bagType();
	}
}
