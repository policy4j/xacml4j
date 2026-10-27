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

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.Versionable;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;

/** Identity of a policy or policy set: type, id, and version. */
public final class Rules
{
	private Rules(){
	}

	/**
	 * @param rule a policy or policy set
	 * @return its version
	 * @throws IllegalArgumentException if the rule has no version
	 */
	public static Version version(CompositeDecisionRule rule){
		if(rule instanceof Versionable versionable){
			return versionable.getVersion();
		}
		throw new IllegalArgumentException("Rule \"" + rule.getId() + "\" has no version: " + rule.getClass().getName());
	}

	/**
	 * @param rule a policy or policy set
	 * @return {@code Policy}, {@code PolicySet}, or the class name
	 */
	public static String kind(CompositeDecisionRule rule){
		if(rule instanceof Policy){
			return "Policy";
		}
		if(rule instanceof PolicySet){
			return "PolicySet";
		}
		return rule.getClass().getName();
	}

	/**
	 * @param rule a rule
	 * @return {@code <kind> "<id>" version "<version>"}
	 */
	public static String describe(CompositeDecisionRule rule){
		return kind(rule) + " \"" + rule.getId() + "\" version \"" + version(rule).getValue() + "\"";
	}

	/**
	 * @param expected the rule of the parsed corpus
	 * @param actual the rule that a store returned
	 * @return whether both have the same kind, id, and version
	 */
	public static boolean sameIdentity(CompositeDecisionRule expected, CompositeDecisionRule actual){
		return kind(expected).equals(kind(actual))
				&& expected.getId().equals(actual.getId())
				&& version(expected).equals(version(actual));
	}
}
