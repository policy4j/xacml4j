package com.artagon.xacml.v3.spi.pip.impl;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.spi.pip.PolicyInformationPointContext;
import com.google.common.base.Preconditions;

public class ResolverMethodInfo
{
	private final static Logger log = LoggerFactory.getLogger(ResolverMethodInfo.class);
	
	private List<RequestContextKeyInfo> keyInfo;
	private Method m;
	private Object instance;
	
	public ResolverMethodInfo(
			Object instance,
			Method m, 
			List<RequestContextKeyInfo> keyInfo)
	{
		Preconditions.checkArgument(instance != null);
		Preconditions.checkArgument(m != null);
		this.m = m;
		this.instance = instance;
		this.keyInfo = new LinkedList<RequestContextKeyInfo>(keyInfo);
	}
	
	public Object invoke(PolicyInformationPointContext context) throws Exception
	{
		Object[] params = new Object[keyInfo.size() + 1];
		params[0] = context;
		for(int i  = 0; i < keyInfo.size(); i++){
			if(log.isDebugEnabled()){
				log.debug("Resolving request key=\"{}\"", keyInfo);
			}
			params[i + 1] = keyInfo.get(i).getKey(context);
		}
		return m.invoke(instance, params);
	}
}
