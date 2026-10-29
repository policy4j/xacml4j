package org.xacml4j.v30.policy.function.regex.jdk;

/*
 * #%L
 * Xacml4J Core
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.function.impl.RegExpFunctions;
import org.xacml4j.v30.policy.function.regex.CompiledRegex;
import org.xacml4j.v30.policy.function.regex.RegexBudget;
import org.xacml4j.v30.policy.function.regex.RegexCompileException;
import org.xacml4j.v30.policy.function.regex.RegexEngine;
import org.xacml4j.v30.policy.function.regex.RegexException;
import org.xacml4j.v30.policy.function.regex.RegexLimits;
import org.xacml4j.v30.policy.function.regex.RegexProfile;

class CompilationLimitTest
{
	@Test
	void jdkWrappedStackOverflowIsATransientLimit(){
		for(RegexProfile profile : RegexProfile.values()){
			PatternSyntaxException wrapped = new PatternSyntaxException("Stack overflow during pattern compilation", "", -1);
			RegexCompileException failure = JdkRegexEngine.compilationFailure(wrapped, profile);
			assertEquals(RegexException.Kind.LIMIT, failure.kind());
			assertFalse(failure.isDeterministic());
			assertEquals(RegexBudget.LIMIT_EXCEEDED_MESSAGE, failure.getMessage());
			RegexEngine engine = new RegexEngine(){
				@Override public String name(){ return "jdk"; }
				@Override public Set<RegexProfile> profiles(){ return Set.of(profile); }
				@Override public CompiledRegex compile(String pattern, RegexProfile p, RegexLimits limits){ throw failure; }
			};
			EvaluationException result = assertThrows(EvaluationException.class,
					() -> new RegExpFunctions.Matching(engine, profile, RegexLimits.defaults()).matches("a", "a"));
			assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
			assertEquals(Optional.of(RegexBudget.LIMIT_EXCEEDED_MESSAGE), result.getStatus().getMessage());
		}
	}
}
