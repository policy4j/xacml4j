package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;

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
	BagOfAttributeValuesType<RFC822NameValue> bagOf();
	
	final class RFC822NameValue extends BaseAttributeValue<RFC822Name>
	{
		public RFC822NameValue(RFC822NameType type, RFC822Name value) {
			super(type, value);
		}
	}
}