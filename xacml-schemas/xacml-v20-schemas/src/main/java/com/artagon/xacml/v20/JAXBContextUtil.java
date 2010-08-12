package com.artagon.xacml.v20;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.google.common.base.Preconditions;

public class JAXBContextUtil 
{
	private static JAXBContext INSTANCE;
	
	static{
		StringBuilder b = new StringBuilder();
		b.append(org.oasis.xacml.v20.jaxb.context.ObjectFactory.class.getPackage().getName());
		b.append(":");
		b.append(org.oasis.xacml.v20.jaxb.policy.ObjectFactory.class.getPackage().getName());
		try{
			INSTANCE = JAXBContext.newInstance(b.toString());
		}catch(JAXBException e){
			
		}
	}
	
	public static JAXBContext getInstance()
	{
		Preconditions.checkState(INSTANCE != null, "Failed to initialize JAXB context");
		return INSTANCE;
	}
}
