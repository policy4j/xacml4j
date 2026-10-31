package org.xacml4j.jmh.harness;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.xacml4j.jmh.testing.TestCorpora;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;

/**
 * The identity that the lookup check compares: type, id, and version
 * (spec "Decisions and lookups are checked before measurement").
 */
public class RulesTest
{
	private static final String ID = "urn:xacml4j:jmh:test:same";

	@Test
	public void ruleHasTheIdentityOfItsCopy() throws Exception
	{
		CompositeDecisionRule rule = TestCorpora.rule(TestCorpora.readPolicy(ID, "1.0", "one"));
		CompositeDecisionRule copy = TestCorpora.rule(TestCorpora.readPolicy(ID, "1.0", "another description"));

		assertThat(Rules.sameIdentity(rule, copy)).isTrue();
		assertThat(Rules.version(rule)).isEqualTo(Version.parse("1.0"));
		assertThat(Rules.kind(rule)).isEqualTo("Policy");
	}

	@Test
	public void anotherVersionIsAnotherIdentity() throws Exception
	{
		CompositeDecisionRule rule = TestCorpora.rule(TestCorpora.readPolicy(ID, "1.0", "one"));
		CompositeDecisionRule later = TestCorpora.rule(TestCorpora.readPolicy(ID, "1.1", "one"));

		assertThat(Rules.sameIdentity(rule, later)).isFalse();
	}

	@Test
	public void anotherIdIsAnotherIdentity() throws Exception
	{
		CompositeDecisionRule rule = TestCorpora.rule(TestCorpora.readPolicy(ID, "1.0", "one"));
		CompositeDecisionRule other = TestCorpora.rule(TestCorpora.readPolicy(ID + ":other", "1.0", "one"));

		assertThat(Rules.sameIdentity(rule, other)).isFalse();
	}

	@Test
	public void policySetWithTheIdAndVersionOfAPolicyIsAnotherIdentity() throws Exception
	{
		CompositeDecisionRule policy = TestCorpora.rule(TestCorpora.readPolicy(ID, "1.0", "one"));
		CompositeDecisionRule policySet = TestCorpora.rule(
				TestCorpora.rootPolicySet(ID, TestCorpora.policyReference(TestCorpora.DOCUMENTS_ID)));

		assertThat(Rules.kind(policySet)).isEqualTo("PolicySet");
		assertThat(Rules.sameIdentity(policy, policySet)).isFalse();
		assertThat(Rules.sameIdentity(policySet, policy)).isFalse();
	}
}
