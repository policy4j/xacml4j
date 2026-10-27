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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.xacml4j.jmh.harness.PolicyStore;
import org.xacml4j.jmh.harness.PolicyStoreContext;
import org.xacml4j.jmh.harness.StoreSession;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.spi.repository.PolicyImportTool;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * Test stores for the fixes of the review gate (rv004 to rv007). Each one
 * is correct in the serial setup checks and wrong in one way that only a
 * later check can see. ServiceLoader makes a new instance per lookup, so
 * their switches and counters are static; tests reset them first.
 */
public final class ReviewFixStores
{
	private ReviewFixStores(){
	}

	/** A repository whose single-rule lookups answer {@code null} when {@link #lost()} says so. */
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
	 * Store {@code thread-confined}: answers correctly on the thread that
	 * opened the session and finds nothing on any other thread, while it
	 * declares concurrent reads. Only a concurrent lookup check sees it: the
	 * PDP's reference cache hides the store from concurrent decisions
	 * (review rv006).
	 */
	public static final class ThreadConfinedStore implements PolicyStore
	{
		public static final String NAME = "thread-confined";

		@Override
		public String name(){
			return NAME;
		}

		@Override
		public StoreSession open(PolicyStoreContext context){
			Thread owner = Thread.currentThread();
			PolicyRepository repository = new LosingLookups(TestStores.inMemory(context)){
				@Override
				boolean lost(){
					return Thread.currentThread() != owner;
				}
			};
			return () -> repository;
		}
	}

	/** Store {@code import-counting}: counts the import tools that its repositories create. */
	public static final class ImportCountingStore implements PolicyStore
	{
		public static final String NAME = "import-counting";
		private static final AtomicInteger TOOLS = new AtomicInteger();

		public static void reset(){
			TOOLS.set(0);
		}

		public static int tools(){
			return TOOLS.get();
		}

		@Override
		public String name(){
			return NAME;
		}

		@Override
		public StoreSession open(PolicyStoreContext context){
			PolicyRepository repository = new ForwardingPolicyRepository(TestStores.inMemory(context)){
				@Override
				public PolicyImportTool newImportTool(){
					TOOLS.incrementAndGet();
					return super.newImportTool();
				}
			};
			return () -> repository;
		}
	}

	/**
	 * Store {@code add-refusing}: its repository refuses {@code add}, while
	 * its session's {@code load} fills the store directly. The fixture and
	 * {@code loadParsed} work; the production import tool ignores the refused
	 * adds, so an {@code importXml} session stays empty (review rv007).
	 */
	public static final class AddRefusingStore implements PolicyStore
	{
		public static final String NAME = "add-refusing";

		@Override
		public String name(){
			return NAME;
		}

		@Override
		public StoreSession open(PolicyStoreContext context){
			PolicyRepository backing = TestStores.inMemory(context);
			PolicyRepository repository = new ForwardingPolicyRepository(backing){
				@Override
				public boolean add(CompositeDecisionRule r){
					return false;
				}
			};
			return new StoreSession(){
				@Override
				public PolicyRepository repository(){
					return repository;
				}

				@Override
				public void load(List<CompositeDecisionRule> policies){
					for(CompositeDecisionRule policy : policies){
						backing.add(policy);
					}
				}
			};
		}
	}

	/**
	 * Store {@code close-failing}: the first session of a test closes
	 * normally; every later session counts as closed and then throws a
	 * checked exception from {@code close} (review rv009).
	 */
	public static final class CloseFailingStore implements PolicyStore
	{
		public static final String NAME = "close-failing";
		private static final AtomicInteger OPENED = new AtomicInteger();
		private static final AtomicInteger OPEN = new AtomicInteger();

		public static void reset(){
			OPENED.set(0);
			OPEN.set(0);
		}

		/** @return sessions opened and not yet closed */
		public static int open(){
			return OPEN.get();
		}

		@Override
		public String name(){
			return NAME;
		}

		@Override
		public StoreSession open(PolicyStoreContext context){
			PolicyRepository repository = TestStores.inMemory(context);
			boolean fails = OPENED.incrementAndGet() > 1;
			OPEN.incrementAndGet();
			return new StoreSession(){
				@Override
				public PolicyRepository repository(){
					return repository;
				}

				@Override
				public void close() throws Exception {
					OPEN.decrementAndGet();
					if(fails){
						throw new Exception("checked close failure");
					}
				}
			};
		}
	}

	/**
	 * Store {@code drifting}: correct until {@link #drift()} is called, then
	 * every single-rule lookup finds nothing, as a store that loses its data
	 * during a trial (review rv005).
	 */
	public static final class DriftingStore implements PolicyStore
	{
		public static final String NAME = "drifting";
		private static volatile boolean drifted;
		private static volatile boolean failClose;

		public static void reset(){
			drifted = false;
			failClose = false;
		}

		public static void drift(){
			drifted = true;
		}

		/** From now on, closing a session throws a checked exception (review rv009). */
		public static void failClose(){
			failClose = true;
		}

		@Override
		public String name(){
			return NAME;
		}

		@Override
		public StoreSession open(PolicyStoreContext context){
			PolicyRepository repository = new LosingLookups(TestStores.inMemory(context)){
				@Override
				boolean lost(){
					return drifted;
				}
			};
			return new StoreSession(){
				@Override
				public PolicyRepository repository(){
					return repository;
				}

				@Override
				public void close() throws Exception {
					if(failClose){
						throw new Exception("checked close failure");
					}
				}
			};
		}
	}
}
