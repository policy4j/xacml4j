package com.artagon.xacml.v30.marshall.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.google.common.base.Preconditions;

public class JAXBContextUtil 
{
	private static JAXBContext INSTANCE;
	
	static{
		StringBuilder b = new StringBuilder();
		b.append(org.oasis.xacml.v30.jaxb.ObjectFactory.class.getPackage().getName()).append(":");
		b.append(org.oasis.xacml.v20.jaxb.policy.ObjectFactory.class.getPackage().getName()).append(":");
		b.append(org.oasis.xacml.v20.jaxb.context.ObjectFactory.class.getPackage().getName());
		try{
			INSTANCE = JAXBContext.newInstance(b.toString());
		}catch(JAXBException e){
			e.printStackTrace(System.err);
		}
	}
	
	public static JAXBContext getInstance()
	{
		Preconditions.checkState(INSTANCE != null, "Failed to initialize JAXB context");
		return INSTANCE;
	}
}
