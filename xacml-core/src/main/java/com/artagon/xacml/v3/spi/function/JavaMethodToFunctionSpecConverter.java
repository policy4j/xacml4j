package com.artagon.xacml.v3.spi.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionParametersValidator;
import com.artagon.xacml.v3.FunctionReturnTypeResolver;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.FunctionSpecBuilder;
import com.google.common.base.Preconditions;

class JavaMethodToFunctionSpecConverter 
{
	private final static Logger log = LoggerFactory.getLogger(JavaMethodToFunctionSpecConverter.class);
	
	public FunctionSpec createFunctionSpec(Method m)
	{
		Preconditions.checkArgument(m != null, "Method can not be null");
		Preconditions.checkArgument(!m.getReturnType().equals(Void.TYPE), 
				"Method=\"%s\" must have other then void return type", m.getName());
		Preconditions.checkArgument(Expression.class.isAssignableFrom(m.getReturnType()), 
				"Method=\"%s\" must return XACML expression", m.getName());
		XacmlFunc funcId = m.getAnnotation(XacmlFunc.class);
		Preconditions.checkArgument(funcId != null, 
				"Method=\"%s\" must be annotated via XacmlFunc annotation", m.getName());
		
		Preconditions.checkArgument(Modifier.isStatic(m.getModifiers()), 
				"Only static method can be annotiated via XacmlFunc annotiation, method=\"%s\" " +
							"in the declaring class=\"%s\" is not static", m.getName(), 
							m.getDeclaringClass());
		XacmlLegacyFunc legacyFuncId = m.getAnnotation(XacmlLegacyFunc.class);
		XacmlFuncReturnType returnType = m.getAnnotation(XacmlFuncReturnType.class);
		XacmlFuncReturnTypeResolver returnTypeResolver = m.getAnnotation(XacmlFuncReturnTypeResolver.class);
		XacmlFuncParamValidator validator = m.getAnnotation(XacmlFuncParamValidator.class);
		if(!(returnType == null ^ 
				returnTypeResolver == null)){
			throw new IllegalArgumentException(
					"Either \"XacmlFuncReturnTypeResolver\" or " +
					"\"XacmlFuncReturnType\" annotiation must be specified, not both");
		}
		FunctionSpecBuilder b = new FunctionSpecBuilder(funcId.id(), (legacyFuncId == null)?null:legacyFuncId.id());
		Annotation[][] params = m.getParameterAnnotations();
		Class<?>[] types = m.getParameterTypes();
		boolean evalContextParamFound = false;
		for(int  i = 0; i < params.length; i++)
		{
			if(params[i] == null){
				throw new IllegalArgumentException(
						String.format("Method=\"%s\" contains parameter without annotation", m.getName()));
			}
			if(params[i][0] instanceof XacmlParamEvaluationContext){
				if(!types[i].isInstance(EvaluationContext.class)){
					new IllegalArgumentException(
							String.format("XACML evaluation context " +
									"annotation annotates wrong parameter type"));
				}
				if(i > 0){
					new IllegalArgumentException(
							String.format(
									"XACML evaluation context parameter must " +
									"be a first parameter " +
									"in the method=\"%s\" signature", m.getName()));
				}
				evalContextParamFound = true;
				continue;
			}
			if(params[i][0] instanceof XacmlParam){
				XacmlParam param = (XacmlParam)params[i][0];
				AttributeValueType type = param.type().getType();
				if(param.isBag() && 
						!Expression.class.isAssignableFrom(types[i])){
					log.debug("Excpecting bag at index=\"{}\", actual type type=\"{}\"", i, types[i].getName());
					throw new IllegalArgumentException(String.format(
							"Parameter type annotates bag of=\"%s\" " +
							"but method=\"%s\" is of class=\"%s\"", 
							type, m.getName(), types[i]));
				}
				if(!param.isBag() && 
						!Expression.class.isAssignableFrom(types[i])){
					log.debug("Excpecting attribute value at index=\"{}\", " +
							"actual type type=\"{}\"", i, types[i].getName());
					throw new IllegalArgumentException(String.format(
							"Parameter type annotates attribute value of " +
							"type=\"%s\" but method=\"%s\" parameter is type of=\"%s\"", 
							type, m.getName(), types[i]));
				}
				b.withParam(param.isBag()?type.bagOf():type);
				continue;
			}
			if(params[i][0] instanceof XacmlParamVarArg){
				if(!m.isVarArgs()){
					throw new IllegalArgumentException(String.format("Found varArg parameter " +
							"declaration but actual method=\"%s\" is not varArg", m.getName()));
				}
				if(m.isVarArgs() && 
						i < params.length - 1){
					throw new IllegalArgumentException(String.format("Found varArg parameter " +
							"declaration in incorect place, " +
							"varArg parameter must be a last parameter in the method"));
				}
				XacmlParamVarArg param = (XacmlParamVarArg)params[i][0];
				AttributeValueType type = param.type().getType();
				b.withParam(param.isBag()?type.bagOf():type, param.min(), param.max());
				continue;
			}
			if(params[i][0] instanceof XacmlParamFuncReference){
				b.withParamFunctionReference();
				continue;
			}
			if(params[i][0] instanceof XacmlParamAnyBag){
				b.withParamAnyBag();
				continue;
			}
			if(params[i][0] instanceof XacmlParamAnyAttribute){
				b.withParamAnyAttribute();
				continue;
			}
			if(params[i][0] == null){
				throw new IllegalArgumentException(String.format(
						"Found method=\"%s\" parameter at " +
						"index=\"%s\" with no annotation", m.getName(), i));
			}
			throw new IllegalArgumentException(String.format(
						"Found method=\"%s\" parameter at " +
						"index=\"%s\" with unknown annotation=\"%s\"", m.getName(), i, params[i][0]));
		}
		if(returnType != null){
			AttributeValueType type = returnType.type().getType();
			return b.build(
					returnType.isBag()?type.bagOf():type, 
					(validator != null)?createValidator(validator.validatorClass()):null,
					new DefaultFunctionInvocation(m, evalContextParamFound));
		}
		if(returnTypeResolver != null){
			return b.build(
					createResolver(returnTypeResolver.resolverClass()), 
					(validator != null)?createValidator(validator.validatorClass()):null,
					new DefaultFunctionInvocation(m, evalContextParamFound));
		}
		throw new IllegalArgumentException("Either static return type or return type resolver must be specified");
	}
	
	
	private FunctionReturnTypeResolver createResolver(Class<? extends FunctionReturnTypeResolver> clazz)
	{
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					String.format(
							"Failed with error=\"%s\" to create instance of " +
							"function return type resolver, class=\"%s\"", e.getMessage(), clazz.getName()));
		} 
	}
	
	private FunctionParametersValidator createValidator(Class<? extends FunctionParametersValidator> clazz)
	{
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					String.format(
							"Failed with error=\"%s\" to create instance of " +
							"function parameter validator, class=\"%s\"", e.getMessage(), clazz.getName()));
		} 
	}
}
