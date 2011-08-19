package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

/** 
 * XACML DataType:  <b>urn:oasis:names:tc:xacml:2.0:data-type:dnsName</b>. 
 * <br>The "urn:oasis:names:tc:xacml:2.0:data-type:dnsName" primitive 
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
 * <p>For both the "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress" and 
 * "urn:oasis:names:tc:xacml:2.0:data-type:dnsName" data-types, the port 
 * or port range syntax SHALL be
 * <pre>     portrange = portnumber | "-"portnumber | portnumber"-"[portnumber]
 * </pre>where "portnumber" is a decimal port number. 
 * <br>If the port number is of the form "-x", where "x" is a port 
 * number, then the range is all ports numbered "x" and below. 
 * <br>If the port number is of the form"x-", then the range is 
 * all ports numbered "x" and above. 
 * <br>[This syntax is taken from the Java SocketPermission.]
 */
public enum DNSNameType implements AttributeValueType
{
	DNSNAME("urn:oasis:names:tc:xacml:2.0:data-type:dnsName");
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private DNSNameType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return String.class.isInstance(any);
	}
	
	public DNSNameValue create(String name){
		return create(name, PortRange.getAnyPort());
	}
	
	public DNSNameValue create(String name, PortRange range){
		return new DNSNameValue(this, new DNSName(name, range));
	}
	
	public DNSNameValue create(String name, Integer lowerBound, Integer upperBound ){
		return create(name, PortRange.getRange(lowerBound, upperBound));
	}
	
	@Override
	public DNSNameValue create(Object o, Object ...params) {
		Preconditions.checkNotNull(o);
		Preconditions.checkArgument(isConvertableFrom(o), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"DNSName\" type", 
				o, o.getClass()));
		return fromXacmlString((String)o);
	}

	@Override
	public DNSNameValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		int pos = v.indexOf(':');
		if(pos == -1){
			return new DNSNameValue(this, new 
					DNSName(v, PortRange.getAnyPort()));
		}
		String name = v.substring(0, pos);
		return new DNSNameValue(this, 
				new DNSName(name, PortRange.valueOf(pos + 1, v)));
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeValuesType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeValues bagOf(AttributeValue... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeValues bagOf(Collection<AttributeValue> values) {
		return bagType.create(values);
	}
	
	@Override
	public BagOfAttributeValues bagOf(Object... values) {
		return bagType.bagOf(values);
	}
	
	@Override
	public BagOfAttributeValues emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}
