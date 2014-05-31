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

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.DNSName;

public final class DNSNameExp extends BaseAttributeExp<DNSName>
{
	private static final long serialVersionUID = -1729624624549215684L;

	DNSNameExp(DNSName value){
		super(XacmlTypes.DNSNAME, value);
	}
	
	public static DNSNameExp valueOf(String v){
		return new DNSNameExp(DNSName.parse(v));
	}
	
	public static DNSNameExp valueOf(StringExp v){
		return DNSNameExp.valueOf(v.getValue());
	}
	
	public static DNSNameExp valueOf(DNSName v){
		return new DNSNameExp(v);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.DNSNAME.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.DNSNAME.bag();
	}
}

