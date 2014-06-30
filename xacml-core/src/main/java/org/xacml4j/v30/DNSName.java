package org.xacml4j.v30;

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

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.net.InternetDomainName;

public final class DNSName implements Serializable
{
	private static final long serialVersionUID = -1729624624549215684L;

	private InternetDomainName name;
	private PortRange portRange;

	public DNSName(String domainName, PortRange range){
		Preconditions.checkNotNull(domainName);
		Preconditions.checkNotNull(range);
		this.name = InternetDomainName.from(domainName);
		this.portRange = range;
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
	public static DNSName parse(String value){
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
		return name.name();
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
		return name.publicSuffix().name();
	}

	public List<String> getDomainNameParts(){
		return name.parts();
	}

	public PortRange getPortRange(){
		return portRange;
	}

	public String getTopPrivateDomain(){
		return name.topPrivateDomain().name();
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
		return name.equals(n.name) && portRange.equals(n.portRange);
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(name, portRange);
	}

	@Override
	public String toString(){
		return toXacmlString();
	}

	public String toXacmlString() {
		StringBuilder b = new StringBuilder(name.name());
		if(!portRange.isUnbound()){
			b.append(":").append(portRange.toString());
		}
		return b.toString();
	}
}
