package org.xacml4j.v30.policy.function;

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
import org.xacml4j.v30.spi.function.AnnotationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.RFC822NameExp;
import org.xacml4j.v30.types.StringExp;


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
		FunctionProvider f = new AnnotationBasedFunctionProvider(RegularExpressionFunctions.class);
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match"), notNullValue());
	}

	@Test
	public void testXacmlRegExpToJERegExpWithCharacterSubtraction() {
		assertThat(RegularExpressionFunctions.covertXacmlToJavaSyntax("[0-9]{3}-[0-9]{3}-[0-9]{4}"), equalTo(".*[0-9]{3}-[0-9]{3}-[0-9]{4}.*"));
	}

	@Test
	public void testXacmlRegExpWithSpaceBugTrimming() {
		StringExp regexp1 = StringExp.valueOf("   This  is n*o*t* *IT!  ");
		StringExp regexp2 = StringExp.valueOf("  *This .*is not IT! *");
		StringExp input1 = StringExp.valueOf("   This  is not IT!  ");
		c.replay();
		assertThat(RegularExpressionFunctions.stringRegexpMatch(context, regexp1, input1), equalTo(BooleanExp.valueOf(true)));
		c.verify();
		c.reset();
		c.replay();
		assertThat(RegularExpressionFunctions.stringRegexpMatch(context, regexp2, input1), equalTo(BooleanExp.valueOf(true)));
		c.verify();
	}

	@Test
	public void testRegExpMatchFromIIC168ConformanceTest() {
		StringExp regexp1 = StringExp.valueOf("   This  is n*o*t* *IT!  ");
		StringExp input1 = StringExp.valueOf("   This  is IT!  ");
		StringExp input2 = StringExp.valueOf("   This  is not IT!  ");
		c.replay();
		assertThat(RegularExpressionFunctions.stringRegexpMatch(context, regexp1, input1), equalTo(BooleanExp.valueOf(true)));
		c.verify();
		c.reset();
		c.replay();
		assertThat(RegularExpressionFunctions.stringRegexpMatch(context, regexp1, input2), equalTo(BooleanExp.valueOf(true)));
		c.verify();
	}

	@Test
	public void testStringRegExpMatch() throws EvaluationException {
		StringExp regexp = StringExp.valueOf("S*,Value");
		StringExp input = StringExp.valueOf("Some,Value");
		c.replay();
		assertThat(RegularExpressionFunctions.stringRegexpMatch(context, regexp, input), equalTo(BooleanExp.valueOf(true)));
		c.verify();
	}

	@Test
	public void testAnyURIRegExpMatch() throws EvaluationException {
		StringExp regexp = StringExp.valueOf("http://www.test.org/public/*");
		AnyURIExp input = AnyURIExp.valueOf("http://www.test.org/public/test/a");
		c.replay();
		assertThat(RegularExpressionFunctions.anyURIRegexpMatch(context, regexp, input), equalTo(BooleanExp.valueOf(true)));
		c.verify();

	}

	@Test
	public void testRFC822NameRegExpMatch() throws EvaluationException{
		StringExp regexp = StringExp.valueOf(".*@example.com");
		RFC822NameExp input = RFC822NameExp.valueOf("test@example.com");
		c.replay();
		assertThat(RegularExpressionFunctions.rfc822NameRegexpMatch(context, regexp, input), equalTo(BooleanExp.valueOf(true)));
		c.verify();
	}
}
