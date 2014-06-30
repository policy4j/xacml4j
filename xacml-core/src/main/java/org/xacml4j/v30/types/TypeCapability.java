package org.xacml4j.v30.types;

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

import java.util.Arrays;
import java.util.Map;

import org.xacml4j.v30.AttributeExpType;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * A marker interface for type capability
 * 
 * @author Giedrius Trumpickas
 */
public interface TypeCapability 
{
	AttributeExpType getType();
	
	
	/**
	 * An index for type capabilities
	 * 
	 * @author Giedrius Trumpickas
	 *
	 * @param <T>
	 */
	public class Index<T extends TypeCapability>
	{
		private Map<AttributeExpType, T> byType;
		private Map<String, T> byTypeId;
		
		private Index(ImmutableMap.Builder<AttributeExpType, T> byType, 
				ImmutableMap.Builder<String, T> byTypeId){
			this.byType = byType.build();
			this.byTypeId = byTypeId.build();
		}
		
		public static <T extends TypeCapability> Index<T> build(T[] capabilities){
			return build(Arrays.asList(capabilities));
		}
		
		public static <T extends TypeCapability> Index<T> build(Iterable<? extends T> capabilities){
			ImmutableMap.Builder<AttributeExpType, T> byType = ImmutableMap.builder();
			ImmutableMap.Builder<String, T> byTypeId = ImmutableMap.builder();
			for(T c: capabilities){
				byType.put(c.getType(), c);
				byTypeId.put(c.getType().getDataTypeId(), c);
				for(String a : c.getType().getDataTypeIdAliases()){
					byTypeId.put(a, c);
				}
			}
			return new Index<T>(byType, byTypeId);
		}
		
		public Optional<T> get(AttributeExpType type){
			return Optional.fromNullable(byType.get(type));
		}
		
		public Optional<T> get(String typeId){
			return Optional.fromNullable(byTypeId.get(typeId));
		}
	}
}
