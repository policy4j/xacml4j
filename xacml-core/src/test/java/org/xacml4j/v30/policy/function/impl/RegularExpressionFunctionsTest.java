package org.xacml4j.v30.policy.function.impl;

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

import static org.easymock.EasyMock.createControl;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.AnyURI;
import org.xacml4j.v30.types.RFC822Name;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;



public class RegularExpressionFunctionsTest
{
	private IMocksControl c;
	private EvaluationContext context;

	@Before
	public void init(){
		c = createControl();
		this.context = c.createMock(EvaluationContext.class);
	}

	@Test
	public void testFunctionIfImplemented() throws Exception {
		FunctionProvider f = FunctionProviderBuilder.builder()
		                                            .withDefaultFunctions()
		                                            .build();
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match"), notNullValue());
	}

	@Test
	public void testXacmlRegExpToJERegExpWithCharacterSubtraction() {
		assertThat(RegExpFunctions.covertXacmlToJavaSyntax("[0-9]{3}-[0-9]{3}-[0-9]{4}"), equalTo(".*[0-9]{3}-[0-9]{3}-[0-9]{4}.*"));
	}

	@Test
	public void testXacmlRegExpWithSpaceBugTrimming() {
		StringVal regexp1 = XacmlTypes.STRING.ofAny("   This  is n*o*t* *IT!  ");
		StringVal regexp2 = XacmlTypes.STRING.ofAny("  *This .*is not IT! *");
		StringVal input1 = XacmlTypes.STRING.ofAny("   This  is not IT!  ");
		c.replay();
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp1, input1), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		c.verify();
		c.reset();
		c.replay();
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp2, input1), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		c.verify();
	}

	@Test
	public void testRegExpMatchFromIIC168ConformanceTest() {
		StringVal regexp1 = XacmlTypes.STRING.ofAny("   This  is n*o*t* *IT!  ");
		StringVal input1 = XacmlTypes.STRING.ofAny("   This  is IT!  ");
		StringVal input2 = XacmlTypes.STRING.ofAny("   This  is not IT!  ");
		c.replay();
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp1, input1), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		c.verify();
		c.reset();
		c.replay();
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp1, input2), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		c.verify();
	}

	@Test
	public void testStringRegExpMatch() throws EvaluationException {
		StringVal regexp = XacmlTypes.STRING.ofAny("S*,Value");
		StringVal input = XacmlTypes.STRING.ofAny("Some,Value");
		c.replay();
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp, input), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		c.verify();
	}

	@Test
	public void testAnyURIRegExpMatch() throws EvaluationException {
		StringVal regexp = XacmlTypes.STRING.ofAny("http://www.test.org/public/*");
		AnyURI input = XacmlTypes.ANYURI.ofAny("http://www.test.org/public/test/a");
		c.replay();
		assertThat(RegExpFunctions.anyURIRegexpMatch(context, regexp, input), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		c.verify();

	}

	@Test
	public void testRFC822NameRegExpMatch() throws EvaluationException{
		StringVal regexp = XacmlTypes.STRING.ofAny(".*@example.com");
		RFC822Name input = XacmlTypes.RFC822NAME.ofAny("test@example.com");
		c.replay();
		assertThat(RegExpFunctions.rfc822NameRegexpMatch(context, regexp, input), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		c.verify();
	}
}
