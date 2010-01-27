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
import com.artagon.xacml.v3.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParam;
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
		int paramCount = 0;
		for(Annotation[] p : params)
		{
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
		return b.build(returnType.isBag()?type.bagOf():type, 
				new FunctionInvocation() 
		{
				@SuppressWarnings("unchecked")
				@Override
				public <T extends Value> T invoke(FunctionSpec spec,
						EvaluationContext context, Expression... arguments)
						throws EvaluationException {

					try
					{
						Object[] p = arguments;
						if(m.isVarArgs()){
							log.debug("Function=\"{}\" number of expected params=\"{}\"", spec.getXacmlId(), spec.getNumberOfParams());
							log.debug("Function=\"{}\" number of given params=\"{}\" in invocation", spec.getXacmlId(), arguments.length);
							p = new Object[spec.getNumberOfParams()];
							System.arraycopy(arguments, 0, p, 0, spec.getNumberOfParams() - 1); 
							Object[] varArg = new Object[arguments.length - (spec.getNumberOfParams() - 1)];
							log.debug("VarArg array length=\"{}\"", varArg.length);
							System.arraycopy(arguments, spec.getNumberOfParams() - 1, varArg, 0, varArg.length); 
							p[p.length - 1] = varArg;
							for(Object o : p){
								log.debug(o.toString());
							}
						}
						return (T)m.invoke(null, p);
					}catch(Exception e){
						log.error("Failed to invoke function=\"{}\"", spec.getXacmlId());
						log.error(e.getMessage(), e);
						throw new EvaluationException(e, "Failed to invoke function=\"%s\"", spec.getXacmlId());
					}
				}
		});
	}
}
