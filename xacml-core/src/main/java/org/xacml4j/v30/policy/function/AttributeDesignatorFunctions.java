package org.xacml4j.v30.policy.function;

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
import org.xacml4j.v30.spi.function.XacmlFuncParamOptional;
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
				.category(anyURI.getValue())
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
