package com.artagon.xacml.v3.types;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;

/** 
 * XACML DataType:  <b>urn:oasis:names:tc:xacml:2.0:data-type:dnsName</b>. 
 * <br>The “urn:oasis:names:tc:xacml:2.0:data-type:dnsName” primitive 
 * type represents a Domain Name Service (DNS) host name, with optional 
 * port or port range. The syntax SHALL be:
 * <pre>
 *     dnsName = hostname [ ":" portrange ]
 * </pre>
 * The hostname is formatted in accordance with IETF RFC 2396 
 * "Uniform Resource Identifiers (URI): Generic Syntax", section 3.2, 
 * except that a wildcard "*" may be used in the left-most component 
 * of the hostname to indicate "any subdomain" under the domain 
 * specified to its right.
 * <p>For both the “urn:oasis:names:tc:xacml:2.0:data-type:ipAddress” and 
 * “urn:oasis:names:tc:xacml:2.0:data-type:dnsName” data-types, the port 
 * or port range syntax SHALL be
 * <pre>     portrange = portnumber | "-"portnumber | portnumber"-"[portnumber]
 * </pre>where "portnumber" is a decimal port number. 
 * <br>If the port number is of the form "-x", where "x" is a port 
 * number, then the range is all ports numbered "x" and below. 
 * <br>If the port number is of the form"x-", then the range is 
 * all ports numbered "x" and above. 
 * <br>[This syntax is taken from the Java SocketPermission.]
 */
public interface DNSNameType extends AttributeValueType
{
	DNSNameValue create(Object o, Object ...params);
	DNSNameValue fromXacmlString(String v, Object ...params);
	
	
	final class DNSNameValue extends BaseAttributeValue<DNSName>
	{
		public DNSNameValue(DNSNameType type, DNSName value) {
			super(type, value);
		}
		
	}
	
	public final class Factory
	{
		private final static DNSNameType INSTANCE = new DNSNameTypeImpl("urn:oasis:names:tc:xacml:2.0:data-type:dnsName");
		
		public static DNSNameType getInstance(){
			return INSTANCE;
		}
		
		public static DNSNameValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static DNSNameValue fromXacmlString(String v, Object ...params){
			return INSTANCE.fromXacmlString(v, params);
		}
		
		public static BagOfAttributeValues bagOf(AttributeValue ...values){
			return INSTANCE.bagType().create(values);
		}
		
		public static BagOfAttributeValues bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagType().create(values);
		}
		
		public static BagOfAttributeValues emptyBag(){
			return INSTANCE.bagType().createEmpty();
		}
	}
}
