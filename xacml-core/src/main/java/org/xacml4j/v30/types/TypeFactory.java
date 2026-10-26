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
		private final Map<String, ValueType> byTypeId;
		private final Map<String, ValueType> byAbbreviatedTypeId;
		private final Map<String, ValueType> byAliasTypeId;

		public BaseTypeFactory(Collection<ValueType> types){
			Map<String, ValueType> identifiers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			Map<String, ValueType> canonicalIds = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			Map<String, ValueType> abbreviatedIds = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			Map<String, ValueType> aliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			for (ValueType type : types) {
				register(identifiers, type.getTypeId(), type);
				register(canonicalIds, type.getTypeId(), type);
				register(identifiers, type.getShortTypeId(), type);
				register(abbreviatedIds, type.getShortTypeId(), type);
				for (String alias : type.getTypeIdAliases()) {
					register(identifiers, alias, type);
					register(aliases, alias, type);
				}
			}
			this.byTypeId = Collections.unmodifiableMap(canonicalIds);
			this.byAbbreviatedTypeId = Collections.unmodifiableMap(abbreviatedIds);
			this.byAliasTypeId = Collections.unmodifiableMap(aliases);
		}

		private static void register(Map<String, ValueType> identifiers, String id, ValueType type) {
			ValueType previous = identifiers.putIfAbsent(id, type);
			if (previous != null && previous != type) {
				throw new IllegalArgumentException("Duplicate data type identifier '" + id
						+ "' claimed by " + describe(previous) + " and " + describe(type));
			}
		}

		private static String describe(ValueType type) {
			return "'" + type.getTypeId() + "' (" + type + ", "
					+ type.getClass().getName() + ")";
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
