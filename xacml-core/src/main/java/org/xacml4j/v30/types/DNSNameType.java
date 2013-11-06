package org.xacml4j.v30.types;

import java.util.Collection;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.DNSName;
import org.xacml4j.v30.PortRange;

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
public enum DNSNameType implements AttributeExpType
{
	DNSNAME("urn:oasis:names:tc:xacml:2.0:data-type:dnsName");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private DNSNameType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertibleFrom(Object any) {
		return String.class.isInstance(any);
	}

	public DNSNameExp create(String name){
		return create(name, PortRange.getAnyPort());
	}

	public DNSNameExp create(String name, PortRange range){
		return new DNSNameExp(this, new DNSName(name, range));
	}

	public DNSNameExp create(String name, Integer lowerBound, Integer upperBound ){
		return create(name, PortRange.getRange(lowerBound, upperBound));
	}

	@Override
	public DNSNameExp create(Object any, Object ...params) {
		Preconditions.checkNotNull(any);
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		return fromXacmlString((String) any);
	}

	@Override
	public DNSNameExp fromXacmlString(String v, Object ...params) {
		return new DNSNameExp(this,
				DNSName.parse(v));
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
	public BagOfAttributeExp bagOf(AttributeExp... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
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
