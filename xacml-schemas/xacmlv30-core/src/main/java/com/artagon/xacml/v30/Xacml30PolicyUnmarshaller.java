package com.artagon.xacml.v30;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.google.common.base.Preconditions;

public class Xacml30PolicyUnmarshaller implements PolicyUnmarshaller
{
	private JAXBContext context;
	private Xacml30PolicyMapper mapper;
	
	public Xacml30PolicyUnmarshaller(PolicyFactory factory){
		Preconditions.checkNotNull(factory);
		this.mapper = new Xacml30PolicyMapper(factory);
		try{
			this.context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
		}catch(JAXBException e){
			throw new IllegalStateException(e);
		}
	}
	
	public Xacml30PolicyUnmarshaller(PolicyFactory factory, JAXBContext context){
		Preconditions.checkNotNull(factory);
		this.mapper = new Xacml30PolicyMapper(factory);
		try{
			this.context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
		}catch(JAXBException e){
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public CompositeDecisionRule unmarshall(Object source)
			throws PolicySyntaxException 
	{
		Preconditions.checkNotNull(source);
		try{
			Unmarshaller u = context.createUnmarshaller();
			JAXBElement<?> policy = null;
			if(source instanceof InputSource){
				policy =  (JAXBElement<?>)u.unmarshal((InputSource)source);
			}
			if(source instanceof InputStream){
				policy =  (JAXBElement<?>)u.unmarshal((InputStream)source);
			}
			if(source instanceof JAXBElement<?>){
				policy =  (JAXBElement<?>)source;
			}
			if(source instanceof byte[]){
				policy =  (JAXBElement<?>)u.unmarshal(new ByteArrayInputStream((byte[])source));
			}
			if(policy != null){
				return mapper.create(policy.getValue());
			}
			throw new IllegalArgumentException(
					String.format("Unsupported policy source=\"%s\"", 
							source.getClass().getName()));
		}catch(JAXBException e){
			throw new PolicySyntaxException(e);
		}
	}
}
