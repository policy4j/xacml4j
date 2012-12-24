package org.xacml4j.v30.types;

import java.net.InetAddress;
import java.util.Collection;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.IPAddress;
import org.xacml4j.v30.PortRange;


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
public enum IPAddressType implements AttributeExpType
{
	IPADDRESS("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress");

	private String typeId;
	private BagOfAttributeExpType bagType;

	private IPAddressType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public IPAddressExp create(InetAddress address, PortRange portRange) {
		return new IPAddressExp(this, new IPAddress(address, portRange));
	}

	public IPAddressExp create(InetAddress address, InetAddress mask) {
		return new IPAddressExp(this, new IPAddress(address, mask));
	}

	public IPAddressExp create(InetAddress address, InetAddress mask,
			PortRange portRange) {
		return new IPAddressExp(this, new IPAddress(address, mask, portRange));
	}

	public boolean isConvertableFrom(Object any) {
		return String.class.isInstance(any)
		|| InetAddress.class.isInstance(any) || IPAddress.class.isInstance(any);
	}

	@Override
	public IPAddressExp create(Object any, Object ...params) {
		return new IPAddressExp(this, IPAddress.parse(any));
	}

	@Override
	public IPAddressExp fromXacmlString(String v, Object ...params)
	{
		return new IPAddressExp(this, IPAddress.parse(v));
	}

	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeExpType bagType() {
		return bagType;
	}

	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
	}

	@Override
	public BagOfAttributeExp bagOf(AttributeExp... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp bagOf(Collection<AttributeExp> values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp bagOf(Object... values) {
		return bagType.bagOf(values);
	}

	@Override
	public BagOfAttributeExp emptyBag() {
		return bagType.createEmpty();
	}

	@Override
	public String toString(){
		return typeId;
	}
}