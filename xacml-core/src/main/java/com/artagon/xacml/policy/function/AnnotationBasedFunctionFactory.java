package com.artagon.xacml.policy.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.policy.AttributeType;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.EvaluationException;
import com.artagon.xacml.policy.EvaluationIndeterminateException;
import com.artagon.xacml.policy.Value;
import com.artagon.xacml.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.policy.function.annotations.XacmlFuncParam;
import com.artagon.xacml.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.policy.function.annotations.XacmlFuncVarArgParam;
import com.artagon.xacml.util.Reflections;


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
		for(Annotation[] p : params){
			if(p[0] instanceof XacmlFuncParam){
				XacmlFuncParam param = (XacmlFuncParam)p[0];
				AttributeType type = param.type().getType(); 
				b.withParam(param.isBag()?type.bagOf():type);
			}
			if(p[0] instanceof XacmlFuncVarArgParam){
				XacmlFuncVarArgParam param = (XacmlFuncVarArgParam)p[0];
				AttributeType type = param.type().getType();
				b.withParam(param.isBag()?type.bagOf():type, param.min(), param.max());
			}
		}
		AttributeType type = returnType.type().getType();
		return b.build(new StaticallyTypedFunction(returnType.isBag()?type.bagOf():type) 
		{
			@Override
			public Value invoke(EvaluationContext context,
					Expression... parameters)
					throws EvaluationException {
				try{
					return (Value)m.invoke(null, parameters);
				}catch(Exception e){
					throw new EvaluationIndeterminateException(e, "failed to invoke function");
				}
			}
			
		});
	}
}
