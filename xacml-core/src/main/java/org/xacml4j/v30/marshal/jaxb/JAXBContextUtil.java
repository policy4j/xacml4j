package org.xacml4j.v30.marshal.jaxb;

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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.google.common.base.Preconditions;

public class JAXBContextUtil
{
	private static final JAXBContext INSTANCE;
	private static final char SEP = ':';
	static{
			try{
			INSTANCE = JAXBContext.newInstance(
					org.oasis.xacml.v30.jaxb.ObjectFactory.class.getPackage().getName() +
					SEP +
					org.oasis.xacml.v20.jaxb.policy.ObjectFactory.class.getPackage().getName() +
					SEP +
					org.oasis.xacml.v20.jaxb.context.ObjectFactory.class.getPackage().getName());
		}catch(JAXBException e){
			throw new IllegalStateException("Failed to initialize JAXB context", e);
		}
	}

	/** Private constructor for utility class */
	private JAXBContextUtil() {}

	public static JAXBContext getInstance()
	{
		Preconditions.checkState(INSTANCE != null, "Failed to initialize JAXB context");
		return INSTANCE;
	}
}
