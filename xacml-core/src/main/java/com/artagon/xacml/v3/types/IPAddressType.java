package com.artagon.xacml.v3.types;

import java.net.InetAddress;
import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

/** 
 * XACML DataType:  <b>urn:oasis:names:tc:xacml:2.0:data-type:ipAddress</b>. 
 * <br>The "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress" primitive 
 * type represents an IPv4 or IPv6 network address, with optional mask 
 * and optional port or port range. The syntax SHALL be:
 * <pre>
 *     ipAddress = address [ "/" mask ] [ ":" [ portrange ] ]
 * </pre>
 * For an IPv4 address, the address and mask are formatted in accordance 
 * with the syntax for a "host" in IETF RFC 2396 "Uniform Resource Identifiers 
 * (URI): Generic Syntax", section 3.2. 
 * <p>
 * For an IPv6 address, the address and mask are formatted in accordance with 
 * the syntax for an "ipv6reference" in IETF RFC 2732 "Format for Literal IPv6 
 * Addresses in URL's". (Note that an IPv6 address or mask, in this syntax, 
 * is enclosed in literal "[" "]" brackets.) 
 */
public interface IPAddressType extends AttributeValueType
{
	IPAddressValue create(Object v, Object ...params);
	IPAddressValue create(InetAddress address, PortRange portRange);
	IPAddressValue create(InetAddress address, InetAddress mask);
	IPAddressValue create(InetAddress address, InetAddress mask, PortRange portRange);
	IPAddressValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType<IPAddressValue> bagOf();
	
	public final class IPAddressValue extends BaseAttributeValue<IPAddress>
	{
		public IPAddressValue(IPAddressType type, IPAddress value) {
			super(type, value);
		}
	}
	
	public final class Factory
	{
		private final static IPAddressType INSTANCE = new IPAddressTypeImpl("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress");
		
		public static IPAddressType getInstance(){
			return INSTANCE;
		}
		
		public static IPAddressValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static IPAddressValue fromXacmlString(String v, Object ...params){
			return INSTANCE.fromXacmlString(v, params);
		}
		
		public static BagOfAttributeValues<IPAddressValue> bagOf(AttributeValue ...values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues<IPAddressValue> bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues<IPAddressValue> emptyBag(){
			return INSTANCE.bagOf().createEmpty();
		}
	}
}