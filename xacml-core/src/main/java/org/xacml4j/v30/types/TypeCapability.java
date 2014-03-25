package org.xacml4j.v30.types;

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
