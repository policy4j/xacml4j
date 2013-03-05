package org.xacml4j.v30.marshal.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestDefaults;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.ResourceAttributes;
import org.xacml4j.v30.SubjectAttributes;
import org.xacml4j.v30.marshall.Marshaller;
import org.xacml4j.v30.marshall.Unmarshaller;
import org.xacml4j.v30.types.StringType;
import org.xacml4j.v30.types.Types;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableList;

public class GsonRequestUnmarshallerTest {
	private static final Logger log = LoggerFactory.getLogger(GsonRequestUnmarshallerTest.class);

	private Unmarshaller<RequestContext> unmarshaller;
	private Marshaller<RequestContext> marshaller;

	private Types types;

	@Before
	public void init() {
		types = Types.builder().defaultTypes().create();
		unmarshaller = new JsonRequestContextUnmarshaller(types);
		marshaller = new JsonRequestContextMarshaller();
	}

	@Test
	public void testXacmlJsonRequestRoundtrip() throws Exception {
		RequestContext reqIn = createTestRequest();
		Object o = marshaller.marshal(reqIn);
		log.debug("JSON request: {}", o);
		RequestContext reqOut = unmarshaller.unmarshal(o);
		assertThat(reqOut, is(equalTo(reqIn)));
	}

	private RequestContext createTestRequest() throws Exception {
		Attributes subjectAttributes = Attributes
				.builder(AttributeCategories.SUBJECT_ACCESS)
				.id("SubjectAttributes")
				.content(sampleContent1())
				.attributes(
						ImmutableList.<Attribute> of(
								Attribute
										.builder(SubjectAttributes.SUBJECT_ID.toString())
										.includeInResult(false)
										.issuer("testIssuer")
										.value(types.valueOf(
												StringType.STRING.getDataTypeId(),
												"VFZTAQEAABRcZ03t-NNkK__rcIbvgKcK6e5oHBD5fD0qkdPIuqviWHzzFVR6AAAAgFl8GkUGZQG8TPXg9T6cQCoMO3a_sV1FR8pJC4BPfXfXlOvWDPUt4pr0cBkGTeaSU9RjSvEiXF-kTq5GFPkBHXcYnBW7eNjhq2EB_RWHh7_0sWqY32yb4fxlPLOsh5cUR4WbYZJE-zNuVzudco5cOjHU6Zwlr2HACpHW5siAVKfW"))
										.build(),
								Attribute.builder(SubjectAttributes.SUBJECT_ID_QUALIFIER.toString())
										.includeInResult(false).issuer("testIssuer")
										.value(types.valueOf(StringType.STRING.getDataTypeId(), "TestDomain")).build()))
				.build();
		Attributes resourceAttributes = Attributes
				.builder(AttributeCategories.RESOURCE)
				.id("ResourceAttributes")
				.attributes(
						ImmutableList.<Attribute> of(Attribute.builder(ResourceAttributes.RESOURCE_ID.toString())
								.includeInResult(true)
								.value(types.valueOf(StringType.STRING.getDataTypeId(), "testResourceId")).build()))
				.build();
		Attributes actionAttributes = Attributes
				.builder(AttributeCategories.ACTION)
				.attributes(
						ImmutableList.<Attribute> of(Attribute.builder(SubjectAttributes.SUBJECT_ID.toString())
								.includeInResult(false).value(types.valueOf(StringType.STRING.getDataTypeId(), "VIEW"))
								.build())).build();
		Attributes environmentAttributes = Attributes
				.builder(AttributeCategories.ENVIRONMENT)
				.id("EnvironmentAttributes")
				.attributes(
						ImmutableList.<Attribute> of(Attribute.builder(ResourceAttributes.TARGET_NAMESPACE.toString())
								.includeInResult(false)
								.value(types.valueOf(StringType.STRING.getDataTypeId(), "json\\-\"test\"")).build()))
				.build();
		Attributes subjectIntermAttributes = Attributes
				.builder(AttributeCategories.SUBJECT_INTERMEDIARY)
				.id("SubjectIntermediaryAttributes")
				.attributes(
						ImmutableList.<Attribute> of(Attribute.builder(SubjectAttributes.AUTHN_METHOD.toString())
								.includeInResult(false)
								.value(StringType.STRING, "koks oras paryziuj?", "as vistiek nesiojuosi sketi").build()))
				.build();

		RequestReference requestRef1 = RequestReference.builder().reference(subjectAttributes, resourceAttributes)
				.build();
		RequestReference requestRef2 = RequestReference.builder()
				.reference(subjectAttributes, environmentAttributes, subjectIntermAttributes).build();

		RequestContext reqIn = RequestContext
				.builder()
				.combineDecision(false)
				.returnPolicyIdList()
				.reqDefaults(new RequestDefaults())
				.attributes(subjectAttributes, resourceAttributes, actionAttributes, environmentAttributes,
						subjectIntermAttributes).reference(requestRef1, requestRef2).build();
		return reqIn;
	}

	private Node sampleContent1() throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		return documentBuilder.parse(new InputSource(new StringReader(
				"<security>\n<through obscurity=\"true\"></through></security>")));
	}

}
