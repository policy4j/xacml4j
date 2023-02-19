package org.xacml4j.v30.types;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.xacml4j.v30.ValueType;

public interface TypeFactory
{
	Optional<ValueType> forType(String typeId);

	Map<String, ValueType> asMapByTypeId();
	Map<String, ValueType> asMapByAbbreviatedTypeId();

	class BaseTypeFactory implements TypeFactory
	{
		private Map<String, ValueType> byTypeId;
		private Map<String, ValueType> byAbbreviatedTypeId;

		public BaseTypeFactory(Collection<ValueType> types){
			this.byTypeId = Collections.unmodifiableMap(types.stream()
			                     .collect(Collectors
					                              .toMap(v->v.getDataTypeId(), v->v, (a, b)->a,
					                                     ()->new TreeMap<>(String.CASE_INSENSITIVE_ORDER))));
			this.byAbbreviatedTypeId = Collections.unmodifiableMap(types.stream()
			                                            .collect(Collectors
					                                   .toMap(v->v.getAbbrevDataTypeId(), v->v, (a, b)->a,
					                                          ()->new TreeMap<>(String.CASE_INSENSITIVE_ORDER))));
		}

		@Override
		public final Optional<ValueType> forType(String typeId) {
			return Optional.ofNullable(byTypeId.get(typeId))
			               .or(()->Optional.ofNullable(byAbbreviatedTypeId.get(typeId)));
		}

		@Override
		public Map<String, ValueType> asMapByTypeId() {
			return byTypeId;
		}

		@Override
		public Map<String, ValueType> asMapByAbbreviatedTypeId() {
			return byAbbreviatedTypeId;
		}
	}
}
