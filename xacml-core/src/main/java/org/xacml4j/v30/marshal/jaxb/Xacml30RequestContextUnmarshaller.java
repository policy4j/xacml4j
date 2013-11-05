package org.xacml4j.v30.marshal.jaxb;

import javax.xml.bind.JAXBElement;

import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.RequestUnmarshaller;

import com.google.common.base.Preconditions;

public class Xacml30RequestContextUnmarshaller extends
	BaseJAXBUnmarshaller<RequestContext>
implements RequestUnmarshaller
{
	private Xacml30RequestContextFromJaxbToObjectModelMapper mapper;

	public Xacml30RequestContextUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Xacml30RequestContextFromJaxbToObjectModelMapper();
	}

	@Override
	protected RequestContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue()
				instanceof org.oasis.xacml.v30.jaxb.RequestType));
		return mapper.create((org.oasis.xacml.v30.jaxb.RequestType)jaxbInstance.getValue());
	}
}
