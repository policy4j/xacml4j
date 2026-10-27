package org.xacml4j.jmh.testing;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyStore;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.Versionable;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyImportTool;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * The test stores of xacml-jmh, registered in {@code META-INF/services} of the
 * test classes as a store in another jar would be. Each wraps an
 * {@link InMemoryPolicyRepository} and changes one behavior.
 */
public final class TestStores
{
	public static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	public static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();

	private TestStores(){
	}

	/**
	 * @return a repository of {@code store}, opened with the default functions and
	 * algorithms and loaded with {@code rules}; close it with {@code store.close}
	 */
	public static PolicyRepository openLoaded(PolicyStore store, Optional<String> argument, String id,
			List<CompositeDecisionRule> rules) throws Exception {
		PolicyRepository repository = store.open(id, argument, FUNCTIONS, ALGORITHMS);
		PdpFixture.load(repository, rules);
		return repository;
	}

	/** A store that adds nothing to the in-memory repository. */
	private abstract static class Base implements PolicyStore
	{
		@Override
		public PolicyRepository open(String id, Optional<String> argument,
				FunctionProvider functions, DecisionCombiningAlgorithmProvider algorithms){
			return wrap(new InMemoryPolicyRepository(id, functions, algorithms), argument);
		}

		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			return repository;
		}
	}

	/** {@code probe}: records the argument and id of every repository. */
	public static final class Probe extends Base
	{
		public static final String NAME = "probe";
		private static final List<Optional<String>> ARGUMENTS = Collections.synchronizedList(new ArrayList<>());
		private static final List<String> IDS = Collections.synchronizedList(new ArrayList<>());

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			ARGUMENTS.add(argument);
			IDS.add(repository.getId());
			return repository;
		}

		/** @return the arguments since the last reset, in order */
		public static List<Optional<String>> arguments(){
			synchronized(ARGUMENTS){
				return List.copyOf(ARGUMENTS);
			}
		}

		/** @return the repository ids since the last reset, in order */
		public static List<String> repositoryIds(){
			synchronized(IDS){
				return List.copyOf(IDS);
			}
		}

		public static void reset(){
			ARGUMENTS.clear();
			IDS.clear();
		}
	}

	/** {@code counting}: records the id of every opened and every closed repository. */
	public static final class Counting extends Base
	{
		public static final String NAME = "counting";
		private static final List<String> OPENED = Collections.synchronizedList(new ArrayList<>());
		private static final List<String> CLOSED = Collections.synchronizedList(new ArrayList<>());

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			OPENED.add(repository.getId());
			return repository;
		}

		@Override
		public void close(PolicyRepository repository){
			CLOSED.add(repository.getId());
		}

		public static List<String> opened(){
			synchronized(OPENED){
				return List.copyOf(OPENED);
			}
		}

		public static List<String> closed(){
			synchronized(CLOSED){
				return List.copyOf(CLOSED);
			}
		}

		public static void reset(){
			OPENED.clear();
			CLOSED.clear();
		}
	}

	/** One of two stores named {@code twin}; a lookup of that name must fail. */
	public static final class FirstTwin extends Base
	{
		public static final String NAME = "twin";

		@Override
		public String name(){
			return NAME;
		}
	}

	/** The other store named {@code twin}. */
	public static final class SecondTwin extends Base
	{
		@Override
		public String name(){
			return FirstTwin.NAME;
		}
	}

	/**
	 * {@code losing:<id>}: says it added the rule with that id but does not keep it;
	 * every lookup of it finds nothing.
	 */
	public static final class Losing extends Base
	{
		public static final String NAME = "losing";

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			return new LosingRepository(repository, lostId(NAME, argument), false);
		}
	}

	/** {@code throwing:<id>}: as {@link Losing}, but a lookup of the lost rule throws {@link LostPolicyException}. */
	public static final class Throwing extends Base
	{
		public static final String NAME = "throwing";

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			return new LosingRepository(repository, lostId(NAME, argument), true);
		}
	}

	/** {@code stale-latest}: answers a reference with the earliest matching version, not the latest. */
	public static final class StaleLatest extends Base
	{
		public static final String NAME = "stale-latest";

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			return new ForwardingPolicyRepository(repository){
				@Override
				public Policy getPolicy(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
					return earliestOf(getPolicies(id, version, earliest, latest));
				}

				@Override
				public PolicySet getPolicySet(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
					return earliestOf(getPolicySets(id, version, earliest, latest));
				}
			};
		}

		/** @return the earliest version, or {@code null} as the repository contract states for no match */
		private static <T extends Versionable> T earliestOf(Collection<T> found){
			T earliest = null;
			for(T candidate : found){
				if(earliest == null || candidate.getVersion().compareTo(earliest.getVersion()) < 0){
					earliest = candidate;
				}
			}
			return earliest;
		}
	}

	/** {@code recording}: records every lookup of every repository. */
	public static final class Recording extends Base
	{
		public static final String NAME = "recording";
		private static final List<RecordingPolicyRepository.Read> READS = Collections.synchronizedList(new ArrayList<>());

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			return new RecordingPolicyRepository(repository, READS::add);
		}

		public static List<RecordingPolicyRepository.Read> reads(){
			synchronized(READS){
				return List.copyOf(READS);
			}
		}

		public static int count(){
			return READS.size();
		}

		public static void reset(){
			READS.clear();
		}
	}

	/** {@code thread-confined}: finds nothing off the thread that opened the repository. */
	public static final class ThreadConfined extends Base
	{
		public static final String NAME = "thread-confined";

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			Thread owner = Thread.currentThread();
			return new LosingLookups(repository){
				@Override
				boolean lost(){
					return Thread.currentThread() != owner;
				}
			};
		}
	}

	/** {@code import-counting}: counts the import tools of all its repositories. */
	public static final class ImportCounting extends Base
	{
		public static final String NAME = "import-counting";
		private static final AtomicInteger TOOLS = new AtomicInteger();

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			return new ForwardingPolicyRepository(repository){
				@Override
				public PolicyImportTool newImportTool(){
					TOOLS.incrementAndGet();
					return super.newImportTool();
				}
			};
		}

		public static int tools(){
			return TOOLS.get();
		}

		public static void reset(){
			TOOLS.set(0);
		}
	}

	/**
	 * {@code import-refusing}: a direct {@code add} works, but every add of its
	 * import tool is refused, which the production import tool ignores (review rv007).
	 */
	public static final class ImportRefusing extends Base
	{
		public static final String NAME = "import-refusing";

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			PolicyRepository refusing = new ForwardingPolicyRepository(repository){
				@Override
				public boolean add(CompositeDecisionRule r){
					return false;
				}
			};
			return new ForwardingPolicyRepository(repository){
				@Override
				public PolicyImportTool newImportTool(){
					return refusing.newImportTool();
				}
			};
		}
	}

	/**
	 * {@code import-failing}: a direct {@code add} works, but every add of its import tool
	 * throws a checked {@link IOException}; records the id of every opened and closed
	 * repository (review RVJ1).
	 */
	public static final class ImportFailing extends Base
	{
		public static final String NAME = "import-failing";
		public static final String MESSAGE = "checked import failure";
		private static final List<String> OPENED = Collections.synchronizedList(new ArrayList<>());
		private static final List<String> CLOSED = Collections.synchronizedList(new ArrayList<>());

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			OPENED.add(repository.getId());
			PolicyRepository failing = new ForwardingPolicyRepository(repository){
				@Override
				public boolean add(CompositeDecisionRule r){
					throw TestStores.<RuntimeException>sneaky(new IOException(MESSAGE));
				}
			};
			return new ForwardingPolicyRepository(repository){
				@Override
				public PolicyImportTool newImportTool(){
					return failing.newImportTool();
				}
			};
		}

		@Override
		public void close(PolicyRepository repository){
			CLOSED.add(repository.getId());
		}

		public static List<String> opened(){
			synchronized(OPENED){
				return List.copyOf(OPENED);
			}
		}

		public static List<String> closed(){
			synchronized(CLOSED){
				return List.copyOf(CLOSED);
			}
		}

		public static void reset(){
			OPENED.clear();
			CLOSED.clear();
		}
	}

	/** {@code close-failing}: every repository after the first throws a checked exception on close. */
	public static final class CloseFailing extends Base
	{
		public static final String NAME = "close-failing";
		private static final AtomicInteger OPENED = new AtomicInteger();
		private static final AtomicInteger OPEN = new AtomicInteger();
		private static final Set<String> FAILING = ConcurrentHashMap.newKeySet();

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			if(OPENED.incrementAndGet() > 1){
				FAILING.add(repository.getId());
			}
			OPEN.incrementAndGet();
			return repository;
		}

		@Override
		public void close(PolicyRepository repository) throws Exception {
			OPEN.decrementAndGet();
			if(FAILING.remove(repository.getId())){
				throw new Exception("checked close failure");
			}
		}

		/** @return the repositories opened and not closed since the last reset */
		public static int open(){
			return OPEN.get();
		}

		public static void reset(){
			OPENED.set(0);
			OPEN.set(0);
			FAILING.clear();
		}
	}

	/** {@code drifting}: loses every lookup after {@link #drift}, and fails to close after {@link #failClose}. */
	public static final class Drifting extends Base
	{
		public static final String NAME = "drifting";
		private static volatile boolean drifted;
		private static volatile boolean failClose;

		@Override
		public String name(){
			return NAME;
		}

		@Override
		PolicyRepository wrap(PolicyRepository repository, Optional<String> argument){
			return new LosingLookups(repository){
				@Override
				boolean lost(){
					return drifted;
				}
			};
		}

		@Override
		public void close(PolicyRepository repository) throws Exception {
			if(failClose){
				throw new Exception("checked close failure");
			}
		}

		public static void drift(){
			drifted = true;
		}

		public static void failClose(){
			failClose = true;
		}

		public static void reset(){
			drifted = false;
			failClose = false;
		}
	}

	/** What {@link Throwing} throws on a lookup of the rule that it lost, as a store with a broken connection would. */
	public static final class LostPolicyException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		LostPolicyException(String repository, String id){
			super("Repository \"" + repository + "\" lost \"" + id + "\"");
		}
	}

	/** Throws a checked exception from a method that does not declare it, as a store driver can. */
	@SuppressWarnings("unchecked")
	private static <E extends Throwable> E sneaky(Throwable failure) throws E {
		throw (E) failure;
	}

	private static String lostId(String store, Optional<String> argument){
		return argument.filter(id -> !id.isEmpty()).orElseThrow(() -> new IllegalArgumentException(
				"Store \"" + store + "\" needs the id of the rule to lose: " + store + ":<id>"));
	}

	/** Finds nothing for any lookup while {@link #lost} is true. */
	private abstract static class LosingLookups extends ForwardingPolicyRepository
	{
		LosingLookups(PolicyRepository delegate){
			super(delegate);
		}

		abstract boolean lost();

		@Override
		public CompositeDecisionRule get(String id, Version v){
			return lost() ? null : super.get(id, v);
		}

		@Override
		public Policy getPolicy(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
			return lost() ? null : super.getPolicy(id, version, earliest, latest);
		}

		@Override
		public PolicySet getPolicySet(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
			return lost() ? null : super.getPolicySet(id, version, earliest, latest);
		}
	}

	/**
	 * Says it added the rule with one id but does not keep it. Every lookup of that id
	 * finds nothing ({@code null} or an empty collection, as the {@link PolicyRepository}
	 * contract states), or throws {@link LostPolicyException}.
	 */
	private static final class LosingRepository extends ForwardingPolicyRepository
	{
		private final String lost;
		private final boolean throwing;

		LosingRepository(PolicyRepository delegate, String lost, boolean throwing){
			super(delegate);
			this.lost = lost;
			this.throwing = throwing;
		}

		@Override
		public Collection<Policy> getPolicies(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
			return isLost(id) ? List.of() : super.getPolicies(id, version, earliest, latest);
		}

		@Override
		public Collection<PolicySet> getPolicySets(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
			return isLost(id) ? List.of() : super.getPolicySets(id, version, earliest, latest);
		}

		@Override
		public Policy getPolicy(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
			return isLost(id) ? null : super.getPolicy(id, version, earliest, latest);
		}

		@Override
		public PolicySet getPolicySet(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
			return isLost(id) ? null : super.getPolicySet(id, version, earliest, latest);
		}

		@Override
		public CompositeDecisionRule get(String id, Version v){
			return isLost(id) ? null : super.get(id, v);
		}

		/** Reports the lost rule as added, as a store that drops a write silently would. */
		@Override
		public boolean add(CompositeDecisionRule r){
			return lost.equals(r.getId()) || super.add(r);
		}

		private boolean isLost(String id){
			if(!lost.equals(id)){
				return false;
			}
			if(throwing){
				throw new LostPolicyException(getId(), id);
			}
			return true;
		}
	}
}
