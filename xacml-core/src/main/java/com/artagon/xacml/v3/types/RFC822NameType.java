package com.artagon.xacml.v3.types;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

/** XACML DataType:  <b>urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name</b>. 
 * <br>The “urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name” primitive type 
 * represents an electronic mail address. The valid syntax for such a 
 * name is described in IETF RFC 2821, Section 4.1.2, Command Argument 
 * Syntax, under the term "Mailbox". 
 * From rfc 2821:
 * <pre>
 *   Domain = (sub-domain 1*("." sub-domain)) / address-literal
 *   sub-domain = Let-dig [Ldh-str]
 *   address-literal = "[" IPv4-address-literal /
 *                         IPv6-address-literal /
 *                         General-address-literal "]"
 *   Mailbox = Local-part "@" Domain
 *   Local-part = Dot-string / Quoted-string
 *   Dot-string = Atom *("." Atom)
 *   Atom = 1*atext
 *   Quoted-string = DQUOTE *qcontent DQUOTE
 *   String = Atom / Quoted-string
 * </pre>
 * */
public interface RFC822NameType extends AttributeValueType
{	
	RFC822NameValue create(Object value, Object ...params);
	RFC822NameValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType bagOf();
	
	final class RFC822NameValue extends BaseAttributeValue<RFC822Name>
	{
		public RFC822NameValue(RFC822NameType type, RFC822Name value) {
			super(type, value);
		}
	}
	
	public final class Factory
	{
		private final static RFC822NameType INSTANCE = new RFC822NameTypeImpl("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name");
		
		public static RFC822NameType getInstance(){
			return INSTANCE;
		}
		
		public static RFC822NameValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static RFC822NameValue fromXacmlString(String v, Object ...params){
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