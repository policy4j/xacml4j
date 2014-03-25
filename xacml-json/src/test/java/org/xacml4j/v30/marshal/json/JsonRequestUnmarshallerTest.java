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
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestDefaults;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.ResourceAttributes;
import org.xacml4j.v30.SubjectAttributes;
import org.xacml4j.v30.marshal.Marshaller;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xacml4j.v30.types.StringExp;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableList;

public class JsonRequestUnmarshallerTest {
	private static final Logger log = LoggerFactory.getLogger(JsonRequestUnmarshallerTest.class);

	private Unmarshaller<RequestContext> unmarshaller;
	private Marshaller<RequestContext> marshaller;
	
	@Before
	public void init() {
		unmarshaller = new JsonRequestContextUnmarshaller();
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
				.entity(
						Entity
						.builder()
						.content(sampleContent1())
						.attributes(
						ImmutableList.<Attribute> of(
								Attribute
										.builder(SubjectAttributes.SUBJECT_ID.toString())
										.includeInResult(false)
										.issuer("testIssuer")
										.value(StringExp.valueOf(
												"VFZTAQEAABRcZ03t-NNkK__rcIbvgKcK6e5oHBD5fD0qkdPIuqviWHzzFVR6AAAAgFl8GkUGZQG8TPXg9T6cQCoMO3a_sV1FR8pJC4BPfXfXlOvWDPUt4pr0cBkGTeaSU9RjSvEiXF-kTq5GFPkBHXcYnBW7eNjhq2EB_RWHh7_0sWqY32yb4fxlPLOsh5cUR4WbYZJE-zNuVzudco5cOjHU6Zwlr2HACpHW5siAVKfW"))
										.build(),
								Attribute.builder(SubjectAttributes.SUBJECT_ID_QUALIFIER.toString())
										.includeInResult(false).issuer("testIssuer")
										.value(StringExp.valueOf("TestDomain")).build()))
						.build())
				.build();
		Attributes resourceAttributes = Attributes
				.builder(AttributeCategories.RESOURCE)
				.id("ResourceAttributes")
				.entity(Entity
						.builder()
						.attributes(
						ImmutableList.<Attribute> of(Attribute.builder(ResourceAttributes.RESOURCE_ID.toString())
								.includeInResult(true).value(StringExp.valueOf("testResourceId")).build())).build())
						.build();
		Attributes actionAttributes = Attributes
				.builder(AttributeCategories.ACTION)
				.entity(Entity
						.builder()
						.attributes(
						ImmutableList.<Attribute> of(Attribute.builder(SubjectAttributes.SUBJECT_ID.toString())
								.includeInResult(false).value(StringExp.valueOf("VIEW")).build())).build())
				.build();
		Attributes environmentAttributes = Attributes
				.builder(AttributeCategories.ENVIRONMENT)
				.id("EnvironmentAttributes")
				.entity(Entity
						.builder()
						.attributes(
						ImmutableList.<Attribute> of(Attribute.builder(ResourceAttributes.TARGET_NAMESPACE.toString())
								.includeInResult(false).value(StringExp.valueOf("json\\-\"test\"")).build()))
						.build())
				.build();
		Attributes subjectIntermAttributes = Attributes
				.builder(AttributeCategories.SUBJECT_INTERMEDIARY)
				.id("SubjectIntermediaryAttributes")
				.entity(Entity
						.builder()
						.attributes(
						ImmutableList.<Attribute> of(Attribute.builder(SubjectAttributes.AUTHN_METHOD.toString())
								.includeInResult(false)
								.value(StringExp.valueOf("koks oras paryziuj?")).build()))
						.build())
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
