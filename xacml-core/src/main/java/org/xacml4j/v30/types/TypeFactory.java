package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2023 Xacml4J.org
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.xacml4j.v30.ValueType;

/**
 * A provider for new types
 *
 * @author Giedrius Trumpickas
 */
public interface TypeFactory
{
	Optional<ValueType> forType(String typeId);

	Map<String, ValueType> asMapByTypeId();
	Map<String, ValueType> asMapByAbbreviatedTypeId();
	Map<String, ValueType> asMapByAliasTypeId();

	class BaseTypeFactory implements TypeFactory
	{
		private Map<String, ValueType> byTypeId;
		private Map<String, ValueType> byAbbreviatedTypeId;
		private Map<String, ValueType> byAliasTypeId;

		public BaseTypeFactory(Collection<ValueType> types){
			this.byTypeId = Collections.unmodifiableMap(types.stream()
			                     .collect(Collectors
					                              .toMap(v->v.getTypeId(), v->v, (a, b)->a,
					                                     ()->new TreeMap<>(String.CASE_INSENSITIVE_ORDER))));
			this.byAbbreviatedTypeId = Collections.unmodifiableMap(types.stream()
			                                            .collect(Collectors
					                                   .toMap(v->v.getShortTypeId(), v->v, (a, b)->a,
					                                          ()->new TreeMap<>(String.CASE_INSENSITIVE_ORDER))));
			this.byAliasTypeId  = Collections.unmodifiableMap(types.stream()
			                           .flatMap(t->t.getTypeIdAliases()
			                                        .stream()
			                                        .collect(Collectors.toMap(a->a, a->t, (v1, v2)->v1))
			                                        .entrySet()
			                                        .stream())
			                           .collect(Collectors.toMap(a->a.getKey(), a->a.getValue(), (a, b)->a)));
		}

		@Override
		public final Optional<ValueType> forType(String typeId) {
			return Optional.ofNullable(byTypeId.get(typeId))
			               .or(()->Optional
					               .ofNullable(byAbbreviatedTypeId.get(typeId))
					               .or(()->Optional
							               .ofNullable(byAliasTypeId.get(typeId))));
		}

		@Override
		public final Map<String, ValueType> asMapByTypeId() {
			return byTypeId;
		}

		@Override
		public final Map<String, ValueType> asMapByAbbreviatedTypeId() {
			return byAbbreviatedTypeId;
		}

		@Override
		public final Map<String, ValueType> asMapByAliasTypeId() {
			return byAliasTypeId;
		}
	}
}
