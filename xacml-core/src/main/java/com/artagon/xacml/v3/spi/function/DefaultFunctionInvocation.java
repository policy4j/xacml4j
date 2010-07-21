package com.artagon.xacml.v3.spi.function;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.ValueExpression;
import com.google.common.base.Preconditions;

public class DefaultFunctionInvocation extends BaseReflectionFunctionInvocation
{
	private final static Logger log = LoggerFactory.getLogger(DefaultFunctionInvocation.class);
	
	private Method functionMethod;
	private Object instance;
	
	public DefaultFunctionInvocation(
			Method m, 
			Object instance,
			boolean evalContextRequired)
	{
		super(evalContextRequired);
		Preconditions.checkNotNull(m);
		this.instance = instance;
		this.functionMethod = m;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected <T extends ValueExpression> T invoke(Object ...params) throws Exception
	{
		try{
			if(log.isDebugEnabled()){
				log.debug("Invoking method=\"{}\" " +
						"on instace=\"{}\"", functionMethod.getName(), instance);
			}
			return (T)functionMethod.invoke(instance, params);
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug("Failed to invoke methd=\"{}\" with error message=\"{}\"", 
						functionMethod.getName(), e.getMessage());
				log.debug("Stack trace", e);
			}
			throw e;
		}
	}
}