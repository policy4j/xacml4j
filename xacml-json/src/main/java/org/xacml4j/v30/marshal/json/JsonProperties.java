package org.xacml4j.v30.marshal.json;

/*
 * #%L
 * Xacml4J Gson Integration
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

public interface JsonProperties {
	
	static final String ATTRIBUTE_ID_PROPERTY = "AttributeId";
	static final String VALUE_PROPERTY = "Value";
	static final String DATA_TYPE_PROPERTY = "DataType";
	static final String CATEGORY_PROPERTY = "Category";
	static final String ISSUER_PROPERTY = "Issuer";
	static final String INCLUDE_IN_RESULT_PROPERTY = "IncludeInResult";
	static final String CATEGORY_ARRAY_NAME = "Category";
	static final String CATEGORY_ID_PROPERTY = "CategoryId";
	static final String ID_PROPERTY = "Id";
	static final String CONTENT_PROPERTY = "ContentRef";
	static final String ATTRIBUTE_PROPERTY = "Attribute";
}
