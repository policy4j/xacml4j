package com.artagon.xacml.v20;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.oasis.xacml.v20.policy.ObjectFactory;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.XacmlPolicyReader;
import com.google.common.base.Preconditions;

public class Xacml20PolicyReader implements XacmlPolicyReader
{
	private JAXBContext context;
	private Xacml20PolicyMapper mapper;
	
	public Xacml20PolicyReader(PolicyFactory factory){
		Preconditions.checkNotNull(factory);
		this.mapper = new Xacml20PolicyMapper(factory);
		try{
			this.context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
		}catch(JAXBException e){
			throw new IllegalStateException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompositeDecisionRule getPolicy(InputStream source)
			throws PolicySyntaxException 
	{
		Preconditions.checkNotNull(source);
		try{
			Unmarshaller u = context.createUnmarshaller();
			JAXBElement<CompositeDecisionRule> policy =  (JAXBElement<CompositeDecisionRule>)u.unmarshal(source);
			return mapper.create(policy.getValue());
		}catch(JAXBException e){
			throw new PolicySyntaxException(e);
		}
	}
}
