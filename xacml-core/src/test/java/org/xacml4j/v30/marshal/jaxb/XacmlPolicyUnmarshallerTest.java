package org.xacml4j.v30.marshal.jaxb;

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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.PolicyMarshaller;
import org.xacml4j.v30.marshal.PolicyUnmarshaller;
import org.xacml4j.v30.pdp.AttributeAssignmentExpression;
import org.xacml4j.v30.pdp.AttributeDesignator;
import org.xacml4j.v30.pdp.MatchAnyOf;
import org.xacml4j.v30.pdp.ObligationExpression;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.pdp.Target;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.StringExp;

import com.google.common.collect.Iterables;


public class XacmlPolicyUnmarshallerTest
{
	private static PolicyUnmarshaller reader;
	private static PolicyMarshaller writer;

	@BeforeClass
	public static void init_static() throws Exception
	{
		reader = new XacmlPolicyUnmarshaller(
				FunctionProviderBuilder
				.builder()
				.defaultFunctions()
				.build(),
				DecisionCombiningAlgorithmProviderBuilder
				.builder()
				.withDefaultAlgorithms().create());
		writer = new Xacml30PolicyMarshaller();
	}

	@SuppressWarnings("unchecked")
	private static <T extends CompositeDecisionRule> T getPolicy(String name) throws Exception
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

	@Test(expected=XacmlSyntaxException.class)
	public void testFeatures002Policy() throws Exception
	{
		Policy p = getPolicy("002B-Policy.xml");
		assertThat(p.getVariableDefinitions().size(), is(2));
		assertThat(p.getVariableDefinition("VAR01"), notNullValue());
		assertThat(p.getVariableDefinition("VAR02"), notNullValue());
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
	public void testPolicy3() throws Exception
	{
		Policy p = getPolicy("Policy3.xml");
		assertThat(p.getVariableDefinitions().size(), is(5));
		assertThat(p.getVariableDefinition("VAR01"), notNullValue());
		assertThat(p.getVariableDefinition("VAR02"), notNullValue());
		assertThat(p.getVariableDefinition("VAR03"), notNullValue());
		assertThat(p.getVariableDefinition("VAR04"), notNullValue());
		assertThat(p.getVariableDefinition("VAR05"), notNullValue());
		Object jaxb = writer.marshal(p);
		Policy p1 = (Policy)reader.unmarshal(jaxb);
//		FIXME: implement marshalling properly
//		assertEquals(p, p1);
	}

	@Test
	public void testXacml20ObligationExpressions() throws Exception {
		Policy p = getPolicy("xacml2.0-policy-with-obligations.xml");

		Collection<ObligationExpression> obligations = p.getObligationExpressions();
		assertThat(obligations.size(), is(2));

		ObligationExpression o1 = Iterables.get(obligations, 0);
		assertThat(o1.getId(), is("urn:org:xacml4j:tests:policy-with-obligations:obligation1"));
		Collection<AttributeAssignmentExpression> o1AttrExps = o1.getAttributeAssignmentExpressions();
		assertThat(o1AttrExps.size(), is(1));
		AttributeAssignmentExpression o1AttrExp1 = Iterables.get(o1AttrExps, 0);
		assertThat(o1AttrExp1.getAttributeId(), is("urn:org:xacml4j:tests:policy-with-obligations:obligation1:assignment1"));
		assertThat(o1AttrExp1.getExpression(), instanceOf(StringExp.class));

		ObligationExpression o2 = Iterables.get(obligations, 1);
		assertThat(o2.getId(), is("urn:org:xacml4j:tests:policy-with-obligations:obligation2"));
		Collection<AttributeAssignmentExpression> o2AttrExps = o2.getAttributeAssignmentExpressions();
		assertThat(o2AttrExps.size(), is(1));
		AttributeAssignmentExpression o2AttrExp1 = Iterables.get(o2AttrExps, 0);
		assertThat(o2AttrExp1.getAttributeId(), is("urn:org:xacml4j:tests:policy-with-obligations:obligation2:assignment1"));
		assertThat(o2AttrExp1.getExpression(), instanceOf(AttributeDesignator.class));

	}
}
