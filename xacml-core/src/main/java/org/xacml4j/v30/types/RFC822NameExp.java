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
import org.xacml4j.v30.RFC822Name;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class RFC822NameExp extends BaseAttributeExp<RFC822Name>
{
	private static final long serialVersionUID = -1983511364298319436L;

	RFC822NameExp(RFC822Name value) {
		super(XacmlTypes.RFC822NAME, value);
	}
	
	public static RFC822NameExp valueOf(String v){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
        return new RFC822NameExp(RFC822Name.parse(v));
	}
	
	public static RFC822NameExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public static RFC822NameExp valueOf(RFC822Name n){
		return new RFC822NameExp(n);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.RFC822NAME.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.RFC822NAME.bag();
	}
}
