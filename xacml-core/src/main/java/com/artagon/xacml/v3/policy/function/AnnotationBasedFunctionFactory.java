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
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamEvaluationContext;
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
	
	// TODO: Monster method :)
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
			}
			if(params[0][0] instanceof XacmlParamVarArg){
				if(m.isVarArgs()){
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
			}
		}
		final boolean evalContextRequired = evalContextParamFound;
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
						int paramStartIndex = 0;
						if(evalContextRequired){
							log.debug("Passing evaluation context as first parameter");
							p[0] = context;
							paramStartIndex++;
						}
						if(m.isVarArgs()){
							log.debug("Function=\"{}\" number of expected params=\"{}\"", spec.getId(), spec.getNumberOfParams());
							log.debug("Function=\"{}\" number of given params=\"{}\" in invocation", spec.getId(), arguments.length);
							p = new Object[spec.getNumberOfParams() + (evalContextRequired?1:0)];
							System.arraycopy(arguments, 0, p, paramStartIndex, spec.getNumberOfParams() - 1); 
							Object[] varArg = null;
							if(spec.getNumberOfParams() < arguments.length - 1){
								varArg = new Object[arguments.length - (spec.getNumberOfParams() - 1)];
								log.debug("VarArg array length=\"{}\"", varArg.length);
								System.arraycopy(arguments, spec.getNumberOfParams() - 1, varArg, 0, varArg.length);
							} 
							p[p.length - 1] = varArg;
						}
						return (T)m.invoke(null, p);
					}catch(Exception e){
						log.error("Failed to invoke function=\"{}\"", spec.getId());
						log.error(e.getMessage(), e);
						throw new EvaluationException(e, "Failed to invoke function=\"%s\"", spec.getId());
					}
				}
		});
	}
}
