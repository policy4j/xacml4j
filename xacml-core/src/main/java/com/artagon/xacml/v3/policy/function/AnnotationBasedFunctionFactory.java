package com.artagon.xacml.v3.policy.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.Value;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFuncReturnType;
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
		XacmlFunc funcId = m.getAnnotation(XacmlFunc.class);
		XacmlFuncReturnType returnType = m.getAnnotation(XacmlFuncReturnType.class);
		log.debug("Found functionId=\"{}\" method name=\"{}\"", funcId.id(), m.getName());
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder(funcId.id());
		Annotation[][] params = m.getParameterAnnotations();
		int paramCount = 0;
		for(Annotation[] p : params){
			if(p[0] instanceof XacmlParam){
				XacmlParam param = (XacmlParam)p[0];
				AttributeValueType type = param.type().getType(); 
				b.withParam(param.isBag()?type.bagOf():type);
				paramCount++;
			}
			if(p[0] instanceof XacmlParamVarArg){
				XacmlParamVarArg param = (XacmlParamVarArg)p[0];
				AttributeValueType type = param.type().getType();
				b.withParam(param.isBag()?type.bagOf():type, param.min(), param.max());
				paramCount++;
			}
		}
		AttributeValueType type = returnType.type().getType();
		return b.build(new StaticallyTypedFunction(returnType.isBag()?type.bagOf():type) 
		{
			@Override
			public Value invoke(EvaluationContext context,
					Expression... parameters)
					throws EvaluationException {
				try
				{
					return (Value)m.invoke(null, parameters);
				}catch(Exception e){
					throw new EvaluationException(e, "failed to invoke function");
				}
			}
			
		});
	}
}
