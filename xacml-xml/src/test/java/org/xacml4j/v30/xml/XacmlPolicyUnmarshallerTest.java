package org.xacml4j.v30.xml;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.oasis.xacml.v30.jaxb.PolicyType;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.marshal.Marshaller;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeAssignmentExpression;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.AttributeSelector;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.VariableDefinition;
import org.xacml4j.v30.policy.VariableReference;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.types.String;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.Iterables;


public class XacmlPolicyUnmarshallerTest
{
	@org.junit.Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static Unmarshaller<CompositeDecisionRule> reader;
	private static Marshaller<CompositeDecisionRule> writer;
	private static FunctionProvider functionProvider;

	@BeforeClass
	public static void init_static() throws Exception
	{
		functionProvider = FunctionProvider.builder()
		                                   .withDefaultFunctions()
		                                   .build();
		reader = new XacmlPolicyUnmarshaller(true);
		writer = new XacmlPolicyMarshaller();
	}

	@SuppressWarnings("unchecked")
	private static <T extends CompositeDecisionRule> T getPolicy(java.lang.String name) throws Exception
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		assertThat(stream, notNullValue());
		return  (T)reader.unmarshal(stream);
	}

	@Test
	public void testPolicyIIIF005Mapping() throws Exception
	{
		Policy p0 = getPolicy("IIIF005Policy.xml");
		assertThat(p0.getId(), is("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIF005:policy"));
		assertThat(p0.getDescription(), is("Policy for Conformance Test IIIF005."));
		assertThat(p0.getDefaults(), notNullValue());
		assertThat(p0.getDefaults().getXPathVersion(), is(XPathVersion.XPATH1));
		assertThat(p0.getTarget(), notNullValue());
		assertThat(p0.getTarget().getAnyOf().size(), is(0));
		assertThat(p0.getRules().size(), is(1));
		Rule r = p0.getRules().get(0);
		assertThat(r.getId(), is("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIF005:rule"));
		assertThat(r.getDescription(), is("Julius Hibbert can read or write Bart Simpson's medical record."));
		assertThat(r.getEffect(), is(Effect.PERMIT));
		assertThat(r.getTarget(), notNullValue());
		assertThat(r.getCondition(), nullValue());
		Target target = r.getTarget();
		assertThat(target.getAnyOf().size(), is(3));
		Iterator<MatchAnyOf> it = target.getAnyOf().iterator();
		MatchAnyOf m0 = it.next();
		MatchAnyOf m1 = it.next();
		MatchAnyOf m2 = it.next();
		assertThat(m0.getAllOf().size(), is(2));
		assertThat(m1.getAllOf().size(), is(1));
		assertThat(m2.getAllOf().size(), is(1));
	}

	@Test
	public void testPolicyIIIF006Mapping() throws Exception
	{
		PolicySet p0 = getPolicy("IIIF006Policy.xml");
		assertThat(p0, notNullValue());
		assertThat(p0.getId(), is("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIF006:policySet"));
		assertThat(p0.getDescription(), is("Policy Set for Conformance Test IIIF006."));
		assertThat(p0.getDefaults(), notNullValue());
		assertThat(p0.getDefaults().getXPathVersion(), is(XPathVersion.XPATH1));
		assertThat(p0.getTarget(), notNullValue());
		assertThat(p0.getDecisions().size(), is(1));
	}

	@Test
	public void testPolicyIIIF007Mapping() throws Exception
	{
		Policy p = getPolicy("IIIF007Policy.xml");
		assertThat(p, notNullValue());
	}


	@Test
	public void testPolicyIIC231Mapping() throws Exception
	{
		Policy p = getPolicy("IIC231Policy.xml");
		assertThat(p, notNullValue());
	}

	@Test
	public void testFeatures001Policy() throws Exception
	{
		Policy p = getPolicy("001B-Policy.xml");
		assertThat(p.getVariableDefinitions().size(), is(5));
		assertThat(p.getVariableDefinition("VAR01"), notNullValue());
		assertThat(p.getVariableDefinition("VAR02"), notNullValue());
		assertThat(p.getVariableDefinition("VAR03"), notNullValue());
		assertThat(p.getVariableDefinition("VAR04"), notNullValue());
		assertThat(p.getVariableDefinition("VAR05"), notNullValue());
	}

	@Test
	public void testFeatures002Policy() throws Exception {
		expectedException.expect(SyntaxException.class);
		getPolicy("002B-Policy.xml");
	}

	@Test
	public void testFeatures003Policy() throws Exception {
		Policy p = getPolicy("003B-Policy.xml");
		assertThat(p.getVariableDefinitions().size(), is(5));
		assertThat(p.getVariableDefinition("VAR01"), notNullValue());
		assertThat(p.getVariableDefinition("VAR02"), notNullValue());
		assertThat(p.getVariableDefinition("VAR03"), notNullValue());
		assertThat(p.getVariableDefinition("VAR04"), notNullValue());
		assertThat(p.getVariableDefinition("VAR05"), notNullValue());
	}

	@Test
	public void testPolicy1Mapping() throws Exception
	{
		Policy p = getPolicy("Policy1.xml");
		assertThat(p.getId(), is("urn:oasis:names:tc:xacml:3.0:example:policyid:1"));
		assertThat(p.getVersion(), is(Version.parse("1.0")));
		assertThat(p.getRuleCombiningAlgorithm().getId(), is("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides"));
		assertThat(p.getTarget(), notNullValue());
		assertThat(p.getTarget().getAnyOf().size(), is(0));
		assertThat(p.getRules().size(), is(1));
		Rule r = p.getRules().get(0);
		assertThat(r.getId(), is("urn:oasis:names:tc:xacml:3.0:example:ruleid:1"));
		assertThat(r.getEffect(), is(Effect.PERMIT));
		assertThat(r.getDescription(), notNullValue());
		assertThat(r.getTarget(), notNullValue());
		assertThat(r.getTarget().getAnyOf().size(), is(2));
		Iterator<MatchAnyOf> it = r.getTarget().getAnyOf().iterator();
		MatchAnyOf m1 = it.next();
		MatchAnyOf m2 = it.next();
		assertThat(m1.getAllOf().size(), is(1));
		assertThat(m2.getAllOf().size(), is(1));
	}

	@Test
	public void testPolicy2Mapping() throws Exception
	{
		Policy p = getPolicy("Policy2.xml");
		assertThat(p.getId(), is("urn:oasis:names:tc:xacml:3.0:example:policyid:3"));
		assertThat(p.getVersion(), is(Version.parse("1.0")));
		assertThat(p.getRuleCombiningAlgorithm().getId(), is("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides"));
	}

	@Test
	public void testPolicySet1Mapping() throws Exception
	{
		getPolicy("PolicySet1.xml");
	}

	@Test
	public void testPolicyWithVariables1() throws Exception
	{
		final VariableDefinition expectedVar01 = new VariableDefinition(
				"VAR01",
				AttributeDesignator.builder().key(
						AttributeDesignatorKey.builder()
				                   .attributeId("urn:oasis:names:tc:xacml:2.0:example:attribute:patient-number")
				                   .category("urn:oasis:names:tc:xacml:3.0:attribute-category:resource")
				                   .dataType(XacmlTypes.STRING)
				                   .build()).mustBePresent(true).build());
		final VariableDefinition expectedVar02 = new VariableDefinition(
				"VAR02",
				AttributeSelector.builder().key(
						AttributeSelectorKey.builder()
						.xpath("//md:record/md:patient/md:patient-number/text()")
						.category("urn:oasis:names:tc:xacml:3.0:attribute-category:resource")
						.dataType(XacmlTypes.STRING)
						.build())
				                 .mustBePresent(true)
				                 .build());

		final VariableDefinition expectedVar03 = new VariableDefinition(
				"VAR03",
				Apply.builder("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only", (id)->functionProvider.getFunction(id))
						.param(new VariableReference(expectedVar01))
						.build());

		final VariableDefinition expectedVar04 = new VariableDefinition(
				"VAR04",
				Apply.builder("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only", (id)->functionProvider.getFunction(id))
				     .param(new VariableReference(expectedVar02))
				     .build());

		final VariableDefinition expectedVar05 = new VariableDefinition(
				"VAR05",
				Apply.builder("urn:oasis:names:tc:xacml:1.0:function:string-equal", (id)->functionProvider.getFunction(id))
				     .param(new VariableReference(expectedVar03))
				     .param(new VariableReference(expectedVar04))
				     .build());

		Policy p = getPolicy("v30-policy-with-variables-1.xml");
		assertThat(p.getVersion().getValue(), is("1.0"));
		assertThat(p.getVariableDefinitions().size(), is(5));
		assertThat(p.getVariableDefinition("VAR01"), equalTo(expectedVar01));
		assertThat(p.getVariableDefinition("VAR02"), equalTo(expectedVar02));
		assertThat(p.getVariableDefinition("VAR03"), equalTo(expectedVar03));
		assertThat(p.getVariableDefinition("VAR04"), equalTo(expectedVar04));
		assertThat(p.getVariableDefinition("VAR05"), equalTo(expectedVar05));

		@SuppressWarnings("unchecked")
		JAXBElement<PolicyType> jaxb = (JAXBElement<PolicyType>) writer.marshal(p);
		assertThat(jaxb.getValue().getVersion(), is("1.0"));
		Policy p1 = (Policy)reader.unmarshal(jaxb);
		assertThat(p, is(p1));
	}

	@Test
	public void testPolicyWithVariables2() throws Exception
	{
		final VariableDefinition expectedVar05 = new VariableDefinition(
				"VAR05",
				AttributeDesignator.builder()
				                   .key(AttributeDesignatorKey.builder()
				                   .attributeId("urn:oasis:names:tc:xacml:2.0:example:attribute:patient-number")
				                   .category("urn:oasis:names:tc:xacml:3.0:attribute-category:resource")
				                   .dataType(XacmlTypes.STRING)
				                   .build()).mustBePresent(true)
				                   .build());

		Policy p = getPolicy("v30-policy-with-variables-2.xml");
		assertThat(p.getVersion().getValue(), is("1.0"));
		assertThat(p.getVariableDefinitions().size(), is(5));
		assertThat(p.getVariableDefinition("VAR01"), notNullValue());
		assertThat(p.getVariableDefinition("VAR02"), notNullValue());
		assertThat(p.getVariableDefinition("VAR03"), notNullValue());
		assertThat(p.getVariableDefinition("VAR04"), notNullValue());
		assertThat(p.getVariableDefinition("VAR05"), equalTo(expectedVar05));
		@SuppressWarnings("unchecked")
		JAXBElement<PolicyType> jaxb = (JAXBElement<PolicyType>) writer.marshal(p);
		assertThat(jaxb.getValue().getVersion(), is("1.0"));
		Policy p1 = (Policy)reader.unmarshal(jaxb);
		assertThat(p, is(p1));
	}

	@Test
	public void testPolicyWithVariables3_CyclicError() throws Exception {
		expectedException.expect(SyntaxException.class);
		getPolicy("v30-policy-with-variables-3.xml");
	}

	@Test
	public void testPolicyRoundTrip() throws Exception
	{
		Policy p = getPolicy("v30-test-policy.xml");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		writer.marshal(p, os);
		os.close();
		Policy p1 = (Policy)reader.unmarshal(new ByteArrayInputStream(os.toByteArray()));
		assertThat(p, is(p1));
	}

	@Test
	public void testPolicySetRoundTrip() throws Exception
	{
		PolicySet p = getPolicy("PolicySet1.xml");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		writer.marshal(p, os);
		os.close();
		PolicySet p1 = (PolicySet)reader.unmarshal(new ByteArrayInputStream(os.toByteArray()));
		assertThat(p, is(p1));
	}

	@Test
	public void testXacml20ObligationExpressions() throws Exception {
		Policy p = getPolicy("xacml2.0-policy-with-obligations.xml");

		Collection<ObligationExpression> obligations = p.getObligationExpressions();
		assertThat(obligations.size(), is(1));

		ObligationExpression o1 = Iterables.get(obligations, 0);
		assertThat(o1.getId(), is("urn:oasis:names:tc:xacml:example:obligation:email"));
		Collection<AttributeAssignmentExpression> o1AttrExps = o1.getAttributeAssignmentExpressions();
		assertThat(o1AttrExps.size(), is(3));

		AttributeAssignmentExpression o1AttrExp1 = Iterables.get(o1AttrExps, 0);
		assertThat(o1AttrExp1.getAttributeId(), is("urn:oasis:names:tc:xacml:2.0:example:attribute:mailto"));
		assertThat(o1AttrExp1.getExpression(), instanceOf(AttributeSelector.class));
		AttributeSelector attributeSelector = (AttributeSelector) o1AttrExp1.getExpression();
		System.err.println(attributeSelector.getReferenceKey());
		assertThat(attributeSelector.getReferenceKey().getPath(), is("//md:/record/md:patient/md:patientContact/md:email"));
		assertThat(attributeSelector.getReferenceKey().getDataType(), is(XacmlTypes.STRING.getValueType()));

		AttributeAssignmentExpression o1AttrExp2 = Iterables.get(o1AttrExps, 1);
		assertThat(o1AttrExp2.getAttributeId(), is("urn:oasis:names:tc:xacml:2.0:example:attribute:text"));
		assertThat(o1AttrExp2.getExpression(), instanceOf(String.class));

		AttributeAssignmentExpression o1AttrExp3 = Iterables.get(o1AttrExps, 2);
		assertThat(o1AttrExp3.getAttributeId(), is("urn:oasis:names:tc:xacml:2.0:example:attribute:text"));
		assertThat(o1AttrExp3.getExpression(), instanceOf(AttributeDesignator.class));
		AttributeDesignator attributeDesignator = (AttributeDesignator) o1AttrExp3.getExpression();
		assertThat(attributeDesignator.getReferenceKey().getCategory().getId(), is(CategoryId.SUBJECT_ACCESS.getId()));
		assertThat(attributeDesignator.getReferenceKey().getAttributeId(), is("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertThat(attributeDesignator.getReferenceKey().getDataType(), is(XacmlTypes.STRING.getValueType()));
	}
}
