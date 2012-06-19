package com.artagon.xacml.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.marshall.Marshaller;
import com.artagon.xacml.v30.marshall.Unmarshaller;
import com.artagon.xacml.v30.marshall.json.JsonRequestContextMarshaller;
import com.artagon.xacml.v30.marshall.json.JsonRequestContextUnmarshaller;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.types.DataTypeRegistryBuilder;

public class GsonRequestUnmarshallerTest
{
	private Unmarshaller<RequestContext> unmarshaller;
	private Marshaller<RequestContext> marshaller;

	@Before
	public void init(){
		this.unmarshaller = new JsonRequestContextUnmarshaller(DataTypeRegistryBuilder.builder()
				.withDefaultTypes()
				.build());
		this.marshaller = new JsonRequestContextMarshaller();
	}

	@Test
	public void testXacmlJsonRequestRoundtrip() throws Exception
	{
		InputStream in = getClassPathResource("xacml30-req.json");
		RequestContext req1 = unmarshaller.unmarshal(new InputStreamReader(in));
		assertNotNull(req1);
		Object o = marshaller.marshal(req1);
		RequestContext req2 = unmarshaller.unmarshal(o);
		assertEquals(req1, req2);
	}


	private InputStream getClassPathResource(String resource)
				throws Exception
	{
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
	}
}
