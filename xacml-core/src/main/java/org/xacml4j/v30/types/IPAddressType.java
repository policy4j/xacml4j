package org.xacml4j.v30.types;

import java.net.InetAddress;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.IPAddress;
import org.xacml4j.v30.PortRange;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.common.base.Preconditions;


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
public enum IPAddressType implements AttributeExpType, TypeToString, TypeToXacml30
{
	IPADDRESS("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private IPAddressType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public IPAddressExp create(InetAddress address, PortRange portRange) {
		return new IPAddressExp(new IPAddress(address, portRange));
	}

	public IPAddressExp create(InetAddress address, InetAddress mask) {
		return new IPAddressExp(new IPAddress(address, mask));
	}

	public IPAddressExp create(InetAddress address, InetAddress mask,
			PortRange portRange) {
		return new IPAddressExp(new IPAddress(address, mask, portRange));
	}

	public boolean isConvertibleFrom(Object any) {
		return String.class.isInstance(any)
				|| InetAddress.class.isInstance(any)
				|| IPAddress.class.isInstance(any);
	}

	public IPAddressExp create(Object any) {
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		return new IPAddressExp(IPAddress.parse(any));
	}
	
	@Override
	public AttributeValueType toXacml30(Types types, AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public IPAddressExp fromXacml30(Types types, AttributeValueType v) {
		if(v.getContent().size() > 0){
			return fromString((String)v.getContent().get(0));
		}
		throw new XacmlSyntaxException(
				"No content found for the attribute value");
	}

	@Override
	public String toString(AttributeExp exp) {
		IPAddressExp v = (IPAddressExp)exp;
		return v.getValue().toString();
	}

	@Override
	public IPAddressExp fromString(String v) {
       return create(v);
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeExpType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
	}

	@Override
	public BagOfAttributeExp bagOf(AttributeExp... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp bagOf(Iterable<AttributeExp> values) {
		return bagType.create(values);
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
