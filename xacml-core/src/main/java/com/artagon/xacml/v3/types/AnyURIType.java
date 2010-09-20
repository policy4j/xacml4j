package com.artagon.xacml.v3.types;

import java.net.URI;
import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

public interface AnyURIType extends AttributeValueType
{	
	AnyURIValue create(Object value, Object ...params);
	
	AnyURIValue fromXacmlString(String v, Object ...params);
	
	BagOfAttributeValuesType<AnyURIValue> bagOf();
	
	final class AnyURIValue extends BaseAttributeValue<URI> 
	{
		public AnyURIValue(AnyURIType type, URI value) {
			super(type, value);
		}
	}
	
	/**
	 * A type safe factory to create instances
	 * of {@link AnyURIValue}
	 */
	public final class Factory
	{
		private final static AnyURIType INSTANCE = new AnyURITypeImpl("http://www.w3.org/2001/XMLSchema#anyURI");
		
		public static AnyURIType getInstance(){
			return INSTANCE;
		}
		
		public static AnyURIValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static AnyURIValue fromXacmlString(String v, Object ...params){
			return INSTANCE.fromXacmlString(v, params);
		}
		
		public static BagOfAttributeValues<AnyURIValue> bagOf(AttributeValue ...values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues<AnyURIValue> bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues<AnyURIValue> emptyBag(){
			return INSTANCE.bagOf().createEmpty();
		}
	}
}