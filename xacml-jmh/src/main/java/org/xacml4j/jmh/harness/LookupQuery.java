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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.BaseCompositeDecisionRuleIDReference;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicySetIDReference;
import org.xacml4j.v30.policy.PolicyVisitorSupport;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * One store lookup that a corpus implies: a rule by its id and version, or
 * the target of a policy or policy-set reference with the reference's
 * version constraints, as the PDP's reference resolver asks for it.
 *
 * @param label a readable name of the query
 * @param kind how the query calls the repository
 * @param id the rule id
 * @param version the version constraint of a reference
 * @param earliest the earliest-version constraint of a reference
 * @param latest the latest-version constraint of a reference
 * @param exact the version of an {@link Kind#EXACT} query
 * @param expected the parsed rule that the store must return
 */
public record LookupQuery(String label, Kind kind, String id, Optional<VersionMatch> version,
		Optional<VersionMatch> earliest, Optional<VersionMatch> latest, Optional<Version> exact,
		CompositeDecisionRule expected)
{
	/** How a query calls the repository. */
	public enum Kind {
		/** {@code get(id, version)} */
		EXACT,
		/** {@code getPolicy(id, version, earliest, latest)} */
		POLICY_REFERENCE,
		/** {@code getPolicySet(id, version, earliest, latest)} */
		POLICY_SET_REFERENCE
	}

	public LookupQuery {
		Objects.requireNonNull(label, "label");
		Objects.requireNonNull(kind, "kind");
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(version, "version");
		Objects.requireNonNull(earliest, "earliest");
		Objects.requireNonNull(latest, "latest");
		Objects.requireNonNull(exact, "exact");
		Objects.requireNonNull(expected, "expected");
		if(kind == Kind.EXACT && exact.isEmpty()){
			throw new IllegalArgumentException("An exact query needs a version: " + label);
		}
	}

	/**
	 * Runs the query.
	 *
	 * @param repository the repository to ask
	 * @return the rule that the repository returned, if any
	 */
	public Optional<CompositeDecisionRule> find(PolicyRepository repository){
		return Optional.ofNullable(switch(kind){
			case EXACT -> repository.get(id, exact.get());
			case POLICY_REFERENCE -> repository.getPolicy(id, version.orElse(null), earliest.orElse(null), latest.orElse(null));
			case POLICY_SET_REFERENCE -> repository.getPolicySet(id, version.orElse(null), earliest.orElse(null), latest.orElse(null));
		});
	}

	/**
	 * The queries of a parsed corpus, in corpus order: one {@link Kind#EXACT}
	 * query per rule, then one query per policy or policy-set reference whose
	 * target is in the corpus. A reference to a rule outside the corpus gives
	 * no query.
	 *
	 * @param policies the parsed corpus rules
	 * @return the queries
	 */
	public static List<LookupQuery> of(List<CompositeDecisionRule> policies){
		List<LookupQuery> queries = new ArrayList<>();
		for(CompositeDecisionRule rule : policies){
			Version v = Rules.version(rule);
			queries.add(new LookupQuery("exact " + Rules.describe(rule), Kind.EXACT, rule.getId(),
					Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(v), rule));
		}
		for(CompositeDecisionRule rule : policies){
			rule.accept(new PolicyVisitorSupport(){
				@Override
				public void visitEnter(PolicyIDReference ref){
					reference(ref, Kind.POLICY_REFERENCE, Policy.class, policies).ifPresent(queries::add);
				}

				@Override
				public void visitEnter(PolicySetIDReference ref){
					reference(ref, Kind.POLICY_SET_REFERENCE, PolicySet.class, policies).ifPresent(queries::add);
				}
			});
		}
		return List.copyOf(queries);
	}

	private static Optional<LookupQuery> reference(BaseCompositeDecisionRuleIDReference ref, Kind kind,
			Class<? extends CompositeDecisionRule> type, List<CompositeDecisionRule> policies)
	{
		Optional<VersionMatch> version = Optional.ofNullable(ref.getVersion());
		Optional<VersionMatch> earliest = Optional.ofNullable(ref.getEarliestVersion());
		Optional<VersionMatch> latest = Optional.ofNullable(ref.getLatestVersion());
		CompositeDecisionRule target = null;
		for(CompositeDecisionRule candidate : policies){
			if(!type.isInstance(candidate) || !candidate.getId().equals(ref.getId())){
				continue;
			}
			Version v = Rules.version(candidate);
			// XACML 3.0 §5.13 matching, not the engine's (see matches)
			if(version.map(m -> matches(m.getPattern(), v.getValue())).orElse(true)
					&& earliest.map(m -> matches(m.getPattern(), v.getValue())).orElse(true)
					&& latest.map(m -> matches(m.getPattern(), v.getValue())).orElse(true)
					&& (target == null || Rules.version(target).compareTo(v) < 0)){
				target = candidate;
			}
		}
		if(target == null){
			return Optional.empty();
		}
		String label = (kind == Kind.POLICY_REFERENCE ? "reference Policy \"" : "reference PolicySet \"")
				+ ref.getId() + "\"" + version.map(m -> " version " + m).orElse("")
				+ earliest.map(m -> " earliest " + m).orElse("") + latest.map(m -> " latest " + m).orElse("");
		return Optional.of(new LookupQuery(label, kind, ref.getId(), version, earliest, latest, Optional.empty(), target));
	}

	/**
	 * Version matching of XACML 3.0 §5.13 for the lookup check. The engine's
	 * {@link VersionMatch#match} reads {@code *} as one digit, so {@code 1.*}
	 * misses {@code 1.10} (bead policy4j-w842); the check must not copy that, or
	 * it would reject a correct store and pass a store with the same defect
	 * (review rv006). Known limit: {@code EarliestVersion} and {@code LatestVersion}
	 * of a reference apply as patterns, as the engine does; no built-in corpus uses them.
	 *
	 * @param pattern dot-separated numbers, {@code *} for any one number, and a final
	 * {@code +} for any further numbers, also none
	 * @param version a version string
	 * @return whether the version matches the pattern
	 */
	static boolean matches(String pattern, String version){
		String[] want = pattern.split("\\.", -1);
		String[] have = version.split("\\.", -1);
		for(int i = 0; i < want.length; i++){
			if(want[i].equals("+") && i == want.length - 1){
				return true;
			}
			if(i >= have.length){
				return false;
			}
			if(!want[i].equals("*") && !want[i].equals(have[i])){
				return false;
			}
		}
		return want.length == have.length;
	}
}
