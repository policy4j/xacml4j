package com.artagon.xacml.v3.spi.function;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Value;
import com.google.common.base.Preconditions;

public class DefaultFunctionInvocation extends BaseReflectionFunctionInvocation
{
	private final static Logger log = LoggerFactory.getLogger(DefaultFunctionInvocation.class);
	
	private Method functionMethod;
	private Object instance;
	
	public DefaultFunctionInvocation(
			Method m, 
			boolean evalContextRequired)
	{
		super(evalContextRequired);
		Preconditions.checkNotNull(m);
		Preconditions.checkArgument(Modifier.isStatic(m.getModifiers()));
		this.functionMethod = m;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected <T extends Value> T invoke(Object ...params) throws Exception
	{
		try{
			return (T)functionMethod.invoke(instance, params);
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug("Failed to invoke methd=\"{}\" with error message=\"{}\"", 
						functionMethod.getName(), e.getMessage());
			}
			throw e;
		}
	}
}