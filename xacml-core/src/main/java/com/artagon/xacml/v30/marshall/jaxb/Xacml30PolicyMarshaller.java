package com.artagon.xacml.v30.marshall.jaxb;

import java.io.IOException;

import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.PolicySetType;
import org.oasis.xacml.v30.jaxb.PolicyType;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.Policy;
import com.artagon.xacml.v30.PolicySet;
import com.google.common.base.Preconditions;

public class Xacml30PolicyMarshaller 
	extends BaseJAXBMarshaller<CompositeDecisionRule>
{
	private ObjectFactory f;
	
	public Xacml30PolicyMarshaller(){
		super(JAXBContextUtil.getInstance());
		this.f = new ObjectFactory();
	}
	
	@Override
	public Object marshal(CompositeDecisionRule source) throws IOException 
	{
		Preconditions.checkNotNull(source);
		if(source instanceof Policy){
			PolicyType p = toJaxb((Policy)source);
			return f.createPolicy(p);
		}
		if(source instanceof PolicySet){
			PolicySetType p = toJaxb((PolicySet)source);
			return f.createPolicySet(p);
		}
		throw new IllegalArgumentException(String.format(
				"Given type=\"%s\" can not be " +
				"marshalled to XACML policy or policy set", 
				source.getClass().getName()));
	}
	
	private PolicyType toJaxb(Policy p){
		return null;
	}
	
	private PolicySetType toJaxb(PolicySet p){
		return null;
	}
}
