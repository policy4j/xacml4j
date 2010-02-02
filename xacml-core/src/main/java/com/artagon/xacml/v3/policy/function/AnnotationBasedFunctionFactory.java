package com.artagon.xacml.v3.policy.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.DefaultFunctionSpecBuilder;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamEvaluationContext;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamFuncReference;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamVarArg;


public class AnnotationBasedFunctionFactory extends BaseFunctionFacatory
{
	private final static Logger log = LoggerFactory.getLogger(AnnotationBasedFunctionFactory.class);
	
	public AnnotationBasedFunctionFactory(Class<?> factoryClass)
	{
		List<FunctionSpec> functions = findFunctions(factoryClass);
		for(FunctionSpec spec : functions){
			add(spec);
		}
	}
	private List<FunctionSpec> findFunctions(Class<?> clazz)
	{
		List<FunctionSpec> specs = new LinkedList<FunctionSpec>();
		List<Method> methods  = Reflections.getAnnotatedMethods(clazz, XacmlFunc.class);
		for(final Method m : methods){
			specs.add(build(m));
		}
		return specs;
	}
	
	private FunctionSpec build(final Method m)
	{
		final XacmlFunc funcId = m.getAnnotation(XacmlFunc.class);
		XacmlFuncReturnType returnType = m.getAnnotation(XacmlFuncReturnType.class);
		log.debug("Found functionId=\"{}\" method name=\"{}\"", funcId.id(), m.getName());
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder(funcId.id());
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
							String.format("XACML evaluation context annotation annotates wrong parameter type"));
				}
				if(i > 0){
					new IllegalArgumentException(
							String.format(
									"XACML evaluation context parameter must " +
									"be a first parameter in the method=\"%s\" signature", m.getName()));
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
					log.debug("Excpecting attribute value at index=\"{}\", actual type type=\"{}\"", i, types[i].getName());
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
					throw new IllegalArgumentException(String.format("Found vararg parameter " +
							"declaration but actual method=\"%s\" is not vararg", m.getName()));
				}
				if(m.isVarArgs() && 
						i < params.length - 1){
					throw new IllegalArgumentException(String.format("Found vararg parameter " +
							"declaration in incorect place, vararg parameter must be a last parameter in the method"));
				}
				XacmlParamVarArg param = (XacmlParamVarArg)params[i][0];
				AttributeValueType type = param.type().getType();
				b.withParam(param.isBag()?type.bagOf():type, param.min(), param.max());
				continue;
			}
			if(params[i][0] instanceof XacmlParamFuncReference){
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
		AttributeValueType type = returnType.type().getType();
		return b.build(returnType.isBag()?type.bagOf():type, 
				new ReflectionBasedFunctionInvocation(null, m, evalContextParamFound));
	}
}
