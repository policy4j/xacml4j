package com.artagon.xacml.v3.spi.pip.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.v3.spi.pip.PolicyInformationPointContext;
import com.google.common.base.Preconditions;

public class ResolverMethodInfo
{
	private List<RequestContextKeyInfo> keyInfo;
	private Method m;
	private Object instance;
	
	public ResolverMethodInfo(
			Object instance,
			Method m, 
			Collection<RequestContextKeyInfo> keyInfo)
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
		for(int i  = 1; i < keyInfo.size(); i++){
			params[i] = keyInfo.get(i).getKey(context);
		}
		return m.invoke(instance, params);
	}
}
