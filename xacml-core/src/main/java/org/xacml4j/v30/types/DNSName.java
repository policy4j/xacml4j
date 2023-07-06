package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Optional;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.net.InternetDomainName;
import org.xacml4j.v30.SyntaxException;

public final class DNSName extends Value.SelfValue<DNSName>
{
	private static final long serialVersionUID = -1729624624549215684L;

	private InternetDomainName name;
	private PortRange portRange;

	private DNSName(String domainName, PortRange range){
		super(XacmlTypes.DNSNAME);
		Preconditions.checkNotNull(domainName);
		Preconditions.checkNotNull(range);
		this.name = InternetDomainName.from(domainName);
		this.portRange = range;
	}

	public static DNSName ofAny(Object v, Object ...params) {
		if (v instanceof StringVal) {
			return DNSName.of((StringVal) v);
		}
		if (v instanceof String) {
			return DNSName.of((String) v);
		}
		if (v instanceof String) {
			return DNSName.of((String) v);
		}
		throw SyntaxException
				.invalidAttributeValue(v,XacmlTypes.DNSNAME);
	}

	public static DNSName of(StringVal v){
		return of(v.get());
	}

	public static DNSName of(String v, PortRange portRange){
		return new DNSName(v, portRange);
	}

	/**
	 * Parses XACML DNS name textual representation
	 *
	 * @param value a textual representation of XACML
	 * DNS Name type
	 * @return {@link DNSName}
	 * @exception IllegalArgumentException if given string
	 * does not represent valid XACML DNS name
	 */
	public static DNSName of(String value){
		Preconditions.checkArgument(
				!Strings.isNullOrEmpty(value),
				"XACML DNS name can not be null or empty");
		int pos = value.indexOf(':');
		if(pos == -1){
			return new DNSName(value,
					PortRange.getAnyPort());
		}
		return new DNSName(value.substring(0, pos),
				PortRange.valueOf(pos + 1, value));
	}

	public String getDomainName(){
		return name.toString();
	}

	/**
	 * Indicates whether this domain name ends in a public suffix,
	 * while not being a public suffix itself.  For example, returns
	 * true for www.google.com, foo.co.uk and bar.ca.us,
	 * but not for google, com, or google.foo
	 *
	 * @return {@code true} if this domain name ends in a public suffix
	 */
	public boolean isUnderPublicSuffix(){
		return name.isUnderPublicSuffix();
	}

	public StringVal toStringExp(){
		return StringVal.of(get().toXacmlString());
	}

	/**
	 * Indicates whether this domain name represents a public suffix,
	 * as defined by the Mozilla Foundation's Public Suffix List (PSL).
	 * A public suffix is one under which Internet users can directly
	 * register names, such as com, co.uk or pvt.k12.wy.us.
	 * Examples of domain names that are not public suffixes
	 * include google, google.com and foo.co.uk.
	 *
	 * @return {@code true} if the suffix is public; returns {@code false} otherwise
	 */
	public boolean isPublicSuffix(){
		return name.isPublicSuffix();
	}

	public boolean isTopPrivateDomain(){
		return name.isTopPrivateDomain();
	}

	public String getPublicSuffix(){
		return name.publicSuffix().toString();
	}

	public List<String> getDomainNameParts(){
		return name.parts();
	}

	public PortRange getPortRange(){
		return portRange;
	}

	public String getTopPrivateDomain(){
		return name.topPrivateDomain().toString();
	}

	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof DNSName)){
			return false;
		}
		DNSName n = (DNSName)o;
		return name.equals(n.name) && portRange.equals(n.portRange) &&
				getEvaluatesTo().equals(n.getEvaluatesTo());
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(name, portRange);
	}

	@Override
	protected String formatVal(DNSName v) {
		return toXacmlString();
	}

	public String toXacmlString() {
		StringBuilder b = new StringBuilder(name.toString());
		if(!portRange.isUnbound()){
			b.append(':').append(portRange);
		}
		return b.toString();
	}
}
