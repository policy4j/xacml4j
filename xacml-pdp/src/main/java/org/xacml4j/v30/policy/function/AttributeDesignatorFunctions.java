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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.xacml4j.v30.*;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.*;
import org.xacml4j.v30.types.*;

import java.util.List;

@XacmlFunctionProvider(description="Attribute designator functions for XACML Entity type")
public class AttributeDesignatorFunctions implements FunctionReturnTypeResolver
{
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:category-designator")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfAttributeExp designator(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamAnyAttribute AttributeExp categoryOrEntity,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp attributeId,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp dataType,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#boolean", value={"false"})BooleanExp mustBePresent,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp issuer)
	{
		Preconditions.checkArgument(categoryOrEntity.getType().equals(XacmlTypes.ENTITY) || 
				categoryOrEntity.getType().equals(XacmlTypes.ANYURI));
		AttributeExpType type = getType(dataType);
		if(categoryOrEntity.getType().equals(XacmlTypes.ENTITY)){
			Entity entity = ((EntityExp)categoryOrEntity).getValue();
			return entity.getAttributeValues(attributeId.getValue().toString(), type, 
					issuer != null?issuer.toString():null);
		}
		AnyURIExp anyURI = (AnyURIExp)categoryOrEntity; 
		return context.resolve(AttributeDesignatorKey
				.builder()
				.category(anyURI.getValue())
				.dataType(type)
				.issuer(issuer != null?issuer.toString():null)
				.attributeId(attributeId.getValue().toString())
				.build());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:category-selector")
	@XacmlFuncReturnTypeResolver(resolverClass=AttributeDesignatorFunctions.class)
	public static BagOfAttributeExp selector(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamAnyAttribute AttributeExp categoryOrEntity,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp attributeId,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp dataType,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#boolean", value={"false"})BooleanExp mustBePresent,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp issuer)
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
			throw new XacmlSyntaxException("Unknown XACML type attributeId=\"%s\"",
						typeId);
		}
		return resolvedType.get();
	}
	
	@Override
	public ValueType resolve(FunctionSpec spec, List<Expression> arguments) {
		return getType((AnyURIExp)arguments.get(2));
	}
}
