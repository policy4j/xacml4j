package com.artagon.xacml.v3.types;

import java.util.Collection;

import javax.security.auth.x500.X500Principal;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;

/** 
 * XACML DataType:  <b>urn:oasis:names:tc:xacml:1.0:data-type:x500Name</b>.
 * <br>The “urn:oasis:names:tc:xacml:1.0:data-type:x500Name” 
 * primitive type represents an ITU-T Rec. X.520 Distinguished 
 * Name. 
 * <br>The valid syntax for such a name is described in IETF 
 * RFC 2253 "Lightweight Directory Access Protocol (v3): 
 * UTF-8 String Representation of Distinguished Names"
 * <p>
 * For example the general nature of this syntax is described in
 * the constructor for javax.security.auth.x500.X500Principal(String name): Creates an X500Principal 
 * from a string representation of an X.500 distinguished name 
 * <br>(ex: "CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US"). 
 * <br>The distinguished name must be specified using the grammar defined 
 * in RFC 1779 or RFC 2253 (either format is acceptable). 
 */
public interface X500NameType extends AttributeValueType
{
	X500NameValue create(Object v, Object ...params);
	X500NameValue fromXacmlString(String v, Object ...params);
	
	final class X500NameValue extends BaseAttributeValue<X500Principal>
	{
		public X500NameValue(X500NameType type,
				X500Principal value) {
			super(type, value);
		}
		
	}
	
	public final class Factory
	{
		private final static X500NameType INSTANCE = new X500NameTypeImpl("urn:oasis:names:tc:xacml:1.0:data-type:x500Name");
		
		public static X500NameType getInstance(){
			return INSTANCE;
		}
		
		public static X500NameValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static X500NameValue fromXacmlString(String v, Object ...params){
			return INSTANCE.fromXacmlString(v, params);
		}
		
		public static BagOfAttributeValues bagOf(AttributeValue ...values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues emptyBag(){
			return INSTANCE.bagOf().createEmpty();
		}
	}
}