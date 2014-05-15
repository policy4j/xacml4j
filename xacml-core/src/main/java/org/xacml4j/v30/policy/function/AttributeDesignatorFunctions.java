package org.xacml4j.v30.policy.function;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.FunctionReturnTypeResolver;
import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamAnyAttribute;
import org.xacml4j.v30.spi.function.XacmlFuncParamEvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncReturnTypeResolver;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.EntityExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;

@XacmlFunctionProvider(description="Attribute designator functions")
public class AttributeDesignatorFunctions implements FunctionReturnTypeResolver
{
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:attribute-designator")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfAttributeExp designator(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamAnyAttribute AttributeExp categoryOrEntity,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp attributeId,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp dataType,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", defaultValue="false")BooleanExp mustBePresent,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", optional=true)StringExp issuer)
	{
		AttributeExpType type = getType(dataType);
		if(categoryOrEntity.getType().equals(XacmlTypes.ENTITY)){
			Entity entity = ((EntityExp)categoryOrEntity).getValue();
			return entity.getAttributeValues(attributeId.getValue().toString(), type, 
					issuer != null?issuer.toString():null);
		}
		AnyURIExp anyURI = (AnyURIExp)categoryOrEntity; 
		return context.resolve(AttributeDesignatorKey
				.builder()
				.category(Categories.parse(anyURI.getValue()))
				.dataType(type)
				.issuer(issuer != null?issuer.toString():null)
				.attributeId(attributeId.getValue().toString())
				.build());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:attribute-selector")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfAttributeExp selector(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamAnyAttribute AttributeExp categoryOrEntity,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp attributeId,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp dataType,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", defaultValue="false")BooleanExp mustBePresent,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", optional=true)StringExp issuer)
	{
		AttributeExpType type = getType(dataType);
		if(categoryOrEntity.getType().equals(XacmlTypes.ENTITY)){
			Entity entity = ((EntityExp)categoryOrEntity).getValue();
			return entity.getAttributeValues(attributeId.getValue().toString(), type, 
					issuer != null?issuer.toString():null);
		}
		AnyURIExp anyURI = (AnyURIExp)categoryOrEntity; 
		return context.resolve(AttributeDesignatorKey
				.builder()
				.category(Categories.parse(anyURI.getValue()))
				.dataType(type)
				.issuer(issuer != null?issuer.toString():null)
				.attributeId(attributeId.getValue().toString())
				.build());
	}
	
	private static AttributeExpType getType(AnyURIExp typeUri){
		AttributeExpType typeId = (AttributeExpType)typeUri.getEvaluatesTo();
		Optional<AttributeExpType> resolvedType = XacmlTypes.getType(typeUri.getValue().toString());
		if(!resolvedType.isPresent()){
			throw new XacmlSyntaxException("Unknown XACML type id=\"%s\"",
						typeId);
		}
		return resolvedType.get();
	}
	
	@Override
	public ValueType resolve(FunctionSpec spec, List<Expression> arguments) {
		return getType((AnyURIExp)arguments.get(2));
	}
}
