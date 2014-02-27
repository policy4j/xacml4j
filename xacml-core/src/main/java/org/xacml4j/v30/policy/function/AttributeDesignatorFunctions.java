package org.xacml4j.v30.policy.function;

import java.util.List;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.FunctionReturnTypeResolver;
import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamAnyAttribute;
import org.xacml4j.v30.spi.function.XacmlFuncParamEvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncReturnTypeResolver;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.AnyURIType;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.StringExp;

@XacmlFunctionProvider(description="Attribute designator functions")
public class AttributeDesignatorFunctions 
{
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:map")
	@XacmlFuncReturnTypeResolver(resolverClass=ReturnTypeResolver.class)
	public static BagOfAttributeExp designator(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamAnyAttribute AttributeExp categoryOrEntity,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp attributeId,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp dataType,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", defaultValue="false")BooleanExp mustBePresent,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", optional=true)StringExp issuer)
	{
		return null;
	}
	
	public class ReturnTypeResolver implements FunctionReturnTypeResolver
	{
		@Override
		public ValueType resolve(FunctionSpec spec, List<Expression> arguments) {
			Expression type = arguments.get(2);
			AttributeExpType typeId = (AttributeExpType)type.getEvaluatesTo();
			if(typeId.equals(AnyURIType.ANYURI)){
				// hack to cast to AnyURI without evaluating first
				AttributeExpType resolvedType = spec.getTypes().getType(((AnyURIExp)type).getValue().toString());
				return resolvedType;
			}
			return typeId;
		}
	}
}
