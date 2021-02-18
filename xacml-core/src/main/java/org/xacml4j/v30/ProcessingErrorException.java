package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2021 Xacml4J.org
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

public class ProcessingErrorException extends XacmlException
{
    public ProcessingErrorException(String template, Object... arguments) {
        super(Status.processingError()
                        .detail(String.format(template, arguments)).build(),
                String.format(template, arguments));
    }

    public ProcessingErrorException(Throwable cause) {
        super(Status.processingError().detail(cause).build(),
                cause);
    }

    public static ProcessingErrorException invalidResolverAttributeId(String id, String  attributeId){
        return new ProcessingErrorException(
                "Invalid resolverId=\"%s\" attributeId=\"%s\"",
                id, attributeId);
    }

    public static ProcessingErrorException invalidResolverAttributeType(String id, String attributeId,
                                                                        AttributeValueType type,
                                                                        AttributeValueType expectedType){
        return new ProcessingErrorException(
                "Invalid resolverId=\"%s\", attributeId=\"%s\" XACML type=\"%s\" expectedType=\"%s\"",
                id, attributeId, type.getAbbrevDataTypeId(),
                expectedType.getAbbrevDataTypeId());
    }
}
