package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#BASE64BINARY} type.

*/
public final class Base64Binary extends Value.OtherValue<Binary>
{
    private static final long serialVersionUID = 3298986540546649848L;

    private Base64Binary(Binary value) {
        super(XacmlTypes.BASE64BINARY, value);
    }

    public static Base64Binary ofAny(Object v, Object ... p) throws SyntaxException{
        if (v instanceof java.lang.String) {
            String lexical = (String) v;
            validateLexical(lexical);
            return new Base64Binary(Binary.ofBase64(removeWhitespace(lexical)));
        }
        if (v instanceof StringVal) {
            String lexical = ((StringVal) v).get();
            validateLexical(lexical);
            return new Base64Binary(Binary.ofBase64(removeWhitespace(lexical)));
        }
        return Binary.ofAnyBinary(v, p)
                .map(Base64Binary::new)
                .orElseThrow(()->SyntaxException
                        .invalidAttributeValue(v,
                                XacmlTypes.BASE64BINARY));
    }

    private static void validateLexical(String lexical)
    {
        String compact = removeWhitespace(lexical);
        if (compact.isEmpty()) {
            return;
        }
        int padding = compact.endsWith("==") ? 2 : compact.endsWith("=") ? 1 : 0;
        int dataLength = compact.length() - padding;
        if (compact.length() % 4 != 0
                || padding > 2
                || padding == 1 && dataLength % 4 != 3
                || padding == 2 && dataLength % 4 != 2
                || padding == 0 && dataLength % 4 != 0) {
            throw SyntaxException.invalidAttributeValue(lexical, XacmlTypes.BASE64BINARY);
        }
        for (int index = 0; index < dataLength; index++) {
            char value = compact.charAt(index);
            if (!(value >= 'A' && value <= 'Z'
                    || value >= 'a' && value <= 'z'
                    || value >= '0' && value <= '9'
                    || value == '+' || value == '/')) {
                throw SyntaxException.invalidAttributeValue(lexical, XacmlTypes.BASE64BINARY);
            }
        }
        for (int index = dataLength; index < compact.length(); index++) {
            if (compact.charAt(index) != '=') {
                throw SyntaxException.invalidAttributeValue(lexical, XacmlTypes.BASE64BINARY);
            }
        }
    }

    private static String removeWhitespace(String lexical)
    {
        StringBuilder compact = new StringBuilder(lexical.length());
        for (int index = 0; index < lexical.length(); index++) {
            char value = lexical.charAt(index);
            if (value != ' ' && value != '\t' && value != '\r' && value != '\n') {
                compact.append(value);
            }
        }
        return compact.toString();
    }

    public StringVal toStringExp(){
        return StringVal.of(toXacmlString());
    }

    public String toXacmlString(){
        return get().toBase64Encoded();
    }

}
